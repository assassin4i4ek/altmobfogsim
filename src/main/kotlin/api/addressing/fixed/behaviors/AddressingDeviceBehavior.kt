package api.addressing.fixed.behaviors

import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.TupleTargetNextHopMapQuantifierContainer
import api.common.utils.TupleRecipientPair
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.application.AppModule
import org.fog.entities.Tuple
import java.lang.Exception

interface AddressingDeviceBehavior<T: BaseBehavior<T, out NetworkDevice>>
    : BaseBehavior<AddressingDeviceBehavior<T>, AddressingDevice> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        device.mChildrenIds.add(device.mId)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onNetworkAddressTuple(ev)
            Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag -> onCreateChildrenMapping(ev)
            Events.ADDRESSING_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    private fun onNetworkAddressTuple(ev: SimEvent): Boolean {
        val (tuple, _) = ev.data as TupleRecipientPair
//        Logger.debug(device.mName, "Trying to address tuple ${tuple.cloudletId}")
        if (device.addressingChildrenMapping[tuple] == null) {
            device.addressingChildrenMapping[tuple] = mutableMapOf()
            val (targetDeviceIds, quantifier) = getTargetDevicesForTuple(tuple)
            val nextHopIds = device.addressingModel.idsOfNextHopTo(device, targetDeviceIds, quantifier, tuple.cloudletFileSize)
            device.mSendEvent(device.mId, 0.0, Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag,
                    BaseEventWrapper(ev, TupleTargetNextHopMapQuantifierContainer(tuple, nextHopIds, quantifier))
            )
            device.mWaitForEvent(PredicateType(Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag))
        }

        device.mSendEvent(device.mId, 0.0, Events.ADDRESSING_DEVICE_ADDRESS_TUPLE.tag, ev)
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onCreateChildrenMapping(ev: SimEvent): Boolean {
        val (originalEvent, container) = ev.data as BaseEventWrapper<TupleTargetNextHopMapQuantifierContainer>
        val (tuple, targetNextHopMap, quantifier) = container
        val addressingChildrenMappingForTuple = device.addressingChildrenMapping[tuple]!!
        when (tuple.direction){
            Tuple.UP -> {
                assert(targetNextHopMap.size == 1)
                assert(quantifier == AddressingModel.Quantifier.ANY || quantifier == AddressingModel.Quantifier.SINGLE)
                assert((originalEvent.data as TupleRecipientPair).recipientId == device.mParentId)
                addressingChildrenMappingForTuple[targetNextHopMap.values.first()] = true
            }
            Tuple.DOWN -> {
                device.mChildrenIds.forEach { addressingChildrenMappingForTuple[it] = false }
                addressingChildrenMappingForTuple[device.mParentId] = false
                targetNextHopMap.forEach { (_, nextHop) ->
                    addressingChildrenMappingForTuple[nextHop] = true
                }
//            if (device.addressingType == AddressingDevice.AddressingType.HIERARCHICAL) {
                if (quantifier == AddressingModel.Quantifier.ALL) {
                    assert(addressingChildrenMappingForTuple[device.mParentId] == false)
                }
            }
            else -> throw Exception("Unknown tuple ${tuple.cloudletId} direction")
        }
        return false
    }

    private fun onAddressTuple(ev: SimEvent): Boolean {
        val originalEvent = ev.data as SimEvent
        val (tuple, recipientId) = originalEvent.data as TupleRecipientPair
        var res = true
//        Logger.debug(device.mName, "Addressing tuple ${tuple.cloudletId}")
        val addressingChildrenMappingForTuple = device.addressingChildrenMapping[tuple]!!
        when (tuple.direction) {
            Tuple.UP -> {
                assert(addressingChildrenMappingForTuple.size == 1)
                assert(addressingChildrenMappingForTuple.values.first())
                (originalEvent.data as TupleRecipientPair).recipientId = addressingChildrenMappingForTuple.keys.first()
                addressingChildrenMappingForTuple.remove((originalEvent.data as TupleRecipientPair).recipientId)
                res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
            }
            Tuple.DOWN -> {
                if (addressingChildrenMappingForTuple.remove(recipientId)!!) {
                    res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
                }
                if (
                        addressingChildrenMappingForTuple.size == 1 &&
                        addressingChildrenMappingForTuple.remove(device.mParentId)!!/* &&
                        device.addressingType == AddressingDevice.AddressingType.PEER_TO_PEER*/) {
                    (originalEvent.data as TupleRecipientPair).recipientId = device.mParentId
                    res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
                }
            }
            else -> throw Exception("Unknown tuple ${tuple.cloudletId} direction")
        }
        if (addressingChildrenMappingForTuple.isEmpty()) {
            device.addressingChildrenMapping.remove(tuple)
        }
        return res
    }

    private fun getTargetDevicesForTuple(tuple: Tuple): Pair<List<Int>, AddressingModel.Quantifier> {
        val modulePlacement = device.controller.appModulePlacementPolicy[tuple.appId]!!
        // get devices with necessary module
        val devicesWithModule: List<Int> = modulePlacement.moduleToDeviceMap[tuple.destModuleName]!!
        return if (tuple.moduleCopyMap.containsKey(tuple.destModuleName)) {
            // if tuple has to be sent to specific module with module.id == tuple.moduleCopyMap[tuple.destModuleName]
            val specificModuleId: Int = tuple.moduleCopyMap[tuple.destModuleName]!!
            val specificDeviceId: Int = devicesWithModule.find { deviceId ->
                modulePlacement.deviceToModuleMap[deviceId]!!.find { module ->
                    module.name == tuple.destModuleName
                }!!.id == specificModuleId
            } ?: run {
                val specificDeviceId = devicesWithModule.find { deviceId ->
                    modulePlacement.deviceToModuleMap[deviceId]!!.find { module ->
                        module.name == tuple.destModuleName
                    } != null
                }!!
                tuple.moduleCopyMap[tuple.destModuleName] = modulePlacement.deviceToModuleMap[specificDeviceId]!!.find { module ->
                    module.name == tuple.destModuleName
                }!!.id
                specificDeviceId
            }

            Pair(listOf(specificDeviceId), AddressingModel.Quantifier.SINGLE)
        } else {
            when (tuple.direction) {
                Tuple.UP -> Pair(devicesWithModule, AddressingModel.Quantifier.ANY)
                Tuple.DOWN -> {
                    when (device.addressingType) {
                        AddressingDevice.AddressingType.HIERARCHICAL -> {
                            Pair(device.addressingModel.filterInChildren(device, devicesWithModule), AddressingModel.Quantifier.ALL)
                        }
                        AddressingDevice.AddressingType.PEER_TO_PEER -> {
                            Pair(devicesWithModule, AddressingModel.Quantifier.ALL)
                        }
                    }
                }
                else -> throw Exception("Unknown tuple ${tuple.cloudletId} direction")
            }
        }
    }


}
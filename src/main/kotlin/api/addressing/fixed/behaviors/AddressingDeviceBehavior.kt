package api.addressing.fixed.behaviors

import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.TupleNextHopsTargetsContainer
import api.common.utils.TupleRecipientPair
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.entities.Tuple
import org.fog.utils.Logger

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
//            Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag -> onAddressTupleFreeLink(ev)
            Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag -> onCreateChildrenMapping(ev)
            Events.ADDRESSING_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    private fun onNetworkAddressTuple(ev: SimEvent): Boolean {
        val (tuple, _) = ev.data as TupleRecipientPair
        Logger.debug(device.mName, "Trying to address tuple ${tuple.cloudletId}")
        if (device.addressingChildrenMapping[tuple] == null) {
            device.addressingChildrenMapping[tuple] = mutableMapOf()
            val targetDeviceIds = getTargetDevicesForTuple(tuple)
            val quantifier =
                    if (tuple.direction == Tuple.UP) AddressingModel.Quantifier.ANY else AddressingModel.Quantifier.ALL
            val nextHopIds = device.addressingModel.idsOfNextHopTo(device, targetDeviceIds, quantifier, tuple.cloudletFileSize)
            device.mSendEvent(device.mId, 0.0, Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag,
                    BaseEventWrapper(ev, TupleNextHopsTargetsContainer(tuple, nextHopIds))
            )
            device.mWaitForEvent(PredicateType(Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag))
        }

        device.mSendEvent(device.mId, 0.0, Events.ADDRESSING_DEVICE_ADDRESS_TUPLE.tag, ev)
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onCreateChildrenMapping(ev: SimEvent): Boolean {
        val (originalEvent, container) = ev.data as BaseEventWrapper<TupleNextHopsTargetsContainer>
        val (tuple, targetNextHopMap) = container
        if (tuple.direction == Tuple.UP) {
            assert(targetNextHopMap.size == 1)
            assert((originalEvent.data as TupleRecipientPair).recipientId == device.mParentId)
            device.addressingChildrenMapping[tuple]!![targetNextHopMap.values.first()] = true
        }
        else {
            device.mChildrenIds.forEach { device.addressingChildrenMapping[tuple]!![it] = false }
            device.addressingChildrenMapping[tuple]!![device.mParentId] = false
            targetNextHopMap.forEach { (_, nextHop) ->
                device.addressingChildrenMapping[tuple]!![nextHop] = true
            }
        }
        return false
    }

    private fun onAddressTuple(ev: SimEvent): Boolean {
        val originalEvent = ev.data as SimEvent
        val (tuple, recipientId) = originalEvent.data as TupleRecipientPair
        var res = true
        Logger.debug(device.mName, "Addressing tuple ${tuple.cloudletId}")
        if (tuple.direction == Tuple.UP) {
            assert(device.addressingChildrenMapping.size == 1)
            assert(device.addressingChildrenMapping[tuple]!!.values.first())
            (originalEvent.data as TupleRecipientPair).recipientId = device.addressingChildrenMapping[tuple]!!.keys.first()
            device.addressingChildrenMapping[tuple]!!.remove((originalEvent.data as TupleRecipientPair).recipientId)
            res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
        }
        else {
            if (device.addressingChildrenMapping[tuple]!!.remove(recipientId)!!) {
                res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
            }
            if (device.addressingChildrenMapping[tuple]!!.size == 1 && device.addressingChildrenMapping[tuple]!!.remove(device.mParentId)!!) {
                (originalEvent.data as TupleRecipientPair).recipientId = device.mParentId
                res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
            }
        }
        if (device.addressingChildrenMapping[tuple]!!.isEmpty()) {
            device.addressingChildrenMapping.remove(tuple)
        }
        return res
//        var res = true
//
//        if (recipientId == device.mParentId || device.addressingChildrenMapping.remove(recipientId)!!) {
//            res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
//        }
//        if (device.addressingChildrenMapping.size == 1 && device.addressingChildrenMapping.containsKey(device.mParentId)) {
//            device.addressingChildrenMapping.remove(device.mParentId)
//            (originalEvent.data as TupleRecipientPair).recipientId = device.mParentId
//            res = res && superNetworkDeviceBehavior.processEvent(originalEvent)
//        }
//        return res
    }

    private fun getTargetDevicesForTuple(tuple: Tuple): List<Int> {
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
            }!!
            listOf(specificDeviceId)
        } else {
            devicesWithModule
        }
    }
}
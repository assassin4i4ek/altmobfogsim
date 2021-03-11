package api.addressing.fixed.behaviors

import api.addressing.fixed.entities.AddressingDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.TupleNextHopTargetsContainer
import api.common.utils.TupleRecipientPair
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple

interface AddressingDeviceBehavior<T: BaseBehavior<T, out NetworkDevice>>
    : BaseBehavior<AddressingDeviceBehavior<T>, AddressingDevice> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        device.mChildrenIds.add(device.mId)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag -> onAddressTupleFreeLink(ev)
            Events.ADDRESSING_DEVICE_ADDRESS_TUPLE_TO_TARGET_DEVICES.tag -> onAddressTupleToTargetDevices(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    private fun onAddressTuple(ev: SimEvent): Boolean {
        val tupleRecipientPair = (ev.data as TupleRecipientPair)
        if (tupleRecipientPair.recipientId == device.mParentId && device.mParentId <= 0) {
            tupleRecipientPair.recipientId = device.mId
        }
        if (tupleRecipientPair.recipientId == device.mId || tupleRecipientPair.recipientId == device.mParentId) {
            return superNetworkDeviceBehavior.processEvent(ev)
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTupleFreeLink(ev: SimEvent): Boolean {
        val (tuple: Tuple, recipientId: Int) = ev.data as TupleRecipientPair
        if (recipientId == device.mId || recipientId == device.mParentId) {
            val targetDeviceIds = getTargetDevicesForTuple(tuple)
            val nextHopId = device.addressingModel.idOfNextHopTo(device, targetDeviceIds, tuple.cloudletFileSize)
            device.mSendEvent(device.mId, 0.0,
                    Events.ADDRESSING_DEVICE_ADDRESS_TUPLE_TO_TARGET_DEVICES.tag,
                    BaseEventWrapper(ev, TupleNextHopTargetsContainer(tuple, nextHopId, targetDeviceIds))
            )
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTupleToTargetDevices(ev: SimEvent): Boolean {
        val (baseEvent, container) = ev.data as BaseEventWrapper<TupleNextHopTargetsContainer>
        val (_, nextHopId, _) = container
        (baseEvent.data as TupleRecipientPair).recipientId = nextHopId
        return superNetworkDeviceBehavior.processEvent(baseEvent)
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
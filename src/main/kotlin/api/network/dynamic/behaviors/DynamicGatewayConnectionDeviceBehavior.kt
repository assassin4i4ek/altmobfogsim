package api.network.dynamic.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.TupleRecipientPair
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface DynamicGatewayConnectionDeviceBehavior<T: BaseBehavior<T, out NetworkDevice>>
    : BaseBehavior<DynamicGatewayConnectionDeviceBehavior<T>, DynamicGatewayConnectionDevice> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        device.mSendEvent(device.mId, 0.0, Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag, null)
        device.mWaitForEvent(PredicateType(Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag))
        superNetworkDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag -> onConnectionUpdate()
            FogEvents.UPDATE_NORTH_TUPLE_QUEUE -> onUpdateNorthTupleQueue()
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    private fun onConnectionUpdate(): Boolean {
        if (device.mDynamicParentId <= 0) {
            // if device was disconnected
            if (!device.mNorthLinkBusy) {
                // if device is not transmitting any messages
                device.mNorthLinkBusy = true
                // block north queue
            }
        } else {
            // if device was connected to new gateway
            device.mSendEvent(device.mId, 0.0, FogEvents.UPDATE_NORTH_TUPLE_QUEUE, null)
//            device.mWaitForEvent(PredicateType(FogEvents.UPDATE_NORTH_TUPLE_QUEUE))
            // send event to refresh north queue
        }
        return false
    }

    private fun onUpdateNorthTupleQueue(): Boolean {
        return if (device.mDynamicParentId <= 0) {
            // if device is not connected to gateway
            assert(device.mNorthLinkBusy)
            // assert north queue is blocked
            false
            // block super call of UPDATE_NORTH_TUPLE_QUEUE event
        } else {
            true
            // let super handle UPDATE_NORTH_TUPLE_QUEUE event
        }
    }

    fun onAddressTuple(ev: SimEvent): Boolean {
        val (tuple, recipientId) = ev.data as TupleRecipientPair
        if (recipientId == device.mParentId && device.mParentId <= 0) {
            device.mNorthLinkQueue.add(tuple)
            Logger.debug(device.mName, "Queued tuple with tupleId = ${tuple.cloudletId}, waiting for connection")
        }
        else {
            device.sSendUp(tuple)
        }
        return true
    }

}
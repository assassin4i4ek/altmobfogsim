package api.network.fixed.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.TupleRecipientPair
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.entities.Tuple
import org.fog.utils.Logger

interface NetworkDeviceBehavior
    : BaseBehavior<NetworkDeviceBehavior, NetworkDevice> {
    override fun onStart() {}

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag -> onAddressTupleFreeLink(ev)
            else -> true
        }
    }

    private fun onAddressTuple(ev: SimEvent): Boolean {
        val (tuple: Tuple, recipientId: Int) = ev.data as TupleRecipientPair
        if (recipientId > 0) {
            if (recipientId == device.mParentId) {
                Logger.debug(device.mName, "Trying to send tuple ${tuple.cloudletId} up")
                device.sSendUp(tuple)
            }
            else {
                Logger.debug(device.mName, "Trying to send tuple ${tuple.cloudletId} down")
                device.sSendDown(tuple, recipientId)
            }
            return false
        }
        throw Exception("Error trying to send tuple to $recipientId")
    }

    private fun onAddressTupleFreeLink(ev: SimEvent): Boolean {
        val (tuple: Tuple, recipientId: Int) = ev.data as TupleRecipientPair
        if (recipientId > 0) {
            if (recipientId == device.mParentId) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} up")
                device.sSendUpFreeLink(tuple)
                return false
            }
            else if (device.mChildToLatencyMap.containsKey(recipientId)) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} down")
                device.sSendDownFreeLink(tuple, recipientId)
                return false
            }
        }
        throw Exception("Error sending tuple ${tuple.cloudletId} to $recipientId")
    }
}
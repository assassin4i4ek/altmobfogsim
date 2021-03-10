package api.network.fixed.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.TupleRecipientPair
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple
import org.fog.utils.Logger

interface NetworkDeviceBehavior
    : BaseBehavior<NetworkDeviceBehavior, NetworkDevice> {
    override fun onStart() {}

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTuple(ev: SimEvent): Boolean {
        val (tuple: Tuple, recipientId: Int) = ev.data as TupleRecipientPair
        if (recipientId > 0) {
            if (recipientId == device.mParentId) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} up")
                device.sSendUp(tuple)
                return false
            }
            else if (device.mChildToLatencyMap.containsKey(recipientId)) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} down")
                device.sSendDown(tuple, recipientId)
                return false
            }
        }
        throw Exception("Error sending tuple to $recipientId")
    }
}
package api.network.behavior

import api.common.Events
import api.network.entities.NetworkDevice
import api.original.behavior.OriginalFogDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple
import org.fog.utils.Logger

interface NetworkDeviceBehavior<T: NetworkDevice>: OriginalFogDeviceBehavior<T> {
    override fun onProcessEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> (ev.data as Pair<*, *>).run {
                addressTuple(first as Tuple, second as Int)
            }
            else -> super.onProcessEvent(ev)
        }
    }

    fun addressTuple(tuple: Tuple, recipientId: Int): Boolean {
        if (recipientId > 0) {
            if (recipientId == device.mParentId) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} up")
                device.mSendUp(tuple)
                return false
            }
            else if (device.mChildToLatencyMap.containsKey(recipientId)) {
                Logger.debug(device.mName, "Sending tuple ${tuple.cloudletId} up")
                device.mSendDown(tuple, recipientId)
                return false
            }
        }
        throw Exception("Error sending tuple to $recipientId")
    }

    fun onSendUp(tuple: Tuple) {
        device.mSendEvent(device.mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, Pair(tuple, device.mParentId))
    }

    fun onSendDown(tuple: Tuple, recipientId: Int) {
        device.mSendEvent(device.mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, Pair(tuple, recipientId))
    }
}
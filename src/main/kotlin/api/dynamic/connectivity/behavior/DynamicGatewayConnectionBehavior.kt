package api.dynamic.connectivity.behavior

import api.common.Events
import api.dynamic.connectivity.entities.DynamicGatewayConnectionDevice
import api.network.behavior.NetworkDeviceBehavior
import api.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface DynamicGatewayConnectionBehavior<T: DynamicGatewayConnectionDevice>: NetworkDeviceBehavior<T> {

    override fun onProcessEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.DYNAMIC_GATEWAY_CONNECTION_CHANGED.tag -> connectionUpdate()
            FogEvents.UPDATE_NORTH_TUPLE_QUEUE -> updateNorthTuple()
            else -> super.onProcessEvent(ev)
        }
    }

    override fun onStartEntity(device: T) {
        super.onStartEntity(device)
        device.mSendEvent(device.mId, 0.0, Events.DYNAMIC_GATEWAY_CONNECTION_CHANGED.tag, null)
    }

    fun connectionUpdate(): Boolean {
        if (device.mParentId <= 0 && !device.mNorthLinkBusy) {
            device.mNorthLinkBusy = true
        }
        else {
            device.mSendEvent(device.mId, 0.0, FogEvents.UPDATE_NORTH_TUPLE_QUEUE, null)
        }
        return false
    }

    fun updateNorthTuple(): Boolean {
        return if (device.mParentId <= 0) {
            assert(device.mNorthLinkBusy)
            false
        } else {
            true
        }
    }

    override fun addressTuple(tuple: Tuple, recipientId: Int): Boolean {
        return if (recipientId == device.mParentId && device.mParentId <= 0) {
            device.mNorthLinkQueue.add(tuple)
            Logger.debug(device.mName, "Queued tuple with tupleId = ${tuple.cloudletId}, waiting for connection")
            false
        } else {
            super.addressTuple(tuple, recipientId)
        }
    }

    fun onSetParentId(prevParentId: Int, newParentId: Int) {
        if (prevParentId != newParentId) {
            device.mSendEvent(device.mId, 0.0, Events.DYNAMIC_GATEWAY_CONNECTION_CHANGED.tag, null)
        }
    }

    companion object {
        fun connectChildToParent(parent: NetworkDevice, child: DynamicGatewayConnectionDevice) {
            Logger.debug(child.mName, "Connecting to ${parent.mName}")
            parent.mChildrenIds.add(child.mId)
            parent.mChildToLatencyMap[child.mId] = child.mUplinkLatency
            child.mDynamicParentId = parent.mId
        }

        fun disconnectChildFromParent(parent: NetworkDevice, child: DynamicGatewayConnectionDevice) {
            Logger.debug(child.mName, "Disconnecting from deviceId = ${parent.mName}")
            parent.mChildrenIds.remove(child.mId)
            parent.mChildToLatencyMap.remove(child.mId)
            child.mDynamicParentId = -1
        }
    }
}
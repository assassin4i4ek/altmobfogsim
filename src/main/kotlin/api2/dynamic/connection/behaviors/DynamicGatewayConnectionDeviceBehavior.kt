package api2.dynamic.connection.behaviors

import api2.common.Events
import api2.common.behaviors.BaseBehavior
import api2.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api2.network.behaviors.NetworkDeviceBehavior
import api2.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface DynamicGatewayConnectionDeviceBehavior
    : BaseBehavior<DynamicGatewayConnectionDeviceBehavior, DynamicGatewayConnectionDevice> {
    val superNetworkDeviceBehavior: NetworkDeviceBehavior

    override fun processEvent(ev: SimEvent): Boolean {
        return when(ev.tag) {
            Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag -> onConnectionUpdate()
            FogEvents.UPDATE_NORTH_TUPLE_QUEUE -> onUpdateNorthTupleQueue()
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev) && super.processEvent(ev)
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
        }
        else {
            // if device was connected to new gateway
            device.mSendEvent(device.mId, 0.0, FogEvents.UPDATE_NORTH_TUPLE_QUEUE, null)
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

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTuple(ev: SimEvent): Boolean {
        val (tuple, recipientId) = ev.data as Pair<Tuple, Int>
        return if (recipientId == device.mParentId && device.mParentId <= 0) {
            device.mNorthLinkQueue.add(tuple)
            Logger.debug(device.mName, "Queued tuple with tupleId = ${tuple.cloudletId}, waiting for connection")
            false
        } else {
            superNetworkDeviceBehavior.processEvent(ev)
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
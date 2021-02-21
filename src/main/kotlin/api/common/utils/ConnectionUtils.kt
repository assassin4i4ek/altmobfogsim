package api.common.utils

import api.addressing.entities.AddressingDevice
import api.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api.network.entities.NetworkDevice
import org.fog.utils.Logger

object ConnectionUtils {
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

    fun connectPeerToPeer(device1: AddressingDevice, device2: AddressingDevice, latency: Double) {
        device1.mChildrenIds.add(device2.mId)
        device1.mChildToLatencyMap[device2.mId] = latency
        device2.mChildrenIds.add(device1.mId)
        device2.mChildToLatencyMap[device1.mId] = latency
    }

    fun disconnectPeerToPeer(device1: AddressingDevice, device2: AddressingDevice) {
        device1.mChildrenIds.remove(device2.mId)
        device1.mChildToLatencyMap.remove(device2.mId)
        device2.mChildrenIds.remove(device1.mId)
        device2.mChildToLatencyMap.remove(device1.mId)
    }
}
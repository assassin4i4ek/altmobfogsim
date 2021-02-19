package api.dynamic.connection.entites

import api.network.entities.NetworkDevice
import org.fog.utils.Logger

object DynamicConnections {
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
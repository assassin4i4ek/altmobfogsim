package api.dynamic.notification

import api.dynamic.connectivity.behavior.DynamicGatewayConnectionBehavior
import api.dynamic.connectivity.entities.DynamicGatewayConnectionDevice

interface NotificationOnConnectedBehavior: DynamicGatewayConnectionBehavior<DynamicGatewayConnectionDevice> {
    override fun onSetParentId(prevParentId: Int, newParentId: Int) {
        super.onSetParentId(prevParentId, newParentId)
        if (newParentId != -1) {
            //notifyAll...
        }
    }
}
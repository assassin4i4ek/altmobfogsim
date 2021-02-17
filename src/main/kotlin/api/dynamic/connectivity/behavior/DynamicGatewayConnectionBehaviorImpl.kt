package api.dynamic.connectivity.behavior

import api.dynamic.connectivity.entities.DynamicGatewayConnectionDevice

class DynamicGatewayConnectionBehaviorImpl: DynamicGatewayConnectionBehavior<DynamicGatewayConnectionDevice> {
    override lateinit var device: DynamicGatewayConnectionDevice
}
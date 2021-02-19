package api.dynamic.connection.behaviors

import api.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api.network.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionDeviceBehaviorImpl(
    override val device: DynamicGatewayConnectionDevice,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior
) : DynamicGatewayConnectionDeviceBehavior {
}
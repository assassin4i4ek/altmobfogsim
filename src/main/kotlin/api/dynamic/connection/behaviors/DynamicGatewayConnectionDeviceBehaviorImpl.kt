package api.dynamic.connection.behaviors

import api.common.behaviors.BaseBehavior
import api.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api.network.behaviors.NetworkDeviceBehavior
import api.network.entities.NetworkDevice

class DynamicGatewayConnectionDeviceBehaviorImpl(
    override val device: DynamicGatewayConnectionDevice,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior
) : DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior> {
}
package api.network.dynamic.behaviors

import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionDeviceBehaviorImpl(
    override val device: DynamicGatewayConnectionDevice,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior
) : DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>
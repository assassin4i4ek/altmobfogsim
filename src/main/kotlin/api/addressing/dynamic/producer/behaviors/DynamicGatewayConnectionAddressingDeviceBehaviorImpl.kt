package api.addressing.dynamic.producer.behaviors

import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionAddressingDeviceBehaviorImpl(
        override val device: DynamicGatewayConnectionDevice,
        override val superNetworkDeviceBehavior: AddressingDeviceBehavior<NetworkDeviceBehavior>
        ) : DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>
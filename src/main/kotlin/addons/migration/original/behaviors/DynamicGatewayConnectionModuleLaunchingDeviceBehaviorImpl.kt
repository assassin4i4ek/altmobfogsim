package addons.migration.original.behaviors

import addons.migration.original.entities.DynamicGatewayConnectionModuleLaunchingDevice
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionModuleLaunchingDeviceBehaviorImpl(
        override val device: DynamicGatewayConnectionModuleLaunchingDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>
) : DynamicGatewayConnectionModuleLaunchingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>> {

}
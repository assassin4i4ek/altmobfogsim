package api.migration.original.behaviors.addons

import api.migration.original.entites.addons.DynamicGatewayConnectionModuleLaunchingDevice
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionModuleLaunchingDeviceBehaviorImpl(
        override val device: DynamicGatewayConnectionModuleLaunchingDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>
) : DynamicGatewayConnectionModuleLaunchingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>> {

}
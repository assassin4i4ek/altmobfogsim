package api.migration.behaviors

import api.common.behaviors.BaseBehavior
import api.migration.entites.DynamicGatewayConnectionMobileLaunchingDevice
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior

class DynamicGatewayConnectionMobileLaunchingDeviceBehaviorImpl(
        override val device: DynamicGatewayConnectionMobileLaunchingDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>
) : DynamicGatewayConnectionMobileLaunchingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>> {

}
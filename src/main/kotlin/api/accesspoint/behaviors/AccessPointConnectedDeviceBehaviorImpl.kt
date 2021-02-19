package api.accesspoint.behaviors

import api.accesspoint.entities.AccessPointConnectedDevice
import api.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.dynamic.mobility.behaviors.MobileDeviceBehavior

class AccessPointConnectedDeviceBehaviorImpl(
    override val device: AccessPointConnectedDevice,
    override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior,
    override val superMobilityDeviceBehavior: MobileDeviceBehavior
) : AccessPointConnectedDeviceBehavior {
}
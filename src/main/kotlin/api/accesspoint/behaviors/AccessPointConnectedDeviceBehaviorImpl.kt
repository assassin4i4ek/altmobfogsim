package api.accesspoint.behaviors

import api.accesspoint.entities.AccessPointConnectedDevice
import api.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.dynamic.mobility.behaviors.MobileDeviceBehavior
import api.network.behaviors.NetworkDeviceBehavior

class AccessPointConnectedDeviceBehaviorImpl(
    override val device: AccessPointConnectedDevice,
    override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>,
    override val superMobilityDeviceBehavior: MobileDeviceBehavior
) : AccessPointConnectedDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>, MobileDeviceBehavior>
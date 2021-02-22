package api.accesspoint.original.behaviors

import api.accesspoint.original.entities.AccessPointConnectedDevice
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.mobility.behaviors.MobileDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior

class AccessPointConnectedDeviceBehaviorImpl(
    override val device: AccessPointConnectedDevice,
    override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>,
    override val superMobilityDeviceBehavior: MobileDeviceBehavior
) : AccessPointConnectedDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>, MobileDeviceBehavior>
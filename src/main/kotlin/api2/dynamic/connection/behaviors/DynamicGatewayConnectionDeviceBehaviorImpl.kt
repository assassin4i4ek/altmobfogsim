package api2.dynamic.connection.behaviors

import api2.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api2.network.behaviors.NetworkDeviceBehavior
import api2.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent

class DynamicGatewayConnectionDeviceBehaviorImpl(
    override val device: DynamicGatewayConnectionDevice,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior
) : DynamicGatewayConnectionDeviceBehavior {
}
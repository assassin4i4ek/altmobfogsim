package api.migration.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.migration.entites.DynamicGatewayConnectionMobileLaunchingDevice
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import org.cloudbus.cloudsim.core.SimEvent

interface DynamicGatewayConnectionMobileLaunchingDeviceBehavior<
        T1:BaseBehavior<T1, out DynamicGatewayConnectionDevice>,
        >: BaseBehavior<DynamicGatewayConnectionMobileLaunchingDeviceBehavior<T1>, DynamicGatewayConnectionMobileLaunchingDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: T1

    override fun onStart() {
        superDynamicGatewayConnectionDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
    }
}
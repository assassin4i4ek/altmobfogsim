package api.migration.original.behaviors.addons

import api.common.behaviors.BaseBehavior
import api.migration.original.entites.addons.DynamicGatewayConnectionModuleLaunchingDevice
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import org.cloudbus.cloudsim.core.SimEvent

interface DynamicGatewayConnectionModuleLaunchingDeviceBehavior<
        T1:BaseBehavior<T1, out DynamicGatewayConnectionDevice>,
        >: BaseBehavior<DynamicGatewayConnectionModuleLaunchingDeviceBehavior<T1>, DynamicGatewayConnectionModuleLaunchingDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: T1

    override fun onStart() {
        superDynamicGatewayConnectionDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
    }
}
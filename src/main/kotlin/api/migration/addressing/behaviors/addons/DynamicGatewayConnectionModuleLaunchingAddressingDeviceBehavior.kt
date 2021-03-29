package api.migration.addressing.behaviors.addons

import api.common.behaviors.BaseBehavior
import api.migration.addressing.entities.addons.DynamicGatewayConnectionModuleLaunchingAddressingDevice
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import org.cloudbus.cloudsim.core.SimEvent

interface DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehavior<
        T: BaseBehavior<T, out DynamicGatewayConnectionDevice>
        >: BaseBehavior<DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehavior<T>, DynamicGatewayConnectionModuleLaunchingAddressingDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: T

    override fun onStart() {
        superDynamicGatewayConnectionDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
    }
}
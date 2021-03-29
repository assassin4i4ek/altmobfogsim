package api.common.entities

import api.common.behaviors.BaseBehavior
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppModule
import org.fog.entities.FogDevice

interface SimEntityBehaviorWrapper<Self: SimEntity, T: BaseBehavior<T, Self>> {
    fun startEntity() {
        behavior.onStart()
        (this as FogDevice).host.vmDestroyAll()
    }

    val behavior: T

    fun onProcessEvent(ev: SimEvent): Boolean {
        return behavior.processEvent(ev)
    }
}
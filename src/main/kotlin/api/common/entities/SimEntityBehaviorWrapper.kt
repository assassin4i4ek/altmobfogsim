package api.common.entities

import api.common.behaviors.BaseBehavior
import org.cloudbus.cloudsim.core.SimEvent

interface SimEntityBehaviorWrapper<Self: SimEntity, T: BaseBehavior<T, Self>> {
    fun startEntity() {
        behavior.onStart()
    }

    fun processOtherEvent(ev: SimEvent) {}

    val behavior: T

    fun onProcessEvent(ev: SimEvent): Boolean {
        return behavior.processEvent(ev)
    }
}
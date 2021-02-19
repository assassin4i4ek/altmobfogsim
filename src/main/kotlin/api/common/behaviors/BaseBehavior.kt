package api.common.behaviors

import api.common.entities.SimEntity
import org.cloudbus.cloudsim.core.SimEvent

interface BaseBehavior<Self: BaseBehavior<Self, T>, T: SimEntity> {
    val device: T
    fun processEvent(ev: SimEvent): Boolean// = true
    fun onStart()// {}
}

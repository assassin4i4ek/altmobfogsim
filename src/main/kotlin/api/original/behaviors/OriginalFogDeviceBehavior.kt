package api.original.behaviors

import api.common.behaviors.BaseBehavior
import api.original.entities.OriginalFogDevice
import org.cloudbus.cloudsim.core.SimEvent

interface OriginalFogDeviceBehavior: BaseBehavior<OriginalFogDeviceBehavior, OriginalFogDevice> {
    override fun onStart() {}
    override fun processEvent(ev: SimEvent): Boolean = true
}
package api2.original.behaviors

import api2.common.behaviors.BaseBehavior
import api2.original.entities.OriginalFogDevice
import org.cloudbus.cloudsim.core.SimEvent

interface OriginalFogDeviceBehavior: BaseBehavior<OriginalFogDeviceBehavior, OriginalFogDevice> {
    override fun processEvent(ev: SimEvent): Boolean = true
}
package api.original.behavior

import api.original.entities.OriginalFogDevice
import org.cloudbus.cloudsim.core.SimEvent

interface OriginalFogDeviceBehavior<T: OriginalFogDevice> {
    var device: T
    fun onStartEntity(device: T) {
        this.device = device
    }
    fun onProcessEvent(ev: SimEvent): Boolean = true
}
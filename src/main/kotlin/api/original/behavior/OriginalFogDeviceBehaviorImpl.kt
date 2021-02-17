package api.original.behavior

import api.original.entities.OriginalFogDevice

class OriginalFogDeviceBehaviorImpl : OriginalFogDeviceBehavior<OriginalFogDevice> {
    override lateinit var device: OriginalFogDevice
}
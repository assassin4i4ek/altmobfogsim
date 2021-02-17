package api.dynamic.mobility.behavior

import api.dynamic.mobility.entities.MobileDevice

class MobileDeviceBehaviorImpl : MobileDeviceBehavior<MobileDevice> {
    override lateinit var device: MobileDevice
}
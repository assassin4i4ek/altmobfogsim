package api2.dynamic.mobility.behaviors

import api2.common.Events
import api2.common.behaviors.BaseBehavior
import api2.dynamic.mobility.entities.MobileDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface MobileDeviceBehavior: BaseBehavior<MobileDeviceBehavior, MobileDevice> {
    override fun onStart() {
        super.onStart()
        device.mSendEvent(device.mId, device.mobilityModel.nextUpdateTime, Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MOBILE_DEVICE_NEXT_MOVE.tag -> onNextMove(ev)
            else -> super.processEvent(ev)
        }
    }

    private fun onNextMove(ev: SimEvent): Boolean {
        val newPosition = device.mobilityModel.nextMove(device.position)
        device.position = newPosition
        device.mSendEvent(device.mId, device.mobilityModel.nextUpdateTime, api.common.Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
        Logger.debug(device.mName, "Moved to coordinates ${device.position.coordinates}")
        return true
    }
}
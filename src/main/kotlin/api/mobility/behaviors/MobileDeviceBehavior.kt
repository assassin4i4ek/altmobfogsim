package api.mobility.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.mobility.entities.MobileDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface MobileDeviceBehavior: BaseBehavior<MobileDeviceBehavior, MobileDevice> {
    override fun onStart() {
        device.mSendEvent(device.mId, device.mobilityModel.nextUpdateTime, Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MOBILE_DEVICE_NEXT_MOVE.tag -> onNextMove()
            else -> true
        }
    }

    private fun onNextMove(): Boolean {
        val newPosition = device.mobilityModel.nextMove(device.position)
        device.position = newPosition
        device.mSendEvent(device.mId, device.mobilityModel.nextUpdateTime, Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
        Logger.debug(device.mName, "Moved to coordinates ${device.position.coordinates}")
        return true
    }
}
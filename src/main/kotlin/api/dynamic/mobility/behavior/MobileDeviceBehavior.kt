package api.dynamic.mobility.behavior

import api.common.Events
import api.dynamic.mobility.entities.MobileDevice
import api.original.behavior.OriginalFogDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface MobileDeviceBehavior<T: MobileDevice>: OriginalFogDeviceBehavior<T> {
    override fun onStartEntity(device: T) {
        super.onStartEntity(device)
        device.mSendEvent(device.mId, device.positionAndTimestamp.timestamp, Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
    }

    override fun onProcessEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MOBILE_DEVICE_NEXT_MOVE.tag -> nextMove()
            else -> super.onProcessEvent(ev)
        }
    }

    fun nextMove(): Boolean {
        val newPositionAndTimestamp = device.mobilityModel.nextMove(device.positionAndTimestamp)
        device.positionAndTimestamp = newPositionAndTimestamp
        device.mSendEvent(device.mId, newPositionAndTimestamp.timestamp, Events.MOBILE_DEVICE_NEXT_MOVE.tag, null)
        Logger.debug(device.mName, "Moved to coordinates ${device.positionAndTimestamp.position.coordinates}")
        return true
    }
}
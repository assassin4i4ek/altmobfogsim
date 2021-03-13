package experiments

import api.accesspoint.original.entities.AccessPointConnectedDevice
import api.mobility.models.SteadyMobilityModel
import api.mobility.positioning.Position
import org.fog.entities.FogDevice
import org.fog.utils.Logger

class ConnectionAwareMobilityModel(
nextUpdateTime: Double,
private val i: Int,
private val sideLength: Int
) : SteadyMobilityModel(nextUpdateTime) {
    lateinit var device: AccessPointConnectedDevice

    override fun nextMove(currentPosition: Position): Position {
        val newPosition = super.nextMove(currentPosition)
        if (device.accessPoint?.connectionZone?.isInZone(newPosition) == true) {
            val ifBottomLeftAngle = (
                    newPosition.coordinates.coordX <= 100.0 * i + 1e-6)
                    &&
                    (newPosition.coordinates.coordY <= 1e-6)
            val ifBottomRightAngle = (
                    newPosition.coordinates.coordX >= 100.0 * (i + (sideLength.toDouble() - 1) / sideLength) - 1e-6)
                    &&
                    (newPosition.coordinates.coordY <= 1e-6)
            val ifTopRightAngle = (
                    newPosition.coordinates.coordX >= 100.0 * (i + (sideLength.toDouble() - 1) / sideLength) - 1e-6)
                    &&
                    (newPosition.coordinates.coordY >= 100.0 * (sideLength.toDouble() - 1) / sideLength - 1e-6)
            val ifTopLeftAngle = (
                    newPosition.coordinates.coordX <= 100.0 * i + 1e-6)
                    &&
                    (newPosition.coordinates.coordY >= 100.0 * (sideLength.toDouble() - 1) / sideLength - 1e-6)
            if (ifBottomLeftAngle || ifBottomRightAngle || ifTopRightAngle || ifTopLeftAngle) {
                newPosition.orientationDeg += 90.0
                if (newPosition.orientationDeg >= 180.0) {
                    newPosition.orientationDeg -= 360.0
                }
            }
            return newPosition
        }
        else {
            return if (device.accessPoint != null &&
                    (device.accessPoint!! as FogDevice).isSouthLinkBusy) {
                Logger.debug(device.mName, "Remaining position for ${device.accessPoint!!.mName}")
                currentPosition
            } else {
                newPosition
            }
        }
    }
}
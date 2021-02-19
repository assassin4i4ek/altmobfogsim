package api2.accesspoint.entities

import api2.dynamic.mobility.positioning.Coordinates
import api2.dynamic.mobility.positioning.distance

object AccessPointsMap {
    private val accessPoints: MutableList<AccessPoint> = mutableListOf()

    fun getClosestAccessPointsTo(deviceCoord: Coordinates): List<AccessPoint> {
        return accessPoints.sortedBy { distance(it.coordinates, deviceCoord) }
    }

    fun registerAccessPoint(ap : AccessPoint) {
        accessPoints.add(ap)
    }
}
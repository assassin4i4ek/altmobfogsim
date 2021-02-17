package api.accesspoint.entities

import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.distance

object AccessPointsMap {
    private val accessPoints: MutableList<api.accesspoint.entities.AccessPoint> = mutableListOf()

    fun getClosestAccessPointsTo(deviceCoord: Coordinates): List<api.accesspoint.entities.AccessPoint> {
        return api.accesspoint.entities.AccessPointsMap.accessPoints.sortedBy { distance(it.coordinates, deviceCoord) }
    }

    fun registerAccessPoint(ap: api.accesspoint.entities.AccessPoint) {
        api.accesspoint.entities.AccessPointsMap.accessPoints.add(ap)
    }
}
package api.accesspoint.entities

import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Zone
import api.network.entities.NetworkDevice

interface AccessPoint: NetworkDevice {
    val coordinates: Coordinates
    val connectionZone: Zone

    val accessPointsMap: AccessPointsMap
}
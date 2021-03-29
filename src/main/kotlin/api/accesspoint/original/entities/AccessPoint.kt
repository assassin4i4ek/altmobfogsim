package api.accesspoint.original.entities

import api.accesspoint.original.utils.AccessPointsMap
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Zone
import api.network.fixed.entities.NetworkDevice

interface AccessPoint: NetworkDevice {
    val coordinates: Coordinates
    val connectionZone: Zone

    val accessPointsMap: AccessPointsMap
}
package api.accesspoint.original.entities

import api.accesspoint.utils.AccessPointsMap
import api.common.positioning.Coordinates
import api.common.positioning.Zone
import api.network.fixed.entities.NetworkDevice

interface AccessPoint: NetworkDevice {
    val coordinates: Coordinates
    val connectionZone: Zone
    val downlinkLatency: Double
    val accessPointsMap: AccessPointsMap
}
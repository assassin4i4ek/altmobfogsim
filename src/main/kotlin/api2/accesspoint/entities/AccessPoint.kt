package api2.accesspoint.entities

import api2.dynamic.mobility.positioning.Coordinates
import api2.dynamic.mobility.positioning.Zone
import api2.network.entities.NetworkDevice

interface AccessPoint: NetworkDevice {
    val coordinates: Coordinates
    val connectionZone: Zone
}
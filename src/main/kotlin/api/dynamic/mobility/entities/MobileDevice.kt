package api.dynamic.mobility.entities

import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.positioning.PositionAndTimestamp
import api.original.entities.OriginalFogDevice

interface MobileDevice: OriginalFogDevice {
    var positionAndTimestamp: PositionAndTimestamp
    val mobilityModel: MobilityModel
}
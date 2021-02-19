package api2.dynamic.mobility.entities

import api2.dynamic.mobility.positioning.Position
import api2.common.entities.SimEntity
import api2.dynamic.mobility.models.MobilityModel

interface MobileDevice: SimEntity {
    var position: Position
    val mobilityModel: MobilityModel
}
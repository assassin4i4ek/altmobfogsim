package api.dynamic.mobility.entities

import api.dynamic.mobility.positioning.Position
import api.common.entities.SimEntity
import api.dynamic.mobility.models.MobilityModel

interface MobileDevice: SimEntity {
    var position: Position
    val mobilityModel: MobilityModel
}
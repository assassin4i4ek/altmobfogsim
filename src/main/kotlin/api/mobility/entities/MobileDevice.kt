package api.mobility.entities

import api.common.positioning.Position
import api.common.entities.SimEntity
import api.mobility.models.MobilityModel

interface MobileDevice: SimEntity {
    var position: Position
    val mobilityModel: MobilityModel
}
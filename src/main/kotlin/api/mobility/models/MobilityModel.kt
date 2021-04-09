package api.mobility.models

import api.common.positioning.Position

interface MobilityModel {
    var modelTimeUnitsPerSec: Double
    val nextUpdateTime: Double
    fun nextMove(currentPosition: Position): Position
}
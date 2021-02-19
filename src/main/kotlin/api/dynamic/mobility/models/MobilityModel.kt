package api.dynamic.mobility.models

import api.dynamic.mobility.positioning.Position

interface MobilityModel {
    var nextUpdateTime: Double
    fun nextMove(currentPosition: Position): Position
}
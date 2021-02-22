package api.mobility.models

import api.mobility.positioning.Position

interface MobilityModel {
    var nextUpdateTime: Double
    fun nextMove(currentPosition: Position): Position
}
package api2.dynamic.mobility.models

import api2.dynamic.mobility.positioning.Position

interface MobilityModel {
    var nextUpdateTime: Double
    fun nextMove(currentPosition: Position): Position
}
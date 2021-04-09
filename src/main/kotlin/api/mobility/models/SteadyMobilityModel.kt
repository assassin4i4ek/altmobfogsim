package api.mobility.models

import api.common.positioning.Coordinates
import api.common.positioning.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class SteadyMobilityModel(
    override var nextUpdateTime: Double,
    override var modelTimeUnitsPerSec: Double = 1.0
): MobilityModel {
    override fun nextMove(currentPosition: Position): Position {
        return Position(Coordinates(
                currentPosition.coordinates.coordX + cos(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime,
                currentPosition.coordinates.coordY + sin(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
        ), currentPosition.speed, currentPosition.orientationDeg)
    }
}
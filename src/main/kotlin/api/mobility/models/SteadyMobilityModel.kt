package api.mobility.models

import api.mobility.positioning.Coordinates
import api.mobility.positioning.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class SteadyMobilityModel(
    override var nextUpdateTime: Double
): MobilityModel {
    override fun nextMove(currentPosition: Position): Position {
        //        prevUpdateTime = prevUpdateTime
        return Position(Coordinates(
                currentPosition.coordinates.coordX + cos(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime,
                currentPosition.coordinates.coordY + sin(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
        ), currentPosition.speed, currentPosition.orientationDeg)
    }
}
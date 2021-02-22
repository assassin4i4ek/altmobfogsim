package api.mobility.models

import api.mobility.positioning.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SteadyMobilityModel(
    override var nextUpdateTime: Double
): MobilityModel {
    override fun nextMove(currentPosition: Position): Position {
        currentPosition.coordinates.coordX += cos(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
        currentPosition.coordinates.coordY += sin(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
//        prevUpdateTime = prevUpdateTime
        return currentPosition
    }
}
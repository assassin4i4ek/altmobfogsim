package api.dynamic.mobility.models

import api.dynamic.mobility.positioning.PositionAndTimestamp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SteadyMobilityModel(
): MobilityModel {
    override fun nextMove(currentPositionAndTimestamp: PositionAndTimestamp): PositionAndTimestamp {
        val (currentPosition, nextUpdateTime) = currentPositionAndTimestamp
        currentPosition.coordinates.coordX += cos(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
        currentPosition.coordinates.coordY += sin(currentPosition.orientationDeg * PI / 180) * currentPosition.speed * nextUpdateTime
        return PositionAndTimestamp(currentPosition, nextUpdateTime)
    }
}
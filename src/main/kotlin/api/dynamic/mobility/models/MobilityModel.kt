package api.dynamic.mobility.models

import api.dynamic.mobility.positioning.PositionAndTimestamp

interface MobilityModel {
    fun nextMove(currentPositionAndTimestamp: PositionAndTimestamp): PositionAndTimestamp
}
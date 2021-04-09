package api.common.positioning

import kotlin.math.pow
import kotlin.math.sqrt

fun distance(coord1: Coordinates, coord2: Coordinates): Double {
    return sqrt((coord1.coordX - coord2.coordX).pow(2) + (coord1.coordY - coord2.coordY).pow(2))
}
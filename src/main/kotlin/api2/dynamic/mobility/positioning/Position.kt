package api2.dynamic.mobility.positioning

import kotlin.math.abs

data class Position(
    val coordinates: Coordinates,
    var speed: Double,
    var orientationDeg: Double
) {
    override fun equals(other: Any?): Boolean {
        if (other is Position) {
            return distance(coordinates, other.coordinates) <= 1e-7 &&
                    abs(speed - other.speed) <= 1e-7 && abs(orientationDeg - other.orientationDeg) <= 1e-7
        }
        return false
    }

    override fun hashCode(): Int {
        var result = coordinates.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + orientationDeg.hashCode()
        return result
    }

    override fun toString(): String {
        return "(coordinates=$coordinates, speed=%.2f, orientationDeg=%.2f)".format(speed, orientationDeg)
    }
}

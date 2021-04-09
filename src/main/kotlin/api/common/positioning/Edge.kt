package api.common.positioning

import kotlin.math.max
import kotlin.math.min

class Edge(point1: Coordinates, point2: Coordinates) {
    private val start: Coordinates
    private val end: Coordinates

    init {
        if (point1.coordY <= point2.coordY) {
            start = point1
            end = point2
        }
        else {
            start = point2
            end = point1
        }
    }

    fun intersectsHorizontalRay(rayStart: Coordinates): Boolean {
        val rayStartCopy = rayStart.copy()
        if (rayStartCopy.coordY == start.coordY || rayStartCopy.coordY == end.coordY) {
            rayStartCopy.coordY += 1e-6
        }
        if (rayStartCopy.coordY < start.coordY ||
                rayStartCopy.coordY > end.coordY ||
                rayStartCopy.coordX >= max(start.coordX, end.coordX)) {
            return false
        }

        if (rayStartCopy.coordX < min(start.coordX, end.coordX)) {
            return true
        }
        val k1: Double = (end.coordY - start.coordY) / (end.coordX - start.coordX)
        val k2: Double = (rayStartCopy.coordY - start.coordY) / (rayStartCopy.coordX - start.coordX)
        return k2 >= k1
    }
}

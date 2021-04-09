package api.common.positioning

class PolygonalZone(val points: List<Coordinates>): Zone {
    init {
        assert(points.size >= 3)
    }

    val edges: List<Edge> = points.asSequence().plus(points.first()).zipWithNext { start, end ->
        Edge(start, end)
    }.toList()

    override fun isInZone(position: Position): Boolean {
        val intersectionCount = edges.map {
            if (it.intersectsHorizontalRay(position.coordinates)) 1 else 0
        }.sum()
        return intersectionCount % 2 == 1
    }
}
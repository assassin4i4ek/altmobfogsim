package api.mobility.positioning

class RadialZone(
    val center: Coordinates,
    val radius: Double
) : Zone {
    override fun isInZone(position: Position): Boolean {
        val dist = distance(center, position.coordinates)
        return dist <= radius
    }
}
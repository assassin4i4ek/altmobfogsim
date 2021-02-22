package api.mobility.positioning

interface Zone {
    fun isInZone(position: Position): Boolean
}
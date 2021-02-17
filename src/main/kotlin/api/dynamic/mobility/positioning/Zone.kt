package api.dynamic.mobility.positioning

interface Zone {
    fun isInZone(position: Position): Boolean
}
package api.common.positioning

interface Zone {
    fun isInZone(position: Position): Boolean
}
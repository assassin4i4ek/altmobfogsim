package api2.dynamic.mobility.positioning

interface Zone {
    fun isInZone(position: Position): Boolean
}
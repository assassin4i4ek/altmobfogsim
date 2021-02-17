package api.dynamic.mobility.positioning

data class Coordinates(
    var coordX: Double,
    var coordY: Double
) {
    override fun toString(): String {
        return "(x: %.2f; y: %.2f)".format(coordX, coordY)
    }
}

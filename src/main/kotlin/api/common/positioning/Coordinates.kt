package api.common.positioning

data class Coordinates(
    var coordX: Double,
    var coordY: Double
) {
    override fun toString(): String {
        return "(x: %.2f; y: %.2f)".format(coordX, coordY)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Coordinates) {
            return distance(this, other) < 1e-6
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = coordX.hashCode()
        result = 31 * result + coordY.hashCode()
        return result
    }
}

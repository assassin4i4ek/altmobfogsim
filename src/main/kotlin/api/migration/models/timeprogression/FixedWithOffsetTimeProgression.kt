package api.migration.models.timeprogression

class FixedWithOffsetTimeProgression(
        val firstOffset: Double,
        val otherValue: Double
): TimeProgression {
    private var isFirst: Boolean = true

    override fun nextTime(): Double {
        return if (isFirst) {
            isFirst = false
            firstOffset
        }
        else {
            otherValue
        }
    }
}
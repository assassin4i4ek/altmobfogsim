package api.migration.models.timeprogression

class FixedTimeProgression(val value: Double): TimeProgression {
    override fun nextTime(): Double {
        return value
    }
}
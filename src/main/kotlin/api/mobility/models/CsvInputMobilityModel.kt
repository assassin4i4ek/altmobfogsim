package api.mobility.models

import api.common.positioning.Coordinates
import api.common.positioning.Position
import java.io.File

class CsvInputMobilityModel(filename: String, override var modelTimeUnitsPerSec: Double, private val timeOffset: Double = 0.0): MobilityModel {
    override val nextUpdateTime get() = nextTime - currentTime

    private var currentTime: Double = 0.0
    private var nextTime: Double = 0.0
    private val csvFileReader = File(filename).bufferedReader()

    override fun nextMove(currentPosition: Position): Position {
        val line = csvFileReader.readLine()
        return if (line != null) {
            val values = line.split('\t')
            currentTime = nextTime
            nextTime = values[0].toDouble() - timeOffset
            val x = values[1].toDouble()
            val y = values[2].toDouble()
            val speed = values[3].toDouble()
            val angle = values[4].toDouble()
            Position(Coordinates(x, y), speed, angle)
        }
        else {
            nextTime = Double.MAX_VALUE
            csvFileReader.close()
            currentPosition.copy()
        }
    }
}
package api.accesspoint.original.utils

import api.common.positioning.Coordinates
import api.common.positioning.Position
import api.common.positioning.RadialZone
import api.common.positioning.Zone
import java.io.File
import kotlin.streams.toList

object AccessPointsMapUtils {
    fun generateRadialZonesFromCsv(csvFileName: String): List<Triple<String, RadialZone, Int>> {
        return File(csvFileName).bufferedReader().lines().map { line ->
            val values = line.split(',')
            val x = values[0].toDouble()
            val y = values[1].toDouble()
            val radius = values[2].toDouble()
            val type = values[3].toInt()
            val name = values[4]
            Triple(name, RadialZone(Coordinates(x, y), radius), type)
        }.toList()
    }
}
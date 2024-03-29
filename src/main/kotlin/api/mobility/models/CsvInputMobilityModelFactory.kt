package api.mobility.models

import java.io.File
import java.nio.file.Paths

object CsvInputMobilityModelFactory {
    // returns { name -> mobilityModel }
    fun fromDirectory(dirName: String, modelTimeUnitsPerSec: Double, limit: Int? = null, timeOffset: Double = 0.0): Map<String, CsvInputMobilityModel> {
        return File(dirName).listFiles()!!
                .filter { srcFile ->
                    srcFile.nameWithoutExtension.startsWith("pedestrian") && !srcFile.nameWithoutExtension.endsWith("_tr")
                }
                .run { if (limit != null) take(limit) else this }.mapIndexed { i, srcFile ->
                    "${i}_${srcFile.nameWithoutExtension}" to CsvInputMobilityModel(Paths.get(dirName, srcFile.name).toString(), modelTimeUnitsPerSec, timeOffset)
                }.toMap()
    }
}
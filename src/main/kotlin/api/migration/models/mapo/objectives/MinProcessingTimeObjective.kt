package api.migration.models.mapo.objectives

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelPath
import org.cloudbus.cloudsim.Pe
import org.fog.application.AppLoop
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import kotlin.math.min

class MinProcessingTimeObjective: Objective {
    override fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double {
        val avgTupleLengthsForDevice = mutableMapOf<Int, MutableMap<EnvironmentModelPath, MutableList<Double>>>()
        val deviceMap = mutableMapOf<Int, FogDevice>()
        var totalTime = 0.0
        newEnvironmentModel.getAllPaths().forEach { path ->
            path.links.forEach { link ->
                val destDevice = link.destDevice.device
                val appEdge = link.appEdge
                when (destDevice) {
                    is FogDevice -> {
                        if (link.destProcessingModule != null) {
                            avgTupleLengthsForDevice
                                    .getOrPut(destDevice.id) { mutableMapOf() }
                                    .getOrPut(path) { mutableListOf() }
                                    .add(appEdge.tupleCpuLength * link.selectivity / link.timeInterval)
                            deviceMap.putIfAbsent(destDevice.id, destDevice)
                        }
                    }
                }
            }
        }

        deviceMap.values.forEach { device ->
            val tuplesPerPath = avgTupleLengthsForDevice[device.id]!!
            val avgMipsPerPe = device.host.getPeList<Pe>().map {pe -> pe.mips}.average()
            val mipsPortion = min(device.host.totalMips / tuplesPerPath.size, avgMipsPerPe)
            tuplesPerPath.values.forEach { tuplesOnPath ->
                tuplesOnPath.forEach { tuple ->
                    totalTime += tuple / mipsPortion
                }
            }
        }

        return totalTime
    }
}
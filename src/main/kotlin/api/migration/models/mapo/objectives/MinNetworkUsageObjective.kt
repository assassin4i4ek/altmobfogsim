package api.migration.models.mapo.objectives

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelPath
import org.cloudbus.cloudsim.Pe
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import kotlin.math.min

class MinNetworkUsageObjective: Objective {
    override fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double {
        var totalUsage = 0.0
        newEnvironmentModel.getAllPaths().forEach { path ->
            var pathTotalUsage = 0.0
            path.links.forEach { link ->
                val destDevice = link.destDevice.device
                val srcDevice = link.srcDevice.device
                val appEdge = link.appEdge
                if (destDevice is FogDevice && srcDevice is FogDevice) {
                    pathTotalUsage += when {
                        srcDevice.childToLatencyMap.containsKey(destDevice.id) -> {
                            val latencyBetweenDevices = srcDevice.childToLatencyMap[destDevice.id]!!
                            val bandwidthImpact = appEdge.tupleNwLength / srcDevice.downlinkBandwidth
                            (latencyBetweenDevices + bandwidthImpact) * link.selectivity / link.timeInterval
                        }
                        destDevice.childToLatencyMap.containsKey(srcDevice.id) -> {
                            val latencyBetweenDevices = destDevice.childToLatencyMap[srcDevice.id]!!
                            val bandwidthImpact = appEdge.tupleNwLength / srcDevice.uplinkBandwidth
                            (latencyBetweenDevices + bandwidthImpact) * link.selectivity / link.timeInterval
                        }
                        srcDevice.id == destDevice.id -> {
                            0.0
                        }
                        else -> throw Exception("Could not calculate latency between devices")
                    }
                }
            }
            totalUsage += pathTotalUsage
        }

        return totalUsage
    }
}
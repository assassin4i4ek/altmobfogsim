package api.migration.models.problem.objectives

import api.migration.models.problem.environment.EnvironmentModel
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor

class MinProcessingTimeObjective: Objective {
    override fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double {
        val avgTupleLengthsForDevice = mutableMapOf<Int, MutableList<Double>>()
        val deviceMap = mutableMapOf<Int, FogDevice>()
        var totalTime = 0.0
        newEnvironmentModel.getAllPaths().forEach { path ->
            var pathTotalTime = 0.0
            path.links.forEach { link ->
                val destDevice = link.destDevice.device
                val srcDevice = link.srcDevice.device
                val appEdge = link.appEdge
                when (destDevice) {
                    is FogDevice -> {
                        when (srcDevice) {
                            is Sensor -> {
                                pathTotalTime += srcDevice.latency / link.timeInterval
                            }
                            is FogDevice -> {
                                pathTotalTime += when {
                                    srcDevice.childToLatencyMap.containsKey(destDevice.id) -> {
                                        val latencyBetweenDevices = srcDevice.childToLatencyMap[destDevice.id]!!
                                        val bandwidthImpact = appEdge.tupleNwLength / srcDevice.downlinkBandwidth
                                        (latencyBetweenDevices + bandwidthImpact) / link.timeInterval
                                    }
                                    destDevice.childToLatencyMap.containsKey(srcDevice.id) -> {
                                        val latencyBetweenDevices = destDevice.childToLatencyMap[srcDevice.id]!!
                                        val bandwidthImpact = appEdge.tupleNwLength / srcDevice.uplinkBandwidth
                                        (latencyBetweenDevices + bandwidthImpact) / link.timeInterval
                                    }
                                    srcDevice.id == destDevice.id -> {
                                        0.0
                                    }
                                    else -> throw Exception("Could not calculate latency between devices")
                                }
                                if (link.destProcessingModule != null) {
                                    avgTupleLengthsForDevice.getOrPut(destDevice.id) { mutableListOf() }.add(appEdge.tupleCpuLength / link.timeInterval)
                                    deviceMap.putIfAbsent(destDevice.id, destDevice)
                                }
                            }
                        }
                    }
                    is Actuator -> {
                        pathTotalTime += destDevice.latency / link.timeInterval
                    }
                }
            }

            totalTime += pathTotalTime
        }

        deviceMap.values.forEach { device ->
            val tupleLengthsForDevice = avgTupleLengthsForDevice[device.id]!!
            val mipsPortion = device.host.totalMips / tupleLengthsForDevice.size
            tupleLengthsForDevice.forEach { tupleLength ->
                totalTime += tupleLength / mipsPortion
            }
        }

        return totalTime
    }
}
package api.migration.models.mapo.objectives

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelPath
import org.cloudbus.cloudsim.Pe
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
            var pathTotalTime = 0.0
            path.links.forEach { link ->
                val destDevice = link.destDevice.device
                val srcDevice = link.srcDevice.device
                val appEdge = link.appEdge
                when (destDevice) {
                    is FogDevice -> {
                        when (srcDevice) {
                            is Sensor -> {
                                pathTotalTime += srcDevice.latency * link.selectivity / link.timeInterval
                            }
                            is FogDevice -> {
                                pathTotalTime += when {
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
                        if (link.destProcessingModule != null) {
                            avgTupleLengthsForDevice
                                    .getOrPut(destDevice.id) { mutableMapOf() }
                                    .getOrPut(path) { mutableListOf() }
                                    .add(appEdge.tupleCpuLength * link.selectivity / link.timeInterval)
                            deviceMap.putIfAbsent(destDevice.id, destDevice)
                        }
                    }
                    is Actuator -> {
                        pathTotalTime += destDevice.latency * link.selectivity / link.timeInterval
                    }
                }
            }

            totalTime += pathTotalTime
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
//            println()
//            val tupleLengthsForDevice = avgTupleLengthsForDevice[device.id]!!
//            val totalMips = device.host.totalMips//getPeList<Pe>().sumByDouble { it.peProvisioner.mips }
//            val mipsPortion = totalMips / tupleLengthsForDevice.size
//            tupleLengthsForDevice.forEach { tupleLength ->
//                totalTime += tupleLength / totalMips //mipsPortion
//            }
        }

        return totalTime
    }
}
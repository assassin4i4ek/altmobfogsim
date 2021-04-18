package api.migration.models.mapo.objectives

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelPath
import org.cloudbus.cloudsim.Pe
import org.fog.entities.FogDevice
import kotlin.math.min

class MinCostObjective: Objective {
    override fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double {
//        val avgMipsAllocatedByDevice = mutableMapOf<Int, MutableList<AppModule>>()
//        val avgMipsAllocatedByDevice = mutableMapOf<Int, Double>()
        val avgTupleLengthsForDevice = mutableMapOf<Int, MutableMap<EnvironmentModelPath, MutableList<Double>>>()
        val deviceMap = mutableMapOf<Int, FogDevice>()
        newEnvironmentModel.getAllPaths().forEach { path ->
            path.links.forEach { link ->
                val processingDevice = link.destDevice.device
                val processingModule = link.destProcessingModule
                if (processingDevice is FogDevice && processingModule != null) {
                    avgTupleLengthsForDevice
                            .getOrPut(processingDevice.id) { mutableMapOf() }
                            .getOrPut(path) { mutableListOf()}
                            .add(link.appEdge.tupleCpuLength * link.selectivity / link.timeInterval)
//                    avgMipsAllocatedByDevice[processingDevice.id] = (avgMipsAllocatedByDevice[processingDevice.id] ?: 0.0) +
//                            link.appEdge.tupleCpuLength * link.selectivity / link.timeInterval
                    deviceMap.putIfAbsent(processingDevice.id, processingDevice)
                }
            }
        }
        /*
        newEnvironmentModel.fogDevicesToAppModules.forEach { (deviceId, apps) ->
            val device = CloudSim.getEntity(deviceId) as FogDevice
            val controller = CloudSim.getEntity(device.controllerId) as Controller
            val allocatedVms = device.host.getVmList<AppModule>().toList()
            allocatedVms.forEach { vm ->
//                device.host.vmScheduler.deallocatePesForVm(vm)
            }
//            apps.forEach { (appId, appModuleNames) ->
//                appModuleNames.forEach { appModuleName ->
//                    device.vmAllocationPolicy.allocateHostForVm(AppModule(controller.applications[appId]!!.getModuleByName(appModuleName)))
//                }
//            }
//            device.host.getVmList<AppModule>().toList().forEach { vm ->
//                device.vmAllocationPolicy.deallocateHostForVm(vm)
//            }
            allocatedVms.forEach { vm ->
//                device.host.vmScheduler.allocatePesForVm(vm, listOf(device.host.totalMips.toDouble()))
            }
            allocatedVms
        }
        return 0.0*/

        var totalCost = 0.0
        deviceMap.values.map { device ->
            val tuplesPerPath = avgTupleLengthsForDevice[device.id]!!
            val avgMipsPerPe = device.host.getPeList<Pe>().map { pe -> pe.mips}.average()
            val mipsPortion = min(device.host.totalMips / tuplesPerPath.size, avgMipsPerPe)
            tuplesPerPath.values.forEach { tuplesOnPath ->
                tuplesOnPath.forEach { tuple ->
                    totalCost += (tuple / mipsPortion) * device.ratePerMips
                }
            }
        }
//        val totalCost = avgMipsAllocatedByDevice.map { (deviceId, avgMips) ->
//            deviceMap[deviceId]!!.ratePerMips * min(1.0, avgMips / deviceMap[deviceId]!!.host.totalMips)
//        }.sum()
        return totalCost
    }
}
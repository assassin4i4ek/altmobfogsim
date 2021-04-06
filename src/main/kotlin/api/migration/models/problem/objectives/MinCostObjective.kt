package api.migration.models.problem.objectives

import api.migration.models.problem.environment.EnvironmentModel
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.placement.Controller
import kotlin.math.min

class MinCostObjective: Objective {
    override fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double {
//        val avgMipsAllocatedByDevice = mutableMapOf<Int, MutableList<AppModule>>()
        val avgMipsAllocatedByDevice = mutableMapOf<Int, Double>()
        val deviceMap = mutableMapOf<Int, FogDevice>()
        newEnvironmentModel.getAllPaths().forEach { path ->
            path.links.forEach { link ->
                val processingDevice = link.destDevice.device
                val processingModule = link.destProcessingModule
                if (processingDevice is FogDevice && processingModule != null) {
                    avgMipsAllocatedByDevice[processingDevice.id] = (avgMipsAllocatedByDevice[processingDevice.id] ?: 0.0) +
                            link.appEdge.tupleCpuLength / link.timeInterval
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
        val totalCost = avgMipsAllocatedByDevice.map { (deviceId, avgMips) ->
            deviceMap[deviceId]!!.ratePerMips * min(1.0, avgMips / deviceMap[deviceId]!!.host.totalMips)
        }.sum()
        return totalCost
    }
}
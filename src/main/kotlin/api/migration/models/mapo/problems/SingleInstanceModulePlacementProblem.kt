package api.migration.models.mapo.problems

import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.utils.BooleanVariable
import api.migration.models.mapo.problems.utils.SingleBitSetBinaryVariable
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.BinaryIntegerVariable
import org.moeaframework.core.variable.BinaryVariable
import org.moeaframework.core.variable.EncodingUtils

class SingleInstanceModulePlacementProblem(
        objectives: List<Objective>,
        private val devices: List<MigrationSupportingDevice>,
        private val modules: List<AppModule>,
        controller: Controller
): ModulePlacementProblem<BinaryIntegerVariable>(modules.size, objectives, controller) {
    class Factory: ModulePlacementProblemFactory {
        override fun newProblem(objectives: List<Objective>, devices: List<MigrationSupportingDevice>, modules: List<AppModule>, controller: Controller): ModulePlacementProblem<*> {
            return SingleInstanceModulePlacementProblem(objectives, devices, modules, controller)
        }
    }

    override fun newSolutionVariable(): BinaryIntegerVariable = EncodingUtils.newBinaryInt(0, devices.size)

    override fun areVariablesEqual(var1: BinaryIntegerVariable, var2: BinaryIntegerVariable): Boolean {
        if (var1.lowerBound != var2.lowerBound || var2.upperBound != var2.upperBound) {
            return false
        }
        return var1.value == var2.value
    }

    override fun initEnvironmentModelForSolution(envModel: MutableEnvironmentModel, solution: Solution) {
        devices.forEachIndexed { i, device ->
            val deviceNewModulesMask = EncodingUtils.getInt(solution).map { deviceIndex ->
                i == deviceIndex
            }
            val deviceNewModules = deviceNewModulesMask.mapIndexedNotNull { j, isModule ->
                if (isModule) {
                    modules[j]
                }
                else null
            }.toMutableList()
            deviceNewModules.addAll(device.mAppModuleList.filter { module ->
                modules.find { migratingModule -> migratingModule.appId == module.appId && migratingModule.name == module.name } == null
            }.map { deviceModule -> controller.applications[deviceModule.appId]!!.getModuleByName(deviceModule.name) })
            envModel.addFogDeviceWithModules(device.mId, deviceNewModules)
        }

        controller.fogDevices.forEach { fogDevice ->
            if (!envModel.hasFogDevice(fogDevice.id)) {
                envModel.addFogDeviceWithModules(fogDevice.id, fogDevice.getVmList<AppModule>().map { deviceModule ->
                    controller.applications[deviceModule.appId]!!.getModuleByName(deviceModule.name)
                })
            }
        }
    }

    override fun currentEnvironmentAsSolution(): Solution {
        val solution = newSolution()
        devices.forEachIndexed { i, device ->
            modules.forEachIndexed { j, module ->
                if (device.mAppModuleList.find {it.appId == module.appId && it.name == module.name} != null) {
                    // module is located on device
                    EncodingUtils.setInt(solution.getVariable(j), i)
                }
            }
        }
        objectives.forEachIndexed { i, objective ->
            solution.setObjective(i, objective.compute(currentEnvironmentModel, currentEnvironmentModel))
        }
        return solution
    }

    override fun validateSolution(solution: Solution): Boolean {
        return true
    }

    override fun decode(solution: Solution): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        val result = mutableMapOf<MigrationSupportingDevice, MutableList<Pair<AppModule, Boolean>>>()
        devices.forEachIndexed { i, device ->
            result.putIfAbsent(device, mutableListOf())
            modules.forEachIndexed { j, module ->
                result[device]!!.add(module to (EncodingUtils.getInt(solution.getVariable(j)) == i))
            }
        }
        return result
    }
}
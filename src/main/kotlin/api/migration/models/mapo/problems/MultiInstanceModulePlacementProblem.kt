package api.migration.models.mapo.problems

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelImpl
import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.utils.BooleanVariable
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.Solution
import org.moeaframework.core.Variable
import org.moeaframework.core.variable.BinaryVariable
import org.moeaframework.core.variable.EncodingUtils

class MultiInstanceModulePlacementProblem(
        objectives: List<Objective>,
        private val devices: List<MigrationSupportingDevice>,
        private val modules: List<AppModule>,
        controller: Controller
): ModulePlacementProblem<BooleanVariable>(devices.size * modules.size, objectives, controller) {
    class Factory: ModulePlacementProblemFactory {
        override fun newProblem(objectives: List<Objective>, devices: List<MigrationSupportingDevice>, modules: List<AppModule>, controller: Controller): ModulePlacementProblem<*> {
            return MultiInstanceModulePlacementProblem(objectives, devices, modules, controller)
        }
    }

    override fun newSolutionVariable(): BooleanVariable = BooleanVariable()

    override fun areVariablesEqual(var1: BooleanVariable, var2: BooleanVariable): Boolean = var1.get(0) == var2.get(0)

    override fun initEnvironmentModelForSolution(envModel: MutableEnvironmentModel, solution: Solution) {
        devices.forEachIndexed { i, device ->
            val deviceNewModulesMask = IntRange(i * modules.size, (i + 1) * modules.size - 1).map { j ->
                when (val variable = solution.getVariable(j)) {
                    is BooleanVariable -> {
                        variable.value
                    }
                    is BinaryVariable -> {
                        val newVariable = BooleanVariable(variable)
                        solution.setVariable(j, newVariable)
                        newVariable.value
                    }
                    else -> throw Exception("Unknown variable type")
                }
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
                (solution.getVariable(i * modules.size + j) as BooleanVariable).value =
                        device.mAppModuleList.find {it.appId == module.appId && it.name == module.name} != null
            }
        }
        objectives.forEachIndexed { i, objective ->
            solution.setObjective(i, objective.compute(currentEnvironmentModel, currentEnvironmentModel))
        }
        return solution
    }

    override fun validateSolution(solution: Solution): Boolean {
        // module is present at least at one device ~ at least one boolean variable has '1' for device
        for (j in modules.indices) {
            var count = 0
            for (i in devices.indices) {
                if ((solution.getVariable(i * modules.size + j) as BooleanVariable).value) {
                    break
                }
                else {
                    count++
                }
            }
            if (count == devices.size) {
                return false
            }
        }
        return true
    }

    override fun decode(solution: Solution): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        val result = mutableMapOf<MigrationSupportingDevice, MutableList<Pair<AppModule, Boolean>>>()
        devices.forEachIndexed { i, device ->
            result.putIfAbsent(device, mutableListOf())
            modules.forEachIndexed { j, module ->
                result[device]!!.add(module to (solution.getVariable(i * modules.size + j) as BooleanVariable).value)
            }
        }
        return result
    }
}
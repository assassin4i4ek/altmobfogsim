package api.migration.models.mapo.problems

import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.utils.BooleanVariable
import api.migration.models.mapo.problems.utils.SingleBitSetBinaryVariable
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.BinaryVariable
import org.moeaframework.core.variable.EncodingUtils

class SingleInstanceModulePlacementProblem(
        objectives: List<Objective>,
        private val devices: List<MigrationSupportingDevice>,
        private val modules: List<AppModule>,
        controller: Controller
): ModulePlacementProblem<SingleBitSetBinaryVariable>(modules.size, objectives, controller) {
    class Factory: ModulePlacementProblemFactory {
        override fun newProblem(objectives: List<Objective>, devices: List<MigrationSupportingDevice>, modules: List<AppModule>, controller: Controller): ModulePlacementProblem<*> {
            return SingleInstanceModulePlacementProblem(objectives, devices, modules, controller)
        }
    }

    override fun newSolutionVariable(): SingleBitSetBinaryVariable = SingleBitSetBinaryVariable(devices.size)

    override fun areVariablesEqual(var1: SingleBitSetBinaryVariable, var2: SingleBitSetBinaryVariable): Boolean {
        if (var1.numberOfBits != var2.numberOfBits) {
            return false
        }
        for (i in 0 until var1.numberOfBits) {
            if (var1.get(i) != var2.get(i)) {
                return false
            }
        }
        return true
    }

    override fun initEnvironmentModelForSolution(envModel: MutableEnvironmentModel, solution: Solution) {
        devices.forEachIndexed { i, device ->
            val deviceNewModulesMask = IntRange(0, modules.size - 1).map { j ->
                when (val variable = solution.getVariable(j)) {
                    is SingleBitSetBinaryVariable -> {
                        variable.get(i)
                    }
                    is BinaryVariable -> {
                        val newVariable = SingleBitSetBinaryVariable(variable)
                        solution.setVariable(j, newVariable)
                        newVariable.get(i)
                    }
                    else -> throw Exception("Unknown variable type")
                }
//                (solution.getVariable(j) as SingleBitSetBinaryVariable).get(i)
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
                (solution.getVariable(j) as SingleBitSetBinaryVariable)
                        .set(i, device.mAppModuleList.find {it.appId == module.appId && it.name == module.name} != null)
            }
        }
        objectives.forEachIndexed { i, objective ->
            solution.setObjective(i, objective.compute(currentEnvironmentModel, currentEnvironmentModel))
        }
        return solution
    }

    override fun validateSolution(solution: Solution): Boolean {
        for (j in modules.indices) {
            val variable = solution.getVariable(j) as SingleBitSetBinaryVariable
            var onesCount = 0
            for (i in 0 until variable.numberOfBits) {
                if (variable.get(i)) {
                    onesCount++
                }
            }
            if (onesCount != 1) {
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
                result[device]!!.add(module to (solution.getVariable(j) as SingleBitSetBinaryVariable).get(i))
            }
        }
        return result
    }
}
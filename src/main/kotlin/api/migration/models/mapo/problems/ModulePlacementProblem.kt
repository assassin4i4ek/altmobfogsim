package api.migration.models.mapo.problems

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.EnvironmentModelImpl
import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.models.mapo.objectives.Objective
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.Solution
import org.moeaframework.core.Variable
import org.moeaframework.problem.AbstractProblem

abstract class ModulePlacementProblem<T: Variable>(
    numberOfVariables: Int,
    protected val objectives: List<Objective>,
    protected val controller: Controller
): AbstractProblem(numberOfVariables, objectives.size) {
    protected val currentEnvironmentModel: EnvironmentModel

    init {
        //initialize current environment model
        currentEnvironmentModel = EnvironmentModelImpl(controller)
        controller.fogDevices.forEach { fogDevice ->
            currentEnvironmentModel.addFogDeviceWithModules(fogDevice.id, fogDevice.getVmList<AppModule>().map { deviceModule ->
                controller.applications[deviceModule.appId]!!.getModuleByName(deviceModule.name)
            })
        }
    }

    open fun injectedSolutions(populationSize: Int): List<Solution> = emptyList()

    override fun newSolution(): Solution {
        val solution = Solution(getNumberOfVariables(), getNumberOfObjectives())
        repeat(getNumberOfVariables()) {
            solution.setVariable(it, newSolutionVariable())
        }
        return solution
    }

    protected abstract fun newSolutionVariable(): T

    @Suppress("UNCHECKED_CAST")
    fun areSolutionsEqual(solution1: Solution, solution2: Solution): Boolean {
        if (solution1.numberOfVariables != solution2.numberOfVariables) {
            return false
        }
        repeat(solution1.numberOfVariables) {
            if (!areVariablesEqual(solution1.getVariable(it) as T, solution2.getVariable(it) as T)) {
                return false
            }
        }
        return true
    }

    abstract fun areVariablesEqual(var1: T, var2: T): Boolean

    override fun evaluate(solution: Solution) {
        val newEnvModel = EnvironmentModelImpl(controller)
//        repeat(solution.numberOfVariables) {
//            val variable = BinaryVariable(1)
//            variable.set(0, it in arrayOf(0, 1, 2))
//            solution.setVariable(it, variable)
//        }
        initEnvironmentModelForSolution(newEnvModel, solution)
        if (validateSolution(solution) && validateEnvironmentModel(newEnvModel)) {
            objectives.forEachIndexed { i, objective ->
                solution.setObjective(i, objective.compute(currentEnvironmentModel, newEnvModel))
            }
        }
        else {
            repeat(numberOfObjectives) { i ->
                solution.setObjective(i, Double.POSITIVE_INFINITY)
            }
        }
    }

    abstract fun initEnvironmentModelForSolution(envModel: MutableEnvironmentModel, solution: Solution)

    abstract fun validateSolution(solution: Solution): Boolean

    protected fun validateEnvironmentModel(environmentModel: EnvironmentModel): Boolean {
        val modelPaths = environmentModel.getAllPaths()
        val currentReducedPathLinks = currentEnvironmentModel.getAllPaths().flatMap { it.reduce().links }
        val modelReducedPathLinks = modelPaths.flatMap { it.reduce().links }.toMutableList()
        currentReducedPathLinks.forEach { srcLink ->
            val accordingModelLink = modelReducedPathLinks.find { modelLink ->
                modelLink.appEdge == srcLink.appEdge
            }
            if (accordingModelLink != null) {
                modelReducedPathLinks.remove(accordingModelLink)
            }
            else {
                return false
            }
        }
        return modelReducedPathLinks.isEmpty()
    }

    abstract fun currentEnvironmentAsSolution(): Solution

    abstract fun decode(solution: Solution): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>>
}

/*class ModulePlacementProblem(
        private val objectives: List<Objective>,
        private val devices: List<MigrationSupportingDevice>,
        private val modules: List<AppModule>,
        private val controller: Controller
        )
    : AbstractProblem(devices.size * modules.size, objectives.size) {
    private val currentEnvironmentModel: EnvironmentModel

    init {
        currentEnvironmentModel = EnvironmentModelImpl(controller)
        controller.fogDevices.forEach { fogDevice ->
            currentEnvironmentModel.addFogDeviceWithModules(fogDevice.id, fogDevice.getVmList<AppModule>().map { deviceModule ->
                controller.applications[deviceModule.appId]!!.getModuleByName(deviceModule.name)
            })
        }
    }

    override fun newSolution(): Solution {
        val solution = Solution(getNumberOfVariables(), getNumberOfObjectives())
        repeat(getNumberOfVariables()) {
            solution.setVariable(it, BinaryVariable(1))
        }
        return solution
    }

    override fun evaluate(solution: Solution) {
        val newEnvModel = EnvironmentModelImpl(controller)
//        repeat(solution.numberOfVariables) {
//            val variable = BinaryVariable(1)
//            variable.set(0, it in arrayOf(0, 1, 2))
//            solution.setVariable(it, variable)
//        }
        initEnvironmentModelForSolution(newEnvModel, solution)
        if (validateSolution(solution) && validatePaths(newEnvModel.getAllPaths())) {
            objectives.forEachIndexed { i, objective ->
                solution.setObjective(i, objective.compute(currentEnvironmentModel, newEnvModel))
            }
        }
        else {
            repeat(numberOfObjectives) { i ->
                solution.setObjective(i, Double.POSITIVE_INFINITY)
            }
        }
    }

    private fun initEnvironmentModelForSolution(envModel: MutableEnvironmentModel, solution: Solution) {
        devices.forEachIndexed { i, device ->
            val deviceNewModulesMask = IntRange(i * modules.size, (i + 1) * modules.size - 1).map { EncodingUtils.getBinary(solution.getVariable(it))[0] }
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

    private fun validatePaths(modelPaths: List<EnvironmentModelPath>): Boolean {
        val currentReducedPathLinks = currentEnvironmentModel.getAllPaths().flatMap { it.reduce().links }
        val modelReducedPathLinks = modelPaths.flatMap { it.reduce().links }.toMutableList()
        currentReducedPathLinks.forEach { srcLink ->
            val accordingModelLink = modelReducedPathLinks.find { modelLink ->
                modelLink.appEdge == srcLink.appEdge
            }
            if (accordingModelLink != null) {
                modelReducedPathLinks.remove(accordingModelLink)
            }
            else {
                return false
            }
        }
        return modelReducedPathLinks.isEmpty()
    }

    fun decode(solution: Solution): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        val result = mutableMapOf<MigrationSupportingDevice, MutableList<Pair<AppModule, Boolean>>>()
        devices.forEachIndexed { i, device ->
            result.putIfAbsent(device, mutableListOf())
            modules.forEachIndexed { j, module ->
                result[device]!!.add(module to (solution.getVariable(i * modules.size + j) as BinaryVariable).get(0))
            }
        }
        return result
    }

    fun currentEnvironmentAsSolution(): Solution {
        val solution = newSolution()
        devices.forEachIndexed { i, device ->
            modules.forEachIndexed { j, module ->
                (solution.getVariable(i * modules.size + j) as BinaryVariable)
                        .set(0, device.mAppModuleList.find {it.appId == module.appId && it.name == module.name} != null)
            }
        }
        objectives.forEachIndexed { i, objective ->
            solution.setObjective(i, objective.compute(currentEnvironmentModel, currentEnvironmentModel))
        }
        return solution
    }

    private fun validateSolution(solution: Solution): Boolean {
        for (j in modules.indices) {
            var count = 0
            for (i in devices.indices) {
                if ((solution.getVariable(i * modules.size + j) as BinaryVariable).get(0)) {
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

    fun areVariablesEqual(solution1: Solution, solution2: Solution): Boolean {
        if (solution1.numberOfVariables != solution2.numberOfVariables) {
            return false
        }
        repeat(solution1.numberOfVariables) {
            if ((solution1.getVariable(it) as BinaryVariable) != (solution2.getVariable(it) as BinaryVariable)) {
                return false
            }
        }
        return true
    }
}*/
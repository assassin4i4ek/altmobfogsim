package api.migration.models.mapo

import api.migration.models.MigrationModel
import api.migration.models.mapo.problems.ModulePlacementProblemFactory
import api.migration.models.mapo.normalizers.Normalizer
import api.migration.models.mapo.objectives.Objective
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest
import api.migration.models.timeprogression.TimeProgression
import org.moeaframework.Executor
import org.moeaframework.core.PRNG
import kotlin.math.pow

class CentralizedMapoModel(
        private val isCentral: Boolean,
        override val updateTimeProgression: TimeProgression = FixedTimeProgression(Double.MAX_VALUE),
        private val objectives: List<Objective> = emptyList(),
        private val modulePlacementProblemFactory: ModulePlacementProblemFactory? = null,
        private val normalizer: Normalizer? = null,
        private val seed: Long? = null,
) : MigrationModel {
    override lateinit var device: MigrationSupportingDevice
    private val allowedMigrationModules: MutableSet<String> = mutableSetOf()

    override fun decide(): List<MigrationRequest> {
        if (isCentral) {
            assert(modulePlacementProblemFactory != null)
            val migrationSupportingDevices = device.controller.fogDevices.filterIsInstance<MigrationSupportingDevice>()
            val modulesToMigrate = device.controller.applications
                    .flatMap { (_, app) -> app.modules }
                    .filter { sourceModule ->
                        allowedMigrationModules.contains(sourceModule.name)
                    }
//                    .flatMap { sourceModule ->
//                        device.controller.appModulePlacementPolicy[sourceModule.appId]!!.moduleToDeviceMap[sourceModule.name]!!.map { deviceId ->
//                            device.controller.appModulePlacementPolicy[sourceModule.appId]!!.deviceToModuleMap[deviceId]!!.find {
//                                it.name == sourceModule.name
//                            }!!
//                        }
//                    }

            if (migrationSupportingDevices.isNotEmpty() && modulesToMigrate.isNotEmpty() /*&& modulesToMigrate.map { it.numInstances }.sum() > 0*/) {
                val problem = modulePlacementProblemFactory!!.newProblem(objectives, migrationSupportingDevices, modulesToMigrate, device.controller)
                        //ModulePlacementProblem(objectives, migrationSupportingDevices, modulesToMigrate, device.controller)
                if (seed != null) {
                    PRNG.setSeed(seed)
                }
                val executor = Executor()
                        .withProblem(problem)
                        .withAlgorithm("NSGAII")
                        .withMaxEvaluations(10000)
//                        .distributeOnAllCores()
                        .run()

                val currentSolution = problem.currentEnvironmentAsSolution()
                // extend population with current solution
                executor.add(currentSolution)
                // normalization
                if (normalizer != null && executor.size() > 1) {
                    IntRange(0, problem.numberOfObjectives - 1).map { i ->
                        val normalizedObjectives = normalizer.normalize(executor.map { it.getObjective(i) })
                        executor.forEachIndexed { j, solution ->
                            solution.setObjective(i, normalizedObjectives[j])
                        }
                    }
                }
                val newSolution = executor.minByOrNull { s ->
                    IntRange(0, problem.numberOfObjectives - 1).sumByDouble { i ->
                        s.getObjective(i).pow(2)
                    }
                }
                if (newSolution != null && newSolution !== currentSolution && !problem.areSolutionsEqual(newSolution, currentSolution)) {
                    val currentDeviceToModulesMap = problem.decode(currentSolution)
                    val newDeviceToModulesMap = problem.decode(newSolution)
                    val migrationRequests = mutableListOf<MigrationRequest>()
                    val removeModuleMigrationRequests = mutableListOf<MigrationRequest>()
                    migrationSupportingDevices.forEach { destDevice ->
                        currentDeviceToModulesMap[destDevice]!!.zip(newDeviceToModulesMap[destDevice]!!).forEach { (currentModuleIsPresent, newModuleIsPresent) ->
                            assert(currentModuleIsPresent.first === newModuleIsPresent.first)
                            when (currentModuleIsPresent.second to newModuleIsPresent.second) {
                                false to false -> {
                                    // no need to create new module on device
                                }
                                false to true -> {
                                    // need to migrate this module from other device or create new instance of the module
                                    currentDeviceToModulesMap.mapNotNull { (srcDevice, srcModules) ->
                                        val module = srcModules.find { moduleIsPresent ->
                                            moduleIsPresent.first === currentModuleIsPresent.first && moduleIsPresent.second
                                        }?.first
                                        if (module != null) srcDevice to module else null
                                    }.also {
                                        assert(it.isNotEmpty())
                                    }.forEach { (srcDevice, srcModule) ->
                                        migrationRequests.add(MigrationRequest(srcModule.appId, srcModule.name, srcDevice, destDevice, MigrationRequest.Type.COPY))
                                    }
                                }
                                true to false -> {
                                    // need to remove this module from this device
                                    val moduleToRemove = currentModuleIsPresent.first
                                    removeModuleMigrationRequests.add(MigrationRequest(
                                            moduleToRemove.appId, moduleToRemove.name, destDevice, null, MigrationRequest.Type.REMOVE_ALL_INSTANCES
                                    ))
                                }
                                true to true -> {
                                    // the module is already on device
                                }
                            }
                        }
                    }
                    migrationRequests.addAll(removeModuleMigrationRequests)
                    return migrationRequests
                }
            }
        }
        return emptyList()
    }

    override fun canMigrate(request: MigrationRequest): Boolean {
        return true
    }

    override fun allowMigrationForModule(moduleName: String) {
        allowedMigrationModules.add(moduleName)
    }
}
package api.migration.models.mapo

import api.accesspoint.migration.entities.MigrationStimulatorAccessPointConnectedDevice
import api.migration.models.mapo.normalizers.Normalizer
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.ModulePlacementProblemFactory
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.models.timeprogression.TimeProgression
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest
import org.cloudbus.cloudsim.core.CloudSim
import java.util.*
import kotlin.math.roundToInt

class PrioritizedCentralizedMapoModel(
        isCentral: Boolean,
        private val updatePriority: (elapsedTime: Double, prevPriority: Double) -> Double = { _, prevPriority ->
            prevPriority
        },
        private val randomPriorityThreshold: Double = 0.0,
        private val calculatePopulationSize: ((numVariables: Int) -> Int)? = null,
        private val calculateMaxIterations: ((numVariables: Int) -> Int)? = null,
        updateTimeProgression: TimeProgression = FixedTimeProgression(Double.MAX_VALUE),
        objectives: List<Objective> = emptyList(),
        modulePlacementProblemFactory: ModulePlacementProblemFactory? = null,
        normalizer: Normalizer? = null,
        seed: Long? = null,
        logProgress: Boolean = false
): CentralizedMapoModel(isCentral, updateTimeProgression, objectives, modulePlacementProblemFactory,
        normalizer=normalizer, seed=seed, logProgress=logProgress) {
    private val triggeringDeviceToParentIdMap: MutableMap<Int, Int> = mutableMapOf()
    private val allAllowedMigrationModules: MutableSet<String> = mutableSetOf()
    private val allowedMigrationModulesPriority: MutableMap<String, Double> = mutableMapOf()
    private var lastPriorityUpdateTime: Double = 0.0
    val random: Random = if (seed != null) Random(seed) else Random()

    override fun allowMigrationForModule(moduleName: String) {
        allAllowedMigrationModules.add(moduleName)
        allowedMigrationModulesPriority[moduleName] = random.nextDouble() * randomPriorityThreshold
    }

    override fun init(device: MigrationSupportingDevice) {
        super.init(device)
        if (isCentral) {
            device.controller.fogDevices
                    .filter { it is MigrationStimulatorAccessPointConnectedDevice }
                    .forEach { triggeringDevice ->
                        triggeringDeviceToParentIdMap[triggeringDevice.id] = triggeringDevice.parentId
                    }
        }
    }

    override fun decide(): List<MigrationRequest> {
        if (isCentral) {
            // update priority
            val currentTime = CloudSim.clock()
            allowedMigrationModulesPriority.mapValues { (moduleName, prevPriority) ->
                allowedMigrationModulesPriority[moduleName] = updatePriority(currentTime - lastPriorityUpdateTime, prevPriority)
            }
            lastPriorityUpdateTime = currentTime

            device.controller.fogDevices
                    .filter { it is MigrationStimulatorAccessPointConnectedDevice }
                    .mapNotNull { triggeringDevice ->
                        if (triggeringDevice.parentId != triggeringDeviceToParentIdMap[triggeringDevice.id]) {
                            //parent id changed, so this device is going to trigger migration
                            triggeringDeviceToParentIdMap[triggeringDevice.id] = triggeringDevice.parentId
                            triggeringDevice.applicationMap
                                    .flatMap { (_, app) ->
                                        app.modules.mapNotNull { module ->
                                            if (module.name in allAllowedMigrationModules) {
                                                module.name
                                            } else null
                                        }
                                    }.forEach { allowedMigrationModuleName ->
                                        allowedMigrationModulesPriority[allowedMigrationModuleName] = 1.0
                                    }
                        }
                        else null
                    }

            // allow migration of modules which have high priority
            allowedMigrationModules.addAll(
                    allowedMigrationModulesPriority.mapNotNull { (moduleName, priority) ->
                        if (priority >= 1.0) moduleName else null
                    }
            )
            populationSize = calculatePopulationSize!!(allowedMigrationModules.size)
            maxIterations = calculateMaxIterations!!(allowedMigrationModules.size)
        }
        val result = super.decide()
        if (isCentral) {
            allowedMigrationModules.forEach { processedModule ->
                allowedMigrationModulesPriority[processedModule] = random.nextDouble() * randomPriorityThreshold
            }
            allowedMigrationModules.clear()
        }
        return result
    }
}
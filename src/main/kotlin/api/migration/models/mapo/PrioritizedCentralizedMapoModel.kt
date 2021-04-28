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
        private val priorityThreshold: Double = 1.0,
        private val updatePriority: (elapsedTime: Double, prevPriority: Double) -> Double = { _, prevPriority ->
            prevPriority
        },
        isRandomizedPriority: Boolean = false,
        private val populationSizeCoefficient: Double? = null,
        private val maxEvaluationsCoefficient: Double? = null,
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
    val random: Random? = if (isRandomizedPriority) {if (seed != null) Random(seed) else Random()
    } else null

    override fun allowMigrationForModule(moduleName: String) {
        allAllowedMigrationModules.add(moduleName)
        allowedMigrationModulesPriority[moduleName] = random?.nextDouble()?.div(priorityThreshold) ?: 0.0
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
            // update priority
            val currentTime = CloudSim.clock()
            allowedMigrationModulesPriority.mapValues { (moduleName, prevPriority) ->
                allowedMigrationModulesPriority[moduleName] = updatePriority(currentTime - lastPriorityUpdateTime, prevPriority)
            }
            lastPriorityUpdateTime = currentTime
            // allow migration of modules which have high priority
            allowedMigrationModules.addAll(
                    allowedMigrationModulesPriority.mapNotNull { (moduleName, priority) ->
                        if (priority >= priorityThreshold) moduleName else null
                    }
            )
            populationSize = (populationSizeCoefficient!! * allowedMigrationModules.size).roundToInt()
            maxIterations = (maxEvaluationsCoefficient!! * allAllowedMigrationModules.size * allAllowedMigrationModules.size).roundToInt()
        }
        val result = super.decide()
        if (isCentral) {
            allowedMigrationModules.forEach { processedModule ->
                allowedMigrationModulesPriority[processedModule] = random?.nextDouble()?.div(priorityThreshold) ?: 0.0
            }
            allowedMigrationModules.clear()
        }
        return result
    }
}
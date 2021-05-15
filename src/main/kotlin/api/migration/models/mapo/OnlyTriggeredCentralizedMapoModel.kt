package api.migration.models.mapo

import api.accesspoint.migration.entities.MigrationStimulatorAccessPointConnectedDevice
import api.migration.models.mapo.normalizers.Normalizer
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.ModulePlacementProblemFactory
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.models.timeprogression.TimeProgression
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest

open class OnlyTriggeredCentralizedMapoModel(
        isCentral: Boolean,
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

    override fun allowMigrationForModule(moduleName: String) {
        allAllowedMigrationModules.add(moduleName)
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

    override fun decide(isPeriodic: Boolean): List<MigrationRequest> {
        if (isCentral) {
            device.controller.fogDevices
                    .filter { it is MigrationStimulatorAccessPointConnectedDevice }
                    .mapNotNull { triggeringDevice ->
                        if (triggeringDevice.parentId != triggeringDeviceToParentIdMap[triggeringDevice.id]) {
                            //parent id changed, so this device is going to trigger migration
                            triggeringDeviceToParentIdMap[triggeringDevice.id] = triggeringDevice.parentId
                            allowedMigrationModules.addAll(triggeringDevice.applicationMap.flatMap { (_, app) ->
                                app.modules.mapNotNull { module ->
                                    if (module.name in allAllowedMigrationModules) {
                                        module.name
                                    }
                                    else null
                                }
                            })
                        }
                        else null
                    }
            populationSize = calculatePopulationSize!!(allowedMigrationModules.size)
            maxIterations = calculateMaxIterations!!(allowedMigrationModules.size)
        }
        val result = super.decide(isPeriodic)
        if (isCentral) {
            allowedMigrationModules.clear()
        }
        return result
    }
}
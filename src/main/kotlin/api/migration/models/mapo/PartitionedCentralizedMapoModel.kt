package api.migration.models.mapo

import api.migration.models.mapo.normalizers.Normalizer
import api.migration.models.mapo.objectives.Objective
import api.migration.models.mapo.problems.ModulePlacementProblemFactory
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.models.timeprogression.TimeProgression
import api.migration.utils.MigrationRequest

class PartitionedCentralizedMapoModel(
        isCentral: Boolean,
        private val partitionSize: Int? = null,
        updateTimeProgression: TimeProgression = FixedTimeProgression(Double.MAX_VALUE),
        objectives: List<Objective> = emptyList(),
        modulePlacementProblemFactory: ModulePlacementProblemFactory? = null,
        maxIterations: Int? = null,
        populationSize: Int = 100,
        normalizer: Normalizer? = null,
        seed: Long? = null,
        logProgress: Boolean = false
): CentralizedMapoModel(isCentral, updateTimeProgression, objectives, modulePlacementProblemFactory, maxIterations,
        populationSize, normalizer, seed, logProgress) {

    private val allAllowedMigrationModules: MutableList<MutableSet<String>> = mutableListOf(mutableSetOf())
    private var counter = -1

    override fun allowMigrationForModule(moduleName: String) {
        if (allAllowedMigrationModules.last().size >= partitionSize!!) {
            allAllowedMigrationModules.add(mutableSetOf())
        }
        allAllowedMigrationModules.last().add(moduleName)
    }

    override fun decide(isPeriodic: Boolean): List<MigrationRequest> {
        return if (counter == -1) {
            allowedMigrationModules.clear()
            allowedMigrationModules.addAll(allAllowedMigrationModules.flatten())
            val res = super.decide(isPeriodic)
            allowedMigrationModules.clear()
            counter = 0
            res
        }
        else {
            allowedMigrationModules.clear()
            allowedMigrationModules.addAll(allAllowedMigrationModules[counter])
            val res = super.decide(isPeriodic)
            allowedMigrationModules.clear()
            counter = (counter + 1) % allAllowedMigrationModules.size
            res
        }
    }
}
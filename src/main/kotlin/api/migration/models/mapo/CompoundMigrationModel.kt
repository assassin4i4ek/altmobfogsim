package api.migration.models.mapo

import api.migration.models.MigrationModel
import api.migration.models.timeprogression.TimeProgression
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest

class CompoundMigrationModel(
        private val migrationModels: Map<String, MigrationModel>,
        private val selectModel: (models: Map<String, MigrationModel>, isPeriodic: Boolean) -> String,
        override val updateTimeProgression: TimeProgression,
): MigrationModel {
    override fun init(device: MigrationSupportingDevice) {
        migrationModels.forEach { (_, model) -> model.init(device) }
    }

    private lateinit var selectedModel: MigrationModel

    override fun canMigrate(request: MigrationRequest): Boolean = selectedModel.canMigrate(request)

    override fun decide(isPeriodic: Boolean): List<MigrationRequest> {
        selectedModel = migrationModels[selectModel(migrationModels, isPeriodic)]!!
        return selectedModel.decide(isPeriodic)
    }

    override fun allowMigrationForModule(moduleName: String) {
        migrationModels.forEach { (_, model) -> model.allowMigrationForModule(moduleName) }
    }
}
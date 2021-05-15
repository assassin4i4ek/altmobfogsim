package api.migration.models

import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest
import api.migration.models.timeprogression.TimeProgression

interface MigrationModel {
    val updateTimeProgression: TimeProgression
    fun init(device: MigrationSupportingDevice)

    fun decide(isPeriodic: Boolean): List<MigrationRequest>
    fun canMigrate(request: MigrationRequest): Boolean

    fun allowMigrationForModule(moduleName: String)
}
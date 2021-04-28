package api.migration.models

import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest
import api.migration.models.timeprogression.TimeProgression

interface MigrationModel {
    val updateTimeProgression: TimeProgression
//    var device: MigrationSupportingDevice
    fun init(device: MigrationSupportingDevice)

    fun decide(): List<MigrationRequest>
    fun canMigrate(request: MigrationRequest): Boolean

    fun allowMigrationForModule(moduleName: String)
//    fun prepare(request: MigrationRequest)
//    fun start(request: MigrationRequest)
}
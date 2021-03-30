package api.migration.models

import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest

interface MigrationModel {
    val nextUpdateTime: Double
    var device: MigrationSupportingDevice

    fun decide(): List<MigrationRequest>
    fun canMigrate(request: MigrationRequest): Boolean
//    fun prepare(request: MigrationRequest)
//    fun start(request: MigrationRequest)
}
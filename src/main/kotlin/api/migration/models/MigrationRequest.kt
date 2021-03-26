package api.migration.models

import api.migration.entites.MigrationSupportingDevice

data class MigrationRequest(
        val appId: String,
        val appModuleName: String,
        val from: MigrationSupportingDevice,
        val to: MigrationSupportingDevice
)

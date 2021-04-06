package api.migration.utils

import api.migration.original.entites.MigrationSupportingDevice

data class MigrationRequest(
        val appId: String,
        val appModuleName: String,
        val from: MigrationSupportingDevice?,
        val to: MigrationSupportingDevice?,
        val type: Type
) {
    enum class Type {
        REMOVE_SINGLE_INSTANCE, REMOVE_ALL_INSTANCES, COPY
    }
}

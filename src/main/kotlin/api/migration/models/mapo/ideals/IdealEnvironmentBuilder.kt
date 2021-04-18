package api.migration.models.mapo.ideals

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.entities.FogDevice

interface IdealEnvironmentBuilder {
    fun getIdealModulesMapping(
            currentEnvironmentModel: EnvironmentModel,
            migrationSupportingDevices: List<MigrationSupportingDevice>,
            modulesToMigrate: List<AppModule>
    ): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>>
}
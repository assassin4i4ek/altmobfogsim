package api.migration.models.mapo.ideals

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.ideals.utils.ClosestToEdgeModulesMapping
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule

class ClosestToEdgeEndEnvironmentBuilder: IdealEnvironmentBuilder, ClosestToEdgeModulesMapping {
    override fun getIdealModulesMapping(
            currentEnvironmentModel: EnvironmentModel,
            migrationSupportingDevices: List<MigrationSupportingDevice>,
            modulesToMigrate: List<AppModule>
    ): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        return getClosestToEdgeModulesMapping(currentEnvironmentModel, migrationSupportingDevices, modulesToMigrate,
                ClosestToEdgeModulesMapping.Edge.END)
    }
}
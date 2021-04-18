package api.migration.models.mapo.ideals

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.models.mapo.environment.MutableEnvironmentModel
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.entities.FogDevice

class MinCostIdealEnvironmentBuilder: IdealEnvironmentBuilder {
    override fun getIdealModulesMapping(
            currentEnvironmentModel: EnvironmentModel,
            migrationSupportingDevices: List<MigrationSupportingDevice>,
            modulesToMigrate: List<AppModule>
    ): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        val minCostDevice = migrationSupportingDevices.minByOrNull { device ->
            currentEnvironmentModel.getFogDeviceById(device.mId).ratePerMips
        }!!
        return migrationSupportingDevices.associateWith { dev ->
            modulesToMigrate.map { module ->
                module to (dev.mId == minCostDevice.mId)
            }
        }
    }
}
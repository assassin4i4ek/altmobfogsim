package api.migration.addressing

import org.fog.application.AppModule

class MigrationAppModule(migrationDeviceId: Int): AppModule(migrationDeviceId, "migration", "migration", -1, 0.0, 0, 0, 0, null, null, null) {
}
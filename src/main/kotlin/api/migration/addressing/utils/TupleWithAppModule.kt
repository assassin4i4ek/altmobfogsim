package api.migration.addressing.utils

import org.fog.application.AppModule
import org.fog.entities.Tuple

data class TupleWithAppModule(
        val appModule: AppModule,
        val destId: Int
): Tuple("migration", -1, UP, -1, -1, appModule.ram.toLong(), -1,
        null, null, null) {
    init {
        destModuleName = "migration"
        moduleCopyMap["migration"] = destId
    }
}
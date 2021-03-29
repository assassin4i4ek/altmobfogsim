package api.migration.addressing

import org.fog.placement.ModulePlacement

class MigrationApplicationModulePlacement: ModulePlacement() {
    init {
        moduleToDeviceMap = mutableMapOf(Pair("migration", mutableListOf()))
        deviceToModuleMap = mutableMapOf()
    }

    override fun mapModules() {}
}
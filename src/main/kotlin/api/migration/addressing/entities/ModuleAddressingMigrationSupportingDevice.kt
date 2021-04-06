package api.migration.addressing.entities

import api.addressing.fixed.entities.AddressingDevice
import api.migration.original.entites.MigrationSupportingDevice
import org.cloudbus.cloudsim.core.SimEvent

interface ModuleAddressingMigrationSupportingDevice: MigrationSupportingDevice, AddressingDevice {
    val numberOfSuppressedModuleInstances: MutableMap<String, MutableMap<String, Int>>
    val tuplesSuppressedWhileModuleMigration: MutableMap<String, MutableMap<String, MutableList<SimEvent>>>
}
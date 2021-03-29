package api.migration.addressing.behaviors

import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.migration.addressing.entities.ModuleAddressingMigrationSupportingDevice
import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.network.fixed.entities.NetworkDevice

class ModuleAddressingMigrationSupportingDeviceBehaviorImpl(
        override val device: ModuleAddressingMigrationSupportingDevice,
        override val superMigrationSupportingDeviceBehavior: MigrationSupportingDeviceBehavior,
        override val superAddressingDeviceBehavior: AddressingDeviceBehavior<NetworkDeviceBehavior>)
    : ModuleAddressingMigrationSupportingDeviceBehavior<MigrationSupportingDeviceBehavior, AddressingDeviceBehavior<NetworkDeviceBehavior>>
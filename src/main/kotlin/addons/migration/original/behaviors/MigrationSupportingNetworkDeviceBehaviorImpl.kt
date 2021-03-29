package addons.migration.original.behaviors

import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import addons.migration.original.entities.MigrationSupportingNetworkDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior

class MigrationSupportingNetworkDeviceBehaviorImpl(
        override val device: MigrationSupportingNetworkDevice,
        override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
        override val superMigrationSupportingNetworkDeviceBehavior: MigrationSupportingDeviceBehavior)
    : MigrationSupportingNetworkDeviceBehavior<NetworkDeviceBehavior, MigrationSupportingDeviceBehavior>
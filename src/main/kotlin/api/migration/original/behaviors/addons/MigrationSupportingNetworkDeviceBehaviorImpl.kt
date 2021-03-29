package api.migration.original.behaviors.addons

import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.migration.original.entites.addons.MigrationSupportingNetworkDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior

class MigrationSupportingNetworkDeviceBehaviorImpl(
        override val device: MigrationSupportingNetworkDevice,
        override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
        override val superMigrationSupportingNetworkDeviceBehavior: MigrationSupportingDeviceBehavior)
    : MigrationSupportingNetworkDeviceBehavior<NetworkDeviceBehavior, MigrationSupportingDeviceBehavior>
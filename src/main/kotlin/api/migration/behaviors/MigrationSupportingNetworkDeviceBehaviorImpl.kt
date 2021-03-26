package api.migration.behaviors

import api.common.behaviors.BaseBehavior
import api.migration.entites.MigrationSupportingDevice
import api.migration.entites.MigrationSupportingNetworkDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.entities.NetworkDevice

class MigrationSupportingNetworkDeviceBehaviorImpl(
        override val device: MigrationSupportingNetworkDevice,
        override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
        override val superMigrationSupportingNetworkDeviceBehavior: MigrationSupportingDeviceBehavior)
    : MigrationSupportingNetworkDeviceBehavior<NetworkDeviceBehavior, MigrationSupportingDeviceBehavior>
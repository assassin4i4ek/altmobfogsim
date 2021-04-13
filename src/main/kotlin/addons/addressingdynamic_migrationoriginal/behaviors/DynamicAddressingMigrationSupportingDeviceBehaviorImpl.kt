package addons.addressingdynamic_migrationoriginal.behaviors

import addons.addressingdynamic_migrationoriginal.entities.DynamicAddressingMigrationSupportingDevice
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior

class DynamicAddressingMigrationSupportingDeviceBehaviorImpl(
        override val device: DynamicAddressingMigrationSupportingDevice,
        override val superDynamicAddressingNotificationConsumerDeviceBehavior
        : DynamicAddressingNotificationConsumerDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>,
                NotificationConsumerDeviceBehavior>,
        override val superMigrationSupportingDeviceBehavior: MigrationSupportingDeviceBehavior
): DynamicAddressingMigrationSupportingDeviceBehavior<
        DynamicAddressingNotificationConsumerDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>,
                NotificationConsumerDeviceBehavior>,
        MigrationSupportingDeviceBehavior>
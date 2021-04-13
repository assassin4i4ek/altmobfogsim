package addons.accesspoint_addressingdynamic_migrationoriginal.behaviors

import addons.accesspoint_addressingdynamic_migrationoriginal.entities.MigrationSupportingAddressingAccessPoint
import api.accesspoint.migration.behaviors.MigrationSupportingAccessPointBehavior
import api.accesspoint.original.behaviors.AccessPointBehavior
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior

class MigrationSupportingAddressingAccessPointBehaviorImpl(
        override val device: MigrationSupportingAddressingAccessPoint,
        override val superMigrationSupportingDeviceBehavior: MigrationSupportingDeviceBehavior,
        override val superAccessPointBehavior
        : AccessPointBehavior<
                DynamicAddressingNotificationConsumerDeviceBehavior<
                        AddressingDeviceBehavior<
                                NetworkDeviceBehavior>,
                        NotificationConsumerDeviceBehavior>>
)

    : MigrationSupportingAccessPointBehavior<
        MigrationSupportingDeviceBehavior,
        AccessPointBehavior<
                DynamicAddressingNotificationConsumerDeviceBehavior<
                        AddressingDeviceBehavior<
                                NetworkDeviceBehavior>,
                        NotificationConsumerDeviceBehavior>>>
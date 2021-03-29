package addons.accesspoint.addressing.behaviors

import addons.accesspoint.addressing.entities.AddressingAccessPoint
import api.accesspoint.original.behaviors.AccessPointBehavior
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior

class AddressingAccessPointBehaviorImpl(
        override val device: AddressingAccessPoint,
        override val superNetworkDeviceBehavior
        : DynamicAddressingNotificationConsumerDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>,
                NotificationConsumerDeviceBehavior/*<
                        NetworkDeviceBehavior>*/>,
        ) : AccessPointBehavior<
        DynamicAddressingNotificationConsumerDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>,
                NotificationConsumerDeviceBehavior/*<
                        NetworkDeviceBehavior>*/>>
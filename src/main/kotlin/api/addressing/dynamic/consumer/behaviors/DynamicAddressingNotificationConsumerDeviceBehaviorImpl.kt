package api.addressing.dynamic.consumer.behaviors

import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDevice
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior

class DynamicAddressingNotificationConsumerDeviceBehaviorImpl(
        override val device: DynamicAddressingNotificationConsumerDevice,
        override val superAddressingDeviceBehavior:  AddressingDeviceBehavior<NetworkDeviceBehavior>,
        override val superNotificationConsumerDeviceBehavior: NotificationConsumerDeviceBehavior//<NetworkDeviceBehavior>
) : DynamicAddressingNotificationConsumerDeviceBehavior<
        AddressingDeviceBehavior<NetworkDeviceBehavior>,
        NotificationConsumerDeviceBehavior//<NetworkDeviceBehavior>
        >
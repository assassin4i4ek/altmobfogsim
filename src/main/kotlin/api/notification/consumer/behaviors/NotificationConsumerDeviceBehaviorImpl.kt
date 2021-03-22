package api.notification.consumer.behaviors

import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.consumer.entities.NotificationConsumerDevice

class NotificationConsumerDeviceBehaviorImpl(
        override val device: NotificationConsumerDevice,
//        override val superNetworkDeviceBehavior: NetworkDeviceBehavior
        ) : NotificationConsumerDeviceBehavior//<NetworkDeviceBehavior>
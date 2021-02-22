package api.notification.producer.behaviors

import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.entities.NotificationProducerDevice

class NotificationProducerDeviceBehaviorImpl(
        override val device: NotificationProducerDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>
) : NotificationProducerDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>>
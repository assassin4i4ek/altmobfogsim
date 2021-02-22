package api.addressing.dynamic.producer.behaviors

import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.common.behaviors.BaseBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import api.notification.producer.entities.NotificationProducerDevice

internal class NotificationProducerDeviceBehaviorImpl(
        override val device: NotificationProducerDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>
                >
)
    : NotificationProducerDeviceBehavior<
        DynamicGatewayConnectionDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>
                >
        > {
}
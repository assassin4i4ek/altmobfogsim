package api.addressing.dynamic.producer.behaviors

import api.addressing.dynamic.producer.entities.DynamicAddressingNotificationProducerDevice
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import api.notification.producer.entities.NotificationProducerDevice

class DynamicAddressingNotificationProducerDeviceBehaviorImpl(
        override val device: DynamicAddressingNotificationProducerDevice,
        override val superNotificationProducerBehavior: NotificationProducerDeviceBehavior<
                DynamicGatewayConnectionDeviceBehavior<
                        AddressingDeviceBehavior<
                                NetworkDeviceBehavior>
                        >
                > ) : DynamicAddressingNotificationProducerDeviceBehavior
package api.addressing.dynamic.producer.behaviors

import api.addressing.dynamic.producer.entities.DynamicAddressingNotificationProducerDevice
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import api.notification.producer.entities.NotificationProducerDevice

class DynamicAddressingNotificationProducerDeviceBehaviorImpl(
        override val device: NotificationProducerDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>
) : NotificationProducerDeviceBehavior<
        DynamicGatewayConnectionDeviceBehavior<
                AddressingDeviceBehavior<
                        NetworkDeviceBehavior>>>
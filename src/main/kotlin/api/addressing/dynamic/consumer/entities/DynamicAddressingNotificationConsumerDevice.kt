package api.addressing.dynamic.consumer.entities

import api.addressing.fixed.entities.AddressingDevice
import api.notification.consumer.entities.NotificationConsumerDevice

interface DynamicAddressingNotificationConsumerDevice: AddressingDevice, NotificationConsumerDevice {
}
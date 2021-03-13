package api.addressing.dynamic.producer.entities

import api.addressing.fixed.entities.AddressingDevice
import api.notification.producer.entities.NotificationProducerDevice

interface DynamicAddressingNotificationProducerDevice: AddressingDevice, NotificationProducerDevice {
}
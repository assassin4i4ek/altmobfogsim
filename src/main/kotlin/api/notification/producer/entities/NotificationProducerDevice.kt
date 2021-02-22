package api.notification.producer.entities

import api.common.utils.Notification
import api.common.utils.TupleRecipientPair
import api.network.dynamic.entites.DynamicGatewayConnectionDevice

interface NotificationProducerDevice: DynamicGatewayConnectionDevice {
    val producerNotifications: MutableList<Notification<*>>
}
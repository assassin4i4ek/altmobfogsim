package api.notification.consumer.entities

import api.common.entities.SimEntity
import api.common.utils.Notification
import org.fog.entities.Tuple

interface NotificationConsumerDevice: SimEntity {
    val consumerNotifications: MutableList<Notification<*>>
}
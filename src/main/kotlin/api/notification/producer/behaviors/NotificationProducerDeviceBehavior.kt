package api.notification.producer.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.Notification
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.notification.consumer.entities.NotificationConsumerDevice
import api.notification.producer.entities.NotificationProducerDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface NotificationProducerDeviceBehavior<T: BaseBehavior<T, out DynamicGatewayConnectionDevice>>
    : BaseBehavior<NotificationProducerDeviceBehavior<T>, NotificationProducerDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: T

    override fun onStart() {
        superDynamicGatewayConnectionDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NOTIFICATION_PRODUCER_WAIT_NOTIFICATION.tag -> onWaitNotification(ev)
            Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag -> onNewParentId(ev)
            else -> superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
        }
    }

    private fun onWaitNotification(ev: SimEvent): Boolean {
        val notification = ev.data as Notification<*>
        Logger.debug(device.mName, "Consumer ${
            (CloudSim.getEntity(ev.source) as NotificationConsumerDevice).mName
        } is waiting for connection")
        device.producerNotifications.add(notification)
        return false
    }

    private fun onNewParentId(ev: SimEvent): Boolean {
        if (device.mDynamicParentId > 0) {
            device.producerNotifications.forEach {
                Logger.debug(device.mName, "Notifying ${
                    (CloudSim.getEntity(it.consumerId) as NotificationConsumerDevice).mName
                } about connection")
                device.mSendEvent(it.consumerId, 0.0, Events.NOTIFICATION_CONSUMER_NOTIFY.tag, it)
            }
            device.producerNotifications.clear()
        }
        return superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
    }
}
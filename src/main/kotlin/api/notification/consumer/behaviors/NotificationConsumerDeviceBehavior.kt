package api.notification.consumer.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.Notification
import api.common.utils.TupleRecipientPair
import api.common.utils.TupleRecipientsPair
import api.network.fixed.entities.NetworkDevice
import api.notification.consumer.entities.NotificationConsumerDevice
import api.notification.producer.entities.NotificationProducerDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface NotificationConsumerDeviceBehavior<T: BaseBehavior<T, out NetworkDevice>>
    : BaseBehavior<NotificationConsumerDeviceBehavior<T>, NotificationConsumerDevice> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NOTIFICATION_CONSUMER_WAIT_PRODUCERS.tag -> onWaitProducer(ev)
            Events.NOTIFICATION_CONSUMER_NOTIFY.tag -> onNotify(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onWaitProducer(ev: SimEvent): Boolean {
        val (baseEvent, pair) = ev.data as BaseEventWrapper<TupleRecipientsPair>
        val (_, producers) = pair
        val notification = Notification(baseEvent.data as TupleRecipientPair, device.mId)
        device.consumerNotifications.add(notification)
        producers.forEach {
            device.mSendEvent(it, 0.0, Events.NOTIFICATION_PRODUCER_WAIT_NOTIFICATION.tag, notification)
        }
        Logger.debug(device.mName, "Waiting for any of ${producers.map {
            (CloudSim.getEntity(it) as NotificationProducerDevice).mName 
        }} to connect")
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onNotify(ev: SimEvent): Boolean {
        val notification = ev.data as Notification<TupleRecipientPair>
        Logger.debug(device.mName, "Received notification from ${
            (CloudSim.getEntity(ev.source) as NotificationProducerDevice).mName
        }")
        if (device.consumerNotifications.remove(notification)) {
            device.mSendEvent(device.mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag, notification.data)
        }
        return false
    }
}
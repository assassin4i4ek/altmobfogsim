package api.notification.consumer.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.Notification
import api.common.utils.TupleRecipientsPair
import api.notification.consumer.entities.NotificationConsumerDevice
import api.notification.producer.entities.NotificationProducerDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface NotificationConsumerDeviceBehavior/*<T: BaseBehavior<T, out NetworkDevice>>*/
    : BaseBehavior<NotificationConsumerDeviceBehavior/*<T>*/, NotificationConsumerDevice> {
//    val superNetworkDeviceBehavior: T

    override fun onStart() {
//        superNetworkDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NOTIFICATION_CONSUMER_WAIT_PRODUCERS.tag -> onWaitProducers(ev)
            Events.NOTIFICATION_CONSUMER_NOTIFY.tag -> onNotify(ev)
            else -> true //superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onWaitProducers(ev: SimEvent): Boolean {
        val (tuple, producerIds) = ev.data as TupleRecipientsPair
        producerIds.forEach {producerId ->
            val notification = Notification(tuple, device.mId)
            device.consumerNotifications.add(notification)
            device.mSendEvent(producerId, 0.0, Events.NOTIFICATION_PRODUCER_WAIT_NOTIFICATION.tag, notification)
        }
        Logger.debug(device.mName, "Waiting for any of ${producerIds.map {
            (CloudSim.getEntity(it) as NotificationProducerDevice).mName 
        }} to connect")
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun onNotify(ev: SimEvent): Boolean {
        val notification = ev.data as Notification<Tuple>
        Logger.debug(device.mName, "Received notification from ${
            (CloudSim.getEntity(ev.source) as NotificationProducerDevice).mName
        }")
        if (device.consumerNotifications.remove(notification)) {
            device.mSendEvent(device.mId, 0.0, FogEvents.TUPLE_ARRIVAL, notification.data)
//            device.mSendEvent(device.mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, notification.data)
//            device.mWaitForEvent(PredicateType(Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag))
        }
        return false
    }
}
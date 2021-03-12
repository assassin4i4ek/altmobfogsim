package api.addressing.dynamic.consumer.behaviors

import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDevice
import api.addressing.fixed.entities.AddressingDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.TupleNextHopTargetsContainer
import api.common.utils.TupleRecipientsPair
import api.notification.consumer.entities.NotificationConsumerDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType

interface DynamicAddressingNotificationConsumerDeviceBehavior<
        T1 : BaseBehavior<T1, out AddressingDevice>,
        T2: BaseBehavior<T2, out NotificationConsumerDevice>>
    : BaseBehavior<DynamicAddressingNotificationConsumerDeviceBehavior<T1, T2>, DynamicAddressingNotificationConsumerDevice> {
    val superAddressingDeviceBehavior: T1
    val superNotificationConsumerDeviceBehavior: T2

    override fun onStart() {
        superAddressingDeviceBehavior.onStart()
        superNotificationConsumerDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.ADDRESSING_DEVICE_ADDRESS_TUPLE_TO_TARGET_DEVICES.tag -> onAddressTupleToTargetDevices(ev)
            else -> superAddressingDeviceBehavior.processEvent(ev) && superNotificationConsumerDeviceBehavior.processEvent(ev)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTupleToTargetDevices(ev: SimEvent): Boolean {
        val (baseEvent, container) = ev.data as BaseEventWrapper<TupleNextHopTargetsContainer>
        val (tuple, nextHopId, targetDeviceIds) = container
        return if (nextHopId > 0) {
            superAddressingDeviceBehavior.processEvent(ev)
        }
        else {
            device.mSendEvent(device.mId, 0.0, Events.NOTIFICATION_CONSUMER_WAIT_PRODUCERS.tag,
                    BaseEventWrapper(baseEvent, TupleRecipientsPair(tuple, targetDeviceIds)))
            device.mWaitForEvent(PredicateType(Events.NOTIFICATION_CONSUMER_WAIT_PRODUCERS.tag))
            false
        }
    }
}
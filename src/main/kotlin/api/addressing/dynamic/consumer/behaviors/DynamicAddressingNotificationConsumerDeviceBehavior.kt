package api.addressing.dynamic.consumer.behaviors

import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDevice
import api.addressing.fixed.entities.AddressingDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.BaseEventWrapper
import api.common.utils.TupleTargetNextHopMapQuantifierContainer
import api.common.utils.TupleRecipientsPair
import api.notification.consumer.entities.NotificationConsumerDevice
import org.cloudbus.cloudsim.core.SimEvent

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
            Events.ADDRESSING_DEVICE_CREATE_CHILDREN_MAPPING.tag -> onCreateChildrenMapping(ev)
            else -> superAddressingDeviceBehavior.processEvent(ev) && superNotificationConsumerDeviceBehavior.processEvent(ev)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onCreateChildrenMapping(ev: SimEvent): Boolean {
        val (_, container) = ev.data as BaseEventWrapper<TupleTargetNextHopMapQuantifierContainer>
        val (tuple, targetNextHopMap, quantifier) = container
        val newTargetNextHopMap = mutableMapOf<Int, Int>()
        newTargetNextHopMap.putAll(targetNextHopMap)
        val missingTargetIds = mutableListOf<Int>()

        newTargetNextHopMap.forEach { (targetId, nextHopId) ->
            if (nextHopId <= 0) {
                newTargetNextHopMap.remove(targetId)
                missingTargetIds.add(targetId)
            }
        }
        // Await notification from missing targets
        if (missingTargetIds.isNotEmpty()) {
            device.mSendEvent(device.mId, 0.0, Events.NOTIFICATION_CONSUMER_WAIT_PRODUCERS.tag,
                    TupleRecipientsPair(tuple, missingTargetIds))
        }

        (ev.data as BaseEventWrapper<TupleTargetNextHopMapQuantifierContainer>).other =
                TupleTargetNextHopMapQuantifierContainer(tuple, newTargetNextHopMap, quantifier)
        return superAddressingDeviceBehavior.processEvent(ev)
    }
}
package api.addressing.dynamic.producer.behaviors

import api.addressing.dynamic.producer.entities.DynamicAddressingNotificationProducerDevice
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.common.behaviors.BaseBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent

interface DynamicAddressingNotificationProducerDeviceBehavior
    : BaseBehavior<DynamicAddressingNotificationProducerDeviceBehavior, DynamicAddressingNotificationProducerDevice>{
    val superNotificationProducerBehavior: NotificationProducerDeviceBehavior<
            DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>
            >

    override fun onStart() {
        superNotificationProducerBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superNotificationProducerBehavior.processEvent(ev)
    }
}
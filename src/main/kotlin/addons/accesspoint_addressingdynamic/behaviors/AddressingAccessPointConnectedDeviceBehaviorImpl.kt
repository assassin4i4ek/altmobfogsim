package addons.accesspoint_addressingdynamic.behaviors

import api.accesspoint.original.behaviors.AccessPointConnectedDeviceBehavior
import api.accesspoint.original.entities.AccessPointConnectedDevice
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.mobility.behaviors.MobileDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior

class AddressingAccessPointConnectedDeviceBehaviorImpl(
        override val device: AccessPointConnectedDevice,
        override val superDynamicGatewayConnectionDeviceBehavior: NotificationProducerDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>>,
        override val superMobileDeviceBehavior: MobileDeviceBehavior)
    : AccessPointConnectedDeviceBehavior<
        NotificationProducerDeviceBehavior<
                DynamicGatewayConnectionDeviceBehavior<
                        AddressingDeviceBehavior<
                                NetworkDeviceBehavior>>>,
        MobileDeviceBehavior>
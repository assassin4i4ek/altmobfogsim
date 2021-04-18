package addons.accesspoint_addressingdynamic_migrationoriginal.behaviors

import addons.accesspoint_addressingdynamic.behaviors.AddressingAccessPointConnectedDeviceBehaviorImpl
import addons.accesspoint_addressingdynamic_migrationoriginal.entities.MigrationStimulatorAddressingAccessPointConnectedDevice
import api.accesspoint.migration.behaviors.MigrationStimulatorAccessPointConnectedDeviceBehavior
import api.accesspoint.migration.entities.MigrationStimulatorAccessPointConnectedDevice
import api.accesspoint.original.behaviors.AccessPointConnectedDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.mobility.behaviors.MobileDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior

class MigrationStimulatorAddressingAccessPointConnectedDeviceBehaviorImpl(
        override val device: MigrationStimulatorAddressingAccessPointConnectedDevice,
        override val superAccessPointConnectedDeviceBehavior
        : AccessPointConnectedDeviceBehavior<
                NotificationProducerDeviceBehavior<
                        DynamicGatewayConnectionDeviceBehavior<
                                AddressingDeviceBehavior<
                                        NetworkDeviceBehavior>>>,
                MobileDeviceBehavior>
):
        MigrationStimulatorAccessPointConnectedDeviceBehavior<
                AccessPointConnectedDeviceBehavior<
                        NotificationProducerDeviceBehavior<
                                DynamicGatewayConnectionDeviceBehavior<
                                        AddressingDeviceBehavior<
                                                NetworkDeviceBehavior>>>,
                        MobileDeviceBehavior>>
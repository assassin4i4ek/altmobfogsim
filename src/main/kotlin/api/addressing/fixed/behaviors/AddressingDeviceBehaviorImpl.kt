package api.addressing.fixed.behaviors

import api.addressing.fixed.entities.AddressingDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior

class AddressingDeviceBehaviorImpl(
        override val device: AddressingDevice,
        override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
) : AddressingDeviceBehavior<NetworkDeviceBehavior>
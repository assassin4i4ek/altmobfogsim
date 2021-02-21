package api.addressing.behaviors

import api.addressing.entities.AddressingDevice
import api.common.behaviors.BaseBehavior
import api.network.behaviors.NetworkDeviceBehavior
import api.network.entities.NetworkDevice

class AddressingDeviceBehaviorImpl(
    override val device: AddressingDevice,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior
) : AddressingDeviceBehavior<NetworkDeviceBehavior>
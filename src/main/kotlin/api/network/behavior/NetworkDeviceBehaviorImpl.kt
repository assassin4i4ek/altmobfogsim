package api.network.behavior

import api.network.entities.NetworkDevice

class NetworkDeviceBehaviorImpl: NetworkDeviceBehavior<NetworkDevice> {
    override lateinit var device: NetworkDevice
}
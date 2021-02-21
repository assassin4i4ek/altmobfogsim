package api.accesspoint.behaviors

import api.accesspoint.entities.AccessPoint
import api.network.behaviors.NetworkDeviceBehavior

class AccessPointBehaviorImpl(
    override val device: AccessPoint,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
) : AccessPointBehavior<NetworkDeviceBehavior> {
}
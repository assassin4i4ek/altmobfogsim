package api.accesspoint.original.behaviors

import api.accesspoint.original.entities.AccessPoint
import api.network.fixed.behaviors.NetworkDeviceBehavior

class AccessPointBehaviorImpl(
    override val device: AccessPoint,
    override val superNetworkDeviceBehavior: NetworkDeviceBehavior,
) : AccessPointBehavior<NetworkDeviceBehavior> {
}
package api.accesspoint.behaviors

import api.accesspoint.entities.AccessPoint
import api.common.behaviors.BaseBehavior
import api.network.behaviors.NetworkDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent

interface AccessPointBehavior: BaseBehavior<AccessPointBehavior, AccessPoint> {
    val superNetworkDeviceBehavior: NetworkDeviceBehavior

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superNetworkDeviceBehavior.processEvent(ev)
    }
}
package api.accesspoint.behaviors

import api.accesspoint.entities.AccessPoint
import api.common.behaviors.BaseBehavior
import api.network.behaviors.NetworkDeviceBehavior
import api.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent

interface AccessPointBehavior<T: BaseBehavior<T, out NetworkDevice>>: BaseBehavior<AccessPointBehavior<T>, AccessPoint> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        device.accessPointsMap.registerAccessPoint(device)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superNetworkDeviceBehavior.processEvent(ev)
    }
}
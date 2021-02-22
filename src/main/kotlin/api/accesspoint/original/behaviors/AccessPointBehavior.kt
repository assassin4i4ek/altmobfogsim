package api.accesspoint.original.behaviors

import api.accesspoint.original.entities.AccessPoint
import api.common.behaviors.BaseBehavior
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent

interface AccessPointBehavior<T: BaseBehavior<T, NetworkDevice>>: BaseBehavior<AccessPointBehavior<T>, AccessPoint> {
    val superNetworkDeviceBehavior: T

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        device.accessPointsMap.registerAccessPoint(device)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superNetworkDeviceBehavior.processEvent(ev)
    }
}
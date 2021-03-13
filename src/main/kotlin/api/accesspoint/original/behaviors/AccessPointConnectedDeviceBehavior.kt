package api.accesspoint.original.behaviors

import api.accesspoint.original.entities.AccessPointConnectedDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.ConnectionUtils
import api.mobility.entities.MobileDevice
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.utils.Logger

interface AccessPointConnectedDeviceBehavior<
        T1: BaseBehavior<T1, out DynamicGatewayConnectionDevice>,
        T2: BaseBehavior<T2, out MobileDevice>>
    : BaseBehavior<AccessPointConnectedDeviceBehavior<T1, T2>, AccessPointConnectedDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: T1
    val superMobilityDeviceBehavior: T2

    override fun onStart() {
        superDynamicGatewayConnectionDeviceBehavior.onStart()
        superMobilityDeviceBehavior.onStart()
        device.mSendEvent(device.mId, 0.0, Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag, null)
        device.mWaitForEvent(PredicateType(Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag))
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MOBILE_DEVICE_NEXT_MOVE.tag -> onNextMove(ev)
            Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag -> onUpdateConnection()
            else -> superMobilityDeviceBehavior.processEvent(ev) &&
                    superDynamicGatewayConnectionDeviceBehavior.processEvent(ev)
        }
    }

    private fun onNextMove(ev: SimEvent): Boolean {
        device.mSendEvent(device.mId, 0.0, Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag, null)
        device.mWaitForEvent(PredicateType(Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag))
        return superMobilityDeviceBehavior.processEvent(ev)
    }

    private fun onUpdateConnection(): Boolean {
        if (device.accessPoint != null) {
            if (device.accessPoint!!.connectionZone.isInZone(device.position)) {
                //no need to reconnect
            } else {
                // disconnect from current access point
                ConnectionUtils.disconnectChildFromParent(device.accessPoint!!, device)
                Logger.debug(device.accessPoint!!.mName, "Interrupted connection with ${device.mName}")
                device.accessPoint = null
            }
        }

        if (device.accessPoint == null) {
            // if previous actions resulted disconnection from access point or it wasn't connected at all
            for (ap in device.accessPointsMap.getClosestAccessPointsTo(device.position.coordinates)) {
                if (ap.connectionZone.isInZone(device.position)) {
                    // if ap can connect device
                    ConnectionUtils.connectChildToParent(ap, device)
                    Logger.debug(ap.mName, "Established connection with ${device.mName}")
                    device.accessPoint = ap
                    break
                }
            }
        }

        return true
    }
}
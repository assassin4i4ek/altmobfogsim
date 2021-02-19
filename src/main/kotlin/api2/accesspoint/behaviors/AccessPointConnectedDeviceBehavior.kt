package api2.accesspoint.behaviors

import api2.accesspoint.entities.AccessPointsMap
import api2.accesspoint.entities.AccessPointConnectedDevice
import api2.common.Events
import api2.common.behaviors.BaseBehavior
import api2.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehavior
import api2.dynamic.mobility.behaviors.MobileDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface AccessPointConnectedDeviceBehavior: BaseBehavior<AccessPointConnectedDeviceBehavior, AccessPointConnectedDevice> {
    val superDynamicGatewayConnectionDeviceBehavior: DynamicGatewayConnectionDeviceBehavior
    val superMobilityDeviceBehavior: MobileDeviceBehavior
    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MOBILE_DEVICE_NEXT_MOVE.tag -> onNextMove(ev)
            Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag -> onUpdateConnection(ev)
            else -> superMobilityDeviceBehavior.processEvent(ev) &&
                    superDynamicGatewayConnectionDeviceBehavior.processEvent(ev) &&
                    super.processEvent(ev)
        }
    }

    private fun onNextMove(ev: SimEvent): Boolean {
        device.mSendEvent(device.mId, 0.0, Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag, null)
        return superMobilityDeviceBehavior.processEvent(ev)
    }

    private fun onUpdateConnection(ev: SimEvent): Boolean {
        if (device.accessPoint != null) {
            if (device.accessPoint!!.connectionZone.isInZone(device.position)) {
                //no need to reconnect
            } else {
                // disconnect from current access point
                DynamicGatewayConnectionDeviceBehavior.disconnectChildFromParent(device.accessPoint!!, device)
                Logger.debug(device.accessPoint!!.mName, "Interrupted connection with ${device.mName}")
                device.accessPoint = null
            }
        }

        if (device.accessPoint == null) {
            // if previous actions resulted disconnection from access point or it wasn't connected at all
            for (ap in AccessPointsMap.getClosestAccessPointsTo(device.position.coordinates)) {
                if (ap.connectionZone.isInZone(device.position)) {
                    // if ap can connect device
                    DynamicGatewayConnectionDeviceBehavior.connectChildToParent(ap, device)
                    Logger.debug(ap.mName, "Established connection with ${device.mName}")
                    device.accessPoint = ap
                    break
                }
            }
        }

        return true
    }
}
package api.accesspoint.behavior

import api.dynamic.connectivity.behavior.DynamicGatewayConnectionBehavior
import api.dynamic.mobility.behavior.MobileDeviceBehavior
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Logger

interface AccessPointConnectedDeviceBehavior<T: api.accesspoint.entities.AccessPointConnectedDevice>
    : DynamicGatewayConnectionBehavior<T>, MobileDeviceBehavior<T> {


    override fun onStartEntity(device: T) {
        super<DynamicGatewayConnectionBehavior>.onStartEntity(device)
        super<MobileDeviceBehavior>.onStartEntity(device)
    }

    override fun onProcessEvent(ev: SimEvent): Boolean {
        return super<DynamicGatewayConnectionBehavior>.onProcessEvent(ev) &&
                super<MobileDeviceBehavior>.onProcessEvent(ev)
    }

    override fun nextMove(): Boolean {
        val result = super.nextMove()
        updateConnection()
        return result
    }

    fun updateConnection() {
        if (device.accessPoint != null) {
            if (accessPointCanConnect(device.accessPoint!!, device)) {
                //no need to reconnect
            }
            else {
                // disconnect from current access point
                DynamicGatewayConnectionBehavior.disconnectChildFromParent(device.accessPoint!!, device)
                Logger.debug(device.accessPoint!!.mName, "Interrupted connection with ${device.mName}")
                device.accessPoint = null
            }
        }

        if (device.accessPoint == null) {
            // if previous actions resulted disconnection from access point or it wasn't connected at all
            for (ap in api.accesspoint.entities.AccessPointsMap.getClosestAccessPointsTo(device.positionAndTimestamp.position.coordinates)) {
                if (accessPointCanConnect(ap, device)) {
                    // if ap can connect device
                    DynamicGatewayConnectionBehavior.connectChildToParent(ap, device)
                    Logger.debug(ap.mName, "Established connection with ${device.mName}")
                    device.accessPoint = ap
                    break
                }
            }
        }
    }

    fun accessPointCanConnect(accessPoint: api.accesspoint.entities.AccessPoint, device: T): Boolean {
        return accessPoint.connectionZone.isInZone(device.positionAndTimestamp.position)
    }
}
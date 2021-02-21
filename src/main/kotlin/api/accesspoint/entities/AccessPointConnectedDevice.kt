package api.accesspoint.entities

import api.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api.dynamic.mobility.entities.MobileDevice

interface AccessPointConnectedDevice: DynamicGatewayConnectionDevice, MobileDevice {
    var accessPoint: AccessPoint?
    val accessPointsMap: AccessPointsMap
}
package api.accesspoint.original.entities

import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.mobility.entities.MobileDevice

interface AccessPointConnectedDevice: DynamicGatewayConnectionDevice, MobileDevice {
    var accessPoint: AccessPoint?
    val accessPointsMap: AccessPointsMap
}
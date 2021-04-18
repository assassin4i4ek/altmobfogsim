package api.accesspoint.original.entities

import api.accesspoint.utils.AccessPointsMap
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.mobility.entities.MobileDevice

interface AccessPointConnectedDevice: DynamicGatewayConnectionDevice, MobileDevice {
    var accessPoint: AccessPoint?
    val accessPointsMap: AccessPointsMap
    var mDynamicUplinkLatency: Double
    var mDynamicUplinkBandwidth: Double
}
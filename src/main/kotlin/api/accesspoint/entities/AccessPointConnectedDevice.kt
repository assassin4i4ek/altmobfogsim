package api.accesspoint.entities

import api.dynamic.connectivity.entities.DynamicGatewayConnectionDevice
import api.dynamic.mobility.entities.MobileDevice

interface AccessPointConnectedDevice: DynamicGatewayConnectionDevice, MobileDevice {
    var accessPoint: api.accesspoint.entities.AccessPoint?
}
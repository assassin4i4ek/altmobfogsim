package api2.accesspoint.entities

import api2.dynamic.connection.entites.DynamicGatewayConnectionDevice
import api2.dynamic.mobility.entities.MobileDevice

interface AccessPointConnectedDevice: DynamicGatewayConnectionDevice, MobileDevice {
    var accessPoint: AccessPoint?
}
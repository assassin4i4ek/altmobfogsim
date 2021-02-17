package api.accesspoint.behavior

class AccessPointConnectedDeviceBehaviorImpl:
    api.accesspoint.behavior.AccessPointConnectedDeviceBehavior<api.accesspoint.entities.AccessPointConnectedDevice> {
    override lateinit var device: api.accesspoint.entities.AccessPointConnectedDevice
}
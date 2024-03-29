package api.accesspoint.migration.entities

import addons.accesspoint_addressingdynamic.entities.AddressingAccessPointConnectedDevice
import api.accesspoint.original.entities.AccessPointConnectedDevice

interface MigrationStimulatorAccessPointConnectedDevice: AccessPointConnectedDevice {
    val migrationDecisionMakingDeviceId: Int?
}
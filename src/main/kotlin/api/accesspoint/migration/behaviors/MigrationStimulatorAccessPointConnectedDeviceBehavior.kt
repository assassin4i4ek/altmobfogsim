package api.accesspoint.migration.behaviors

import api.accesspoint.migration.entities.MigrationStimulatorAccessPointConnectedDevice
import api.accesspoint.original.entities.AccessPointConnectedDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent

interface MigrationStimulatorAccessPointConnectedDeviceBehavior<
        T: BaseBehavior<T, out AccessPointConnectedDevice>>:
        BaseBehavior<
                MigrationStimulatorAccessPointConnectedDeviceBehavior<T>,
                MigrationStimulatorAccessPointConnectedDevice
                > {
    val superAccessPointConnectedDeviceBehavior: T
    override fun onStart() {
        superAccessPointConnectedDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when(ev.tag) {
            Events.ACCESS_POINT_CONNECTED_DEVICE_UPDATE_CONNECTION.tag -> onConnectionUpdate(ev)
            else -> superAccessPointConnectedDeviceBehavior.processEvent(ev)
        }
    }

    private fun onConnectionUpdate(ev: SimEvent): Boolean {
        val prevAccessPoint = device.accessPoint
        val res = superAccessPointConnectedDeviceBehavior.processEvent(ev)
        val newAccessPoint = device.accessPoint
        if (newAccessPoint != null) {
            if (prevAccessPoint != newAccessPoint) {
                device.mSendEvent(device.migrationDecisionMakingDeviceId, CloudSim.getMinTimeBetweenEvents(),
                        Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_STIMULATE_DECISION.tag, null)
            }
        }
        return res
    }
}
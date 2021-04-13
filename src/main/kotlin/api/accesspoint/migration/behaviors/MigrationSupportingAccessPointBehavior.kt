package api.accesspoint.migration.behaviors

import api.accesspoint.migration.entities.MigrationSupportingAccessPoint
import api.accesspoint.original.entities.AccessPoint
import api.common.behaviors.BaseBehavior
import api.migration.original.entites.MigrationSupportingDevice
import org.cloudbus.cloudsim.core.SimEvent

interface MigrationSupportingAccessPointBehavior<
        T1: BaseBehavior<T1, out MigrationSupportingDevice>,
        T2: BaseBehavior<T2, out AccessPoint>
        >: BaseBehavior<MigrationSupportingAccessPointBehavior<T1, T2>, MigrationSupportingAccessPoint> {
    val superMigrationSupportingDeviceBehavior: T1
    val superAccessPointBehavior: T2

    override fun onStart() {
        superMigrationSupportingDeviceBehavior.onStart()
        superAccessPointBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superMigrationSupportingDeviceBehavior.processEvent(ev) && superAccessPointBehavior.processEvent(ev)
    }
}
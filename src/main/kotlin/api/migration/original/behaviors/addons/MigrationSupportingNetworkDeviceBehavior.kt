package api.migration.original.behaviors.addons

import api.common.behaviors.BaseBehavior
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.original.entites.addons.MigrationSupportingNetworkDevice
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent

interface MigrationSupportingNetworkDeviceBehavior<
        T1:BaseBehavior<T1, out NetworkDevice>,
        T2:BaseBehavior<T2, out MigrationSupportingDevice>>
    : BaseBehavior<MigrationSupportingNetworkDeviceBehavior<T1, T2>, MigrationSupportingNetworkDevice> {
    val superNetworkDeviceBehavior: T1
    val superMigrationSupportingNetworkDeviceBehavior: T2

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        superMigrationSupportingNetworkDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superNetworkDeviceBehavior.processEvent(ev) && superMigrationSupportingNetworkDeviceBehavior.processEvent(ev)
    }
}
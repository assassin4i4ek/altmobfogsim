package addons.addressingdynamic_migrationoriginal.behaviors

import addons.addressingdynamic_migrationoriginal.entities.DynamicAddressingMigrationSupportingDevice
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDevice
import api.common.behaviors.BaseBehavior
import api.migration.original.entites.MigrationSupportingDevice
import org.cloudbus.cloudsim.core.SimEvent

interface DynamicAddressingMigrationSupportingDeviceBehavior<
        T1: BaseBehavior<T1, out DynamicAddressingNotificationConsumerDevice>,
        T2: BaseBehavior<T2, out MigrationSupportingDevice>
        >: BaseBehavior<DynamicAddressingMigrationSupportingDeviceBehavior<T1, T2>, DynamicAddressingMigrationSupportingDevice> {
    val superDynamicAddressingNotificationConsumerDeviceBehavior: T1
    val superMigrationSupportingDeviceBehavior: T2

    override fun onStart() {
        superDynamicAddressingNotificationConsumerDeviceBehavior.onStart()
        superMigrationSupportingDeviceBehavior.onStart()
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return superDynamicAddressingNotificationConsumerDeviceBehavior.processEvent(ev) && superMigrationSupportingDeviceBehavior.processEvent(ev)
    }
}
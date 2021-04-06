package api.migration.addressing.behaviors

import api.addressing.fixed.entities.AddressingDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.migration.addressing.utils.MigrationAppModule
import api.migration.addressing.utils.TupleWithAppModule
import api.migration.addressing.entities.ModuleAddressingMigrationSupportingDevice
import api.migration.addressing.utils.MigrationApplicationModulePlacement
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationRequest
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppModule
import org.fog.entities.Tuple
import org.fog.scheduler.TupleScheduler
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface ModuleAddressingMigrationSupportingDeviceBehavior<
        T1: BaseBehavior<T1, out MigrationSupportingDevice>,
        T2: BaseBehavior<T2, out AddressingDevice>>
    : BaseBehavior<ModuleAddressingMigrationSupportingDeviceBehavior<T1, T2>, ModuleAddressingMigrationSupportingDevice> {
    val superMigrationSupportingDeviceBehavior: T1
    val superAddressingDeviceBehavior: T2

    override fun onStart() {
        superMigrationSupportingDeviceBehavior.onStart()
        superAddressingDeviceBehavior.onStart()
        val controller = device.controller
        val migrationModulePlacement = if (!controller.appModulePlacementPolicy.containsKey("migration")) {
            val placement = MigrationApplicationModulePlacement()
            controller.appModulePlacementPolicy["migration"] = placement
            placement
        }
        else {
            val placement = controller.appModulePlacementPolicy["migration"]!!
            assert(placement is MigrationApplicationModulePlacement)
            placement as MigrationApplicationModulePlacement
        }

        migrationModulePlacement.moduleToDeviceMap["migration"]!!.add(device.mId)
        migrationModulePlacement.deviceToModuleMap[device.mId] = listOf(MigrationAppModule(device.mId))
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MIGRATION_SUPPORTING_DEVICE_MODULE_ARRIVED.tag -> onModuleArrived(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_START.tag -> onMigrationStart(ev)
            Events.MODULE_ADDRESSING_MIGRATION_SUPPORTING_DEVICE_SUPPRESS_MODULE.tag -> onModuleSuppress(ev)
            FogEvents.TUPLE_ARRIVAL -> onProcessTupleArrival(ev)
            else -> superMigrationSupportingDeviceBehavior.processEvent(ev) && superAddressingDeviceBehavior.processEvent(ev)
        }
    }

    private fun onMigrationStart(ev: SimEvent): Boolean {
        val res = superMigrationSupportingDeviceBehavior.processEvent(ev)
        val migrationRequest = ev.data as MigrationRequest
        if (device === migrationRequest.from && migrationRequest.to != null) {
            if (!device.controller.appModulePlacementPolicy[migrationRequest.appId]!!
                            .moduleToDeviceMap[migrationRequest.appModuleName]!!.contains(migrationRequest.to.mId)) {
                val module = device.mAppModuleList.find {
                    module -> module.appId == migrationRequest.appId && module.name == migrationRequest.appModuleName
                }!!
                Logger.debug(device.mName, "Sending tuple with module ${module.name}")
                // handle tuple by addressing behavior; direction UP is necessary for addressing functionality
                device.sendUp(TupleWithAppModule(module, migrationRequest.to.mId))
            }
        }
        return res
    }

    private fun onModuleArrived(ev: SimEvent): Boolean {
        val res = superMigrationSupportingDeviceBehavior.processEvent(ev)
        val module = ev.data as AppModule
        device.mSendEvent(device.mId, 0.0, Events.MODULE_ADDRESSING_MIGRATION_SUPPORTING_DEVICE_SUPPRESS_MODULE.tag, module)
        return res
    }

    private fun onModuleSuppress(ev: SimEvent): Boolean {
        val module = ev.data as AppModule
        val moduleOnDevice = device.mAppModuleList.find { it.name == module.name && it.appId == module.appId }!!
        device.numberOfSuppressedModuleInstances
                .getOrPut(moduleOnDevice.appId) { mutableMapOf() }
                .apply {
                    this[moduleOnDevice.name] = this.getOrDefault(moduleOnDevice.name, 0) + 1
                }
        device.tuplesSuppressedWhileModuleMigration
                .getOrPut(moduleOnDevice.appId) { mutableMapOf() }
                .getOrPut(moduleOnDevice.name) { mutableListOf() }
        Logger.debug(device.mName, "Suppressing module ${moduleOnDevice.name} until it's fully received")
        return false
    }

    private fun onProcessTupleArrival(ev: SimEvent): Boolean {
        return if ((ev.data as? TupleWithAppModule)?.destId ?: -1 == device.mId) {
            val tupleWithAppModule = ev.data as TupleWithAppModule
            val module = tupleWithAppModule.appModule
            Logger.debug(device.mName, "Received tuple with module ${tupleWithAppModule.appModule.name}")
            device.numberOfSuppressedModuleInstances[module.appId]!!.apply {
                this[module.name] = this[module.name]!! - 1
            }
            if (device.numberOfSuppressedModuleInstances[module.appId]!![module.name]!! == 0) {
                device.numberOfSuppressedModuleInstances[module.appId]!!.remove(module.name)
                val suppressedTupleEvents = device.tuplesSuppressedWhileModuleMigration[module.appId]!![module.name]!!
                device.tuplesSuppressedWhileModuleMigration[module.appId]!!.remove(module.name)
                suppressedTupleEvents.forEach {
                    Logger.debug(device.mName, "Releasing tuple ${(it.data as Tuple).cloudletId}")
                    CloudSim.send(it.source, it.destination, 0.0, it.tag, it.data)
                }
            }

            if (device.numberOfSuppressedModuleInstances[module.appId]!!.isEmpty()) {
                device.numberOfSuppressedModuleInstances.remove(module.appId)
            }
            if (device.tuplesSuppressedWhileModuleMigration[module.appId]!!.isEmpty()) {
                device.tuplesSuppressedWhileModuleMigration.remove(module.appId)
            }
            false
        }
        else {
            val tuple = ev.data as Tuple
            if (
                    device.tuplesSuppressedWhileModuleMigration.containsKey(tuple.appId) &&
                    device.tuplesSuppressedWhileModuleMigration[tuple.appId]!!.containsKey(tuple.destModuleName) &&
                    device.mAppToModulesMap.containsKey(tuple.appId) &&
                    device.mAppToModulesMap[tuple.appId]!!.contains(tuple.destModuleName)
            ) {
                Logger.debug(device.mName, "Suppressing tuple ${tuple.cloudletId} with destination module ${tuple.destModuleName}")
                device.tuplesSuppressedWhileModuleMigration[tuple.appId]!![tuple.destModuleName]!!.add(ev)
                false
            }
            else {
                true
            }
        }
    }
}
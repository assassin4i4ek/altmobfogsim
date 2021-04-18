package api.migration.original.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.migration.original.utils.OldNewModulePair
import api.migration.original.entites.MigrationSupportingDevice
import api.migration.utils.MigrationLogger
import api.migration.utils.MigrationRequest
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.CloudSimTags
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.application.AppModule
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface MigrationSupportingDeviceBehavior: BaseBehavior<MigrationSupportingDeviceBehavior, MigrationSupportingDevice> {
    override fun onStart() {
        device.migrationModel.device = device
        device.mSendEvent(device.mId, device.migrationModel.updateTimeProgression.nextTime(),
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag, null)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag -> onMigrationDecision()
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag -> onMigrationPrepare(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_START.tag -> onMigrationStart(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MODULE_DEPARTED.tag -> onModuleDeparted(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MODULE_ARRIVED.tag -> onModuleArrived(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_REMOVE_MODULE.tag -> onRemoveModule(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATE_TUPLES.tag -> onMigrateTuples(ev)
            else -> true
        }
    }

    private fun onMigrationDecision(): Boolean {
        while (true) {
            val a = CloudSim.select(device.mId, PredicateType(Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag))
            if (a == null || a.destination != device.mId) {
                break
            }
        }
        val migrationRequests = device.migrationModel.decide()
        if (migrationRequests.isNotEmpty()) {
            migrationRequests.forEach { migrationRequest ->
                MigrationLogger.logMigrationDecision(device.mName, migrationRequest)
                if (migrationRequest.to != null) {
                    if (migrationRequest.to.migrationModel.canMigrate(migrationRequest)) {
                        Logger.debug(device.mName, "Decided to migrate instance of module ${migrationRequest.appModuleName} from ${migrationRequest.from?.mName} to ${migrationRequest.to.mName}")
                        if (migrationRequest.from != null) {
                            device.mSendEvent(migrationRequest.from.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag, migrationRequest)
                        }
                        device.mSendEvent(migrationRequest.to.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag, migrationRequest)
                    }
                }
                else if (migrationRequest.from != null) {
                    device.mSendEvent(migrationRequest.from.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag, migrationRequest)
                }
            }
        }
        device.mSendEvent(device.mId, device.migrationModel.updateTimeProgression.nextTime(),
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag, null)
        return false
    }

    private fun onMigrationPrepare(ev: SimEvent): Boolean {
        val migrationRequest = ev.data as MigrationRequest
//        device.migrationModel.prepare(migrationRequest)
        device.mSendEvent(device.mId, 0.0,
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_START.tag, migrationRequest)
        return false
    }

    private fun onMigrationStart(ev: SimEvent): Boolean {
        val migrationRequest = ev.data as MigrationRequest
        if (device === migrationRequest.from) {
            val module = device.mAppModuleList.find { module ->
                module.appId == migrationRequest.appId && module.name == migrationRequest.appModuleName
            }!!
            when (migrationRequest.type) {
                MigrationRequest.Type.REMOVE_ALL_INSTANCES -> {
                    // next step will decrement further values which will cause full removal of module
                    module.numInstances = 0
                    device.controller.appModulePlacementPolicy[module.appId]!!
                            .moduleInstanceCountMap[device.mId]!![module.name] = 0
                    device.mSendEvent(device.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MODULE_DEPARTED.tag, module)
                    device.mWaitForEvent(PredicateType(Events.MIGRATION_SUPPORTING_DEVICE_MODULE_DEPARTED.tag))
                }
                MigrationRequest.Type.REMOVE_SINGLE_INSTANCE -> {
                    module.numInstances--
                    device.controller.appModulePlacementPolicy[module.appId]!!
                            .moduleInstanceCountMap[device.mId]!!
                            .run { this[module.name] = this[module.name]!! - 1 }
                    device.mSendEvent(device.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MODULE_DEPARTED.tag, module)
                    device.mWaitForEvent(PredicateType(Events.MIGRATION_SUPPORTING_DEVICE_MODULE_DEPARTED.tag))
                }
                MigrationRequest.Type.COPY -> {}
            }
        }
        else if (device === migrationRequest.to) {
            val module = device.controller.applications[migrationRequest.appId]!!.getModuleByName(migrationRequest.appModuleName)
            device.mSendEvent(device.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MODULE_ARRIVED.tag, module)
        }
        return false
    }

    private fun onModuleDeparted(ev: SimEvent): Boolean {
        val module = ev.data as AppModule
        val modulePlacement = device.controller.appModulePlacementPolicy[module.appId]!!
        if (modulePlacement.moduleInstanceCountMap[device.mId]!![module.name]!! <= 0) {
            // module should be removed
            device.mSendEvent(device.mId, 0.0, CloudSimTags.VM_DESTROY, module)
            device.mSendEvent(device.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_REMOVE_MODULE.tag, module)
        }
        else {
            Logger.debug(device.mName, "Deleting instance of module ${module.name}")
        }
        return false
    }

    private fun onRemoveModule(ev: SimEvent): Boolean {
        val module = ev.data as AppModule
        val modulePlacement = device.controller.appModulePlacementPolicy[module.appId]!!
        Logger.debug(device.mName, "Deleting last instance of module ${module.name}")
        device.mAppToModulesMutableMap[module.appId]!!.remove(module.name)
        // device module information
        if (device.mAppToModulesMutableMap[module.appId]!!.isEmpty()) {
            device.mAppToModulesMutableMap.remove(module.appId)
            device.mApplicationMutableMap.remove(module.appId)
            device.mActiveMutableApplications.remove(module.appId)
        }
        // controller module placement information
        assert(modulePlacement.moduleToDeviceMap[module.name]!!.remove(device.mId))
        if (modulePlacement.moduleToDeviceMap[module.name]!!.isEmpty()) {
            modulePlacement.moduleToDeviceMap.remove(module.name)
        }
        assert(modulePlacement.deviceToModuleMap[device.mId]!!.remove(module))
        if (modulePlacement.deviceToModuleMap[device.mId]!!.isEmpty()) {
            modulePlacement.deviceToModuleMap.remove(device.mId)
        }
        assert(modulePlacement.moduleInstanceCountMap[device.mId]!!.remove(module.name) != null)
        return false
    }

    private fun onModuleArrived(ev: SimEvent): Boolean {
        val module = ev.data as AppModule
        val modulePlacement = device.controller.appModulePlacementPolicy[module.appId]!!

        if (modulePlacement.moduleInstanceCountMap[device.mId]!!.containsKey(module.name) && modulePlacement.moduleInstanceCountMap[device.mId]!![module.name]!! != 0) {
            // there is already an instance of this module on device
            Logger.debug(device.mName, "Launching instance of module ${module.name}")
            val moduleOnDevice = device.mAppModuleList.find {it.appId == module.appId && it.name == module.name }!!
            moduleOnDevice.numInstances++
            modulePlacement.moduleInstanceCountMap[device.mId]!!.run {this[module.name] = this[module.name]!! + 1}
        }
        else {
            // the module has to be created
            Logger.debug(device.mName, "Launching new instance of module ${module.name}")

            val newModule = AppModule(module)
            newModule.numInstances = 1
            // device module information
//                device.mSendEvent(device.mId, 0.0, FogEvents.APP_SUBMIT, app)
            val app = device.controller.applications[module.appId]!!
            device.mApplicationMutableMap[module.appId] = app
            device.mSendEvent(device.mId, 0.0, FogEvents.ACTIVE_APP_UPDATE, app)
            device.mSendEvent(device.mId, 0.0, FogEvents.LAUNCH_MODULE, newModule)
            device.mSendEvent(device.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATE_TUPLES.tag, OldNewModulePair(module, newModule))
            device.mWaitForEvent(PredicateType(FogEvents.LAUNCH_MODULE))
            // controller module placement information
            if (!modulePlacement.moduleToDeviceMap.containsKey(newModule.name)) {
                modulePlacement.moduleToDeviceMap[newModule.name] = mutableListOf()
            }
            modulePlacement.moduleToDeviceMap[newModule.name]!!.add(device.mId)
            if (!modulePlacement.deviceToModuleMap.containsKey(device.mId)) {
                modulePlacement.deviceToModuleMap[device.mId] = mutableListOf()
            }
            modulePlacement.deviceToModuleMap[device.mId]!!.add(newModule)
            modulePlacement.moduleInstanceCountMap[device.mId]!![newModule.name] = 1
        }
        return false
    }

    private fun onMigrateTuples(ev: SimEvent): Boolean {
        val (oldModule, newModule) = ev.data as OldNewModulePair
        while (oldModule.cloudletScheduler.runningCloudlets() > 0) {
            val tuple = oldModule.cloudletScheduler.migrateCloudlet()
            Logger.debug(device.mName, "Migrating tuple ${tuple.cloudletId} to new module ${newModule.name}")
            newModule.cloudletScheduler.cloudletSubmit(tuple)
        }
        return false
    }
}
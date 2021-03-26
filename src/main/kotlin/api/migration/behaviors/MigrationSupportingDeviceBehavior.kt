package api.migration.behaviors

import api.common.Events
import api.common.behaviors.BaseBehavior
import api.migration.entites.MigrationSupportingDevice
import api.migration.models.MigrationRequest
import org.cloudbus.cloudsim.core.CloudSimTags
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.utils.FogEvents
import org.fog.utils.Logger

interface MigrationSupportingDeviceBehavior: BaseBehavior<MigrationSupportingDeviceBehavior, MigrationSupportingDevice> {
    override fun onStart() {
        device.migrationModel.device = device
        device.mSendEvent(device.mId, device.migrationModel.nextUpdateTime,
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag, null)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag -> onMigrationDecision()
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag -> onMigrationPrepare(ev)
            Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_START.tag -> onMigrationStart(ev)
            else -> true
        }
    }

    fun onMigrationDecision(): Boolean {
        Logger.debug(device.mName, "${(device as FogDevice).energyConsumption}")
        val migrationRequests = device.migrationModel.decide()
        if (migrationRequests.isNotEmpty()) {
            migrationRequests.forEach { migrationRequest ->
                if (migrationRequest.to.migrationModel.canMigrate(migrationRequest)) {
                    Logger.debug(device.mName, "Decided to migrate instance of module ${migrationRequest.appModuleName} to ${migrationRequest.to.mName}")
                    device.mSendEvent(migrationRequest.from.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag, migrationRequest)
                    device.mSendEvent(migrationRequest.to.mId, 0.0, Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_PREPARE.tag, migrationRequest)
                }
            }
        }
        device.mSendEvent(device.mId, device.migrationModel.nextUpdateTime,
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_DECISION.tag, null)
        return true
    }

    fun onMigrationPrepare(ev: SimEvent): Boolean {
        val migrationRequest = ev.data as MigrationRequest
//        device.migrationModel.prepare(migrationRequest)
        device.mSendEvent(device.mId, 0.0,
                Events.MIGRATION_SUPPORTING_DEVICE_MIGRATION_START.tag, migrationRequest)
        return false
    }

    fun onMigrationStart(ev: SimEvent): Boolean {
        val migrationRequest = ev.data as MigrationRequest
//        device.migrationModel.start(migrationRequest)
        if (device === migrationRequest.from) {
//            device.mAppToModulesMutableMap[migrationRequest.appName]!!.remove()
            val module = device.mAppModuleList.find { module ->
                module.appId == migrationRequest.appId && module.name == migrationRequest.appModuleName
            }!!

            module.numInstances--
            val modulePlacement = device.controller.appModulePlacementPolicy[module.appId]!!
            modulePlacement.moduleInstanceCountMap[device.mId]!!.run { this[module.name] = this[module.name]!! - 1 }
            if (modulePlacement.moduleInstanceCountMap[device.mId]!![module.name]!! <= 0) {
                // module should be removed
                Logger.debug(device.mName, "Deleting last instance of module ${module.name}")
                device.mSendEvent(device.mId, 0.0, CloudSimTags.VM_DESTROY, module)
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
            }
            else {
                Logger.debug(device.mName, "Deleting instance of module ${module.name}")
            }
        }
        else if (device == migrationRequest.to) {
            val controller = device.controller
            val app = controller.applications[migrationRequest.appId]!!
            val module = app.getModuleByName(migrationRequest.appModuleName)
            val modulePlacement = controller.appModulePlacementPolicy[app.appId]!!


            if (modulePlacement.moduleInstanceCountMap[device.mId]!!.containsKey(module.name) && modulePlacement.moduleInstanceCountMap[device.mId]!![module.name]!! != 0) {
                // there is already an instance of this module on device
                Logger.debug(device.mName, "Launching instance of module ${module.name}")
                val moduleOnDevice = device.mAppModuleList.find {it.appId == app.appId && it.name == module.name }!!
                moduleOnDevice.numInstances++
                modulePlacement.moduleInstanceCountMap[device.mId]!!.run {this[module.name] = this[module.name]!! + 1}
            }
            else {
                // the module has to be created
                Logger.debug(device.mName, "Launching new instance of module ${module.name}")

                val newModule = AppModule(module)
                // device module information
//                device.mSendEvent(device.mId, 0.0, FogEvents.APP_SUBMIT, app)
                device.mApplicationMutableMap[app.appId] = app
                device.mSendEvent(device.mId, 0.0, FogEvents.ACTIVE_APP_UPDATE, app)
                device.mSendEvent(device.mId, 0.0, FogEvents.LAUNCH_MODULE, newModule)
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
        }
        return false
    }
}
package api.migration.models

import api.migration.original.entites.MigrationSupportingDevice
import api.migration.original.entites.ModuleLaunchingDevice
import api.migration.utils.MigrationRequest
import org.cloudbus.cloudsim.core.CloudSim

class MigrationModelImpl(
        override val nextUpdateTime: Double,
) : MigrationModel {
    override lateinit var device: MigrationSupportingDevice

    override fun decide(): List<MigrationRequest> {
        //{appName: { appModuleName: {downAppModuleName: [downModuleId]}}}
        val unhandledDownModules = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<Int>>>>()
        val childrenDevices = device.mChildrenModuleLaunchingDevices

        // for each application
        device.mAppToModulesMap.forEach { (appName, appModuleNames) ->
            // for each module in application
            appModuleNames.forEach { appModuleName ->
                val appModule = device.mAppModuleList.find { it.name == appModuleName }!!
                // for fixed mapped modules numInstances == 0, they cannot take part in migration
                if (appModule.numInstances != 0) {
                    // for each downModule in module
                    appModule.downInstanceIdsMaps.forEach { (downModuleName, downModuleIds) ->
                        // for each child device with downModule
                        downModuleIds.removeIf { downModuleIdToSearch ->
                            val childWithModule = childrenDevices.find { child ->
                                child.mAppModuleList.find { childModule -> childModule.id == downModuleIdToSearch } != null
                            }
                            // if child device with downModule is not a child anymore
                            if (childWithModule == null) {
                                if (!unhandledDownModules.containsKey(appName)) {
                                    unhandledDownModules[appName] = mutableMapOf()
                                }
                                if (!unhandledDownModules[appName]!!.containsKey(appModuleName)) {
                                    unhandledDownModules[appName]!![appModuleName] = mutableMapOf()
                                }
                                if (!unhandledDownModules[appName]!![appModuleName]!!.containsKey(downModuleName)) {
                                    unhandledDownModules[appName]!![appModuleName]!![downModuleName] = mutableListOf()
                                }
                                unhandledDownModules[appName]!![appModuleName]!![downModuleName]!!.add(downModuleIdToSearch)
                                return@removeIf true
                            }
                            return@removeIf false
                        }
                    }
                }
            }
        }

        val migrationRequests = mutableListOf<MigrationRequest>()
        unhandledDownModules.forEach {(appName, appModules) ->
            appModules.forEach { (appModule, appUnhandledDownModules) ->
                appUnhandledDownModules.forEach { (unhandledDownModuleName, unhandledDownModuleIds) ->
                    unhandledDownModuleIds.forEach { unhandledDownModuleId ->
                        val appModulePlacement = device.controller.appModulePlacementPolicy[appName]!!
                        appModulePlacement.moduleToDeviceMap[unhandledDownModuleName]!!.forEach { deviceWithModuleId ->
                            val deviceWithModule = CloudSim.getEntity(deviceWithModuleId) as ModuleLaunchingDevice
                            if (deviceWithModule.mAppModuleList.find {appModule -> appModule.id == unhandledDownModuleId} != null) {
                                migrationRequests.add(MigrationRequest(appName, appModule,
                                        device, CloudSim.getEntity(deviceWithModule.mParentId) as MigrationSupportingDevice))
                            }
                        }
                    }
                }
            }
        }

        return migrationRequests
    }

    override fun canMigrate(request: MigrationRequest): Boolean {
        return true
    }

//    override fun prepare(request: MigrationRequest) {
//
//    }

//    override fun start(request: MigrationRequest) {
//
//    }
}
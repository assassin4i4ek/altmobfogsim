package api.migration.original.entites

import api.migration.models.MigrationModel
import org.fog.application.Application
import org.fog.placement.Controller

interface MigrationSupportingDevice: ModuleLaunchingDevice {
    val migrationModel: MigrationModel
    val mChildrenModuleLaunchingDevices: List<ModuleLaunchingDevice>
    val controller: Controller
//    val mVmAllocationPolicy: VmAllocationPolicy
    val mAppToModulesMutableMap: MutableMap<String, MutableList<String>>
    val mApplicationMutableMap: MutableMap<String, Application>
    val mActiveMutableApplications: MutableList<String>

    override val mAppToModulesMap: Map<String, List<String>>
        get() = mAppToModulesMutableMap
}
package api.migration.entites

import api.common.entities.SimEntityBehaviorWrapper
import api.migration.behaviors.MigrationSupportingDeviceBehavior
import api.migration.behaviors.MigrationSupportingDeviceBehaviorImpl
import api.migration.models.MigrationModel
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.application.AppModule
import org.fog.application.Application
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.placement.Controller

class MigrationSupportingDeviceImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double, override val migrationModel: MigrationModel)
    : FogDevice(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips), MigrationSupportingDevice, SimEntityBehaviorWrapper<MigrationSupportingDevice, MigrationSupportingDeviceBehavior> {
    /* SimEntityInterface */
    override val mId: Int get() = id
    override val mName: String get() = name
    override fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?) = send(id, delay, tag, data)
    override fun mWaitForEvent(p: Predicate) = waitForEvent(p)
    override fun startEntity() {
        super<FogDevice>.startEntity()
        super<SimEntityBehaviorWrapper>.startEntity()
    }

    override fun processOtherEvent(ev: SimEvent) {
        if (super.onProcessEvent(ev)) {
            super.processOtherEvent(ev)
        }
    }

    /* ModuleLaunchingDevice */
    override val mAppToModulesMutableMap: MutableMap<String, MutableList<String>> get() = appToModulesMap
    override val mAppModuleList: List<AppModule> get() = getVmList()
    override val mParentId: Int get() = parentId
    /* MigrationSupportingDevice */
    override val mChildrenModuleLaunchingDevices: List<ModuleLaunchingDevice> get() = childrenIds.map { CloudSim.getEntity(it) as ModuleLaunchingDevice}
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val mActiveMutableApplications: MutableList<String> get() = activeApplications
    override val mApplicationMutableMap: MutableMap<String, Application> get() = applicationMap
//    override val mVmAllocationPolicy: VmAllocationPolicy get() = vmAllocationPolicy

    override val behavior: MigrationSupportingDeviceBehavior = MigrationSupportingDeviceBehaviorImpl(this)
}
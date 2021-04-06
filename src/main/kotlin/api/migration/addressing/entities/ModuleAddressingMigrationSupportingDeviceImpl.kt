package api.migration.addressing.entities

import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import api.migration.addressing.behaviors.ModuleAddressingMigrationSupportingDeviceBehavior
import api.migration.addressing.behaviors.ModuleAddressingMigrationSupportingDeviceBehaviorImpl
import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.migration.original.behaviors.MigrationSupportingDeviceBehaviorImpl
import api.migration.original.entites.ModuleLaunchingDevice
import api.migration.models.MigrationModel
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.application.AppModule
import org.fog.application.Application
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.placement.Controller

class ModuleAddressingMigrationSupportingDeviceImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double, override val migrationModel: MigrationModel, override val addressingType: AddressingDevice.AddressingType) :
        FogDevice(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips),
        ModuleAddressingMigrationSupportingDevice,
        SimEntityBehaviorWrapper<ModuleAddressingMigrationSupportingDevice, ModuleAddressingMigrationSupportingDeviceBehavior<MigrationSupportingDeviceBehavior, AddressingDeviceBehavior<NetworkDeviceBehavior>>> {
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

    override fun toString(): String = asString()

    /* NetworkDevice */
    override val mParentId: Int get() = parentId
    override val mChildrenIds: MutableList<Int> get() = childrenIds
    override val mChildToLatencyMap: MutableMap<Int, Double> get() = childToLatencyMap
    override val mUplinkLatency: Double get() = uplinkLatency
    override val mUplinkBandwidth: Double get() = uplinkBandwidth
    override val mDownlinkBandwidth: Double get() = downlinkBandwidth
    override fun sSendUpFreeLink(tuple: Tuple) = super<FogDevice>.sendUpFreeLink(tuple)
    override fun sendUpFreeLink(tuple: Tuple) = super<ModuleAddressingMigrationSupportingDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<ModuleAddressingMigrationSupportingDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<ModuleAddressingMigrationSupportingDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<ModuleAddressingMigrationSupportingDevice>.sendDown(tuple, childId)

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

    /* ModuleLaunchingDevice */
    override val mAppToModulesMutableMap: MutableMap<String, MutableList<String>> get() = appToModulesMap
    override val mAppModuleList: List<AppModule> get() = getVmList()

    /* MigrationSupportingDevice */
    override val mChildrenModuleLaunchingDevices: List<ModuleLaunchingDevice> get() = childrenIds.map { CloudSim.getEntity(it) as ModuleLaunchingDevice }
    override val mActiveMutableApplications: MutableList<String> get() = activeApplications
    override val mApplicationMutableMap: MutableMap<String, Application> get() = applicationMap

    override val numberOfSuppressedModuleInstances: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    override val tuplesSuppressedWhileModuleMigration: MutableMap<String, MutableMap<String, MutableList<SimEvent>>> = mutableMapOf()

    override val behavior: ModuleAddressingMigrationSupportingDeviceBehavior<MigrationSupportingDeviceBehavior, AddressingDeviceBehavior<NetworkDeviceBehavior>> =
            ModuleAddressingMigrationSupportingDeviceBehaviorImpl(this,
                    MigrationSupportingDeviceBehaviorImpl(this),
                    AddressingDeviceBehaviorImpl(this,
                            NetworkDeviceBehaviorImpl(this)
                    )
            )
}
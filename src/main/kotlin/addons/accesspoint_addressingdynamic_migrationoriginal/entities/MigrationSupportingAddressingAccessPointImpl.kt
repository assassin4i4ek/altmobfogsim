package addons.accesspoint_addressingdynamic_migrationoriginal.entities

import addons.accesspoint_addressingdynamic.behaviors.AddressingAccessPointBehaviorImpl
import addons.accesspoint_addressingdynamic_migrationoriginal.behaviors.MigrationSupportingAddressingAccessPointBehaviorImpl
import api.accesspoint.migration.behaviors.MigrationSupportingAccessPointBehavior
import api.accesspoint.migration.entities.MigrationSupportingAccessPoint
import api.accesspoint.original.behaviors.AccessPointBehavior
import api.accesspoint.utils.AccessPointsMap
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehaviorImpl
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import api.common.positioning.Coordinates
import api.common.positioning.Zone
import api.common.utils.Notification
import api.migration.models.MigrationModel
import api.migration.original.behaviors.MigrationSupportingDeviceBehavior
import api.migration.original.behaviors.MigrationSupportingDeviceBehaviorImpl
import api.migration.original.entites.ModuleLaunchingDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehaviorImpl
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

class MigrationSupportingAddressingAccessPointImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double, override val coordinates: Coordinates, override val connectionZone: Zone,
        override val downlinkLatency: Double, override val accessPointsMap: AccessPointsMap, override val migrationModel: MigrationModel
): FogDevice(name, characteristics, vmAllocationPolicy, storageList,
        schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips),
        MigrationSupportingAddressingAccessPoint,
        SimEntityBehaviorWrapper<MigrationSupportingAccessPoint,
                MigrationSupportingAccessPointBehavior<
                        MigrationSupportingDeviceBehavior,
                        AccessPointBehavior<
                                DynamicAddressingNotificationConsumerDeviceBehavior<
                                        AddressingDeviceBehavior<
                                                NetworkDeviceBehavior>,
                                        NotificationConsumerDeviceBehavior>>>>{
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
    override fun sendUpFreeLink(tuple: Tuple) = super<MigrationSupportingAddressingAccessPoint>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<MigrationSupportingAddressingAccessPoint>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<MigrationSupportingAddressingAccessPoint>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<MigrationSupportingAddressingAccessPoint>.sendDown(tuple, childId)

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingType: AddressingDevice.AddressingType = AddressingDevice.AddressingType.HIERARCHICAL
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

    /* DynamicAddressingTuple */
    override val consumerNotifications: MutableList<Notification<*>> = mutableListOf()

    /* ModuleLaunchingDevice */
    override val mAppToModulesMutableMap: MutableMap<String, MutableList<String>> get() = appToModulesMap
    override val mAppModuleList: MutableList<AppModule> get() = getVmList()
    /* MigrationSupportingDevice */
    override val mChildrenModuleLaunchingDevices: List<ModuleLaunchingDevice> get() = childrenIds.map { CloudSim.getEntity(it) as ModuleLaunchingDevice }
    override val mActiveMutableApplications: MutableList<String> get() = activeApplications
    override val mApplicationMutableMap: MutableMap<String, Application> get() = applicationMap

    override val behavior:
            MigrationSupportingAccessPointBehavior<
                    MigrationSupportingDeviceBehavior,
                    AccessPointBehavior<
                            DynamicAddressingNotificationConsumerDeviceBehavior<
                                    AddressingDeviceBehavior<
                                            NetworkDeviceBehavior>,
                                    NotificationConsumerDeviceBehavior>>> =
            MigrationSupportingAddressingAccessPointBehaviorImpl(this,
                    MigrationSupportingDeviceBehaviorImpl(this),
                    AddressingAccessPointBehaviorImpl(this,
                            DynamicAddressingNotificationConsumerDeviceBehaviorImpl(this,
                                    AddressingDeviceBehaviorImpl(this,
                                            NetworkDeviceBehaviorImpl(this)),
                                    NotificationConsumerDeviceBehaviorImpl(this))))
}
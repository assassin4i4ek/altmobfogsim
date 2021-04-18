package addons.accesspoint_addressingdynamic_migrationoriginal.entities

import addons.accesspoint_addressingdynamic.behaviors.AddressingAccessPointConnectedDeviceBehaviorImpl
import addons.accesspoint_addressingdynamic_migrationoriginal.behaviors.MigrationStimulatorAddressingAccessPointConnectedDeviceBehaviorImpl
import addons.addressing_dynamic.producer.behaviors.DynamicAddressingNotificationProducerDeviceBehaviorImpl
import addons.addressing_dynamic.producer.behaviors.DynamicGatewayConnectionAddressingDeviceBehaviorImpl
import api.accesspoint.migration.behaviors.MigrationStimulatorAccessPointConnectedDeviceBehavior
import api.accesspoint.migration.entities.MigrationStimulatorAccessPointConnectedDevice
import api.accesspoint.original.behaviors.AccessPointConnectedDeviceBehavior
import api.accesspoint.original.entities.AccessPoint
import api.accesspoint.utils.AccessPointsMap
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import api.common.positioning.Position
import api.common.utils.Notification
import api.mobility.behaviors.MobileDeviceBehavior
import api.mobility.behaviors.MobileDeviceBehaviorImpl
import api.mobility.models.MobilityModel
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.placement.Controller
import java.util.*

class MigrationStimulatorAddressingAccessPointConnectedDeviceImpl(
        name: String,
        characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double,
        override var position: Position,
        override val mobilityModel: MobilityModel,
        override val accessPointsMap: AccessPointsMap,
        override val migrationDecisionMakingDeviceId: Int
): FogDevice(
        name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips),
        MigrationStimulatorAddressingAccessPointConnectedDevice,
        SimEntityBehaviorWrapper<MigrationStimulatorAccessPointConnectedDevice,
                MigrationStimulatorAccessPointConnectedDeviceBehavior<
                        AccessPointConnectedDeviceBehavior<
                                NotificationProducerDeviceBehavior<
                                        DynamicGatewayConnectionDeviceBehavior<
                                                AddressingDeviceBehavior<
                                                        NetworkDeviceBehavior>>>,
                                MobileDeviceBehavior>>> {
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
    override fun sendUpFreeLink(tuple: Tuple) = super<MigrationStimulatorAddressingAccessPointConnectedDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<MigrationStimulatorAddressingAccessPointConnectedDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<MigrationStimulatorAddressingAccessPointConnectedDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<MigrationStimulatorAddressingAccessPointConnectedDevice>.sendDown(tuple, childId)

    /* DynamicGatewayConnectionDevice */
    override val mNorthLinkQueue: Queue<Tuple> get() = northTupleQueue

    override var mNorthLinkBusy: Boolean
        get() = isNorthLinkBusy
        set(value) {
            isNorthLinkBusy = value
        }

    override var mDynamicParentId: Int
        get() = parentId
        set(value) {
            parentId = value
            super.onSetParentId()
        }

    /* AccessPointConnectedDevice */
    override var accessPoint: AccessPoint? = null
    override var mDynamicUplinkLatency: Double
        get() = uplinkLatency
        set(value) {
            uplinkLatency = value
        }
    override var mDynamicUplinkBandwidth: Double
        get() = uplinkBandwidth
        set(value) {
            uplinkBandwidth = value
        }

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingType: AddressingDevice.AddressingType = AddressingDevice.AddressingType.HIERARCHICAL
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

    /* NotificationProducer */
    override val producerNotifications: MutableList<Notification<*>> = mutableListOf()

    override val behavior: MigrationStimulatorAccessPointConnectedDeviceBehavior<
            AccessPointConnectedDeviceBehavior<
                    NotificationProducerDeviceBehavior<
                            DynamicGatewayConnectionDeviceBehavior<
                                    AddressingDeviceBehavior<
                                            NetworkDeviceBehavior>>>,
                    MobileDeviceBehavior>>
    = MigrationStimulatorAddressingAccessPointConnectedDeviceBehaviorImpl(this,
            AddressingAccessPointConnectedDeviceBehaviorImpl(this,
                    DynamicAddressingNotificationProducerDeviceBehaviorImpl(this,
                            DynamicGatewayConnectionAddressingDeviceBehaviorImpl(this,
                                    AddressingDeviceBehaviorImpl(this,
                                            NetworkDeviceBehaviorImpl(this)))),
                    MobileDeviceBehaviorImpl(this)))
}
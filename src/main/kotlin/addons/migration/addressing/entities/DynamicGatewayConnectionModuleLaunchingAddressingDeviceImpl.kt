package addons.migration.addressing.entities

import addons.addressing.dynamic.producer.behaviors.DynamicGatewayConnectionAddressingDeviceBehaviorImpl
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import addons.migration.addressing.behaviors.DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehavior
import addons.migration.addressing.behaviors.DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehaviorImpl
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.placement.Controller
import java.util.*

class DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double, override val addressingType: AddressingDevice.AddressingType)
    : FogDevice(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips),
        DynamicGatewayConnectionModuleLaunchingAddressingDevice,
        SimEntityBehaviorWrapper<DynamicGatewayConnectionModuleLaunchingAddressingDevice, DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>>>
{
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

    /* NetworkDevice */
    override val mParentId: Int get() = parentId
    override val mChildrenIds: MutableList<Int> get() = childrenIds
    override val mChildToLatencyMap: MutableMap<Int, Double> get() = childToLatencyMap
    override val mUplinkLatency: Double get() = uplinkLatency
    override val mUplinkBandwidth: Double get() = uplinkBandwidth
    override val mDownlinkBandwidth: Double get() = downlinkBandwidth
    override fun sSendUpFreeLink(tuple: Tuple) = super<FogDevice>.sendUpFreeLink(tuple)
    override fun sendUpFreeLink(tuple: Tuple) = super<DynamicGatewayConnectionModuleLaunchingAddressingDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<DynamicGatewayConnectionModuleLaunchingAddressingDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<DynamicGatewayConnectionModuleLaunchingAddressingDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<DynamicGatewayConnectionModuleLaunchingAddressingDevice>.sendDown(tuple, childId)

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

    /* ModuleLaunchingDevice */
    override val mAppModuleList: List<AppModule> get() = getVmList()
    override val mAppToModulesMap: Map<String, List<String>> get() = appToModulesMap

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

    override val behavior: DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<AddressingDeviceBehavior<NetworkDeviceBehavior>>> =
            DynamicGatewayConnectionModuleLaunchingAddressingDeviceBehaviorImpl(this,
                    DynamicGatewayConnectionAddressingDeviceBehaviorImpl(this,
                            AddressingDeviceBehaviorImpl(this,
                                    NetworkDeviceBehaviorImpl(this))))
}
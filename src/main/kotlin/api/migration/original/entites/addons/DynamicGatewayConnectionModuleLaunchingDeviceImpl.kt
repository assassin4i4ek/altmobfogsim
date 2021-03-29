package api.migration.original.entites.addons

import api.common.entities.SimEntityBehaviorWrapper
import api.migration.original.behaviors.addons.DynamicGatewayConnectionModuleLaunchingDeviceBehavior
import api.migration.original.behaviors.addons.DynamicGatewayConnectionModuleLaunchingDeviceBehaviorImpl
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehaviorImpl
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import java.util.*

class DynamicGatewayConnectionModuleLaunchingDeviceImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double)
    : FogDevice(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips), DynamicGatewayConnectionModuleLaunchingDevice,
        SimEntityBehaviorWrapper<DynamicGatewayConnectionModuleLaunchingDevice, DynamicGatewayConnectionModuleLaunchingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>>>
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
    override fun sendUpFreeLink(tuple: Tuple) = super<DynamicGatewayConnectionModuleLaunchingDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<DynamicGatewayConnectionModuleLaunchingDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<DynamicGatewayConnectionModuleLaunchingDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<DynamicGatewayConnectionModuleLaunchingDevice>.sendDown(tuple, childId)

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

    override val behavior: DynamicGatewayConnectionModuleLaunchingDeviceBehavior<DynamicGatewayConnectionDeviceBehavior<NetworkDeviceBehavior>> =
            DynamicGatewayConnectionModuleLaunchingDeviceBehaviorImpl(this,
                    DynamicGatewayConnectionDeviceBehaviorImpl(this,
                            NetworkDeviceBehaviorImpl(this)))
}
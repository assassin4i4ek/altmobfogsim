package api.accesspoint.original.entities

import api.accesspoint.original.behaviors.AccessPointBehavior
import api.accesspoint.original.behaviors.AccessPointBehaviorImpl
import api.accesspoint.original.utils.AccessPointsMap
import api.common.entities.SimEntityBehaviorWrapper
import api.common.positioning.Coordinates
import api.common.positioning.Zone
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple

class AccessPointImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double, override val coordinates: Coordinates, override val connectionZone: Zone,
        override val downlinkLatency: Double, override val accessPointsMap: AccessPointsMap,
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList,
    schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips),
        AccessPoint,
        SimEntityBehaviorWrapper<AccessPoint, AccessPointBehavior<NetworkDeviceBehavior>> {
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
    override fun sendUpFreeLink(tuple: Tuple) = super<AccessPoint>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<AccessPoint>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<AccessPoint>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<AccessPoint>.sendDown(tuple, childId)

    override val behavior: AccessPointBehavior<NetworkDeviceBehavior> =
            AccessPointBehaviorImpl(this,
                    NetworkDeviceBehaviorImpl(this)
            )
}
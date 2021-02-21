package api.accesspoint.entities

import api.accesspoint.behaviors.AccessPointBehavior
import api.accesspoint.behaviors.AccessPointBehaviorImpl
import api.accesspoint.behaviors.AccessPointConnectedDeviceBehavior
import api.common.entities.SimEntityBehaviorWrapper
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Zone
import api.network.behaviors.NetworkDeviceBehavior
import api.network.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.Tuple
import org.fog.policy.AppModuleAllocationPolicy

class AccessPointImpl(
        name: String, override val coordinates: Coordinates, override val connectionZone: Zone,
        override val accessPointsMap: AccessPointsMap, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double,
): FogDevice(
    name, accessPointsMap.accessPointCharacteristics(), AppModuleAllocationPolicy(emptyList()), emptyList(),
    0.0, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, 0.0),
        AccessPoint,
        SimEntityBehaviorWrapper<AccessPoint, AccessPointBehavior<NetworkDeviceBehavior>> {
    /* SimEntityInterface */
    override val mId: Int get() = id
    override val mName: String get() = name
    override fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?) = send(id, delay, tag, data)
    override fun startEntity() {
        super<FogDevice>.startEntity()
        super<SimEntityBehaviorWrapper>.startEntity()
    }

    override fun processOtherEvent(ev: SimEvent) {
        if (super.onProcessEvent(ev)) {
            super<FogDevice>.processOtherEvent(ev)
        }
    }

    /* NetworkDevice */
    override val mParentId: Int get() = parentId
    override val mChildrenIds: MutableList<Int> get() = childrenIds
    override val mChildToLatencyMap: MutableMap<Int, Double> get() = childToLatencyMap
    override val mUplinkLatency: Double get() = uplinkLatency
    override val mUplinkBandwidth: Double get() = uplinkBandwidth
    override val mDownlinkBandwidth: Double get() = downlinkBandwidth
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<AccessPoint>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) =  super<AccessPoint>.sendDown(tuple, childId)

    override val behavior: AccessPointBehavior<NetworkDeviceBehavior> =
            AccessPointBehaviorImpl(this,
                    NetworkDeviceBehaviorImpl(this)
            )
}
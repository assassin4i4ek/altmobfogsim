package api.accesspoint.original.entities

import api.accesspoint.original.behaviors.AccessPointBehavior
import api.accesspoint.original.behaviors.AccessPointBehaviorImpl
import api.common.entities.SimEntityBehaviorWrapper
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Zone
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.power.models.PowerModel
import org.fog.entities.FogDevice
import org.fog.entities.Tuple
import org.fog.policy.AppModuleAllocationPolicy

class AccessPointImpl(
        name: String, override val coordinates: Coordinates, override val connectionZone: Zone,
        override val accessPointsMap: AccessPointsMap, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, powerModel: PowerModel
): FogDevice(
    name, accessPointsMap.accessPointCharacteristics(powerModel), AppModuleAllocationPolicy(emptyList()), emptyList(),
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
    override fun sSendUpFreeLink(tuple: Tuple) = super<FogDevice>.sendUpFreeLink(tuple)
    override fun sendUpFreeLink(tuple: Tuple) = super<AccessPoint>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<AccessPoint>.sendDownFreeLink(tuple, childId)

    override val behavior: AccessPointBehavior<NetworkDeviceBehavior> =
            AccessPointBehaviorImpl(this,
                    NetworkDeviceBehaviorImpl(this)
            )
}
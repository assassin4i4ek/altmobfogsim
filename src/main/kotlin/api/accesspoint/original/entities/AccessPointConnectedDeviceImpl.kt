package api.accesspoint.original.entities

import api.accesspoint.original.behaviors.AccessPointConnectedDeviceBehavior
import api.accesspoint.original.behaviors.AccessPointConnectedDeviceBehaviorImpl
import api.accesspoint.original.utils.AccessPointsMap
import api.common.entities.SimEntityBehaviorWrapper
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehaviorImpl
import api.mobility.behaviors.MobileDeviceBehavior
import api.mobility.behaviors.MobileDeviceBehaviorImpl
import api.mobility.models.MobilityModel
import api.mobility.positioning.Position
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import java.util.*

class AccessPointConnectedDeviceImpl(
        name: String,
        characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double,
        override var position: Position,
        override val mobilityModel: MobilityModel,
        override val accessPointsMap: AccessPointsMap,
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips),
        AccessPointConnectedDevice,
        SimEntityBehaviorWrapper<AccessPointConnectedDevice,
                AccessPointConnectedDeviceBehavior<
                        DynamicGatewayConnectionDeviceBehavior<
                                NetworkDeviceBehavior>,
                        MobileDeviceBehavior>
                > {
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
    override fun sendUpFreeLink(tuple: Tuple) = super<AccessPointConnectedDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<AccessPointConnectedDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<AccessPointConnectedDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<AccessPointConnectedDevice>.sendDown(tuple, childId)

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

    override val behavior: AccessPointConnectedDeviceBehavior<
            DynamicGatewayConnectionDeviceBehavior<
                    NetworkDeviceBehavior>,
            MobileDeviceBehavior> =
        AccessPointConnectedDeviceBehaviorImpl(this,
                DynamicGatewayConnectionDeviceBehaviorImpl(this,
                        NetworkDeviceBehaviorImpl(this)
                ),
                MobileDeviceBehaviorImpl(this)
        )
}
package api.accesspoint.entities

import api.accesspoint.behaviors.AccessPointConnectedDeviceBehavior
import api.accesspoint.behaviors.AccessPointConnectedDeviceBehaviorImpl
import api.common.entities.SimEntityBehaviorWrapper
import api.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehaviorImpl
import api.dynamic.mobility.behaviors.MobileDeviceBehavior
import api.dynamic.mobility.behaviors.MobileDeviceBehaviorImpl
import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.positioning.Position
import api.network.behaviors.NetworkDeviceBehavior
import api.network.behaviors.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import java.util.*

class AccessPointConnectedDeviceImpl(
        name: String,
        override var position: Position,
        override val mobilityModel: MobilityModel,
        override val accessPointsMap: AccessPointsMap,
        characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double
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
    override fun sendUp(tuple: Tuple) = super<AccessPointConnectedDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) =
        super<AccessPointConnectedDevice>.sendDown(tuple, childId)

    /* DynamicConnectionDevice */
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

    /* AccessPoint*/
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
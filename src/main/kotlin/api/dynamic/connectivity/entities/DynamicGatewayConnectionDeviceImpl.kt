package api.dynamic.connectivity.entities

import api.dynamic.connectivity.behavior.DynamicGatewayConnectionBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import java.util.*

class DynamicGatewayConnectionDeviceImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips), DynamicGatewayConnectionDevice {
    /* OriginalFogDevice */
    override val mId: Int get() = id
    override val mName: String get() = name
    override fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?) = send(id, delay, tag, data)

    /* NetworkDevice */
    override val mParentId: Int get() = parentId
    override val mChildrenIds: MutableList<Int> get() = childrenIds
    override val mChildToLatencyMap: MutableMap<Int, Double> get() = childToLatencyMap
    override val mUplinkLatency: Double get() = uplinkLatency
    override fun mSendUp(tuple: Tuple) = super.sendUp(tuple)
    override fun mSendDown(tuple: Tuple, recipientId: Int) = super.sendDown(tuple, recipientId)

    override fun sendUp(tuple: Tuple) {
        behavior.onSendUp(tuple)
    }

    override fun sendDown(tuple: Tuple, recipientId: Int) {
        behavior.onSendDown(tuple, recipientId)
    }

    /* DynamicGatewayConnectionDevice */
    override var mNorthLinkBusy: Boolean
        get() = isNorthLinkBusy
        set(value) {
            isNorthLinkBusy = value
        }

    override val mNorthLinkQueue: Queue<Tuple> get() = northTupleQueue

    private var behavior = DynamicGatewayConnectionBehaviorImpl()

    override fun startEntity() {
        super.startEntity()
        behavior.onStartEntity(this)
    }

    override fun processOtherEvent(ev: SimEvent) {
        if (behavior.onProcessEvent(ev)) {
            super.processOtherEvent(ev)
        }
    }

    override var mDynamicParentId: Int
        get() = parentId
        set(value) {
            behavior.onSetParentId(parentId, value)
            parentId = value
        }
}
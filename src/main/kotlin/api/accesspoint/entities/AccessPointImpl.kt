package api.accesspoint.entities

import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.RadialZone
import api.dynamic.mobility.positioning.Zone
import api.network.behavior.NetworkDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple

class AccessPointImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double, override val coordinates: Coordinates, radius: Double
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips), api.accesspoint.entities.AccessPoint {
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

    private val behavior = NetworkDeviceBehaviorImpl()

    override val connectionZone: Zone = RadialZone(coordinates, radius)

    override fun startEntity() {
        super.startEntity()
        behavior.onStartEntity(this)
    }

    override fun processOtherEvent(ev: SimEvent) {
        if (behavior.onProcessEvent(ev)) {
            super.processOtherEvent(ev)
        }
    }
}
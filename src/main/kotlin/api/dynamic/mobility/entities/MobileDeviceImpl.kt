package api.dynamic.mobility.entities

import api.dynamic.mobility.behavior.MobileDeviceBehaviorImpl
import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.positioning.PositionAndTimestamp
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics

class MobileDeviceImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double,
    initPositionAndTimestamp: PositionAndTimestamp,
    override val mobilityModel: MobilityModel
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips), MobileDevice {
    /* OriginalFogDevice */
    override val mId: Int get() = id
    override val mName: String get() = name
    override fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?) = send(id, delay, tag, data)

    private val behavior = MobileDeviceBehaviorImpl()

    override fun startEntity() {
        super.startEntity()
        behavior.onStartEntity(this)
    }

    override fun processOtherEvent(ev: SimEvent) {
        if (behavior.onProcessEvent(ev)) {
            super.processOtherEvent(ev)
        }
    }

    override var positionAndTimestamp: PositionAndTimestamp = initPositionAndTimestamp
}
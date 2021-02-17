package api.original.entities

import api.original.behavior.OriginalFogDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics

class OriginalFogDeviceImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips), OriginalFogDevice {
    /* OriginalFogDevice */
    override val mId: Int get() = id
    override val mName: String get() = name
    override fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?) = send(id, delay, tag, data)

    private val behavior = OriginalFogDeviceBehaviorImpl()

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
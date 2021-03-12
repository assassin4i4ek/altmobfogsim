package api.original.entities

import api.common.entities.SimEntityBehaviorWrapper
import api.original.behaviors.OriginalFogDeviceBehavior
import api.original.behaviors.OriginalFogDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics

class OriginalFogDeviceImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips), OriginalFogDevice,
    SimEntityBehaviorWrapper<OriginalFogDevice, OriginalFogDeviceBehavior> {
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
            super<FogDevice>.processOtherEvent(ev)
        }
    }

    override val behavior: OriginalFogDeviceBehavior = OriginalFogDeviceBehaviorImpl(this)
}
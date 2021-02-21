package api.addressing.entities

import api.addressing.behaviors.AddressingDeviceBehavior
import api.addressing.behaviors.AddressingDeviceBehaviorImpl
import api.common.entities.SimEntityBehaviorWrapper
import api.dynamic.connection.behaviors.DynamicGatewayConnectionDeviceBehaviorImpl
import api.dynamic.mobility.behaviors.MobileDeviceBehaviorImpl
import api.network.behaviors.NetworkDeviceBehavior
import api.network.behaviors.NetworkDeviceBehaviorImpl
import api.network.entities.NetworkDevice
import api.original.behaviors.OriginalFogDeviceBehaviorImpl
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.placement.Controller

class AddressingDeviceImpl(
    name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
    storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
    uplinkLatency: Double, ratePerMips: Double
): FogDevice(
    name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
    uplinkLatency, ratePerMips),
        AddressingDevice,
        SimEntityBehaviorWrapper<AddressingDevice, AddressingDeviceBehavior<NetworkDeviceBehavior>> {
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
    override fun sendUp(tuple: Tuple) = super<AddressingDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) =  super<AddressingDevice>.sendDown(tuple, childId)

    /* AddressingDevice */
    override val controller: Controller get() = (CloudSim.getEntity(controllerId) as Controller)

    override val behavior: AddressingDeviceBehavior<NetworkDeviceBehavior> =
            AddressingDeviceBehaviorImpl(this,
                    NetworkDeviceBehaviorImpl(this)
            )

    override fun toString(): String {
        return "$name ($id)"
    }
}
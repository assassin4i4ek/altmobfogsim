package api.addressing.dynamic.producer.entities

import api.addressing.dynamic.producer.behaviors.DynamicAddressingNotificationProducerDeviceBehaviorImpl
import api.addressing.dynamic.producer.behaviors.DynamicGatewayConnectionAddressingDeviceBehaviorImpl
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import api.common.utils.Notification
import api.network.dynamic.behaviors.DynamicGatewayConnectionDeviceBehavior
import api.network.dynamic.entites.DynamicGatewayConnectionDevice
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.notification.producer.behaviors.NotificationProducerDeviceBehavior
import api.notification.producer.entities.NotificationProducerDevice
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.placement.Controller
import java.util.*

class DynamicAddressingNotificationProducerDeviceImpl(
        name: String, characteristics: FogDeviceCharacteristics, vmAllocationPolicy: VmAllocationPolicy,
        storageList: List<Storage>, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
        uplinkLatency: Double, ratePerMips: Double
): FogDevice(
        name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, ratePerMips),
        DynamicAddressingNotificationProducerDevice,
        SimEntityBehaviorWrapper<NotificationProducerDevice,
                NotificationProducerDeviceBehavior<
                        DynamicGatewayConnectionDeviceBehavior<
                                AddressingDeviceBehavior<
                                        NetworkDeviceBehavior>>>> {
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

    /* NetworkDevice */
    override val mParentId: Int get() = parentId
    override val mChildrenIds: MutableList<Int> get() = childrenIds
    override val mChildToLatencyMap: MutableMap<Int, Double> get() = childToLatencyMap
    override val mUplinkLatency: Double get() = uplinkLatency
    override val mUplinkBandwidth: Double get() = uplinkBandwidth
    override val mDownlinkBandwidth: Double get() = downlinkBandwidth
    override fun sSendUpFreeLink(tuple: Tuple) = super<FogDevice>.sendUpFreeLink(tuple)
    override fun sendUpFreeLink(tuple: Tuple) = super<DynamicAddressingNotificationProducerDevice>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<DynamicAddressingNotificationProducerDevice>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<DynamicAddressingNotificationProducerDevice>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<DynamicAddressingNotificationProducerDevice>.sendDown(tuple, childId)

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

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

    /* NotificationProducer */
    override val producerNotifications: MutableList<Notification<*>> = mutableListOf()

    override val behavior =
            DynamicAddressingNotificationProducerDeviceBehaviorImpl(this,
                    DynamicGatewayConnectionAddressingDeviceBehaviorImpl(this,
                             AddressingDeviceBehaviorImpl(this,
                                     NetworkDeviceBehaviorImpl(this)
                             )
                    )
            )
}
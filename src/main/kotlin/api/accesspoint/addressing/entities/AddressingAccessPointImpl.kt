package api.accesspoint.addressing.entities

import api.accesspoint.addressing.behaviors.AddressingAccessPointBehaviorImpl
import api.accesspoint.original.behaviors.AccessPointBehavior
import api.accesspoint.original.entities.AccessPoint
import api.accesspoint.original.entities.AccessPointsMap
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehavior
import api.addressing.dynamic.consumer.behaviors.DynamicAddressingNotificationConsumerDeviceBehaviorImpl
import api.addressing.fixed.behaviors.AddressingDeviceBehavior
import api.addressing.fixed.behaviors.AddressingDeviceBehaviorImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import api.addressing.models.BreadthFirstSearchAddressingModel
import api.common.entities.SimEntityBehaviorWrapper
import api.common.utils.Notification
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Zone
import api.network.fixed.behaviors.NetworkDeviceBehavior
import api.network.fixed.behaviors.NetworkDeviceBehaviorImpl
import api.network.fixed.entities.NetworkDevice
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehavior
import api.notification.consumer.behaviors.NotificationConsumerDeviceBehaviorImpl
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.core.predicates.Predicate
import org.cloudbus.cloudsim.power.models.PowerModel
import org.fog.entities.FogDevice
import org.fog.entities.Tuple
import org.fog.placement.Controller
import org.fog.policy.AppModuleAllocationPolicy

class AddressingAccessPointImpl(
        name: String, uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
        powerModel: PowerModel, override val coordinates: Coordinates, override val connectionZone: Zone,
        override val accessPointsMap: AccessPointsMap,
): FogDevice(
        name, accessPointsMap.accessPointCharacteristics(powerModel), AppModuleAllocationPolicy(emptyList()), emptyList(),
        0.0, uplinkBandwidth, downlinkBandwidth,
        uplinkLatency, 0.0), AddressingAccessPoint,
        SimEntityBehaviorWrapper<AccessPoint,
                        AccessPointBehavior<
                                DynamicAddressingNotificationConsumerDeviceBehavior<
                                        AddressingDeviceBehavior<
                                                NetworkDeviceBehavior>,
                                        NotificationConsumerDeviceBehavior<
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
    override fun sendUpFreeLink(tuple: Tuple) = super<AddressingAccessPoint>.sendUpFreeLink(tuple)
    override fun sSendDownFreeLink(tuple: Tuple, childId: Int) = super<FogDevice>.sendDownFreeLink(tuple, childId)
    override fun sendDownFreeLink(tuple: Tuple, childId: Int) =  super<AddressingAccessPoint>.sendDownFreeLink(tuple, childId)
    override fun sSendUp(tuple: Tuple) = super<FogDevice>.sendUp(tuple)
    override fun sendUp(tuple: Tuple) = super<AddressingAccessPoint>.sendUp(tuple)
    override fun sSendDown(tuple: Tuple, childId: Int) = super<FogDevice>.sendDown(tuple, childId)
    override fun sendDown(tuple: Tuple, childId: Int) = super<AddressingAccessPoint>.sendDown(tuple, childId)

    /* AddressingDevice */
    override val controller: Controller get() = CloudSim.getEntity(controllerId) as Controller
    override val addressingModel: AddressingModel = BreadthFirstSearchAddressingModel()
    override val addressingType: AddressingDevice.AddressingType = AddressingDevice.AddressingType.HIERARCHICAL
    override val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>> = mutableMapOf()

    /* DynamicAddressingTuple */
    override val consumerNotifications: MutableList<Notification<*>> = mutableListOf()
    override val behavior: AccessPointBehavior<
            DynamicAddressingNotificationConsumerDeviceBehavior<
                    AddressingDeviceBehavior<
                            NetworkDeviceBehavior>,
                    NotificationConsumerDeviceBehavior<
                            NetworkDeviceBehavior>>>
        = NetworkDeviceBehaviorImpl(this).let { networkDeviceBehavior ->
        AddressingAccessPointBehaviorImpl(this,
                DynamicAddressingNotificationConsumerDeviceBehaviorImpl(this,
                        AddressingDeviceBehaviorImpl(this,
                                networkDeviceBehavior),
                        NotificationConsumerDeviceBehaviorImpl(this,
                                networkDeviceBehavior)))
    }
}
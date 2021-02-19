package api2.network.entities

import api2.common.Events
import api2.common.behaviors.BaseBehavior
import api2.common.entities.SimEntity
import api2.common.entities.SimEntityBehaviorWrapper
import api2.network.behaviors.NetworkDeviceBehavior
import org.fog.entities.Tuple

interface NetworkDevice : SimEntity {
    val mParentId: Int
    val mChildrenIds: MutableList<Int>
    val mChildToLatencyMap: MutableMap<Int, Double>
    val mUplinkLatency: Double
    fun sSendUp(tuple: Tuple)
    fun sendUp(tuple: Tuple) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, Pair(tuple, mParentId))
    }
    fun sSendDown(tuple: Tuple, childId: Int)
    fun sendDown(tuple: Tuple, childId: Int) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, Pair(tuple, childId))
    }
}
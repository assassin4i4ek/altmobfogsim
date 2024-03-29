package api.network.fixed.entities

import api.common.Events
import api.common.entities.SimEntity
import api.common.utils.TupleRecipientPair
import org.cloudbus.cloudsim.core.predicates.PredicateType
import org.fog.entities.Tuple

interface NetworkDevice : SimEntity {
    val mParentId: Int
    val mChildrenIds: MutableList<Int>
    val mChildToLatencyMap: MutableMap<Int, Double>
    val mUplinkLatency: Double
    val mUplinkBandwidth: Double
    val mDownlinkBandwidth: Double
    fun sSendUpFreeLink(tuple: Tuple)
    fun sendUpFreeLink(tuple: Tuple) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag, TupleRecipientPair(tuple, mParentId))
        mWaitForEvent(PredicateType(Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag))
    }
    fun sSendDownFreeLink(tuple: Tuple, childId: Int)
    fun sendDownFreeLink(tuple: Tuple, childId: Int) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag, TupleRecipientPair(tuple, childId))
        mWaitForEvent(PredicateType(Events.NETWORK_DEVICE_ADDRESS_TUPLE_FREE_LINK.tag))
    }
    fun sSendUp(tuple: Tuple)
    fun sendUp(tuple: Tuple) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, TupleRecipientPair(tuple, mParentId))
//        mWaitForEvent(PredicateType(Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag))
    }
    fun sSendDown(tuple: Tuple, childId: Int)
    fun sendDown(tuple: Tuple, childId: Int) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, TupleRecipientPair(tuple, childId))
//        mWaitForEvent(PredicateType(Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag))
    }
}
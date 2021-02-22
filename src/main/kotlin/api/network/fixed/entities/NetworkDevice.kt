package api.network.fixed.entities

import api.common.Events
import api.common.entities.SimEntity
import api.common.utils.TupleRecipientPair
import org.fog.entities.Tuple

interface NetworkDevice : SimEntity {
    val mParentId: Int
    val mChildrenIds: MutableList<Int>
    val mChildToLatencyMap: MutableMap<Int, Double>
    val mUplinkLatency: Double
    val mUplinkBandwidth: Double
    val mDownlinkBandwidth: Double
    fun sSendUp(tuple: Tuple)
    fun sendUp(tuple: Tuple) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, TupleRecipientPair(tuple, mParentId))
    }
    fun sSendDown(tuple: Tuple, childId: Int)
    fun sendDown(tuple: Tuple, childId: Int) {
        mSendEvent(mId, 0.0, Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag, TupleRecipientPair(tuple, childId))
    }
}
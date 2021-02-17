package api.network.entities

import api.original.entities.OriginalFogDevice
import org.fog.entities.Tuple

interface NetworkDevice: OriginalFogDevice {
    val mParentId: Int
    val mChildrenIds: MutableList<Int>
    val mChildToLatencyMap: MutableMap<Int, Double>
    val mUplinkLatency: Double

    fun mSendUp(tuple: Tuple)
    fun mSendDown(tuple: Tuple, recipientId: Int)

    fun sendUp(tuple: Tuple)
    fun sendDown(tuple: Tuple, recipientId: Int)
}
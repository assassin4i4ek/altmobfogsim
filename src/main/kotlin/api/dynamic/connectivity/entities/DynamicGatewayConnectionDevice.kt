package api.dynamic.connectivity.entities

import api.network.entities.NetworkDevice
import org.fog.entities.Tuple
import java.util.*

interface DynamicGatewayConnectionDevice: NetworkDevice {
    var mNorthLinkBusy: Boolean
    val mNorthLinkQueue: Queue<Tuple>
    var mDynamicParentId: Int
    override val mParentId: Int get() = mDynamicParentId

//    fun setParentId(parentId: Int)
}
package api2.dynamic.connection.entites

import api2.common.Events
import api2.network.entities.NetworkDevice
import org.fog.entities.Tuple
import java.util.*

interface DynamicGatewayConnectionDevice : NetworkDevice {
    var mNorthLinkBusy: Boolean
    val mNorthLinkQueue: Queue<Tuple>
    var mDynamicParentId: Int
    override val mParentId: Int get() = mDynamicParentId
    fun onSetParentId() {
        mSendEvent(mId, 0.0, Events.DYNAMIC_GATEWAY_CONNECTION_DEVICE_NEW_PARENT_ID.tag, null)
    }
}
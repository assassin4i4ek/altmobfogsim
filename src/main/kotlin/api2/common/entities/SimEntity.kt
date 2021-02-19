package api2.common.entities

import org.cloudbus.cloudsim.core.SimEvent

interface SimEntity {
    val mId: Int
    val mName: String
    fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?)
//    fun startEntity()
//    fun processOtherEvent(ev: SimEvent)
//    fun onProcessEvent(ev: SimEvent): Boolean
}
package api.common.entities

import org.cloudbus.cloudsim.core.predicates.Predicate

interface SimEntity {
    val mId: Int
    val mName: String
    fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?)
    fun mWaitForEvent(p: Predicate)

    fun asString(): String {
        return "$mName ($mId)"
    }
//    fun startEntity()
//    fun processOtherEvent(ev: SimEvent)
//    fun onProcessEvent(ev: SimEvent): Boolean
}
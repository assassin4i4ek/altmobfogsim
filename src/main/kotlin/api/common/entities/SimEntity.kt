package api.common.entities

interface SimEntity {
    val mId: Int
    val mName: String
    fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?)
//    fun startEntity()
//    fun processOtherEvent(ev: SimEvent)
//    fun onProcessEvent(ev: SimEvent): Boolean
}
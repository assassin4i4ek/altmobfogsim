package api.original.entities

interface OriginalFogDevice {
    val mId: Int
    val mName: String
    fun mSendEvent(id: Int, delay: Double, tag: Int, data: Any?)
}
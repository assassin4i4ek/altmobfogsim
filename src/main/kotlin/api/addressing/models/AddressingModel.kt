package api.addressing.models

import api.addressing.fixed.entities.AddressingDevice

interface AddressingModel {
    fun idOfNextHopTo(src: AddressingDevice, dst: List<Int>, fileSize: Long): Int

    fun deliveryTimeTo(from: AddressingDevice, to: AddressingDevice, fileSize: Long): Double {
        if (from.mParentId == to.mId) {
            return from.mUplinkLatency + fileSize / from.mUplinkBandwidth
        }
        else if (from.mChildToLatencyMap.containsKey(to.mId)) {
            return from.mChildToLatencyMap[to.mId]!! + fileSize / from.mDownlinkBandwidth
        }
        throw Exception("Devices ${from.mName} and ${to.mName} are not connected")
    }
}
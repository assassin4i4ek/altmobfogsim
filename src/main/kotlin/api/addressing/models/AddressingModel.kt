package api.addressing.models

import api.addressing.fixed.entities.AddressingDevice

interface AddressingModel {
    enum class Quantifier {
        SINGLE, //tuple.direction == Tuple.DOWN and tuple is addressed to specific module
        ALL, //tuple.direction == Tuple.DOWN and tuple is addressed to all modules below
        ANY //tuple.direction == Tuple.UP and tuple is addressed to closest module above
    }

    fun filterInChildren(src: AddressingDevice, devices: List<Int>): List<Int>

    fun idsOfNextHopTo(src: AddressingDevice, targetDeviceIds: List<Int>, quantifier: Quantifier, fileSize: Long): Map<Int, Int>

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
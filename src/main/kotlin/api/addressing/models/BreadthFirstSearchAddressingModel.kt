package api.addressing.models

import api.addressing.fixed.entities.AddressingDevice

class BreadthFirstSearchAddressingModel(): AddressingModel {
    override fun idOfNextHopTo(src: AddressingDevice, dst: List<Int>, fileSize: Long): Int {
        val devicePathsAndTimings = mutableMapOf<Int, Pair<Array<Int>, Double>>()
        devicePathsAndTimings[src.mId] = Pair(emptyArray(), 0.0)
        var devicesInGraph: MutableList<AddressingDevice>
        var childDevicesToAppend = mutableListOf<AddressingDevice>()
        childDevicesToAppend.add(src)
        var lastParentDeviceId: Int? = null

        while (childDevicesToAppend.isNotEmpty()) {
            devicesInGraph = childDevicesToAppend
            childDevicesToAppend = mutableListOf()

            for (parentDevice in devicesInGraph) {
                val pathAndTimeToParent = devicePathsAndTimings[parentDevice.mId]!!
                val (pathToParent, deliveryTimeToParent) = pathAndTimeToParent

                for (childDevice in parentDevice.connectedDevices.filter { it.mId != parentDevice.mId }) {
                    val deliveryTimeToChild = deliveryTimeToParent + super.deliveryTimeTo(parentDevice, childDevice, fileSize)
                    if (!devicePathsAndTimings.containsKey(childDevice.mId)) {
                        childDevicesToAppend.add(childDevice)
                        devicePathsAndTimings[childDevice.mId] = Pair(
                                if (lastParentDeviceId != null) pathToParent.plus(parentDevice.mId)
                                else pathToParent,
                                deliveryTimeToChild
                        )
                    } else if (deliveryTimeToChild < devicePathsAndTimings[childDevice.mId]!!.second) {
                        //if quicker way found
                        childDevicesToAppend.add(childDevice)
                        devicePathsAndTimings[childDevice.mId] = Pair(
                                if (lastParentDeviceId != null) pathToParent.plus(parentDevice.mId)
                                else pathToParent,
                                deliveryTimeToChild
                        )
                    }
                }

                lastParentDeviceId = parentDevice.mId
            }
        }

        val targetDeviceId = dst.minByOrNull { dstId -> devicePathsAndTimings[dstId]?.second ?: Double.POSITIVE_INFINITY }!!

        return devicePathsAndTimings[targetDeviceId]?.run {
            first.firstOrNull() ?: targetDeviceId
        } ?: -1
    }
}
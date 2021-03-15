package api.addressing.models

import api.addressing.fixed.entities.AddressingDevice
import org.cloudbus.cloudsim.core.CloudSim

class BreadthFirstSearchAddressingModel(): AddressingModel {
    override fun idsOfNextHopTo(src: AddressingDevice, targetDeviceIds: List<Int>,
                                quantifier: AddressingModel.Quantifier, fileSize: Long): Map<Int, Int> {
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
        val targetNextHopMap = mutableMapOf<Int, Int>()

        when (quantifier) {
            AddressingModel.Quantifier.ANY -> {
                val minLatencyTargetDeviceId = targetDeviceIds.minByOrNull { dstId ->
                    devicePathsAndTimings[dstId]?.second ?: Double.POSITIVE_INFINITY
                }!!
                val minLatencyTargetDeviceNextHopId = devicePathsAndTimings[minLatencyTargetDeviceId]?.run {
                    first.firstOrNull() ?: minLatencyTargetDeviceId
                } ?: -1
                targetNextHopMap[minLatencyTargetDeviceId] = minLatencyTargetDeviceNextHopId
            }
            AddressingModel.Quantifier.ALL, AddressingModel.Quantifier.SINGLE -> {
                    targetDeviceIds.forEach { targetDeviceId ->
                    val targetDeviceNextHop = devicePathsAndTimings[targetDeviceId]?.run {
                        first.firstOrNull() ?: targetDeviceId
                    } ?: -1
                    targetNextHopMap[targetDeviceId] = targetDeviceNextHop
                }
            }
        }

        return targetNextHopMap
    }

    override fun filterInChildren(src: AddressingDevice, devices: List<Int>): List<Int> {
        when (src.addressingType) {
            AddressingDevice.AddressingType.HIERARCHICAL -> {
                val filteredDevices = mutableListOf<Int>()
                var devicesInTree: MutableList<Int>
                var childrenToAppend = mutableListOf<Int>()
                childrenToAppend.addAll(src.mChildrenIds)
                childrenToAppend.remove(src.mId)
                while (childrenToAppend.isNotEmpty()) {
                    devicesInTree = childrenToAppend
                    childrenToAppend = mutableListOf()

                    for (childId in devicesInTree) {
                        if (devices.contains(childId)) {
                            filteredDevices.add(childId)
                        }
                        childrenToAppend.addAll((CloudSim.getEntity(childId) as AddressingDevice).mChildrenIds)
                        childrenToAppend.remove(childId)
                    }
                }
                return filteredDevices
            }
            AddressingDevice.AddressingType.PEER_TO_PEER -> {
                TODO("Broadcasting down-tuples are not yet supported for peer-to-peer devices")
            }
        }
    }
}
package api.addressing.behaviors

import api.addressing.entities.AddressingDevice
import api.common.Events
import api.common.behaviors.BaseBehavior
import api.common.utils.TupleRecipientPair
import api.network.behaviors.NetworkDeviceBehavior
import api.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Tuple

interface AddressingDeviceBehavior<T: BaseBehavior<T, out NetworkDevice>>
    : BaseBehavior<AddressingDeviceBehavior<T>, AddressingDevice> {
    val superNetworkDeviceBehavior: NetworkDeviceBehavior

    override fun onStart() {
        superNetworkDeviceBehavior.onStart()
        device.mChildrenIds.add(device.mId)
    }

    override fun processEvent(ev: SimEvent): Boolean {
        return when (ev.tag) {
            Events.NETWORK_DEVICE_ADDRESS_TUPLE.tag -> onAddressTuple(ev)
            else -> superNetworkDeviceBehavior.processEvent(ev)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onAddressTuple(ev: SimEvent): Boolean {
        val (tuple: Tuple, recipientId: Int) = ev.data as TupleRecipientPair
        return if (recipientId == device.mId || recipientId == device.mParentId) {
            val targetDevicesIds = getTargetDevicesForTuple(tuple)
            val nextHopId = idOfNextHopTo(targetDevicesIds, tuple.cloudletFileSize)
            (ev.data as TupleRecipientPair).recipientId = nextHopId
            superNetworkDeviceBehavior.processEvent(ev)
        }
        else {
            false
        }
    }

    private fun getTargetDevicesForTuple(tuple: Tuple): List<Int> {
        val modulePlacement = device.controller.appModulePlacementPolicy[tuple.appId]!!
        // get devices with necessary module
        val devicesWithModule: List<Int> = modulePlacement.moduleToDeviceMap[tuple.destModuleName]!!
        val targetDevices = if (tuple.moduleCopyMap.containsKey(tuple.destModuleName)) {
            // if tuple has to be sent to specific module with module.id == tuple.moduleCopyMap[tuple.destModuleName]
            val specificModuleId: Int = tuple.moduleCopyMap[tuple.destModuleName]!!
            val specificDeviceId: Int = devicesWithModule.find {deviceId ->
                modulePlacement.deviceToModuleMap[deviceId]!!.find {
                    module -> module.name == tuple.destModuleName
                }!!.id == specificModuleId
            }!!
            listOf(specificDeviceId)
        }
        else {
            devicesWithModule
        }
        return targetDevices
    }

    private fun idOfNextHopTo(dst: List<Int>, fileSize: Long): Int {
        val devicePathsAndTimings = mutableMapOf<Int, Pair<Array<Int>, Double>>()
        devicePathsAndTimings[device.mId] = Pair(emptyArray(), 0.0)
        var devicesInGraph: MutableList<AddressingDevice>
        var childDevicesToAppend = mutableListOf<AddressingDevice>()
        childDevicesToAppend.add(device)
        var lastParentDeviceId: Int? = null

        while (childDevicesToAppend.isNotEmpty()) {
            devicesInGraph = childDevicesToAppend
            childDevicesToAppend = mutableListOf()

            for (parentDevice in devicesInGraph) {
                val pathAndTimeToParent = devicePathsAndTimings[parentDevice.mId]!!
                val (pathToParent, deliveryTimeToParent) = pathAndTimeToParent

                for (childDevice in parentDevice.connectedDevices.filter{ it.mId != parentDevice.mId }) {
                    val deliveryTimeToChild = deliveryTimeToParent + deliveryTimeTo(parentDevice, childDevice, fileSize)
                    if (!devicePathsAndTimings.containsKey(childDevice.mId)) {
                        childDevicesToAppend.add(childDevice)
                        devicePathsAndTimings[childDevice.mId] = Pair(
                            if (lastParentDeviceId != null) pathToParent.plus(parentDevice.mId)
                            else pathToParent,
                            deliveryTimeToChild
                        )
                    }
                    else if (deliveryTimeToChild < devicePathsAndTimings[childDevice.mId]!!.second) {
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

    private fun deliveryTimeTo(from: AddressingDevice, to: AddressingDevice, fileSize: Long): Double {
        if (from.mParentId == to.mId) {
            return from.mUplinkLatency + fileSize / from.mUplinkBandwidth
        }
        else if (from.mChildToLatencyMap.containsKey(to.mId)) {
            return from.mChildToLatencyMap[to.mId]!! + fileSize / from.mDownlinkBandwidth
        }
        throw Exception("Devices ${from.mName} and ${to.mName} are not connected")
    }
}
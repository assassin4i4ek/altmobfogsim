package api.addressing.entities

import api.network.entities.NetworkDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.placement.Controller

interface AddressingDevice: NetworkDevice {
    val connectedDevices: List<AddressingDevice>
    get() = mutableListOf<Int>().also {
            it.addAll(mChildrenIds)
            if (mParentId > 0)
                it.add(mParentId)
        }.map { id -> CloudSim.getEntity(id) as AddressingDevice}
    val controller: Controller
}
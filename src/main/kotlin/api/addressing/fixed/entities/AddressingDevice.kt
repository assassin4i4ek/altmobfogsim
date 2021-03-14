package api.addressing.fixed.entities

import api.addressing.models.AddressingModel
import api.network.fixed.entities.NetworkDevice
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.entities.Tuple
import org.fog.placement.Controller

interface AddressingDevice: NetworkDevice {
    enum class AddressingType {
        HIERARCHICAL, // hierarchical as well as original don't send tuple with direction Tuple.DOWN up to parent device
        PEER_TO_PEER // peer to peer are allowed to send tuple with direction Tuple.DOWN up to parent device
    }
    val controller: Controller
    val addressingModel: AddressingModel

    val addressingType: AddressingType

    val connectedDevices: List<AddressingDevice>
    get() = mutableListOf<Int>().also {
            it.addAll(mChildrenIds)
            if (mParentId > 0)
                it.add(mParentId)
        }.map { id -> CloudSim.getEntity(id) as AddressingDevice}

    val addressingChildrenMapping: MutableMap<Tuple, MutableMap<Int, Boolean>>
}
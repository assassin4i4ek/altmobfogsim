package utils

import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.VmScheduler
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.power.models.PowerModel
import org.cloudbus.cloudsim.provisioners.BwProvisioner
import org.cloudbus.cloudsim.provisioners.RamProvisioner

class PowerHostWithoutHistory(
        id: Int, ramProvisioner: RamProvisioner, bwProvisioner: BwProvisioner, storage: Long,
        peList: List<Pe>, vmScheduler: VmScheduler?, powerModel: PowerModel?
): PowerHost(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel) {

    override fun addStateHistoryEntry(time: Double, allocatedMips: Double, requestedMips: Double, isActive: Boolean) {

    }
}
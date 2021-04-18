package experiments2

import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationInterface
import org.fog.placement.Controller

class UtilizationHistoryCleaner(private val periodModelUnits: Double, private val controller: Controller): SimEntity("UtilizationHistoryCleaner") {
    override fun startEntity() {
        send(id, periodModelUnits, 0)
    }

    override fun processEvent(p0: SimEvent?) {
        controller.fogDevices.forEach { device ->
            val vmScheduler = device.host.vmScheduler
            if (vmScheduler is PowerUtilizationInterface) {
                vmScheduler.utilizationHisotry.clear()
            }
        }
        send(id, periodModelUnits, 0)
    }

    override fun shutdownEntity() {}
}
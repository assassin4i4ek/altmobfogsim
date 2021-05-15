package utils

import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationInterface
import org.fog.placement.Controller
import org.fog.utils.TimeKeeper

class TimeKeeperCleaner(private val periodModelUnits: Double, private val controller: Controller): SimEntity("UtilizationHistoryCleaner") {
    override fun startEntity() {
        send(id, periodModelUnits, 0)
    }

    override fun processEvent(p0: SimEvent?) {
        TimeKeeper.getInstance().loopIdToTupleIds.clear()
        send(id, periodModelUnits, 0)
    }

    override fun shutdownEntity() {}
}
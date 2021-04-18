package experiments2

import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import java.text.SimpleDateFormat
import java.util.*

class SimulationTimeTracker(private val periodModelUnits: Double): SimEntity("TimeTracker") {
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    override fun startEntity() {
        send(id, periodModelUnits, 0)
    }

    override fun processEvent(p0: SimEvent?) {
        println(dateFormat.format(Date(CloudSim.clock().toLong())))
        send(id, periodModelUnits, 0)
    }

    override fun shutdownEntity() {}
}
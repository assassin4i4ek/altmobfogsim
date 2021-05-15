package utils

import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.placement.Controller
import org.fog.utils.NetworkUsageMonitor
import org.fog.utils.TimeKeeper
import java.text.SimpleDateFormat
import java.util.*

class SimulationTimeTracker(private val periodModelUnits: Double, private val controller: Controller): SimEntity("TimeTracker") {
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    override fun startEntity() {
        send(id, periodModelUnits, 0)
    }

    override fun processEvent(p0: SimEvent?) {
        val delays = TimeKeeper.getInstance().loopIdToCurrentAverage.values.toDoubleArray()
        val avg = Mean().evaluate(delays)
        val cost = controller.fogDevices.filter { it.totalCost.isFinite() }.sumOf { it.totalCost }
        val energy = controller.fogDevices.filter { it.energyConsumption.isFinite() }.sumOf { it.energyConsumption }
        val nw = NetworkUsageMonitor.getNetworkUsage() / CloudSim.clock()
        println("${dateFormat.format(Date(CloudSim.clock().toLong()))}: avg = ${"%.3f".format(avg)}, " +
                "cost = ${"%.3f".format(cost)}, energy = ${"%.0f".format(energy)}, nw = ${"%.0f".format(nw)}")
        send(id, periodModelUnits, 0)
    }

    override fun shutdownEntity() {}
}
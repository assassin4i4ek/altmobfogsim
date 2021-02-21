package utils

import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.placement.Controller
import org.fog.utils.FogEvents

class TestController(
        name: String, fogDevices: MutableList<FogDevice>, sensors: MutableList<Sensor>, actuators: MutableList<Actuator>,
        val onStopSimulation: () -> Unit
) : Controller(name, fogDevices, sensors, actuators) {
    override fun processEvent(ev: SimEvent) {
        if (ev.tag == FogEvents.STOP_SIMULATION) {
            onStopSimulation()
        }
        super.processEvent(ev)
    }
}
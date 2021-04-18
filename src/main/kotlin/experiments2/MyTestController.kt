package experiments2

import org.cloudbus.cloudsim.core.SimEvent
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.placement.Controller
import org.fog.utils.FogEvents

class MyTestController(
        name: String, fogDevices: List<FogDevice>, sensors: List<Sensor>, actuators: List<Actuator>,
        val onStopSimulation: () -> Unit
) : Controller(name, fogDevices, sensors, actuators) {
    override fun processEvent(ev: SimEvent) {
        super.processEvent(ev)
        if (ev.tag == FogEvents.STOP_SIMULATION) {
            onStopSimulation()
        }
    }
}
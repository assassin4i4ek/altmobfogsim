package experiments

import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.Application
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.placement.Controller
import org.fog.utils.FogEvents

class TestController(
        name: String, fogDevices: List<FogDevice>, sensors: List<Sensor>, actuators: List<Actuator>,
        val onStopSimulation: (fogDevices: List<FogDevice>, app: Application) -> Unit
) : Controller(name, fogDevices, sensors, actuators) {
    override fun processEvent(ev: SimEvent) {
        if (ev.tag == FogEvents.STOP_SIMULATION) {
            onStopSimulation(fogDevices, applications.values.first()!!)
        }
        super.processEvent(ev)
    }
}
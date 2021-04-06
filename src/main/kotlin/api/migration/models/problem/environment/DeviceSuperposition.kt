package api.migration.models.problem.environment

import org.cloudbus.cloudsim.core.SimEntity
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import java.lang.Exception

class DeviceSuperposition {
    private val fogDevice: FogDevice?
    private val sensor: Sensor?
    private val actuator: Actuator?

    constructor() {
        this.fogDevice = null
        this.sensor = null
        this.actuator = null
    }

    constructor(fogDevice: FogDevice) {
        this.fogDevice = fogDevice
        this.sensor = null
        this.actuator = null
    }

    constructor(sensor: Sensor) {
        this.fogDevice = null
        this.sensor = sensor
        this.actuator = null
    }

    constructor(actuator: Actuator) {
        this.fogDevice = null
        this.sensor = null
        this.actuator = actuator
    }

    val device: SimEntity? get() = this.fogDevice ?: this.sensor ?: this.actuator

    override fun toString(): String {
        return device.let {
            when (it) {
                is FogDevice -> it.toString()
                is Sensor -> it.name
                is Actuator -> it.name
                else -> it.toString()
            }
        }
    }
}
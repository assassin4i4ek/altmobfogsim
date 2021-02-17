package utils

import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.Application
import org.fog.entities.Actuator
import org.fog.entities.FogBroker
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.placement.Controller
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementMapping
import org.fog.utils.Config
import org.fog.utils.FogEntityFactory
import org.fog.utils.Logger
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.*

abstract class BaseFogDeviceTest {
    protected lateinit var br: FogBroker

    protected lateinit var cloud: FogDevice
    protected lateinit var fogDeviceList: MutableList<FogDevice>

    protected lateinit var sensorList: MutableList<Sensor>
    protected lateinit var actuatorList: MutableList<Actuator>

    protected lateinit var app: Application
    protected lateinit var controller: Controller
    protected lateinit var mm: ModuleMapping

    open fun init(sensorActuatorCount: Int = 1) {
        Log.disable()
        Logger.ENABLED = true
        CloudSim.init(1, Calendar.getInstance(), false)
        Config.MAX_SIMULATION_TIME = 10

        br = FogBroker("Broker")
        app = createApp(br.id)

        cloud = FogEntityFactory.createFogDevice("cloud", 2000, 1000.0, 1000.0, 0.1, 0.01)
        fogDeviceList = mutableListOf(cloud)
        sensorList = MutableList(sensorActuatorCount) {
            Sensor("Sensor$it", "SENSOR", br.id, "App1", DeterministicDistribution(1.0))
        }
        actuatorList = MutableList(sensorActuatorCount) {
            Actuator("Actuator$it", br.id, "App1", "ACTUATOR")
        }

        mm = ModuleMapping.createModuleMapping()
    }

    @AfterEach
    open fun launchTest() {
        controller = Controller("Controller", fogDeviceList, sensorList, actuatorList)
        controller.submitApplication(app, 0, ModulePlacementMapping(fogDeviceList, app, mm))
        CloudSim.startSimulation()
        CloudSim.stopSimulation()
    }

    fun connectSensorsAndActuatorsToDevice(device: FogDevice, pairIndex: Int) {
        sensorList[pairIndex].gatewayDeviceId = device.id
        actuatorList[pairIndex].gatewayDeviceId = device.id
    }
}
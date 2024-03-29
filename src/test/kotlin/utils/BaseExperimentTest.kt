package utils

import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.ModulePlacement
import org.fog.utils.Logger
import java.util.*

abstract class BaseExperimentTest {
    protected lateinit var broker: FogBroker
    protected lateinit var app: Application
    protected val fogDevices: MutableList<FogDevice> = mutableListOf()
    protected val sensors: MutableList<Sensor> = mutableListOf()
    protected val actuators: MutableList<Actuator> = mutableListOf()

    fun init(eegTransRate: Double, seed: Long = 1000) {
        CloudSim.init(1, Calendar.getInstance(), false)
        val randField = Math::class.java.declaredClasses.find { it.simpleName == "RandomNumberGeneratorHolder" }!!
                .getDeclaredField("randomNumberGenerator")
        randField.isAccessible = true
        val randObj = randField.get(null) as Random
        randObj.setSeed(seed)
        Log.disable()
        Logger.ENABLED = false

        broker = FogBroker("Broker")
        app = createApplication(broker.id, eegTransRate)
    }

    fun launchTest(isCloud: Boolean, onStopSimulation: () -> Unit) {
        val controller = TestController(
                "controller", fogDevices, sensors, actuators, onStopSimulation)
        val modulePlacement = placeModules(isCloud)
        controller.submitApplication(app, modulePlacement)
        CloudSim.startSimulation()
        CloudSim.stopSimulation()
    }

    private fun createApplication(brokerId: Int, eegTransRate: Double): Application {
        val app = Application.createApplication("vr_game", brokerId)

        app.addAppModule("client", 10) // adding module Client to the application model
        app.addAppModule("concentration_calculator", 10) // adding module Concentration Calculator to the application model
        app.addAppModule("connector", 10) // adding module Connector to the application model

        /*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
        if (eegTransRate == 10.0) app.addAppEdge("EEG", "client", 2000.0, 500.0, "EEG", Tuple.UP, AppEdge.SENSOR) // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
        else app.addAppEdge("EEG", "client", 2500.0, 500.0, "EEG", Tuple.UP, AppEdge.SENSOR)
        app.addAppEdge("client", "concentration_calculator", 3500.0, 500.0, "_SENSOR", Tuple.UP, AppEdge.MODULE) // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
        app.addAppEdge("concentration_calculator", "connector", 100.0, 1000.0, 1000.0, "PLAYER_GAME_STATE", Tuple.UP, AppEdge.MODULE) // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
        app.addAppEdge("concentration_calculator", "client", 14.0, 500.0, "CONCENTRATION", Tuple.DOWN, AppEdge.MODULE) // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
        app.addAppEdge("connector", "client", 100.0, 28.0, 1000.0, "GLOBAL_GAME_STATE", Tuple.DOWN, AppEdge.MODULE) // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
        app.addAppEdge("client", "DISPLAY", 1000.0, 500.0, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
        app.addAppEdge("client", "DISPLAY", 1000.0, 500.0, "GLOBAL_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE

        /*
		 * Defining the input-output relationships (represented by selectivity) of the application modules.
		 */
        app.addTupleMapping("client", "EEG", "_SENSOR", FractionalSelectivity(0.9)) // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
        app.addTupleMapping("client", "CONCENTRATION", "SELF_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
        app.addTupleMapping("concentration_calculator", "_SENSOR", "CONCENTRATION", FractionalSelectivity(1.0)) // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
        app.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE

        app.loops = listOf(AppLoop(listOf("EEG", "client", "concentration_calculator", "client", "DISPLAY")))
        return app
    }

    abstract fun placeModules(isCloud: Boolean): ModulePlacement
}
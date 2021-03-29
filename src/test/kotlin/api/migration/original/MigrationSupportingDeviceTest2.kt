package api.migration.original

import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.Controller
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementMapping
import org.fog.utils.Config
import org.fog.utils.FogEntityFactory
import org.fog.utils.Logger
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.TestController
import java.util.*

class MigrationSupportingDeviceTest2 {
    fun createApp(userId: Int, id: Int): Application {
        val app = Application.createApplication("App$id", userId)

        app.addAppModule("client$id", 10) // adding module Client to the application model
        app.addAppModule("concentration_calculator$id", 10) // adding module Concentration Calculator to the application model

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        app.addAppEdge("SENSOR$id", "client$id", 100.0, 0.1, "SENSOR$id", Tuple.UP, AppEdge.SENSOR)
        app.addAppEdge("client$id", "concentration_calculator$id", 1000.0, 0.1, "_SENSOR$id", Tuple.UP, AppEdge.MODULE) // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
        app.addAppEdge("concentration_calculator$id", "client$id", 14.0, 0.1, "CONCENTRATION$id", Tuple.DOWN, AppEdge.MODULE) // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
        app.addAppEdge("client$id", "ACTUATOR$id", 100.0, 0.1, "SELF_STATE_UPDATE$id", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        app.addTupleMapping("client$id", "SENSOR$id", "_SENSOR$id", FractionalSelectivity(1.0)) // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
        app.addTupleMapping("client$id", "CONCENTRATION$id", "SELF_STATE_UPDATE$id", FractionalSelectivity(1.0)) // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
        app.addTupleMapping("concentration_calculator$id", "_SENSOR$id", "CONCENTRATION$id", FractionalSelectivity(1.0)) // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
        app.addTupleMapping("client$id", "GLOBAL_GAME_STATE$id", "GLOBAL_STATE_UPDATE$id", FractionalSelectivity(1.0)) // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE

        app.loops = listOf(AppLoop(listOf("SENSOR$id", "client$id", "concentration_calculator$id", "client$id", "ACTUATOR$id")))
        return app
    }

    @Test
    fun test1() {
        CloudSim.init(1, Calendar.getInstance(), false)
        Log.disable()
        Logger.ENABLED = true

        Config.MAX_SIMULATION_TIME = 10

        val numUsers = 2
        val brList = List(numUsers) {
            FogBroker("Broker$it")
        }
        val appList = List(numUsers) {
            createApp(brList[it].id, it)
        }

        val cloud = FogEntityFactory.createFogDevice("cloud", 2000, 1000.0, 1000.0, 0.1, 0.01)
        val mobList = List(numUsers) {
            val mob = FogEntityFactory.createFogDevice("Mob$it", 1000, 1000.0, 1000.0, 0.1, 0.01)
            mob.parentId = cloud.id
            mob
        }
        val sensorList = MutableList(numUsers) {
            val sensor = Sensor("Sensor$it", "SENSOR$it", brList[it].id, appList[it].appId, DeterministicDistribution(1.0))
            sensor.latency = 0.1
            sensor.gatewayDeviceId = mobList[it].id
            sensor
        }
        val actuatorList = MutableList(numUsers) {
            val actuator = Actuator("Actuator$it", brList[it].id, appList[it].appId, "ACTUATOR$it")
            actuator.latency = 0.1
            actuator.gatewayDeviceId = mobList[it].id
            actuator
        }

        val fogDeviceList = mutableListOf(cloud)
        fogDeviceList.addAll(mobList)

        val controller = TestController("Controller", fogDeviceList, sensorList, actuatorList) {
            println()
        }

        appList.forEachIndexed { i, app ->
            val mm = ModuleMapping.createModuleMapping()
            mm.addModuleToDevice("client$i", "Mob$i")
            mm.addModuleToDevice("concentration_calculator$i", "cloud")
            controller.submitApplication(app, ModulePlacementMapping(fogDeviceList, app, mm))
        }

        CloudSim.startSimulation()
    }
}
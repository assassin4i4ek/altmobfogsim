package experiments

import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacement
import org.fog.placement.ModulePlacementEdgewards
import org.fog.placement.ModulePlacementMapping
import org.fog.utils.Config
import org.fog.utils.Logger
import org.fog.utils.NetworkUsageMonitor
import org.fog.utils.TimeKeeper
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*

abstract class Experiment(
        private val resultsPath: String?,
        private val isWarmup: Boolean,
        protected val isLog: Boolean,
        protected val seed: Long,
        protected val eegTransRates: DoubleArray,
        protected val totalGatewaysCount: IntArray,
        protected val numMobilesPerGateway: Int,
        protected val isCloudCount: BooleanArray,
) {
    fun start() {
        val table = mutableMapOf<String, MutableList<Double>>()
        var lastNetworkUsage = NetworkUsageMonitor.getNetworkUsage()

        fun summarize(fogDevices: List<FogDevice>, app: Application) {
            val t = Calendar.getInstance().timeInMillis
            table["Delay"]!!.add(TimeKeeper.getInstance().loopIdToCurrentAverage[app.loops[0].loopId]!!)
            table["DC Energy"]!!.add(fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            table["Mobile Energy"]!!.add(fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            table["Edge Energy"]!!.add(fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            table["Execution Time"]!!.add(t.toDouble() - TimeKeeper.getInstance().simulationStartTime)
            table["Network Usage"]!!.add((NetworkUsageMonitor.getNetworkUsage() - lastNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }

        for (eegTransRate in eegTransRates) {
            table["Delay"] = mutableListOf()
            table["DC Energy"] = mutableListOf()
            table["Mobile Energy"] = mutableListOf()
            table["Edge Energy"] = mutableListOf()
            table["Execution Time"] = mutableListOf()
            table["Network Usage"] = mutableListOf()

            if (isWarmup) {
                warmup()
            }

            for (totalGateways in totalGatewaysCount) {
                for (isCloud in isCloudCount) {
                    lastNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
                    experiment(isCloud, totalGateways, numMobilesPerGateway, eegTransRate, seed, ::summarize)
                }
            }

            (if (resultsPath != null) PrintWriter(FileWriter(resultsPath, true)) else PrintWriter(System.out)).let {
                it.println("\nSummary $eegTransRate\n")
                for (entry in table) {
                    it.print(entry.key + '\t')
                    it.println(entry.value.joinToString("\t") { it.toString().replace('.', ',') })
                }
                it.println()
                it.flush()
            }

            table.clear()
        }
    }

    protected open fun warmup() {
        experiment(true, 4, 4, 10.0, seed) { _, _ -> }
        experiment(false, 4, 4, 10.0, seed) { _, _ -> }
        experiment(true, 8, 4, 5.0, seed) { _, _ -> }
        experiment(false, 8, 4, 5.0, seed) { _, _ -> }
    }

    protected fun experiment(isCloud: Boolean, totalGateways: Int, numMobilesPerGateway: Int, eegTransRate: Double, seed: Long,
                             summarize: (List<FogDevice>, Application) -> Unit) {
        CloudSim.init(1, Calendar.getInstance(), false)
        val randField = Math::class.java.declaredClasses.find { it.simpleName == "RandomNumberGeneratorHolder" }!!
                .getDeclaredField("randomNumberGenerator")
        randField.isAccessible = true
        val randObj = randField.get(null) as Random
        randObj.setSeed(seed)
        Log.disable()
        Logger.ENABLED = isLog

        val broker = FogBroker("Broker")
        val app = createApplication(broker.id, eegTransRate)

        val (fogDevices, sensors, actuators) = createAllDevices(totalGateways, numMobilesPerGateway, broker.id, app.appId, eegTransRate)

        val controller = TestController(
                "controller-$totalGateways-$numMobilesPerGateway-$eegTransRate-${if (isCloud) "cloud" else "fog"}",
                fogDevices, sensors, actuators, summarize)
        val modulePlacement = placeModules(isCloud, fogDevices, app, sensors, actuators)
        controller.submitApplication(app, modulePlacement)

        TimeKeeper.getInstance().simulationStartTime = Calendar.getInstance().timeInMillis

        CloudSim.startSimulation()
        CloudSim.stopSimulation()
    }

    protected fun createApplication(brokerId: Int, eegTransRate: Double): Application {
        val app = Application.createApplication("vr_game", brokerId)

        app.addAppModule("client", 10) // adding module Client to the application model
        app.addAppModule("concentration_calculator", 10) // adding module Concentration Calculator to the application model
        app.addAppModule("connector", 10) // adding module Connector to the application model

        /*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */if (eegTransRate == 10.0) app.addAppEdge("EEG", "client", 2000.0, 500.0, "EEG", Tuple.UP, AppEdge.SENSOR) // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
        else app.addAppEdge("EEG", "client", 2500.0, 500.0, "EEG", Tuple.UP, AppEdge.SENSOR)
        app.addAppEdge("client", "concentration_calculator", 3500.0, 500.0, "_SENSOR", Tuple.UP, AppEdge.MODULE) // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
        app.addAppEdge("concentration_calculator", "connector", 100.0, 1000.0, 1000.0, "PLAYER_GAME_STATE", Tuple.UP, AppEdge.MODULE) // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
        app.addAppEdge("concentration_calculator", "client", 14.0, 500.0, "CONCENTRATION", Tuple.DOWN, AppEdge.MODULE) // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
        app.addAppEdge("connector", "client", 100.0, 28.0, 1000.0, "GLOBAL_GAME_STATE", Tuple.DOWN, AppEdge.MODULE) // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
        app.addAppEdge("client", "DISPLAY", 1000.0, 500.0, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
        app.addAppEdge("client", "DISPLAY", 1000.0, 500.0, "GLOBAL_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE

        /*
		 * Defining the input-output relationships (represented by selectivity) of the application modules.
		 */app.addTupleMapping("client", "EEG", "_SENSOR", FractionalSelectivity(0.9)) // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
        app.addTupleMapping("client", "CONCENTRATION", "SELF_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
        app.addTupleMapping("concentration_calculator", "_SENSOR", "CONCENTRATION", FractionalSelectivity(1.0)) // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
        app.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE

        app.loops = listOf(AppLoop(listOf("EEG", "client", "concentration_calculator", "client", "DISPLAY")))
        return app
    }

    abstract fun createAllDevices(numGateways: Int, numMobilesPerGateway: Int, brokerId: Int, appId: String, eegTransRate: Double):
            Triple<List<FogDevice>, List<Sensor>, List<Actuator>>


    abstract fun placeModules(isCloud: Boolean, fogDevices: List<FogDevice>, app: Application,
                              sensors: List<Sensor>, actuators: List<Actuator>): ModulePlacement
}
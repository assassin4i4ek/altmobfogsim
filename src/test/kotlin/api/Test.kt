package api

import addons.migration.addressing.entities.DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.migration.addressing.entities.ModuleAddressingMigrationSupportingDeviceImpl
import api.migration.models.CentralizedMapoModel
import api.migration.models.MigrationModel
import api.migration.models.problem.normalizers.MinMaxNormalizer
import api.migration.models.problem.objectives.MinCostObjective
import api.migration.models.problem.objectives.MinProcessingTimeObjective
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementEdgewards
import org.fog.utils.Config
import org.fog.utils.Logger
import org.fog.utils.distribution.DeterministicDistribution
import utils.TestController
import utils.createCharacteristicsAndAllocationPolicy
import java.util.*

fun main() {
    val numUsers = 2
    CloudSim.init(1, Calendar.getInstance(), false)
    Log.disable()
    Logger.ENABLED = true

    Config.MAX_SIMULATION_TIME = 10

    val brList = List(numUsers) {
        FogBroker("Broker$it")
    }
    val appList = List(numUsers) {
        createApp(brList[it].id, it)
    }
    val cloud = createModuleAddressingMigrationSupportingDevice(
            "cloud", 10.0, 1000.0, 1000.0, 0.1, 0.01,
            CentralizedMapoModel(1.0, true, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(),123).apply {
                allowMigrationForModule("concentration_calculator0")
            },
            AddressingDevice.AddressingType.HIERARCHICAL)
    val fogDeviceList = mutableListOf<FogDevice>(cloud)

    val sensorList = MutableList(numUsers) {
        val sensor = Sensor("Sensor$it", "SENSOR$it", brList[it].id, appList[it].appId, DeterministicDistribution(1.0))
        sensor.latency = 0.1
        sensor
    }
    val actuatorList = MutableList(numUsers) {
        val actuator = Actuator("Actuator$it", brList[it].id, appList[it].appId, "ACTUATOR$it")
        actuator.latency = 0.1
        actuator
    }

    val mob1 = createMobileAddressingDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
    val mob2 = createMobileAddressingDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
    val serv1 = createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01,
            CentralizedMapoModel(1.0, false, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(), 123), AddressingDevice.AddressingType.HIERARCHICAL
    )
    val serv2 = createModuleAddressingMigrationSupportingDevice("Serv2", 10.0, 1000.0, 1000.0, 0.1, 0.01,
            CentralizedMapoModel(1.0, false, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(), 123), AddressingDevice.AddressingType.HIERARCHICAL
    )
    fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2))

    mob1.parentId = serv1.id
    mob2.parentId = serv2.id
    serv1.parentId = cloud.id
    serv2.parentId = cloud.id
    sensorList[0].gatewayDeviceId = mob1.id
    actuatorList[0].gatewayDeviceId = mob1.id
    sensorList[1].gatewayDeviceId = mob2.id
    actuatorList[1].gatewayDeviceId = mob2.id

    val controller = TestController("Controller", fogDeviceList, sensorList, actuatorList) {

    }

    appList.forEachIndexed { i, app ->
        val mm = ModuleMapping.createModuleMapping()
        mm.addModuleToDevice("connector$i", "cloud")
        controller.submitApplication(app, ModulePlacementEdgewards(fogDeviceList, sensorList, actuatorList, app, mm))
    }

    CloudSim.startSimulation()
}

fun createApp(userId: Int, id: Int): Application {
    val app = Application.createApplication("App$id", userId)

    app.addAppModule("client$id", 10) // adding module Client to the application model
    app.addAppModule("concentration_calculator$id", 10) // adding module Concentration Calculator to the application model
    app.addAppModule("connector$id", 10)

    /*
     * Connecting the application modules (vertices) in the application model (directed graph) with edges
     */
    app.addAppEdge("SENSOR$id", "client$id", 100.0, 0.1, "SENSOR$id", Tuple.UP, AppEdge.SENSOR)
    app.addAppEdge("client$id", "concentration_calculator$id", 1000.0, 0.1, "_SENSOR$id", Tuple.UP, AppEdge.MODULE) // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
    app.addAppEdge("concentration_calculator$id", "client$id", 14.0, 0.1, "CONCENTRATION$id", Tuple.DOWN, AppEdge.MODULE) // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
    app.addAppEdge("client$id", "ACTUATOR$id", 100.0, 0.1, "SELF_STATE_UPDATE$id", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE

    app.addAppEdge("concentration_calculator$id", "connector$id", 5.0, 100.0, 0.1, "PLAYER_GAME_STATE$id", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("connector$id", "client$id", 10.0, 100.0, 0.1, "GLOBAL_GAME_STATE$id", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("client$id", "ACTUATOR$id", 100.0, 0.1, "GAME_STATE_UPDATE$id", Tuple.DOWN, AppEdge.ACTUATOR)
    /*
     * Defining the input-output relationships (represented by selectivity) of the application modules.
     */
    app.addTupleMapping("client$id", "SENSOR$id", "_SENSOR$id", FractionalSelectivity(1.0)) // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
    app.addTupleMapping("client$id", "CONCENTRATION$id", "SELF_STATE_UPDATE$id", FractionalSelectivity(1.0)) // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
    app.addTupleMapping("concentration_calculator$id", "_SENSOR$id", "CONCENTRATION$id", FractionalSelectivity(1.0)) // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
    app.addTupleMapping("client$id", "GLOBAL_GAME_STATE$id", "GAME_STATE_UPDATE$id", FractionalSelectivity(1.0)) // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE

    app.loops = listOf(AppLoop(listOf("SENSOR$id", "client$id", "concentration_calculator$id", "client$id", "ACTUATOR$id")))
    return app
}

fun createModuleAddressingMigrationSupportingDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
        migrationModel: MigrationModel, addressingType: AddressingDevice.AddressingType
): ModuleAddressingMigrationSupportingDeviceImpl {
    return createCharacteristicsAndAllocationPolicy(10000.0).let {
        ModuleAddressingMigrationSupportingDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, migrationModel, addressingType
        )
    }
}

fun createMobileAddressingDevice(
        name: String, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
        ratePerMips: Double, addressingType: AddressingDevice.AddressingType
): DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl {
    return createCharacteristicsAndAllocationPolicy(1000.0).let {
        DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, addressingType
        )
    }
}

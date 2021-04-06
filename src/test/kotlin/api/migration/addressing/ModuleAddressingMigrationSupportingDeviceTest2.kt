package api.migration.addressing

import addons.migration.addressing.entities.DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.fixed.entities.AddressingDeviceImpl
import api.common.utils.ConnectionUtils
import api.migration.addressing.entities.ModuleAddressingMigrationSupportingDeviceImpl
import api.migration.models.CentralizedMapoModel
import api.migration.models.MigrationModel
import api.migration.models.problem.normalizers.MinMaxNormalizer
import api.migration.models.problem.objectives.MinCostObjective
import api.migration.models.problem.objectives.MinProcessingTimeObjective
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.CloudSimTags
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementEdgewards
import org.fog.utils.Config
import org.fog.utils.FogEvents
import org.fog.utils.Logger
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.TestController
import utils.createCharacteristicsAndAllocationPolicy
import java.util.*

class ModuleAddressingMigrationSupportingDeviceTest2 {
    private fun createApp(userId: Int, id: Int): Application {
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

        app.addAppEdge("concentration_calculator$id", "connector$id", 9.0, 100.0, 0.1, "PLAYER_GAME_STATE$id", Tuple.UP, AppEdge.MODULE)
        app.addAppEdge("connector$id", "client$id", 3.0, 100.0, 0.1, "GLOBAL_GAME_STATE$id", Tuple.DOWN, AppEdge.MODULE)
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

    private fun createAddressingDevice(
            name: String, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
            uplinkLatency: Double, ratePerMips: Double, addressingType: AddressingDevice.AddressingType
    ): AddressingDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(0.0).let {
            AddressingDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, addressingType
            )
        }
    }

    private fun createModuleAddressingMigrationSupportingDevice(
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

    private fun createMobileAddressingDevice(
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

    private lateinit var brList: List<FogBroker>
    private lateinit var appList: List<Application>
    private lateinit var fogDeviceList: MutableList<FogDevice>
    private lateinit var sensorList: List<Sensor>
    private lateinit var actuatorList: List<Actuator>

    private fun init(numUsers: Int) {
        CloudSim.init(1, Calendar.getInstance(), false)
        Log.disable()
        Logger.ENABLED = true

        Config.MAX_SIMULATION_TIME = 10

        brList = List(numUsers) {
            FogBroker("Broker$it")
        }
        appList = List(numUsers) {
            createApp(brList[it].id, it)
        }
        fogDeviceList = mutableListOf()
    }

    @Test
    fun test1() {
        val numUsers = 1
        init(numUsers)
        val cloud = createModuleAddressingMigrationSupportingDevice(
                "cloud", 10.0, 1000.0, 1000.0, 0.1, 0.001,
                CentralizedMapoModel(1.0, true, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(), 123).apply {
                    allowMigrationForModule("concentration_calculator0")
                },
                AddressingDevice.AddressingType.HIERARCHICAL)

        sensorList = MutableList(numUsers) {
            val sensor = Sensor("Sensor$it", "SENSOR$it", brList[it].id, appList[it].appId, DeterministicDistribution(1.0))
            sensor.latency = 0.1
            sensor
        }
        actuatorList = MutableList(numUsers) {
            val actuator = Actuator("Actuator$it", brList[it].id, appList[it].appId, "ACTUATOR$it")
            actuator.latency = 0.1
            actuator
        }

        val mob = createMobileAddressingDevice("Mob", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
//        val mob2 = api.createMobileAddressingDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val serv1 = api.createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                CentralizedMapoModel(1.0, false, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(), 123), AddressingDevice.AddressingType.HIERARCHICAL
        )
        val gw1 = createAddressingDevice("GW1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw2 = createAddressingDevice("GW2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw3 = createAddressingDevice("GW3", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw4 = createAddressingDevice("GW4", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        fogDeviceList.addAll(listOf(mob, serv1, cloud, gw1, gw2, gw3, gw4))

        mob.parentId = gw1.id
        gw1.parentId = gw2.id
        gw2.parentId = serv1.id
        serv1.parentId = gw3.id
        gw3.parentId = gw4.id
        gw4.parentId = cloud.id
        sensorList[0].gatewayDeviceId = mob.id
        actuatorList[0].gatewayDeviceId = mob.id

        launchTest {
            println()
        }
    }

    @Test
    fun test2() {
        val numUsers = 2
        init(numUsers)
        val cloud = createModuleAddressingMigrationSupportingDevice(
                "cloud", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                CentralizedMapoModel(1.0, true, listOf(MinCostObjective(), MinProcessingTimeObjective()), MinMaxNormalizer(), 123).apply {
                    allowMigrationForModule("concentration_calculator0")
                },
                AddressingDevice.AddressingType.HIERARCHICAL)

        sensorList = MutableList(numUsers) {
            val sensor = Sensor("Sensor$it", "SENSOR$it", brList[it].id, appList[it].appId, DeterministicDistribution(1.0))
            sensor.latency = 0.1
            sensor
        }
        actuatorList = MutableList(numUsers) {
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
        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2, cloud))

        mob1.parentId = serv1.id
        mob2.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id
        sensorList[0].gatewayDeviceId = mob1.id
        actuatorList[0].gatewayDeviceId = mob1.id
        sensorList[1].gatewayDeviceId = mob2.id
        actuatorList[1].gatewayDeviceId = mob2.id

        launchTest {
            println()
        }
    }

    @Test
    fun test3() {
        val numUsers = 2
        init(1)

        sensorList = MutableList(numUsers) {
            val sensor = Sensor("Sensor$it", "SENSOR0", brList[0].id, appList[0].appId, DeterministicDistribution(1.0))
            sensor.latency = 0.1
            sensor
        }
        actuatorList = MutableList(numUsers) {
            val actuator = Actuator("Actuator$it", brList[0].id, appList[0].appId, "ACTUATOR0")
            actuator.latency = 0.1
            actuator
        }

        val cloud = createModuleAddressingMigrationSupportingDevice(
                "cloud", 1.0, 1000.0, 1000.0, 0.1, 0.001,
                CentralizedMapoModel(1.0, true, listOf(/*MinCostObjective(),*/ MinProcessingTimeObjective()), MinMaxNormalizer(), 123).apply {
                    allowMigrationForModule("concentration_calculator0")
                    allowMigrationForModule("connector0")
                },
                AddressingDevice.AddressingType.HIERARCHICAL)
        val mob1 = createMobileAddressingDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val mob2 = createMobileAddressingDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val serv1 = createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.1,
                CentralizedMapoModel(1.0, false), AddressingDevice.AddressingType.HIERARCHICAL
        )
        val serv2 = createModuleAddressingMigrationSupportingDevice("Serv2", 10.0, 1000.0, 1000.0, 0.1, 0.1,
                CentralizedMapoModel(1.0, false), AddressingDevice.AddressingType.HIERARCHICAL
        )
        val gw1 = createAddressingDevice("GW1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw2 = createAddressingDevice("GW2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw3 = createAddressingDevice("GW3", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val gw4 = createAddressingDevice("GW4", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)

        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2, cloud, gw1, gw2, gw3, gw4))

        mob1.parentId = gw1.id
        mob2.parentId = gw1.id
        gw1.parentId = gw2.id
        gw2.parentId = serv1.id
        serv1.parentId = gw3.id
        serv2.parentId = gw3.id
        gw3.parentId = gw4.id
        gw4.parentId = cloud.id

        sensorList[0].gatewayDeviceId = mob1.id
        actuatorList[0].gatewayDeviceId = mob1.id
        sensorList[1].gatewayDeviceId = mob2.id
        actuatorList[1].gatewayDeviceId = mob2.id

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 0.9, 1, null)
                send(id, 7.9, 2, null)
            }

            override fun processEvent(ev: SimEvent) {
                when (ev.tag) {
                    1 -> {
//                    ConnectionUtils.disconnectChildFromParent(gw1, mob1)
//                        ConnectionUtils.disconnectChildFromParent(gw1, mob2)
//                        ConnectionUtils.connectChildToParent(serv2, mob2)
                    }
                    2 -> {
                        cloud.ratePerMips *= 1000
//                        cloud.mSendEvent(cloud.id, 0.0, CloudSimTags.VM_DATACENTER_EVENT, null)
                    }
                }
            }

            override fun shutdownEntity() {}
        }

        launchTest {
            println()
        }
    }

    private fun launchTest(onStopSimulation: () -> Unit) {
        val controller = TestController("Controller", fogDeviceList, sensorList, actuatorList, onStopSimulation)

        appList.forEachIndexed { i, app ->
            val mm = ModuleMapping.createModuleMapping()
            mm.addModuleToDevice("connector$i", "cloud")
            controller.submitApplication(app, ModulePlacementEdgewards(fogDeviceList, sensorList, actuatorList, app, mm))
        }

        CloudSim.startSimulation()
    }
}
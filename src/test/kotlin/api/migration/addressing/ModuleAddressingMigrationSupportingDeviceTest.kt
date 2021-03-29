package api.migration.addressing

import api.addressing.fixed.entities.AddressingDevice
import api.addressing.fixed.entities.AddressingDeviceImpl
import api.common.utils.ConnectionUtils
import api.migration.addressing.entities.ModuleAddressingMigrationSupportingDeviceImpl
import addons.migration.addressing.entities.DynamicGatewayConnectionModuleLaunchingAddressingDeviceImpl
import api.migration.models.MigrationModel
import api.migration.models.MigrationModelImpl
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
import org.fog.placement.ModulePlacementEdgewards
import org.fog.utils.*
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.TestController
import utils.createCharacteristicsAndAllocationPolicy
import java.util.*
import kotlin.math.round
import kotlin.test.assertEquals

class ModuleAddressingMigrationSupportingDeviceTest {
    private fun createApp(userId: Int, id: Int): Application {
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

    private fun createAddressingDevice(
            name: String, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
            uplinkLatency: Double, ratePerMips: Double, addressingType: AddressingDevice.AddressingType
    ): AddressingDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AddressingDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, addressingType
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

    private lateinit var appList: List<Application>
    private lateinit var cloud: FogDevice
    private lateinit var fogDeviceList: MutableList<FogDevice>
    private lateinit var sensorList: List<Sensor>
    private lateinit var actuatorList: List<Actuator>

    fun init(numUsers: Int) {
        CloudSim.init(1, Calendar.getInstance(), false)
        Log.disable()
        Logger.ENABLED = true

        Config.MAX_SIMULATION_TIME = 10

        val brList = List(numUsers) {
            FogBroker("Broker$it")
        }
        appList = List(numUsers) {
            createApp(brList[it].id, it)
        }
        cloud = createAddressingDevice("cloud", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        fogDeviceList = mutableListOf(cloud)

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
    }

    private fun connectSensorActuatorPairToDevice(dev: FogDevice, pairIndex: Int) {
        sensorList[pairIndex].gatewayDeviceId = dev.id
        actuatorList[pairIndex].gatewayDeviceId = dev.id
    }

    private fun launchTest(onStopSimulation: () -> Unit) {
        val controller = TestController("Controller", fogDeviceList, sensorList, actuatorList, onStopSimulation)

        appList.forEach { app ->
            controller.submitApplication(app, ModulePlacementEdgewards(fogDeviceList, sensorList, actuatorList, app, ModuleMapping.createModuleMapping()))
        }

        CloudSim.startSimulation()
    }

    @Test
    fun test1() {
        init(1)
        val mob = createMobileAddressingDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val serv1 = createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        val serv2 = createModuleAddressingMigrationSupportingDevice("Serv2", 10.0, 1000.0, 1000.0, 3.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        fogDeviceList.addAll(listOf(mob, serv1, serv2))

        connectSensorActuatorPairToDevice(mob, 0)
        mob.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 4.9, 1, null)
                send(serv1.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
                send(serv2.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob)
                    ConnectionUtils.connectChildToParent(serv2, mob)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loopId = appList[0].loops[0].loopId
            assertEquals(2.1385, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! * 10000) / 10000)
            assertEquals(0, serv1.mAppModuleList.size)
            assertEquals(1, serv2.mAppModuleList.size)
            assertEquals(1, serv2.mAppModuleList[0].numInstances)
            assertEquals(3.38, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }

    @Test
    fun test2() {
        init(2)
        val mob1 = createMobileAddressingDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val mob2 = createMobileAddressingDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val serv1 = createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        val serv2 = createModuleAddressingMigrationSupportingDevice("Serv2", 10.0, 1000.0, 1000.0, 3.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2))

        connectSensorActuatorPairToDevice(mob1, 0)
        connectSensorActuatorPairToDevice(mob2, 1)
        mob1.parentId = serv1.id
        mob2.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 4.9, 1, null)
                send(serv1.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
                send(serv2.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob2)
                    ConnectionUtils.connectChildToParent(serv2, mob2)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loop1Id = appList[0].loops[0].loopId
            val loop2Id = appList[1].loops[0].loopId
            assertEquals(1.8645, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loop1Id]!! * 10000) / 10000)
            assertEquals(2.9177, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loop2Id]!! * 10000) / 10000)
            assertEquals(1, serv1.mAppModuleList.size)
            assertEquals(1, serv1.mAppModuleList[0].numInstances)
            assertEquals(1, serv2.mAppModuleList.size)
            assertEquals(1, serv2.mAppModuleList[0].numInstances)
            assertEquals(3.53, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }

    @Test
    fun test3() {
        init(2)
        val mob1 = createMobileAddressingDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val mob2 = createMobileAddressingDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
        val serv1 = createModuleAddressingMigrationSupportingDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        val serv2 = createModuleAddressingMigrationSupportingDevice("Serv2", 10.0, 1000.0, 1000.0, 3.1, 0.01, MigrationModelImpl(1.0), AddressingDevice.AddressingType.HIERARCHICAL)
        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2))

        connectSensorActuatorPairToDevice(mob1, 0)
        connectSensorActuatorPairToDevice(mob2, 1)
        mob1.parentId = serv1.id
        mob2.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 2.9, 1, null)
                send(id, 4.9, 2, null)
                send(serv1.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
                send(serv2.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob1)
                    ConnectionUtils.connectChildToParent(serv2, mob1)
                }
                else if (ev.tag == 2) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob2)
                    ConnectionUtils.connectChildToParent(serv2, mob2)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loop1Id = appList[0].loops[0].loopId
            val loop2Id = appList[1].loops[0].loopId
            assertEquals(2.776, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loop1Id]!! * 1000) / 1000)
            assertEquals(2.4439, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loop2Id]!! * 10000) / 10000)
            assertEquals(0, serv1.mAppModuleList.size)
            assertEquals(2, serv2.mAppModuleList.size)
            assertEquals(1, serv2.mAppModuleList[0].numInstances)
            assertEquals(1, serv2.mAppModuleList[1].numInstances)
            assertEquals(6.73, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }
}
package api.accesspoint.addressing

import api.accesspoint.addressing.entities.AddressingAccessPointConnectedDeviceImpl
import api.accesspoint.addressing.entities.AddressingAccessPointImpl
import api.accesspoint.original.entities.AccessPointsMap
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import api.common.utils.ConnectionUtils
import api.mobility.models.MobilityModel
import api.mobility.models.SteadyMobilityModel
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Position
import api.mobility.positioning.RadialZone
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.Config
import org.fog.utils.TimeKeeper
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import utils.createThreeModulesApp
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddressingAccessPointConnectedDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createAddressingAccessPoint(
            name: String, coordinates: Coordinates, radius: Double, accessPointsMap: AccessPointsMap,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
    ): AddressingAccessPointImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AddressingAccessPointImpl(
                    name, coordinates, RadialZone(coordinates, radius), accessPointsMap,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createAddressingAccessPointConnectedDevice(
            name: String, position: Position, mobilityModel: MobilityModel, accessPointsMap: AccessPointsMap,
            schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
            ratePerMips: Double,
    ): AddressingAccessPointConnectedDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AddressingAccessPointConnectedDeviceImpl(
                    name, position, mobilityModel, accessPointsMap, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createServer(
            name: String, schedulingInterval: Double,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): DynamicAddressingNotificationConsumerDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            DynamicAddressingNotificationConsumerDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val apm = AccessPointsMap()
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val ap = createAddressingAccessPoint(
                "AccessPoint1", Coordinates(5.0, 5.0), 1.0,
                apm, 1000.0, 1000.0, 0.1
        )
        val mob = createAddressingAccessPointConnectedDevice(
                "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
                SteadyMobilityModel(1.0), apm,10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(serv, mob, ap))

        connectSensorsAndActuatorsToDevice(mob, 0)
        ap.parentId = serv.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(5, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 2.7262) < 1e-6)
            assertEquals(Coordinates(10.0, 10.0), mob.position.coordinates)
            assertEquals(0, mob.producerNotifications.size)
            assertEquals(4, mob.northTupleQueue.size)
        }
    }

    @Test
    fun test2() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val apm = AccessPointsMap()
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val ap = createAddressingAccessPoint(
                "AccessPoint1", Coordinates(5.0, 5.0), 1.0,
                apm, 1000.0, 1000.0, 1.0
        )
        val mob = createAddressingAccessPointConnectedDevice(
                "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
                SteadyMobilityModel(1.0), apm,10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(serv, mob, ap))

        connectSensorsAndActuatorsToDevice(mob, 0)
        sensorList[0].transmitDistribution = DeterministicDistribution(5.0)
        ap.parentId = serv.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        launchTest {
            val loopId = app.loops[0].loopId
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assertNull(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(Coordinates(10.0, 10.0), mob.position.coordinates)
            assertEquals(1, mob.producerNotifications.size)
            assertEquals(1, serv.consumerNotifications.size)
            assertEquals(0, mob.northTupleQueue.size)
        }
    }

    @Test
    fun test3() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val apm = AccessPointsMap()
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val ap = createAddressingAccessPoint(
                "AccessPoint1", Coordinates(5.0, 5.0), 1.0,
                apm, 1000.0, 1000.0, 1.0
        )
        val mob = createAddressingAccessPointConnectedDevice(
                "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
                SteadyMobilityModel(1.0), apm,10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(serv, mob, ap))

        connectSensorsAndActuatorsToDevice(mob, 0)
        ap.parentId = serv.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        launchTest {
            val loopId = app.loops[0].loopId
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assertNull(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(Coordinates(10.0, 10.0), mob.position.coordinates)
            assertEquals(5, mob.producerNotifications.size)
            assertEquals(5, serv.consumerNotifications.size)
            assertEquals(4, mob.northTupleQueue.size)
        }
    }

    @Test
    fun test4() {
        init(createApp = ::createThreeModulesApp)
        val apm = AccessPointsMap()
        val mob = createAddressingAccessPointConnectedDevice(
                "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
                SteadyMobilityModel(1.0), apm, 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val ap1 = createAddressingAccessPoint(
                "AccessPoint1", Coordinates(4.0, 4.0), 1.0,
                apm, 1000.0, 1000.0, 0.1
        )
        val ap2 = createAddressingAccessPoint(
                "AccessPoint2", Coordinates(9.0, 9.0), 1.0,
                apm, 1000.0, 1000.0, 0.1
        )
        val serv1 = createServer("Cluster1.Server1",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv2 = createServer("Cluster1.Server2",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv3 = createServer("Cluster1.Server3",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv4 = createServer("Cluster1.Server4",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv5 = createServer("Cluster1.Server5",
                10.0, 1000.0, 1000.0, 0.1, 0.01
        )
        val serv6 = createServer("Cluster2.Server6",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv7 = createServer("Cluster2.Server7",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv8 = createServer("Cluster2.Server8",
                10.0, 1000.0, 1000.0, 100.0, 0.01
        )
        val serv9 = createServer("Cluster2.Server9",
                10.0, 1000.0, 1000.0, 0.1, 0.01
        )

        fogDeviceList.addAll(listOf(mob, ap1, ap2, serv1, serv2, serv3, serv4, serv5, serv6, serv7, serv8, serv9))

        connectSensorsAndActuatorsToDevice(mob, 0)
        ConnectionUtils.connectPeerToPeer(serv1, serv2, 0.1)
        ConnectionUtils.connectPeerToPeer(serv1, serv4, 10.0)
        ConnectionUtils.connectPeerToPeer(serv2, serv3, 0.1)
        ConnectionUtils.connectPeerToPeer(serv3, serv4, 0.1)
        ConnectionUtils.connectPeerToPeer(serv4, serv5, 0.1)
        ConnectionUtils.connectPeerToPeer(serv2, serv5, 10.0)
        serv5.parentId = serv6.id
        ConnectionUtils.connectPeerToPeer(serv6, serv7, 0.1)
        ConnectionUtils.connectPeerToPeer(serv6, serv8, 10.0)
        ConnectionUtils.connectPeerToPeer(serv7, serv8, 0.1)
        ConnectionUtils.connectPeerToPeer(serv8, serv9, 0.1)
        ConnectionUtils.connectPeerToPeer(serv7, serv9, 10.0)
        ap1.parentId = serv1.id
        ap2.parentId = serv9.id

        sensorList[0].transmitDistribution = DeterministicDistribution(4.0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Cluster1.Server3")
        mm.addModuleToDevice("AppModule3", "Cluster2.Server9")

        Config.MAX_SIMULATION_TIME = 11

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 6.009) < 1e-6)
            assertEquals(Coordinates(11.0, 11.0), mob.position.coordinates)
            assertEquals(0, mob.producerNotifications.size)
            assertEquals(0, mob.northTupleQueue.size)
        }
    }
}
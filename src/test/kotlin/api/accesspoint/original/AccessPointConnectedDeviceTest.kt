package api.accesspoint.original

import api.accesspoint.original.entities.AccessPointConnectedDeviceImpl
import api.accesspoint.original.entities.AccessPointImpl
import api.accesspoint.utils.AccessPointsMap
import api.mobility.models.MobilityModel
import api.mobility.models.SteadyMobilityModel
import api.common.positioning.Coordinates
import api.common.positioning.Position
import api.common.positioning.RadialZone
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.TimeKeeper
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.assertEquals

class AccessPointConnectedDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createAccessPoint(
            name: String, coordinates: Coordinates, radius: Double, accessPointsMap: AccessPointsMap, downlinkLatency: Double,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
    ): AccessPointImpl {
        return createCharacteristicsAndAllocationPolicy(0.0).let {
            AccessPointImpl(
                    name, it.first, it.second, emptyList(), 10.0, uplinkBandwidth, downlinkBandwidth, uplinkLatency, 0.0,
                    coordinates, RadialZone(coordinates, radius), downlinkLatency, accessPointsMap
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createAccessPointConnectedDevice(
            name: String, position: Position, mobilityModel: MobilityModel, accessPointsMap: AccessPointsMap,
            schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double,
            ratePerMips: Double,
    ): AccessPointConnectedDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AccessPointConnectedDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval, uplinkBandwidth, downlinkBandwidth,
                    uplinkLatency, ratePerMips, position, mobilityModel, accessPointsMap,
            )
        }
    }

    @Test
    fun test1() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val apm = AccessPointsMap()
        val ap = createAccessPoint(
            "AccessPoint1", Coordinates(5.0, 5.0), 1.0,
                apm, 0.1,1000.0, 1000.0, 0.1
        )

        val dev = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0), apm,10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, ap))

        connectSensorsAndActuatorsToDevice(dev, 0)
        ap.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(5, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 2.726) < 1e-6)
            assertEquals(Coordinates(10.0, 10.0), dev.position.coordinates)
        }
    }

    @Test
    fun test2() {
        /* Expected access point to establish connection at 3.00 and at 6.00 and interrupt at 4.00 and 7.00*/
        init()
        val apm = AccessPointsMap()
        val ap1 = createAccessPoint(
            "AccessPoint1", Coordinates(3.0, 3.0), 1.0,
            apm, 0.1, 1000.0, 1000.0, 0.1,
        )
        val ap2 = createAccessPoint(
            "AccessPoint2", Coordinates(6.0, 6.0), 1.0,
            apm, 0.1,1000.0, 1000.0, 0.1,
        )

        val dev = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0), apm,10.0, 1000.0, 1000.0,
            0.1, 0.01,
        )
        fogDeviceList.addAll(listOf(dev, ap1, ap2))

        connectSensorsAndActuatorsToDevice(dev, 0)
        ap1.parentId = cloud.id
        ap2.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(6, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 1.806167) < 1e-6)
            assertEquals( Coordinates(10.0, 10.0), dev.position.coordinates)
        }
    }

    @Test
    fun test3() {
        init(2)
        val apm = AccessPointsMap()
        val ap1 = createAccessPoint(
            "AccessPoint1", Coordinates(3.0, 3.0), 1.0,
                apm,0.1, 1000.0, 1000.0, 0.1,
        )
        val ap2 = createAccessPoint(
            "AccessPoint2", Coordinates(6.0, 6.0), 1.0,
                apm,0.1,1000.0, 1000.0, 0.1,
        )

        val dev1 = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0),apm,10.0, 1000.0, 1000.0,
            0.1, 0.01,
        )
        val dev2 = createAccessPointConnectedDevice(
            "Mob2", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0),apm,10.0, 1000.0, 1000.0,
            0.1, 0.01,
        )
        fogDeviceList.addAll(listOf(dev1, dev2, ap1, ap2))

        connectSensorsAndActuatorsToDevice(dev1, 0)
        connectSensorsAndActuatorsToDevice(dev2, 1)
        ap1.parentId = cloud.id
        ap2.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule1", "Mob2")
        mm.addModuleToDevice("AppModule2", "cloud")

        object : SimEntity("tester") {
            override fun startEntity() {
                println()
            }

            override fun processEvent(p0: SimEvent?) {}

            override fun shutdownEntity() {}
        }

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(12, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 1.7745) < 1e-6)
            assertEquals(dev1.position.coordinates, Coordinates(10.0, 10.0))
            assertEquals(dev2.position.coordinates, Coordinates(10.0, 10.0))
        }
    }

    @Test
    fun test4() {
        init()
        val apm = AccessPointsMap()
        val ap = createAccessPoint("AccessPoint1", Coordinates(5.0, 5.0), 1.0,
                apm, 0.1,1000.0, 1000.0, 0.5)
        val dev = createAccessPointConnectedDevice("Mob1",
                Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
                SteadyMobilityModel(1.0),
                apm,10.0, 1000.0, 1000.0, 0.1, 0.01
        )
        fogDeviceList.addAll(listOf(ap, dev))

        connectSensorsAndActuatorsToDevice(dev, 0)
        ap.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        launchTest {
            val loopId = app.loops[0].loopId
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
        }
    }
}
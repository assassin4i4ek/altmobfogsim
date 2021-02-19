package api.accesspoint

import api.accesspoint.entities.AccessPointConnectedDeviceImpl
import api.accesspoint.entities.AccessPointImpl
import api.accesspoint.entities.AccessPointsMap
import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.models.SteadyMobilityModel
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Position
import api.dynamic.mobility.positioning.RadialZone
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.sqrt

class AccessPointConnectedDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createAccessPoint(
        name: String, coordinates: Coordinates, radius: Double, uplinkBandwidth: Double,
        downlinkBandwidth: Double, uplinkLatency: Double,
    ): AccessPointImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AccessPointImpl(
                name, coordinates, RadialZone(coordinates, radius),
                uplinkBandwidth, downlinkBandwidth, uplinkLatency
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createAccessPointConnectedDevice(
        name: String, position: Position, mobilityModel: MobilityModel, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
    ): AccessPointConnectedDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AccessPointConnectedDeviceImpl(
                name, position, mobilityModel, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val ap = createAccessPoint(
            "AccessPoint1", Coordinates(5.0, 5.0), 1.0,
            1000.0, 1000.0, 0.1
        )
        AccessPointsMap.registerAccessPoint(ap)

        val dev = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0), 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, ap))

        connectSensorsAndActuatorsToDevice(dev, 0)
        ap.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }

    @Test
    fun test2() {
        /* Expected access point to establish connection at 3.00 and at 6.00 and interrupt at 4.00 and 7.00*/
        init()
        val ap1 = createAccessPoint(
            "AccessPoint1", Coordinates(3.0, 3.0), 1.0,
            1000.0, 1000.0, 0.1,
        )
        val ap2 = createAccessPoint(
            "AccessPoint2", Coordinates(6.0, 6.0), 1.0,
            1000.0, 1000.0, 0.1,
        )
        AccessPointsMap.registerAccessPoint(ap1)
        AccessPointsMap.registerAccessPoint(ap2)

        val dev = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0), 10.0, 1000.0, 1000.0,
            0.1, 0.01,
        )
        fogDeviceList.addAll(listOf(dev, ap1, ap2))

        connectSensorsAndActuatorsToDevice(dev, 0)
        ap1.parentId = cloud.id
        ap2.parentId = cloud.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }

    @Test
    fun test3() {
        init(2)
        val ap1 = createAccessPoint(
            "AccessPoint1", Coordinates(3.0, 3.0), 1.0,
            1000.0, 1000.0, 0.1,
        )
        val ap2 = createAccessPoint(
            "AccessPoint2", Coordinates(6.0, 6.0), 1.0,
            1000.0, 1000.0, 0.1,
        )
        AccessPointsMap.registerAccessPoint(ap1)
        AccessPointsMap.registerAccessPoint(ap2)

        val dev1 = createAccessPointConnectedDevice(
            "Mob1", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0),10.0, 1000.0, 1000.0,
            0.1, 0.01,
        )
        val dev2 = createAccessPointConnectedDevice(
            "Mob2", Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0),10.0, 1000.0, 1000.0,
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
    }
}
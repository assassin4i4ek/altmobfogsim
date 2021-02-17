package api.accesspoint

import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.models.SteadyMobilityModel
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Position
import api.dynamic.mobility.positioning.PositionAndTimestamp
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.sqrt

class AccessPointConnectedDeviceTest: BaseFogDeviceTest() {
    private fun createAccessPoint(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
        coordinates: Coordinates, radius: Double
    ): api.accesspoint.entities.AccessPointImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            api.accesspoint.entities.AccessPointImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips,
                coordinates, radius
            )
        }
    }

    private fun createAccessPointConnectedDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
        initPositionAndTimestamp: PositionAndTimestamp, mobilityModel: MobilityModel
    ): api.accesspoint.entities.AccessPointConnectedDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            api.accesspoint.entities.AccessPointConnectedDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips,
                initPositionAndTimestamp, mobilityModel
            )
        }
    }

    @Test
    fun test1() {
        /* Expected access point to establish connection at 5.00 and interrupt at 6.00*/
        init()
        val ap = createAccessPoint("AccessPoint1", 10.0, 1000.0, 1000.0,
            0.1, 0.01, Coordinates(5.0, 5.0), 1.0
        )
        api.accesspoint.entities.AccessPointsMap.registerAccessPoint(ap)

        val dev = createAccessPointConnectedDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01,
            PositionAndTimestamp(Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0), 1.0),
            SteadyMobilityModel()
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
        val ap1 = createAccessPoint("AccessPoint1", 10.0, 1000.0, 1000.0,
            0.1, 0.01, Coordinates(3.0, 3.0), 1.0
        )
        val ap2 = createAccessPoint("AccessPoint2", 10.0, 1000.0, 1000.0,
            0.1, 0.01, Coordinates(6.0, 6.0), 1.0
        )
        api.accesspoint.entities.AccessPointsMap.registerAccessPoint(ap1)
        api.accesspoint.entities.AccessPointsMap.registerAccessPoint(ap2)

        val dev = createAccessPointConnectedDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01,
            PositionAndTimestamp(Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0), 1.0),
            SteadyMobilityModel()
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
        val ap1 = createAccessPoint("AccessPoint1", 10.0, 1000.0, 1000.0,
            0.1, 0.01, Coordinates(3.0, 3.0), 1.0
        )
        val ap2 = createAccessPoint("AccessPoint2", 10.0, 1000.0, 1000.0,
            0.1, 0.01, Coordinates(6.0, 6.0), 1.0
        )
        api.accesspoint.entities.AccessPointsMap.registerAccessPoint(ap1)
        api.accesspoint.entities.AccessPointsMap.registerAccessPoint(ap2)

        val dev1 = createAccessPointConnectedDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01,
            PositionAndTimestamp(Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0), 1.0),
            SteadyMobilityModel()
        )
        val dev2 = createAccessPointConnectedDevice(
            "Mob2", 10.0, 1000.0, 1000.0,
            0.1, 0.01,
            PositionAndTimestamp(Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0), 1.0),
            SteadyMobilityModel()
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
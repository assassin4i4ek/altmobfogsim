package api.dynamic.mobility

import api.dynamic.connectivity.entities.DynamicGatewayConnectionDeviceImpl
import api.dynamic.mobility.entities.MobileDeviceImpl
import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.models.SteadyMobilityModel
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Position
import api.dynamic.mobility.positioning.PositionAndTimestamp
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.sqrt

class MobileDeviceTest: BaseFogDeviceTest() {
    private fun createMobileDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
        initPositionAndTimestamp: PositionAndTimestamp, mobilityModel: MobilityModel
    ): MobileDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            MobileDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips,
                initPositionAndTimestamp, mobilityModel
            )
        }
    }

    @Test
    fun test1() {
        /* Expected Mob1 moving every second*/
        init()
        val dev = createMobileDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01,
            PositionAndTimestamp(Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0), 1.0),
            SteadyMobilityModel()
        )
        fogDeviceList.add(dev)

        dev.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }
}
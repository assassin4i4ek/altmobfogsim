package api2.dynamic.mobility

import api2.dynamic.mobility.entities.MobileDeviceImpl
import api2.dynamic.mobility.models.MobilityModel
import api2.dynamic.mobility.models.SteadyMobilityModel
import api2.dynamic.mobility.positioning.Coordinates
import api2.dynamic.mobility.positioning.Position
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.sqrt

class MobileDeviceTest: BaseFogDeviceTest() {
    private fun createMobileDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
        initPosition: Position, mobilityModel: MobilityModel
    ): MobileDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            MobileDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips,
                initPosition, mobilityModel
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
            Position(Coordinates(0.0, 0.0), sqrt(2.0), 45.0),
            SteadyMobilityModel(1.0)
        )
        fogDeviceList.add(dev)

        dev.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }
}
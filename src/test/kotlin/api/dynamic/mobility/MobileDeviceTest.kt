package api.dynamic.mobility

import api.dynamic.mobility.entities.MobileDeviceImpl
import api.dynamic.mobility.models.MobilityModel
import api.dynamic.mobility.models.SteadyMobilityModel
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.Position
import org.fog.utils.TimeKeeper
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.assertEquals

class MobileDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
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

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(9, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 0.8045) < 1e-6)
            assertEquals(Coordinates(10.0, 10.0), dev.position.coordinates)
        }
    }
}
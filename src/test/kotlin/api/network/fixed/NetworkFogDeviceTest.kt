package api.network.fixed

import api.network.fixed.entities.NetworkDeviceImpl
import org.fog.utils.TimeKeeper
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.abs
import kotlin.test.assertEquals

class NetworkFogDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createNetworkDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): NetworkDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            NetworkDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        init()
        val dev = createNetworkDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
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
        }
    }
}
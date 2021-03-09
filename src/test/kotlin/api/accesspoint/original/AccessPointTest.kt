package api.accesspoint.original

import api.accesspoint.original.entities.AccessPointImpl
import api.accesspoint.original.entities.AccessPointsMap
import api.mobility.positioning.Coordinates
import api.mobility.positioning.RadialZone
import api.network.fixed.entities.NetworkDeviceImpl
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.TimeKeeper
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import kotlin.math.abs
import kotlin.test.assertEquals

class AccessPointTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createNetworkFogDevice(
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
        val apm = AccessPointsMap()
        val ap = Coordinates(0.0, 0.0).let {
            AccessPointImpl("AccessPoint", it, RadialZone(it, 1.0),
                apm,1000.0, 1000.0, 0.1, FogLinearPowerModel(100.0, 40.0)
            )
        }
        val dev = createNetworkFogDevice("Mob1", 10.0, 1000.0, 1000.0,
        0.1, 0.01)
        fogDeviceList.addAll(listOf(ap, dev))

        ap.parentId = cloud.id
        dev.parentId = ap.mId
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", dev.name)
        mm.addModuleToDevice("AppModule2", cloud.name)

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1, apm.getClosestAccessPointsTo(Coordinates(0.0, 0.0)).size)
            assertEquals(8, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 1.0065) < 1e-6)
        }
    }
}
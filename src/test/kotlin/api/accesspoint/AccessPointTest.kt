package api.accesspoint

import api.accesspoint.entities.AccessPointImpl
import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.RadialZone
import api.network.entities.NetworkDeviceImpl
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy

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
        val ap = Coordinates(0.0, 0.0).let {
            AccessPointImpl("AccessPoint", it, RadialZone(it, 1.0),
                1000.0, 1000.0, 0.1
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
    }
}
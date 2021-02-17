package api.network

import api.network.entities.NetworkFogDeviceImpl
import api.original.entities.OriginalFogDeviceImpl
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy

class NetworkFogDeviceTest: BaseFogDeviceTest() {

    private fun createNetworkFogDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): NetworkFogDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            NetworkFogDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        init()
        val dev = createNetworkFogDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.add(dev)

        dev.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }
}
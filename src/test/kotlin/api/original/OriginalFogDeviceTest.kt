package api.original

import api.original.entities.OriginalFogDeviceImpl
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy

class OriginalFogDeviceTest: BaseFogDeviceTest() {

    private fun createOriginalFogDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): OriginalFogDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            OriginalFogDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        init()
        val dev = createOriginalFogDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.add(dev)

        dev.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }

    @Test
    fun test2() {
        init(2)
        val dev1 = createOriginalFogDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.add(dev1)
        val dev2 = createOriginalFogDevice(
            "Mob2", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.add(dev2)

        dev1.parentId = cloud.id
        dev2.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(dev1, 0)
        connectSensorsAndActuatorsToDevice(dev2, 1)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule1", "Mob2")
        mm.addModuleToDevice("AppModule2", "cloud")
    }
}
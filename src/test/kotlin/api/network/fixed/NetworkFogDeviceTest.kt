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

    @Test
    fun test2() {
        init(4)
        val dev1 = createNetworkDevice(
                "Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val dev2 = createNetworkDevice(
                "Mob2", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val dev3 = createNetworkDevice(
                "Mob3", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val dev4 = createNetworkDevice(
                "Mob4", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val gw = createNetworkDevice("GW1", 10.0, 1000.0, 1000.0,
        0.1, 0.01)
        val serv = createNetworkDevice("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)

        fogDeviceList.addAll(listOf(dev1, dev2, dev3, dev4, gw, serv))

        dev1.parentId = gw.id
        dev2.parentId = gw.id
        dev3.parentId = gw.id
        dev4.parentId = gw.id
        gw.parentId = serv.id
        connectSensorsAndActuatorsToDevice(dev1, 0)
        connectSensorsAndActuatorsToDevice(dev2, 1)
        connectSensorsAndActuatorsToDevice(dev3, 2)
        connectSensorsAndActuatorsToDevice(dev4, 3)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule1", "Mob2")
        mm.addModuleToDevice("AppModule1", "Mob3")
        mm.addModuleToDevice("AppModule1", "Mob4")
        mm.addModuleToDevice("AppModule2", "Server1")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(35, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 0.9344) < 1e-6)
        }
    }
}
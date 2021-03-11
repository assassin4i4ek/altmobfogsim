package api.addressing.fixed

import api.addressing.fixed.entities.AddressingDeviceImpl
import api.common.utils.ConnectionUtils
import org.fog.utils.Config
import org.fog.utils.TimeKeeper
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import utils.createThreeModulesApp
import java.lang.Exception
import kotlin.math.abs
import kotlin.test.assertEquals

class AddressingDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createAddressingDevice(
            name: String, schedulingInterval: Double,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): AddressingDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            AddressingDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Test
    fun test1() {
        init()
        val dev = createAddressingDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv = createAddressingDevice("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, serv))

        dev.parentId = serv.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(9, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 0.805) < 1e-6)
        }
    }

    @Test
    fun test2() {
        init()
        val dev = createAddressingDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv = createAddressingDevice("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, serv))

        dev.parentId = serv.id
        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        assertThrows<Exception> {
            launchTest {}
        }
    }

    @Test
    fun test3() {
        init()
        val dev = createAddressingDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv1 = createAddressingDevice("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv2 = createAddressingDevice("Server2", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv3 = createAddressingDevice("Server3", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv4 = createAddressingDevice("Server4", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, serv1, serv2, serv3, serv4))

        connectSensorsAndActuatorsToDevice(dev, 0)
        dev.parentId = serv1.id
        ConnectionUtils.connectPeerToPeer(serv1, serv2, 0.1)
        ConnectionUtils.connectPeerToPeer(serv2, serv3, 0.1)
        ConnectionUtils.connectPeerToPeer(serv3, serv4, 0.1)
        ConnectionUtils.connectPeerToPeer(serv4, serv1, 10.0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server4")

        sensorList[0].transmitDistribution = DeterministicDistribution(5.0)

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 1.411) < 1e-6)
        }
    }

    @Test
    fun test4() {
        init(createApp = ::createThreeModulesApp)
        val dev = createAddressingDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01
        )
        val serv1 = createAddressingDevice("Cluster1.Server1",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv2 = createAddressingDevice("Cluster1.Server2",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv3 = createAddressingDevice("Cluster1.Server3",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv4 = createAddressingDevice("Cluster1.Server4",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv5 = createAddressingDevice("Cluster1.Server5",
                10.0, 1000.0, 1000.0,0.1, 0.01
        )
        val serv6 = createAddressingDevice("Cluster2.Server6",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv7 = createAddressingDevice("Cluster2.Server7",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv8 = createAddressingDevice("Cluster2.Server8",
                10.0, 1000.0, 1000.0,100.0, 0.01
        )
        val serv9 = createAddressingDevice("Cluster2.Server9",
                10.0, 1000.0, 1000.0,0.1, 0.01
        )
        fogDeviceList.addAll(listOf(dev, serv1, serv2, serv3, serv4, serv5, serv6, serv7, serv8, serv9))

        connectSensorsAndActuatorsToDevice(dev, 0)
        dev.parentId = serv1.id
        ConnectionUtils.connectPeerToPeer(serv1, serv2, 0.1)
        ConnectionUtils.connectPeerToPeer(serv1, serv4, 10.0)
        ConnectionUtils.connectPeerToPeer(serv2, serv3, 0.1)
        ConnectionUtils.connectPeerToPeer(serv3, serv4, 0.1)
        ConnectionUtils.connectPeerToPeer(serv4, serv5, 0.1)
        ConnectionUtils.connectPeerToPeer(serv2, serv5, 10.0)
        serv5.parentId = serv6.id
        ConnectionUtils.connectPeerToPeer(serv6, serv7, 0.1)
        ConnectionUtils.connectPeerToPeer(serv6, serv8, 10.0)
        ConnectionUtils.connectPeerToPeer(serv7, serv8, 0.1)
        ConnectionUtils.connectPeerToPeer(serv8, serv9, 0.1)
        ConnectionUtils.connectPeerToPeer(serv7, serv9, 10.0)

        sensorList[0].transmitDistribution = DeterministicDistribution(5.0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Cluster1.Server3")
        mm.addModuleToDevice("AppModule3", "Cluster2.Server9")

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 2.823) < 1e-6)
        }
    }

    @Test
    fun test5() {

    }
}
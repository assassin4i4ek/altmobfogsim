package api.addressing.dynamic

import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import addons.addressing.dynamic.producer.entities.DynamicAddressingNotificationProducerDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.common.utils.ConnectionUtils
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.utils.TimeKeeper
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy
import utils.createThreeModulesApp
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DynamicAddressingDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createServer(
            name: String, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
            uplinkLatency: Double, ratePerMips: Double, addressingType: AddressingDevice.AddressingType
    ): DynamicAddressingNotificationConsumerDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            DynamicAddressingNotificationConsumerDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, addressingType
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createMobileDevice(
            name: String, schedulingInterval: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
            uplinkLatency: Double, ratePerMips: Double
    ): DynamicAddressingNotificationProducerDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            DynamicAddressingNotificationProducerDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, AddressingDevice.AddressingType.HIERARCHICAL
            )
        }
    }

    @Test
    fun test1() {
        init()
        val dev = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01, AddressingDevice.AddressingType.PEER_TO_PEER)
        fogDeviceList.addAll(listOf(dev, serv))

        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")
        launchTest {
            val loopId = app.loops[0].loopId
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assertNull(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
        }
    }

    @Test
    fun test2() {
        init()
        val dev = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01, AddressingDevice.AddressingType.PEER_TO_PEER)
        fogDeviceList.addAll(listOf(dev, serv))

        connectSensorsAndActuatorsToDevice(dev, 0)
        dev.parentId = serv.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")
        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(9, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 0.805) < 1e-6)
        }
    }

    @Test
    fun test3() {
        init()
        val dev = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01, AddressingDevice.AddressingType.PEER_TO_PEER)
        fogDeviceList.addAll(listOf(dev, serv))

        connectSensorsAndActuatorsToDevice(dev, 0)
        dev.parentId = serv.id

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        object: SimEntity("tester") {
            override fun startEntity() {
                send(id, 5.0, 1, null)
            }

            override fun processEvent(p0: SimEvent?) {
                dev.mDynamicParentId = -1
            }

            override fun shutdownEntity() {}

        }

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(4, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 0.805) < 1e-6)
        }
    }

    @Test
    fun test4() {
        init()
        val dev = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)
        val serv = createServer("Server1", 10.0, 1000.0, 1000.0,
                0.1, 0.01, AddressingDevice.AddressingType.PEER_TO_PEER)
        fogDeviceList.addAll(listOf(dev, serv))

        connectSensorsAndActuatorsToDevice(dev, 0)
        dev.parentId = serv.id
        dev.uplinkLatency = 5.0

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "Server1")

        object: SimEntity("tester") {
            override fun startEntity() {
                send(id, 5.0, 1, null)
            }

            override fun processEvent(p0: SimEvent?) {
                ConnectionUtils.disconnectChildFromParent(serv, dev)
            }

            override fun shutdownEntity() {}

        }

        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(4, dev.producerNotifications.size)
            assertNull(TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assertNull(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
        }
    }

    @Test
    fun test5() {
        init(createApp = ::createThreeModulesApp)
        val dev = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0,
                0.1, 0.01)
        val serv1 = createServer("Cluster1.Server1",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv2 = createServer("Cluster1.Server2",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv3 = createServer("Cluster1.Server3",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv4 = createServer("Cluster1.Server4",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv5 = createServer("Cluster1.Server5",
                10.0, 1000.0, 1000.0,0.1, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv6 = createServer("Cluster2.Server6",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv7 = createServer("Cluster2.Server7",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv8 = createServer("Cluster2.Server8",
                10.0, 1000.0, 1000.0,100.0, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
        )
        val serv9 = createServer("Cluster2.Server9",
                10.0, 1000.0, 1000.0,0.1, 0.01,
                AddressingDevice.AddressingType.PEER_TO_PEER
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

        object: SimEntity("tester") {
            override fun startEntity() {
                send(id, 6.0, 1, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, dev)
                    send(id, 3.0, 2, null)
                }
                else {
                    ConnectionUtils.connectChildToParent(serv6, dev)
                }
            }

            override fun shutdownEntity() {}

        }

        launchTest {
            val loopId = app.loops[0].loopId
            assert(dev.producerNotifications.isEmpty())
            fogDeviceList.filterIsInstance<DynamicAddressingNotificationConsumerDeviceImpl>().forEach {
                assert(it.consumerNotifications.isEmpty())
            }
            assertEquals(1, TimeKeeper.getInstance().loopIdToCurrentNum[loopId])
            assert(abs(TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! - 4.605) < 1e-6)
        }
    }
}
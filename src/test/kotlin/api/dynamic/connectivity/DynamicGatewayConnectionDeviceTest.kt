package api.dynamic.connectivity

import api.dynamic.connectivity.behavior.DynamicGatewayConnectionBehavior
import api.dynamic.connectivity.entities.DynamicGatewayConnectionDeviceImpl
import api.network.entities.NetworkFogDeviceImpl
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.placement.Controller
import org.junit.jupiter.api.Test
import utils.BaseFogDeviceTest
import utils.createCharacteristicsAndAllocationPolicy

class DynamicGatewayConnectionDeviceTest: BaseFogDeviceTest() {
    private fun createDynamicGatewayConnectionDevice(
        name: String, schedulingInterval: Double,
        uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): DynamicGatewayConnectionDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            DynamicGatewayConnectionDeviceImpl(
                name, it.first, it.second, emptyList(), schedulingInterval,
                uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

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
        /* Expected normal behavior */
        init()
        val dev = createDynamicGatewayConnectionDevice(
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
        /* Expected north queue growth for Mob1 */
        init()
        val dev = createDynamicGatewayConnectionDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.add(dev)

        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")
    }

    @Test
    fun test3() {
        /* Expected Mob1 to transmit all tuples only after 5.0 */
        init()
        val dev = createDynamicGatewayConnectionDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )

        val gateway = createNetworkFogDevice("Gateway", 10.0, 1000.0, 1000.0,
        0.1, 0.01
        )
        fogDeviceList.addAll(listOf(gateway, dev))
        gateway.parentId = cloud.id

        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        object: SimEntity("tester") {
            override fun startEntity() {
                send(id, 5.0, 1, null)
            }

            override fun processEvent(p0: SimEvent?) {
                DynamicGatewayConnectionBehavior.connectChildToParent(gateway, dev)
            }

            override fun shutdownEntity() {}
        }
    }

    @Test
    fun test4() {
        /* Expected Mob1 to transmit tuples before 3.0, then to queue tuples until 6.0, then send others */
        init()
        val dev = createDynamicGatewayConnectionDevice(
            "Mob1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        val gateway1 = createNetworkFogDevice("Gateway1", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        val gateway2 = createNetworkFogDevice("Gateway2", 10.0, 1000.0, 1000.0,
            0.1, 0.01
        )
        fogDeviceList.addAll(listOf(gateway1, gateway2, dev))

        gateway1.parentId = cloud.id
        gateway2.parentId = cloud.id
        dev.parentId = gateway1.id

        connectSensorsAndActuatorsToDevice(dev, 0)

        mm.addModuleToDevice("AppModule1", "Mob1")
        mm.addModuleToDevice("AppModule2", "cloud")

        object: SimEntity("tester") {
            override fun startEntity() {
                send(id, 3.0, 1, null)
            }

            override fun processEvent(ev: SimEvent) {
                when (ev.tag) {
                    1 -> {
                        DynamicGatewayConnectionBehavior.disconnectChildFromParent(gateway1, dev)
                        send(id, 3.0, 2, null)
                    }
                    2 -> {
                        DynamicGatewayConnectionBehavior.connectChildToParent(gateway2, dev)
                    }
                }
            }

            override fun shutdownEntity() {}
        }
    }
}
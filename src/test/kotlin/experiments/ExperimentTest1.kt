package experiments

import api.accesspoint.original.entities.AccessPointConnectedDeviceImpl
import api.accesspoint.original.entities.AccessPointImpl
import api.accesspoint.original.entities.AccessPointsMap
import api.mobility.models.MobilityModel
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Position
import api.mobility.positioning.RadialZone
import api.mobility.positioning.Zone
import api.network.fixed.entities.NetworkDeviceImpl
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.power.models.PowerModel
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Sensor
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacement
import org.fog.placement.ModulePlacementEdgewards
import org.fog.placement.ModulePlacementMapping
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.*
import org.fog.utils.distribution.DeterministicDistribution
import org.junit.jupiter.api.Test
import utils.BaseExperimentTest
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.test.assertEquals

class ExperimentTest1: BaseExperimentTest() {
    @Test
    fun test01() {
        init(10.0)
        createFogDevices(10.0, 1)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(23.737580490555537, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.332455921428585E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3490063.293779984, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(5305.2, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test02() {
        init(10.0)
        createFogDevices(10.0, 1)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(231.23521437767323, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.37117097321431E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3496717.6791937635, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(39833.0, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test03() {
        init(10.0)
        createFogDevices(10.0, 2)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(24.286715721866074, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3327340314285936E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6983289.279419976, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(9620.4, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test04() {
        init(10.0)
        createFogDevices(10.0, 2)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(231.33344285408927, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4158811225000383E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6995754.831303791, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(82699.3, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test05() {
        init(10.0)
        createFogDevices(10.0, 4)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(24.111829106410774, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3332902514286151E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3966656099899927E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(18250.8, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test06() {
        init(10.0)
        createFogDevices(10.0, 4)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(231.18234377072426, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.438813887500047E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3990168079218825E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(181966.8, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test07() {
        init(10.0)
        createFogDevices(10.0, 8)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(23.772051146268833, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3344026914286556E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7927961937919892E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(35511.6, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test08() {
        init(10.0)
        createFogDevices(10.0, 8)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(908.393770299444, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4407105643530035E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7952633279918373E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(413261.4, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test09() {
        init(5.0)
        createFogDevices(5.0, 1)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(31.97531766023216, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3324315614285782E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3497125.6280600335, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1073146.1618600108, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(8800.4, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test10() {
        init(5.0)
        createFogDevices(5.0, 1)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(225.89552713840246, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4030679178571757E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3497675.7858000416, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(78954.2, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test11() {
        init(5.0)
        createFogDevices(5.0, 2)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(32.279843791438026, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3327096714285936E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6992501.85366007, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(2146220.606620014, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(16630.7, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test12() {
        init(5.0)
        createFogDevices(5.0, 2)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(226.0508736335263, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4411798317857856E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6998057.028760155, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(165648.9, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test13() {
        init(5.0)
        createFogDevices(5.0, 4)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(32.07413098817, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3332658914286092E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3986499933180125E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(4292553.57003003, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(32244.3, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test14() {
        init(5.0)
        createFogDevices(5.0, 4)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(226.18001607977308, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4425835144358134E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3995580919467691E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(363053.4, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test15() {
        init(5.0)
        createFogDevices(5.0, 8)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(32.71418370481086, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.334378331428655E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.79722008381603E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(8585310.338510059, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(63571.5, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    @Test
    fun test16() {
        init(5.0)
        createFogDevices(5.0, 8)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(true) {
            val loopId = app.loops.first().loopId
            assertEquals(2993.4148283607797, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4441914751595853E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7977617310780127E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(707318.7, (NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME))
        }
    }

    private fun createFogDevices(eegTransRate: Double, numGateways: Int, numMobilesPerGateway: Int = 4) {
        val cloud = createNetworkFogDevice("cloud", 16*2800.0, 16*4000, 100.0, 10000.0, 0.01,
                16*90.5, 16*83.25)
        cloud.parentId = -1
        fogDevices.add(cloud)
        val proxy = createNetworkFogDevice("proxy-server", 2800.0, 4000, 10000.0, 10000.0, 0.0,
                107.339, 83.4333)
        proxy.parentId = cloud.id
        proxy.uplinkLatency = 100.0
        fogDevices.add(proxy)

        val apm = AccessPointsMap()

        for (i in 0 until numGateways) {
            val gw = createNetworkFogDevice("d-$i", 2800.0, 4000, 10000.0, Double.POSITIVE_INFINITY, 0.0,
                    107.339, 83.4333)
            gw.parentId = proxy.id
            gw.uplinkLatency = 4.0
            fogDevices.add(gw)

            val sideLength = ceil(sqrt(numMobilesPerGateway.toDouble())).toInt()
            for (x in 0 until sideLength) {
                for (y in if (x == 0 || x == sideLength - 1) {
                    0 until sideLength
                } else {
                    IntProgression.fromClosedRange(0, sideLength, sideLength - 1)
                }) {
                    val apCoord = Coordinates(100.0 * (i + x.toDouble() / sideLength), 100.0 * y.toDouble() / sideLength)
                    val ap = createAccessPoint("ap-$i-$x-$y",
                            apCoord,
                            RadialZone(apCoord, 50.0 / sideLength),
                            apm, Double.POSITIVE_INFINITY, 10000.0, 0.0, FogLinearPowerModel(1.0, 1.0)
                    )
                    ap.parentId = gw.id
                    ap.uplinkLatency = 0.0
                    fogDevices.add(ap)
//                    println("Created ${ap.name} with coordinates $apCoord and radius ${50.0 / sideLength}")
                }
            }
            for (j in 0 until numMobilesPerGateway) {
                val initCoord = Coordinates(100.0 * i, 0.0)
                val speed = if (sideLength > 1) (100.0 / sideLength) / eegTransRate else 0.0
                val initPosition = Position(initCoord, speed, 0.0)
//                println("Created Mobile device with position $initPosition")
                val mobilityModel = ConnectionAwareMobilityModel(2.0, i, sideLength)
                val mob = createMobileFogDevice("m-$i-$j", 1000.0, 1000, 10000.0, 270.0,
                        0.0, 87.53, 82.44, initPosition, mobilityModel, apm)
                mobilityModel.device = mob
                mob.parentId = gw.id
                mob.uplinkLatency = 2.0
                fogDevices.add(mob)
                val eegSensor = Sensor("s-$i-$j", "EEG", broker.id, app.appId, DeterministicDistribution(eegTransRate))
                eegSensor.gatewayDeviceId = mob.id
                eegSensor.latency = 6.0
                sensors.add(eegSensor)
                val display = Actuator("a-$i-$j", broker.id, app.appId, "DISPLAY")
                display.gatewayDeviceId = mob.id
                display.latency = 1.0
                actuators.add(display)
            }
        }
    }

    private fun createNetworkFogDevice(name: String, mips: Double, ram: Int, upBw: Double, downBw: Double, ratePerMips: Double,
                                       busyPower: Double, idlePower: Double): FogDevice {
        val peList = listOf(Pe(0, PeProvisionerOverbooking(mips)))
        val storage = 1000000L
        val bw = 10000L
        val host = PowerHost(FogUtils.generateEntityId(), RamProvisionerSimple(ram), BwProvisionerOverbooking(bw), storage,
                peList, StreamOperatorScheduler(peList), FogLinearPowerModel(busyPower, idlePower))
        val hostList = listOf(host)
        val arch = "x86"
        val os = "Linux"
        val vmm = "Xen"
        val timezone = 10.0
        val cost = 3.0
        val costPerMem = 0.05
        val costPerStorage = 0.001
        val costPerBw = 0.0
        val storageList = emptyList<Storage>()
        val characteristics = FogDeviceCharacteristics(arch, os, vmm, host, timezone, cost, costPerMem, costPerStorage, costPerBw)
        return NetworkDeviceImpl(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0, upBw, downBw, 0.0, ratePerMips)
    }

    override fun placeModules(isCloud: Boolean): ModulePlacement {
        val moduleMapping = ModuleMapping.createModuleMapping()
        return if (isCloud) {
            moduleMapping.addModuleToDevice("connector", "cloud")
            moduleMapping.addModuleToDevice("concentration_calculator", "cloud")
            fogDevices.filter { it.name.startsWith("m-") }.forEach { moduleMapping.addModuleToDevice("client", it.name) }
            ModulePlacementMapping(fogDevices, app, moduleMapping)
        } else {
            moduleMapping.addModuleToDevice("connector", "cloud")
            ModulePlacementEdgewards(fogDevices, sensors, actuators, app, moduleMapping)
        }.also {
            fogDevices.filter { it.name.startsWith("m-") }.forEach {
                val parent = CloudSim.getEntity(it.parentId) as FogDevice
                parent.childrenIds.remove(it.id)
                parent.childToLatencyMap.remove(it.id)
                it.parentId = -1
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun createAccessPoint(name: String, coordinates: Coordinates, connectionZone: Zone,
                                  accessPointsMap: AccessPointsMap, upBw: Double, downBw: Double, uplinkLatency: Double, powerModel: PowerModel): AccessPointImpl {
        return AccessPointImpl(name, upBw, downBw, uplinkLatency, powerModel, coordinates, connectionZone, accessPointsMap)
    }

    @Suppress("SameParameterValue")
    private fun createMobileFogDevice(name: String, mips: Double, ram: Int, upBw: Double, downBw: Double, ratePerMips: Double,
                                      busyPower: Double, idlePower: Double, initPosition: Position, mobilityModel: MobilityModel,
                                      accessPointsMap: AccessPointsMap): AccessPointConnectedDeviceImpl {
        val peList = listOf(Pe(0, PeProvisionerOverbooking(mips)))
        val storage = 1000000L
        val bw = 10000L
        val host = PowerHost(FogUtils.generateEntityId(), RamProvisionerSimple(ram), BwProvisionerOverbooking(bw), storage,
                peList, StreamOperatorScheduler(peList), FogLinearPowerModel(busyPower, idlePower))
        val hostList = listOf(host)
        val arch = "x86"
        val os = "Linux"
        val vmm = "Xen"
        val timezone = 10.0
        val cost = 3.0
        val costPerMem = 0.05
        val costPerStorage = 0.001
        val costPerBw = 0.0
        val storageList = emptyList<Storage>()
        val characteristics = FogDeviceCharacteristics(arch, os, vmm, host, timezone, cost, costPerMem, costPerStorage, costPerBw)
        return AccessPointConnectedDeviceImpl(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0,
                upBw, downBw, 0.0, ratePerMips, initPosition, mobilityModel, accessPointsMap)
    }
}
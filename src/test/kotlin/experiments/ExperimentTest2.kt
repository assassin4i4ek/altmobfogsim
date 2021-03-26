package experiments

import api.accesspoint.addressing.entities.AddressingAccessPointConnectedDeviceImpl
import api.accesspoint.addressing.entities.AddressingAccessPointImpl
import api.accesspoint.original.entities.AccessPointsMap
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.mobility.models.MobilityModel
import api.mobility.positioning.Coordinates
import api.mobility.positioning.Position
import api.mobility.positioning.RadialZone
import api.mobility.positioning.Zone
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
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.test.assertEquals

class ExperimentTest2: BaseExperimentTest() {
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
            assertEquals(5305.2, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(230.80233742462622, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3586141907143041E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3493160.041508759, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(38641.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(9620.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(231.21970051504167, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4157348678357594E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6985135.758996267, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(76458.7, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(18250.8, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(231.00629340704612, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4377591833929375E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.396567021016507E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(151784.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(35511.6, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(230.86525095769775, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4416203306001177E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7926640110184792E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(304981.0, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(35.56561596638971, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3324315614285782E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3499272.4475400383, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1073093.569320024, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(6655.6, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(225.64119292787365, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3910454381928943E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3493526.1041287677, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(77273.8, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(36.33900446614838, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3327096714285936E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6999184.616460084, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(2146335.3539800514, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(12331.7, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(225.87969759816892, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.440351702814361E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6990021.741616319, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(152656.6, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(35.96908268416765, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3332658914286092E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3997947312640158E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(4292744.815630103, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(23665.6, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(225.865204157136, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4411387131000692E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.398604869068274E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(304054.8, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(36.10655997769389, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.334378331428655E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.799665170170034E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(8585483.654835211, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(46359.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(226.04703267120678, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4452692798358638E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7970859014905527E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(607843.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
        }
    }

    private fun createFogDevices(eegTransRate: Double, numGateways: Int, numMobilesPerGateway: Int = 4) {
        val cloud = createNetworkAddressingFogDevice("cloud", 16*2800.0, 16*4000, 100.0, 10000.0, 0.01,
                16*90.5, 16*83.25, AddressingDevice.AddressingType.HIERARCHICAL)
        cloud.parentId = -1
        fogDevices.add(cloud)
        val proxy = createNetworkAddressingFogDevice("proxy-server", 2800.0, 4000, 10000.0, 10000.0, 0.0,
                107.339, 83.4333, AddressingDevice.AddressingType.HIERARCHICAL)
        proxy.parentId = cloud.id
        proxy.uplinkLatency = 100.0
        fogDevices.add(proxy)

        val apm = AccessPointsMap()

        for (i in 0 until numGateways) {
            val gw = createNetworkAddressingFogDevice("d-$i", 2800.0, 4000, 10000.0, Double.POSITIVE_INFINITY, 0.0,
                    107.339, 83.4333, AddressingDevice.AddressingType.HIERARCHICAL)
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
                    val ap = createAddressingAccessPoint("ap-$i-$x-$y",
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
                val mob = createAddressingMobileFogDevice("m-$i-$j", 1000.0, 1000, 10000.0, 270.0,
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

    private fun createNetworkAddressingFogDevice(name: String, mips: Double, ram: Int, upBw: Double, downBw: Double, ratePerMips: Double,
                                                 busyPower: Double, idlePower: Double, addressingType: AddressingDevice.AddressingType): FogDevice {
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
        return DynamicAddressingNotificationConsumerDeviceImpl(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0, upBw, downBw, 0.0, ratePerMips, addressingType)
    }

    @Suppress("SameParameterValue")
    private fun createAddressingAccessPoint(name: String, coordinates: Coordinates, connectionZone: Zone,
                                            accessPointsMap: AccessPointsMap, upBw: Double, downBw: Double, uplinkLatency: Double, powerModel: PowerModel): AddressingAccessPointImpl {
        return AddressingAccessPointImpl(name, upBw, downBw, uplinkLatency, powerModel, coordinates, connectionZone, accessPointsMap)
    }

    @Suppress("SameParameterValue")
    private fun createAddressingMobileFogDevice(name: String, mips: Double, ram: Int, upBw: Double, downBw: Double, ratePerMips: Double,
                                                busyPower: Double, idlePower: Double, initPosition: Position, mobilityModel: MobilityModel,
                                                accessPointsMap: AccessPointsMap): AddressingAccessPointConnectedDeviceImpl {
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
        return AddressingAccessPointConnectedDeviceImpl(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0,
                upBw, downBw, 0.0, ratePerMips, initPosition, mobilityModel, accessPointsMap)
    }
}
package experiments

import addons.accesspoint.addressing.entities.AddressingAccessPointConnectedDeviceImpl
import addons.accesspoint.addressing.entities.AddressingAccessPointImpl
import api.accesspoint.original.utils.AccessPointsMap
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
import kotlin.test.assertEquals

class ExperimentTest3: BaseExperimentTest() {
    @Test
    fun test01() {
        init(10.0)
        createFogDevices(10.0, 1)
        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        Config.MAX_SIMULATION_TIME = 10000
        launchTest(false) {
            val loopId = app.loops.first().loopId
            assertEquals(24.150529428173023, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.332456501428578E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3491978.8541999697, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
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
            assertEquals(231.0028269317392, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3641765771428782E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3493054.154875022, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(38619.7, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(24.18312582225397, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3327346114285903E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6983645.945899969, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
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
            assertEquals(231.37144595219317, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4170686673214793E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6986042.6977412775, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(77209.65, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(24.35782029672379, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3332908314286109E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3970330122979943E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
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
            assertEquals(231.21819850957064, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.439589487321497E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3967696238218775E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(152888.05, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(23.9555918082616, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3344032714286517E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7929430871199876E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
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
            assertEquals(231.09573226033027, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.442194160228698E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7931936162474025E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(304698.85, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(34.75604359368722, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3324315614285782E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3489821.5544100422, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1073242.8091900032, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(6671.9, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(225.6998253262155, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3922717968214666E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(3494409.7300375323, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(834332.9999999987, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(76419.25, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(33.9390895449427, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3332630535714664E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6978393.508730082, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(2146257.3189450065, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(21116.0, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(226.0948135462271, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4404360277715227E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(6989328.094231322, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(1668665.9999999974, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(152532.6, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(33.95572192568406, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.3365180757144557E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3956824205000173E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(4292730.984475022, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(73023.0,round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(226.10721936561276, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4411351135786392E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(1.3986969187915223E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(3337331.999999995, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(305186.4, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(34.147880585843765, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.348919511429255E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7918653600420382E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(8585408.18112503, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(264211.25, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
            assertEquals(226.34455014494725, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId])
            assertEquals(1.4452870021501692E7, fogDevices.find { it.name == "cloud" }!!.energyConsumption)
            assertEquals(2.7975115389339123E7, fogDevices.filter { it.name.startsWith("m-") }.map { it.energyConsumption }.sum())
            assertEquals(6674663.999999991, fogDevices.filter { it.name.startsWith("d-") }.map { it.energyConsumption }.sum())
            assertEquals(609462.7, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / (Config.MAX_SIMULATION_TIME) * 100) / 100)
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
        val numAccessPoints = (ceil(numMobilesPerGateway.toDouble() * numGateways / 4) * 4).toInt()
        val sideLength = ((numAccessPoints.toDouble() + 4) / 4).toInt()
//        val sideLength = ceil(sqrt(numMobilesPerGateway.toDouble() * numGateways)).toInt()
//        val numAccessPoints = 4 * sideLength - 4

        val gateways = MutableList(numGateways) { i ->
            val gw = createNetworkAddressingFogDevice("d-$i", 2800.0, 4000, 10000.0, 20000.0, 0.0,
                    107.339, 83.4333, AddressingDevice.AddressingType.HIERARCHICAL)
            gw.parentId = proxy.id
            gw.uplinkLatency = 4.0
            fogDevices.add(gw)
            gw
        }

        val apPerGwCounters = MutableList(numGateways) {0}
        for (j in 0 until numAccessPoints) {
            apPerGwCounters[j % numGateways]++
        }

        val accessPoints = MutableList(numAccessPoints) { j ->
            val gwIndex = apPerGwCounters.indexOfFirst { it > 0 }
            apPerGwCounters[gwIndex]--
            val gw = gateways[gwIndex]
            val x = when {
                j < sideLength -> j * 100.0 / sideLength
                j < 2 * sideLength - 1 -> (sideLength - 1) * 100.0 / sideLength
                j < 3 * sideLength - 2 -> (3 * (sideLength - 1) - j) * 100.0 / sideLength
                else -> 0.0
            }
            val y = when {
                j < sideLength -> 0.0
                j < 2 * sideLength - 1 -> (j - sideLength + 1) * 100.0 / sideLength
                j < 3 * sideLength - 2 -> (sideLength - 1) * 100.0 / sideLength
                else -> (4 * (sideLength - 1) - j) * 100.0 / sideLength
            }
            val coordinates = Coordinates(x, y)
            val ap = createAddressingAccessPoint("ap-${gwIndex}-${apPerGwCounters[gwIndex]}",
                    coordinates, RadialZone(coordinates, 50.0 / sideLength), apm, 20000.0, 10000.0, 0.0,
                    FogLinearPowerModel(1.0, 1.0)
            )
            fogDevices.add(ap)
            ap.parentId = gw.id
            ap.uplinkLatency = 1.0
            println("${ap.name}: ${ap.coordinates}")
            ap
        }

        for (j in 0 until numMobilesPerGateway * numGateways) {
            val ap = accessPoints[j]
            val coord = ap.coordinates
            val speed = if (sideLength > 1) (10.0 / sideLength) / eegTransRate else 0.0
            val orientation = when {
                j < sideLength - 1 -> 0.0
                j < 2 * sideLength - 2 -> 90.0
                j < 3 * sideLength - 3 -> 180.0
                else -> -90.0
            }
            println("$coord: $orientation")
            val initPosition = Position(coord, speed, orientation)
            val mobilityModel = ConnectionAwareMobilityModel(1.0, 0, sideLength)
            val mob = createAddressingMobileFogDevice("m-$j", 1000.0, 1000, 10000.0, 270.0,
                    0.0, 87.53, 82.44, initPosition, mobilityModel, apm)
            mobilityModel.device = mob
            mob.parentId = ap.parentId
            mob.uplinkLatency = 1.0
            fogDevices.add(mob)
            val eegSensor = Sensor("s-$j", "EEG", broker.id, app.appId, DeterministicDistribution(eegTransRate))
            eegSensor.gatewayDeviceId = mob.id
            eegSensor.latency = 6.0
            sensors.add(eegSensor)
            val display = Actuator("a-$j", broker.id, app.appId, "DISPLAY")
            display.gatewayDeviceId = mob.id
            display.latency = 1.0
            actuators.add(display)
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
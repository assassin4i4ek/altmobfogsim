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
import org.fog.application.Application
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
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.FogUtils
import org.fog.utils.distribution.DeterministicDistribution
import kotlin.math.ceil

class Experiment4(resultsPath: String?, isWarmup: Boolean, isLog: Boolean, seed: Long, eegTransRates: DoubleArray, totalGatewaysCount: IntArray, numMobilesPerGateway: Int, isCloudCount: BooleanArray)
    : Experiment(resultsPath, isWarmup, isLog, seed, eegTransRates, totalGatewaysCount, numMobilesPerGateway, isCloudCount,
) {
    override fun createAllDevices(numGateways: Int, numMobilesPerGateway: Int, brokerId: Int, appId: String, eegTransRate: Double): Triple<List<FogDevice>, List<Sensor>, List<Actuator>> {
        val fogDevices = mutableListOf<FogDevice>()
        val sensors = mutableListOf<Sensor>()
        val actuators = mutableListOf<Actuator>()
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
            val eegSensor = Sensor("s-$j", "EEG", brokerId, appId, DeterministicDistribution(eegTransRate))
            eegSensor.gatewayDeviceId = mob.id
            eegSensor.latency = 6.0
            sensors.add(eegSensor)
            val display = Actuator("a-$j", brokerId, appId, "DISPLAY")
            display.gatewayDeviceId = mob.id
            display.latency = 1.0
            actuators.add(display)
        }

        return Triple(fogDevices, sensors, actuators)
    }

    override fun placeModules(isCloud: Boolean, fogDevices: List<FogDevice>, app: Application, sensors: List<Sensor>, actuators: List<Actuator>): ModulePlacement {
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
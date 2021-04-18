package experiments

import api.accesspoint.original.entities.AccessPointConnectedDeviceImpl
import api.accesspoint.original.entities.AccessPointImpl
import api.accesspoint.utils.AccessPointsMap
import api.mobility.models.MobilityModel
import api.common.positioning.Coordinates
import api.common.positioning.Position
import api.common.positioning.RadialZone
import api.common.positioning.Zone
import api.network.fixed.entities.NetworkDeviceImpl
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
import kotlin.math.sqrt

class Experiment2(resultsPath: String?, isWarmup: Boolean, isLog: Boolean, seed: Long, eegTransRates: DoubleArray, totalGatewaysCount: IntArray, numMobilesPerGateway: Int, isCloudCount: BooleanArray)
    : Experiment(resultsPath, isWarmup, isLog, seed, eegTransRates, totalGatewaysCount, numMobilesPerGateway, isCloudCount) {
    override fun createAllDevices(numGateways: Int, numMobilesPerGateway: Int, brokerId: Int, appId: String, eegTransRate: Double):
            Triple<List<FogDevice>, List<Sensor>, List<Actuator>> {
        val fogDevices = mutableListOf<FogDevice>()
        val sensors = mutableListOf<Sensor>()
        val actuators = mutableListOf<Actuator>()
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
                            apCoord, RadialZone(apCoord, 50.0 / sideLength), 2.0,
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
                val eegSensor = Sensor("s-$i-$j", "EEG", brokerId, appId, DeterministicDistribution(eegTransRate))
                eegSensor.gatewayDeviceId = mob.id
                eegSensor.latency = 6.0
                sensors.add(eegSensor)
                val display = Actuator("a-$i-$j", brokerId, appId, "DISPLAY")
                display.gatewayDeviceId = mob.id
                display.latency = 1.0
                actuators.add(display)
            }
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

    @Suppress("SameParameterValue")
    private fun createAccessPoint(name: String, coordinates: Coordinates, connectionZone: Zone, downlinkLatency: Double,
                                  accessPointsMap: AccessPointsMap, upBw: Double, downBw: Double, uplinkLatency: Double, powerModel: PowerModel): AccessPointImpl {
        val peList = listOf(Pe(0, PeProvisionerOverbooking(0.0)))
        val storage = 1000000L
        val bw = 10000L
        val host = PowerHost(FogUtils.generateEntityId(), RamProvisionerSimple(0), BwProvisionerOverbooking(bw), storage,
                peList, StreamOperatorScheduler(peList), powerModel)
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
        return AccessPointImpl(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0, upBw, downBw, uplinkLatency, 0.0, coordinates, connectionZone, downlinkLatency, accessPointsMap)
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
package experiments1

import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.application.Application
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacement
import org.fog.placement.ModulePlacementEdgewards
import org.fog.placement.ModulePlacementMapping
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.*
import org.fog.utils.distribution.DeterministicDistribution

class Experiment1(
        resultsPath: String?, isWarmup: Boolean, isLog: Boolean, seed: Long, eegTransRates: DoubleArray,
        totalGatewaysCount: IntArray, numMobilesPerGateway: Int, isCloudCount: BooleanArray)
    : Experiment(resultsPath, isWarmup, isLog, seed, eegTransRates, totalGatewaysCount, numMobilesPerGateway, isCloudCount, ) {
    override fun createAllDevices(numGateways: Int, numMobilesPerGateway: Int, brokerId: Int, appId: String, eegTransRate: Double):
            Triple<List<FogDevice>, List<Sensor>, List<Actuator>> {
        val fogDevices = mutableListOf<FogDevice>()
        val sensors = mutableListOf<Sensor>()
        val actuators = mutableListOf<Actuator>()
        val cloud = createFogDevice("cloud", 16*2800.0, 16*4000, 100.0, 10000.0, 0.01,
                16*90.5, 16*83.25  )
        cloud.parentId = -1
        fogDevices.add(cloud)
        val proxy = createFogDevice("proxy-server", 2800.0, 4000, 10000.0, 10000.0, 0.0,
                107.339, 83.4333)
        proxy.parentId = cloud.id
        proxy.uplinkLatency = 100.0
        fogDevices.add(proxy)

        for (i in 0 until numGateways) {
            val gw = createFogDevice("d-$i", 2800.0, 4000, 10000.0, 10000.0, 0.0,
                    107.339, 83.4333)
            gw.parentId = proxy.id
            gw.uplinkLatency = 4.0
            fogDevices.add(gw)
            for (j in 0 until numMobilesPerGateway) {
                val mob = createFogDevice("m-$i-$j", 1000.0, 1000, 10000.0, 270.0,
                        0.0, 87.53, 82.44)
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

    private fun createFogDevice(name: String, mips: Double, ram: Int, upBw: Double, downBw: Double, ratePerMips: Double,
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
        return FogDevice(name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0, upBw, downBw, 0.0, ratePerMips)
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
        }
    }
}




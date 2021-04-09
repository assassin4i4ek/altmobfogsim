package experiments2

import addons.accesspoint.addressing.entities.AddressingAccessPointConnectedDeviceImpl
import addons.accesspoint.addressing.entities.AddressingAccessPointImpl
import api.accesspoint.original.utils.AccessPointsMap
import api.accesspoint.original.utils.AccessPointsMapUtils
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.addressing.fixed.entities.AddressingDeviceImpl
import api.mobility.entities.MobileDeviceImpl
import api.mobility.models.CsvInputMobilityModelFactory
import api.mobility.models.MobilityModel
import api.common.positioning.Coordinates
import api.common.positioning.PolygonalZone
import api.common.positioning.Position
import api.common.positioning.RadialZone
import api.common.utils.DeterministicDistributionWithRandomOffset
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.application.selectivity.SelectivityModel
import org.fog.entities.*
import org.fog.placement.Controller
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacement
import org.fog.placement.ModulePlacementMapping
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.*
import org.fog.utils.distribution.DeterministicDistribution
import java.lang.Exception
import java.util.*
import kotlin.math.ceil

fun main() {
    val randSeed = 1234L
    setMathRandSeed(randSeed)
    val modelTimeUnitsPerSec = 1000.0 // one CloudSim tick == 1 ms
    CloudSim.init(1, Calendar.getInstance(), false)
    Log.disable()
    Logger.ENABLED = true

    val cloud = createCharacteristicsAndAllocationPolicy(100000.0).let {
        DynamicAddressingNotificationConsumerDeviceImpl("cloud", it.first, it.second, emptyList(), 10.0,
                1000.0, 1000.0, 0.0, 0.01, AddressingDevice.AddressingType.HIERARCHICAL)
    }

    val apm = AccessPointsMap()
    val accessPoints = AccessPointsMapUtils.generateRadialZonesFromCsv("input/accesspoints.csv")
            .mapIndexed { i: Int, connectionZoneWithType: Triple<String, RadialZone, Int> ->
                val (apDownlinkLatency, apDownlinkBandwidth) = when (connectionZoneWithType.third) {
                    3 -> 250.0 /*latency (sec)*/ * modelTimeUnitsPerSec to 2.5e6 /*Mbits per sec*/ / modelTimeUnitsPerSec
                    4 -> 75.0 /*latency (sec)*/ * modelTimeUnitsPerSec to 25e6 /*Mbits per sec*/ / modelTimeUnitsPerSec
                    else -> throw Exception("Unknown Base station type ${connectionZoneWithType.third}")
                }
                val ap = createCharacteristicsAndAllocationPolicy(1.0).let {
                    AddressingAccessPointImpl(connectionZoneWithType.first, it.first, it.second, emptyList(), 10.0,
                            1000.0, apDownlinkBandwidth, 0.0, 0.01,
                            connectionZoneWithType.second.center.copy(), connectionZoneWithType.second, apDownlinkLatency, apm)
                }
                ap.parentId = cloud.id
                ap.uplinkLatency = 0.1 * modelTimeUnitsPerSec
                ap
            }
    val mobiles = CsvInputMobilityModelFactory.fromDirectory("input/gps_data", modelTimeUnitsPerSec, 2).map { (name, mobilityModel) ->
        createCharacteristicsAndAllocationPolicy(2.65 /*DMIPS per MHz*/ * 2000.0 /*MHz*/ / modelTimeUnitsPerSec).let {
            AddressingAccessPointConnectedDeviceImpl(name, it.first, it.second, emptyList(), 10.0,
                    1000.0, 1000.0, 10.0 * modelTimeUnitsPerSec, 0.0,
                    Position(Coordinates(-1000.0, -1000.0), 0.0, 0.0),
                    mobilityModel, apm
            )
        }
    }

    val brokers = mutableListOf<FogBroker>()
    val applications = mutableListOf<Application>()
    val sensors = mutableListOf<Sensor>()
    val actuators = mutableListOf<Actuator>()
    val mappings = mutableMapOf<String, ModuleMapping>()
    mobiles.forEachIndexed { i, mobile ->
        val broker = FogBroker("Broker$i")
        val app = createMedicalApplication(i, broker.id)
        val sensor = Sensor("PatientSensor$i", "PATIENT_SENSOR$i", broker.id, app.appId, DeterministicDistribution(2.0 * modelTimeUnitsPerSec))
        val actuator = Actuator("PatientAlarm$i", broker.id, app.appId, "PATIENT_ALARM$i")
        val moduleMapping = ModuleMapping.createModuleMapping()

        sensor.gatewayDeviceId = mobile.id
        sensor.latency = (app.edgeMap["PATIENT_SENSOR$i"]!!.tupleNwLength / (6000.0 /*bytes per second*/ * 8 /*bits per byte*/)) * modelTimeUnitsPerSec
        actuator.gatewayDeviceId = mobile.id
        actuator.latency = (app.edgeMap["PATIENT_ALARM$i"]!!.tupleNwLength / (6000.0 /*bytes per second*/ * 8 /*bits per byte*/)) * modelTimeUnitsPerSec
        moduleMapping.addModuleToDevice("client$i", mobile.mName)
        moduleMapping.addModuleToDevice("patient_state_analyzer$i", cloud.mName)
        moduleMapping.addModuleToDevice("heart_attack_determiner$i", cloud.mName)
        moduleMapping.addModuleToDevice("patient_disease_history$i", cloud.mName)

        brokers.add(broker)
        applications.add(app)
        sensors.add(sensor)
        actuators.add(actuator)
        mappings[app.appId] = moduleMapping
    }

    val fogDevices = mutableListOf<FogDevice>()

    fogDevices.add(cloud)
    fogDevices.addAll(accessPoints)
    fogDevices.addAll(mobiles)

    val controller = MyTestController("Controller", fogDevices, sensors, actuators) {
        println(accessPoints.map { it.name to it.mChildrenIds.size }.toMap())
        println()
    }
    applications.forEach { app ->
        controller.submitApplication(app, ModulePlacementMapping(fogDevices, app, mappings[app.appId]))
    }

    object: SimEntity("tester") {
        override fun startEntity() {
            send(id, 0.0, 0, null)
        }

        override fun processEvent(p0: SimEvent?) {
            println(CloudSim.clock())
            send(id, 1000.0 * modelTimeUnitsPerSec, 0, null)
        }

        override fun shutdownEntity() {}

    }

    Config.MAX_SIMULATION_TIME = (/*36000*/ 100 * modelTimeUnitsPerSec).toInt()
    CloudSim.startSimulation()
}

fun createCharacteristicsAndAllocationPolicy(mips: Double): Pair<FogDeviceCharacteristics, VmAllocationPolicy> {
    val peList = listOf(Pe(0, PeProvisionerOverbooking(mips))) // need to store Pe id and MIPS Rating
    val hostId = FogUtils.generateEntityId()
    val ram = 2048 // host memory (MB)
    val storage: Long = 1000000 // host storage
    val bw = 10000
    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw.toLong()),storage,
            peList, StreamOperatorScheduler(peList), FogLinearPowerModel(100.0, 40.0)
    )
    val hostList = listOf(host)

    val arch = "x86" // system architecture
    val os = "Linux" // operating system
    val vmm = "Xen"
    val timeZone = 10.0 // time zone this resource located
    val cost = 3.0 // the cost of using processing in this resource
    val costPerMem = 0.05 // the cost of using memory in this resource
    val costPerStorage = 0.001 // the cost of using storage in this resource
    val costPerBw = 0.0 // the cost of using bw in this resource

    return Pair(FogDeviceCharacteristics(
            arch, os, vmm, host, timeZone, cost, costPerMem,
            costPerStorage, costPerBw
    ), AppModuleAllocationPolicy(hostList))
}

fun createMedicalApplication(i: Int, userId: Int): Application {
    val app = Application.createApplication("HeartDiseaseMonitoringApplication$i", userId)

    app.addAppModule("client$i", 30)
    app.addAppModule("patient_state_analyzer$i", 50)
    app.addAppModule("heart_attack_determiner$i", 30)
    app.addAppModule("patient_disease_history$i", 100)
    app.addAppModule("doctors_module$i", 70)
    app.addAppModule("patients_treatment_plan$i", 50)
    // all network length values are in bits
    app.addAppEdge("PATIENT_SENSOR$i", "client$i", 500.0, 16896.0, "PATIENT_SENSOR$i", Tuple.UP, AppEdge.SENSOR)
    app.addAppEdge("client$i", "patient_state_analyzer$i", 2000.0, 26424.0, "PATIENT_STATE$i", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("patient_state_analyzer$i", "patient_disease_history$i", 5000.0, 8192.0, "PATIENT_HISTORY_RECORD_REQUEST$i", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("patient_disease_history$i", "patient_state_analyzer$i", 1000.0, 819200.0, "PATIENT_HISTORY_RECORD_RESPONSE$i", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("patient_state_analyzer$i", "heart_attack_determiner$i", 1000.0, 108344.0, "HEART_ATTACK_DETERMINATION$i", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("heart_attack_determiner$i", "client$i", 250.0, 800.0, "HEART_ATTACK_PATIENT_ALARM$i", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("client$i", "PATIENT_ALARM$i", 100.0, 80.0, "PATIENT_ALARM$i", Tuple.DOWN, AppEdge.ACTUATOR)

    app.addTupleMapping("client$i", "PATIENT_SENSOR$i", "PATIENT_STATE$i", FractionalSelectivity(1.0))
    app.addTupleMapping("client$i", "HEART_ATTACK_PATIENT_ALARM$i", "PATIENT_ALARM$i", FractionalSelectivity(1.0))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "PATIENT_HISTORY_RECORD_REQUEST$i", FractionalSelectivity(1.0))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(0.0))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(1.0))
    app.addTupleMapping("patient_disease_history$i", "PATIENT_HISTORY_RECORD_REQUEST$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", FractionalSelectivity(1.0))
    app.addTupleMapping("heart_attack_determiner$i", "HEART_ATTACK_DETERMINATION$i", "HEART_ATTACK_PATIENT_ALARM$i", FractionalSelectivity(1.0))

    app.loops.add(AppLoop(listOf("client$i", "patient_state_analyzer$i", "heart_attack_determiner$i", "client$i")))

    return app
}

private class MyTestController(
        name: String, fogDevices: List<FogDevice>, sensors: List<Sensor>, actuators: List<Actuator>,
        val onStopSimulation: () -> Unit
) : Controller(name, fogDevices, sensors, actuators) {
    override fun processEvent(ev: SimEvent) {
        if (ev.tag == FogEvents.STOP_SIMULATION) {
            onStopSimulation()
        }
        super.processEvent(ev)
    }
}

fun setMathRandSeed(seed: Long) {
    val randField = Math::class.java.declaredClasses.find { it.simpleName == "RandomNumberGeneratorHolder" }!!
            .getDeclaredField("randomNumberGenerator")
    randField.isAccessible = true
    val randObj = randField.get(null) as Random
    randObj.setSeed(seed)
}
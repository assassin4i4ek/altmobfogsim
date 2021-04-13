package experiments2

import addons.accesspoint_addressingdynamic.entities.AddressingAccessPointConnectedDeviceImpl
import addons.accesspoint_addressingdynamic_migrationoriginal.entities.MigrationSupportingAddressingAccessPointImpl
import addons.addressingdynamic_migrationoriginal.entities.DynamicAddressingMigrationSupportingDeviceImpl
import api.accesspoint.original.utils.AccessPointsMap
import api.accesspoint.original.utils.AccessPointsMapUtils
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.common.positioning.Coordinates
import api.common.positioning.Position
import api.common.positioning.RadialZone
import api.migration.models.mapo.CentralizedMapoModel
import api.migration.models.mapo.normalizers.MinMaxNormalizer
import api.migration.models.mapo.objectives.MinCostObjective
import api.migration.models.mapo.objectives.MinProcessingTimeObjective
import api.migration.models.mapo.problems.SingleInstanceModulePlacementProblem
import api.migration.models.timeprogression.FixedWithOffsetTimeProgression
import api.migration.utils.MigrationLogger
import api.migration.utils.MigrationRequest
import api.mobility.models.CsvInputMobilityModelFactory
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
import org.fog.entities.*
import org.fog.placement.Controller
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementMapping
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.*
import org.fog.utils.distribution.DeterministicDistribution
import java.util.*
import kotlin.math.max

fun main() {
    val randSeed = 12345L
    setMathRandSeed(randSeed)
    val modelTimeUnitsPerSec = 1000.0 // one CloudSim tick == 1 ms
    CloudSim.init(1, Calendar.getInstance(), false)
    Log.disable()
    Logger.ENABLED = true

    val mapoModelMaxEvaluations = 20000
    val populationSize = 400
    val cloudNumPes = 8 * 64
    val numMobiles = 10
    val cloudRatePerMips = 168050.0 /*USD per year */ / 15200 /* MIPS */ / (365 * 24 * 3600) /* sec */
    val cloud = createCharacteristicsAndAllocationPolicy(
            300000.0 /*Intel Core i7-5960x 3.5GHz 2014y*/ / 16 /*threads*/ / modelTimeUnitsPerSec, cloudNumPes,
            189.0 /*Watts 100% load for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
            69.0 /*Watts idle for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec
    ).let {
        /*val ratePerMips = 168050 /* USD/year per equivalent application in AWS */ /
                35720e3 /* USD/year in mainframe */ *
                (1.0 - (3678.0 - 2700.0) / (3 * 3678.0)) /* annual decrease in USD/MIPS ( calculated for 3 year period) */
                        .pow(6) /* 2017, 2018, 2019, 2020, 2021, 2022 = 6 years */ /
                (365 * 24 * 3600) /*days/hour/sec*/ *
                100.0 /*USD to cents*/*/
        DynamicAddressingMigrationSupportingDeviceImpl("cloud", it.first, it.second, emptyList(), 10.0,
                2.5e9 /* 2.5 Gbps link*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                2.5e9 /* 2.5 Gbps link*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                0.0, cloudRatePerMips, AddressingDevice.AddressingType.HIERARCHICAL,
                CentralizedMapoModel(
                        true,  FixedWithOffsetTimeProgression(
                        2.0 * modelTimeUnitsPerSec, 200.0 * modelTimeUnitsPerSec),
                        listOf(
//                                MinCostObjective(),
                                MinProcessingTimeObjective()
                        ),
                        SingleInstanceModulePlacementProblem.Factory(), mapoModelMaxEvaluations, populationSize,
                        MinMaxNormalizer(), randSeed
                )
        )
    }

    val apm = AccessPointsMap()
    val accessPoints = AccessPointsMapUtils.generateRadialZonesFromCsv("input/accesspoints.csv")
            .map { connectionZoneWithType: Triple<String, RadialZone, Int> ->
                val (apUplinkLatency, apDownlinkLatency, apUplinkBandwidth, apDownlinkBandwidth) = when (connectionZoneWithType.third) {
                    3,4 -> BaseStationSpecification(
                            /*total latency 200ms = 90 + 10 + 10 + 90*/
                            10e-3 * modelTimeUnitsPerSec,
                            90.0e-3  * modelTimeUnitsPerSec,
                            34.368e6 /*E3 interface IMA technology Mbits per sec*/ * 0.85 /* 85% efficiency throughput*/ / modelTimeUnitsPerSec,
                            2.5e6 /*Mbits per sec*/ / modelTimeUnitsPerSec,
                    )
//                    4 -> BaseStationSpecification(
//                            /*total latency 100 ms = 45 + 5 + 5 + 45*/
//                            5e-3 * modelTimeUnitsPerSec,
//                            45.0e-3 * modelTimeUnitsPerSec,
//                            2.5e9 /*2.5 Gbit Ethernet*/ * 0.85 /* 85% efficiency throughput*/ / modelTimeUnitsPerSec,
//                            25e6 /*Mbits per sec*/ / modelTimeUnitsPerSec
//                    )
                    else -> throw Exception("Unknown Base station type ${connectionZoneWithType.third}")
                }
                val apRatePerMips = cloudRatePerMips * 2
                val apNumPes = max(cloudNumPes / 2, 1)
                val ap = createCharacteristicsAndAllocationPolicy(
                        300000.0 /*Intel Core i7-5960x 3.5GHz 2014y*/ / 16 /*threads*/ / modelTimeUnitsPerSec, apNumPes,
                        189.0 /*Watts 100% load for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
                        69.0 /*Watts idle for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec
                ).let {
                    MigrationSupportingAddressingAccessPointImpl(connectionZoneWithType.first, it.first, it.second, emptyList(), 10.0,
                            apUplinkBandwidth, apDownlinkBandwidth, apUplinkLatency, apRatePerMips,
                            connectionZoneWithType.second.center.copy(), connectionZoneWithType.second, apDownlinkLatency, apm,
                            CentralizedMapoModel(false))
                }
                ap
            }
    val ispGateway = createCharacteristicsAndAllocationPolicy(0.0, 1, 0.0, 0.0).let {
        DynamicAddressingNotificationConsumerDeviceImpl("ISP_Gateway", it.first, it.second, emptyList(), 10.0,
                39.81e9 /*39.81 Gbps STM-256 bandwidth*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                accessPoints.map { ap -> ap.uplinkBandwidth }.sum(),
                25e-3 /* 50 ms delay between AWS EC2 DC and ISP*/ * modelTimeUnitsPerSec,
                0.0, AddressingDevice.AddressingType.HIERARCHICAL
        )
    }
    ispGateway.parentId = cloud.id
    accessPoints.forEach { ap ->
        ap.parentId = ispGateway.id
    }
    val mobiles = CsvInputMobilityModelFactory.fromDirectory("input/gps_data"/*"E:\\monaco_sumo\\scenario\\gps_data_new"*/, modelTimeUnitsPerSec, numMobiles).map { (name, mobilityModel) ->
        createCharacteristicsAndAllocationPolicy(
                2.65 /*Cortex-A55 DMIPS per MHz*/ * 2000.0 /*MHz*/ / modelTimeUnitsPerSec, 1,
                0.85 /* Watts in idle mode */ * 0.85 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec,
                0.02 /* Watts in idle mode */ * 0.9 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec).let {
            AddressingAccessPointConnectedDeviceImpl(name, it.first, it.second, emptyList(), 10.0,
                    1000.0, 0.0, 10.0 * modelTimeUnitsPerSec, 0.0,
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
//        moduleMapping.addModuleToDevice("patient_state_analyzer$i", "3gBaseStation_1_3")
        moduleMapping.addModuleToDevice("heart_attack_determiner$i", cloud.mName)
        moduleMapping.addModuleToDevice("patient_disease_history$i", cloud.mName)
        cloud.migrationModel.allowMigrationForModule("patient_state_analyzer$i")
        cloud.migrationModel.allowMigrationForModule("heart_attack_determiner$i")
        cloud.migrationModel.allowMigrationForModule("patient_disease_history$i")

        brokers.add(broker)
        applications.add(app)
        sensors.add(sensor)
        actuators.add(actuator)
        mappings[app.appId] = moduleMapping
    }

    val fogDevices = mutableListOf<FogDevice>()

    fogDevices.add(cloud)
    fogDevices.add(ispGateway)
    fogDevices.addAll(accessPoints)
    fogDevices.addAll(mobiles)

    val controller = MyTestController("Controller", fogDevices, sensors, actuators) {
        println()
        println("Base station: number of connected clients")
        println(accessPoints.joinToString("") {
            if (it.childrenIds.size > 1) {
                "${it.name}: ${it.childrenIds.size - 1}\n"
            }
            else ""
        })
        println("Migrations")
        println(MigrationLogger.migrations.groupBy { it.modelTime }.toList().joinToString("") { logEntriesOfModelTime ->
            logEntriesOfModelTime.second.sortedBy { it.migrationRequest.appModuleName }.joinToString("") { logEntry ->
                if (logEntry.migrationRequest.type != MigrationRequest.Type.REMOVE_ALL_INSTANCES) {
                    "${logEntry.modelTime}: ${logEntry.migrationRequest.appModuleName} from ${logEntry.migrationRequest.from} to ${logEntry.migrationRequest.to}\n"
                }
                else ""
            }
        })
    }
    applications.forEach { app ->
        controller.submitApplication(app, ModulePlacementMapping(fogDevices, app, mappings[app.appId]))
    }

    Config.RESOURCE_MGMT_INTERVAL = (0.1 * modelTimeUnitsPerSec)
    Config.MAX_SIMULATION_TIME = (/*36000*/ 20 * modelTimeUnitsPerSec).toInt()
    CloudSim.startSimulation()
}

fun createCharacteristicsAndAllocationPolicy(mips: Double, numPes: Int, maxPowerPerCore: Double, idlePowerPerCore: Double): Pair<FogDeviceCharacteristics, VmAllocationPolicy> {
    val peList = List(numPes) { i -> Pe(i, PeProvisionerOverbooking(mips)) }
    val hostId = FogUtils.generateEntityId()
    val ram: Int = Int.MAX_VALUE // host memory (MB)
    val storage: Long = Long.MAX_VALUE // host storage
    val bw: Long = Long.MAX_VALUE
    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw),storage,
            peList, StreamOperatorScheduler(peList),
            FogLinearPowerModel(maxPowerPerCore * numPes, idlePowerPerCore * numPes)
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
        super.processEvent(ev)
        if (ev.tag == FogEvents.STOP_SIMULATION) {
            onStopSimulation()
        }
    }
}

fun setMathRandSeed(seed: Long) {
    val randField = Math::class.java.declaredClasses.find { it.simpleName == "RandomNumberGeneratorHolder" }!!
            .getDeclaredField("randomNumberGenerator")
    randField.isAccessible = true
    val randObj = randField.get(null) as Random
    randObj.setSeed(seed)
}

private data class BaseStationSpecification(
        val uplinkLatency: Double,
        val downlinkLatency: Double,
        val uplinkBandwidth: Double,
        val downlinkBandwidth: Double
)
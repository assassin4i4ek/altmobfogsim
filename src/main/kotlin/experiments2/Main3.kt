package experiments2

import addons.accesspoint_addressingdynamic_migrationoriginal.entities.MigrationStimulatorAddressingAccessPointConnectedDeviceImpl
import addons.accesspoint_addressingdynamic_migrationoriginal.entities.MigrationSupportingAddressingAccessPointImpl
import addons.addressingdynamic_migrationoriginal.entities.DynamicAddressingMigrationSupportingDeviceImpl
import api.accesspoint.utils.AccessPointEventsLogger
import api.accesspoint.utils.AccessPointsMap
import api.accesspoint.utils.AccessPointsMapUtils
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDeviceImpl
import api.addressing.fixed.entities.AddressingDevice
import api.common.positioning.Coordinates
import api.common.positioning.Position
import api.common.positioning.RadialZone
import api.migration.models.mapo.CentralizedMapoModel
import api.migration.models.mapo.ideals.ClosestToEdgeEndEnvironmentBuilder
import api.migration.models.mapo.ideals.ClosestToEdgeStartEnvironmentBuilder
import api.migration.models.mapo.ideals.MinCostIdealEnvironmentBuilder
import api.migration.models.mapo.normalizers.MinMaxNormalizer
import api.migration.models.mapo.objectives.MinNetworkUsageObjective
import api.migration.models.mapo.problems.SingleInstanceIdealInjectingModulePlacementProblem
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.utils.MigrationLogger
import api.migration.utils.MigrationRequest
import api.mobility.models.CsvInputMobilityModelFactory
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementMapping
import org.fog.utils.Config
import org.fog.utils.Logger
import org.fog.utils.NetworkUsageMonitor
import org.fog.utils.TimeKeeper
import org.fog.utils.distribution.DeterministicDistribution
import java.util.*
import kotlin.math.max
import kotlin.math.min

fun main() {
    val numMobiles = 10
    val populationSize = 100
    val maxEvals = 2000
    val inject = 0.6
    experiment3(numMobiles, populationSize, maxEvals, inject)
}

fun experiment3(numMobiles: Int, populationSize: Int, mapoModelMaxEvaluations: Int, injectedSolutionsFraction: Double) {
    val randSeed = 12345L
    setMathRandSeed(randSeed)
    val modelTimeUnitsPerSec = 1000.0 // one CloudSim tick == 1 ms
    CloudSim.init(1, Calendar.getInstance(), false)
    Log.disable()
//    Logger.ENABLED = true
    Logger.ENABLED = false
    AccessPointEventsLogger.enabled = true
    MigrationLogger.enabled = true

    val numOfInjectedSolutions = max(1, min((populationSize * injectedSolutionsFraction).toInt(), populationSize - 1))
    val cloudNumPes = 16 * 128
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
                        true, FixedTimeProgression(Double.MAX_VALUE),
                        listOf(
//                                MinCostObjective(),
//                                MinProcessingTimeObjective(),
                                MinNetworkUsageObjective()
                        ),
                        SingleInstanceIdealInjectingModulePlacementProblem.Factory(listOf(
                                MinCostIdealEnvironmentBuilder(),
                                ClosestToEdgeStartEnvironmentBuilder(),
                                ClosestToEdgeEndEnvironmentBuilder(),
                        ), numOfInjectedSolutions),
                        mapoModelMaxEvaluations, populationSize,
                        MinMaxNormalizer(), randSeed, true
                )
        )
    }
    val apm = AccessPointsMap()
    val accessPoints = AccessPointsMapUtils.generateRadialZonesFromCsv("input/accesspoints.csv")
            .map { connectionZoneWithType: Triple<String, RadialZone, Int> ->
                val (apUplinkLatency, apDownlinkLatency, apUplinkBandwidth, apDownlinkBandwidth) = when (connectionZoneWithType.third) {
                    3 -> BaseStationSpecification(
                            /*total latency 200ms = 90 + 10 + 10 + 90*/
                            10e-3 * modelTimeUnitsPerSec,
                            90.0e-3  * modelTimeUnitsPerSec,
                            34.368e6 /*E3 interface IMA technology Mbits per sec*/ * 0.85 /* 85% efficiency throughput*/ / modelTimeUnitsPerSec,
                            2.5e6 /*Mbits per sec*/ / modelTimeUnitsPerSec,
                    )
                    4 -> BaseStationSpecification(
                            /*total latency 100 ms = 45 + 5 + 5 + 45*/
                            5e-3 * modelTimeUnitsPerSec,
                            45.0e-3 * modelTimeUnitsPerSec,
                            2.5e9 /*2.5 Gbit Ethernet*/ * 0.85 /* 85% efficiency throughput*/ / modelTimeUnitsPerSec,
                            25e6 /*Mbits per sec*/ / modelTimeUnitsPerSec
                    )
                    else -> throw Exception("Unknown Base station type ${connectionZoneWithType.third}")
                }
                val apRatePerMips = cloudRatePerMips * 1.25
                val apNumPes = 16 * 4//max(cloudNumPes / 8, 1)
                val ap = createCharacteristicsAndAllocationPolicy(
                        300000.0 /*Intel Core i7-5960x 3.5GHz 2014y*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
                        apNumPes,
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
//    val cdn = createCharacteristicsAndAllocationPolicy(1000.0, 1, 0.0, 0.0).let {
//        DynamicAddressingNotificationConsumerDeviceImpl("ContentDeliveryNetwork", it.first, it.second, emptyList(), 10.0,
//                39.81e9 /*39.81 Gbps STM-256 bandwidth*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
//                39.81e9 * 0.85 / modelTimeUnitsPerSec,
//                0.0,0.0, AddressingDevice.AddressingType.HIERARCHICAL
//        )
//    }
    val hospital = run {
        val hospitalNumPes = 16 * 4
        createCharacteristicsAndAllocationPolicy(300000.0 /*Intel Core i7-5960x 3.5GHz 2014y*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
                hospitalNumPes,
                189.0 /*Watts 100% load for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
                69.0 /*Watts idle for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec
        ).let {
            DynamicAddressingNotificationConsumerDeviceImpl("Hospital", it.first, it.second, emptyList(), 10.0,
                    1.0e9 /*100 Mbps STM-256 bandwidth*/ * 0.94 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                    1.0e9 /*100 Mbps STM-256 bandwidth*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                    5e-3 /* 5 ms delay between AWS EC2 DC and ISP*/ * modelTimeUnitsPerSec,
                    0.0, AddressingDevice.AddressingType.HIERARCHICAL
            )
        }
    }
    val ispGateway = createCharacteristicsAndAllocationPolicy(0.0, 1, 0.0, 0.0).let {
        DynamicAddressingNotificationConsumerDeviceImpl("ISP_Gateway", it.first, it.second, emptyList(), 10.0,
                39.81e9 /*39.81 Gbps STM-256 bandwidth*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                accessPoints.sumOf { ap -> ap.uplinkBandwidth } + hospital.uplinkBandwidth,
                70e-3 /* 50 ms delay between AWS EC2 DC and ISP*/ * modelTimeUnitsPerSec,
                0.0, AddressingDevice.AddressingType.HIERARCHICAL
        )
    }
    val mobiles = CsvInputMobilityModelFactory.fromDirectory("input/gps_data"/*"E:\\monaco_sumo\\scenario\\gps_data_new"*/, modelTimeUnitsPerSec, numMobiles).map { (name, mobilityModel) ->
        createCharacteristicsAndAllocationPolicy(
                2.65 /*Cortex-A55 DMIPS per MHz*/ * 2000.0 /*MHz*/ / modelTimeUnitsPerSec, 1,
                0.85 /* Watts in idle mode */ * 0.85 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec,
                0.02 /* Watts in idle mode */ * 0.9 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec).let {
            MigrationStimulatorAddressingAccessPointConnectedDeviceImpl(name, it.first, it.second, emptyList(), 10.0,
                    1000.0, 0.0, 10.0 * modelTimeUnitsPerSec, 0.0,
                    Position(Coordinates(-1000.0, -1000.0), 0.0, 0.0),
                    mobilityModel, apm, cloud.id
            )
        }
    }

    ispGateway.parentId = cloud.id
    hospital.parentId = ispGateway.id
//    cdn.parentId = ispGateway.id
    accessPoints.forEach { ap ->
        ap.parentId = ispGateway.id
    }

    val brokers = mutableListOf<FogBroker>()
    val applications = mutableListOf<Application>()
    val sensors = mutableListOf<Sensor>()
    val actuators = mutableListOf<Actuator>()
    val mappings = mutableMapOf<String, ModuleMapping>()

    mobiles.forEachIndexed { i, mobile ->
        val broker = FogBroker("MobileBroker$i")
        val app = createMedicalApplication3(i, broker.id)
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
        moduleMapping.addModuleToDevice("patient_disease_history$i", hospital.mName)
        cloud.migrationModel.allowMigrationForModule("patient_state_analyzer$i")
        cloud.migrationModel.allowMigrationForModule("heart_attack_determiner$i")
//        cloud.migrationModel.allowMigrationForModule("patient_disease_history$i")

        brokers.add(broker)
        applications.add(app)
        sensors.add(sensor)
        actuators.add(actuator)
        mappings[app.appId] = moduleMapping
    }
//    accessPoints.forEachIndexed { i, ap ->
//        val mediaTriggerPeriod = 0.05 //100ms
//        val broker = FogBroker("AccessPointBroker$i")
//        val app = createSmallMediaApplication(i, broker.id, mediaTriggerPeriod)
//        val mediaTrigger = Sensor("MediaTrigger$i","MEDIA_TRIGGER$i", broker.id, app.appId, DeterministicDistributionWithOffset(Math.random() * mediaTriggerPeriod * modelTimeUnitsPerSec,mediaTriggerPeriod * modelTimeUnitsPerSec))
//        val mediaConsumer = Actuator("MediaConsumer$i", broker.id, app.appId, "MEDIA_CONSUMER$i")
//        val moduleMapping = ModuleMapping.createModuleMapping()
//        mediaTrigger.gatewayDeviceId = ap.mId
//        mediaTrigger.latency = 0.0
//        mediaConsumer.gatewayDeviceId = ap.mId
//        mediaTrigger.latency = 0.0
//        moduleMapping.addModuleToDevice("media_consumer_proxy$i", ap.mName)
//        moduleMapping.addModuleToDevice("media_provider$i", cdn.mName)
//
//        brokers.add(broker)
//        applications.add(app)
//        sensors.add(mediaTrigger)
//        actuators.add(mediaConsumer)
//        mappings[app.appId] = moduleMapping
//    }

    val fogDevices = mutableListOf<FogDevice>()

    fogDevices.add(cloud)
    fogDevices.add(hospital)
    fogDevices.add(ispGateway)
//    fogDevices.add(cdn)
    fogDevices.addAll(accessPoints)
    fogDevices.addAll(mobiles)

    val controller = MyTestController("Controller", fogDevices, sensors, actuators) {
//        val dateFormat = SimpleDateFormat("HH:mm:ss")
        println()
        println("Base station: number of connected clients")
        println(accessPoints.joinToString("") {
            if (it.childrenIds.size > 1) {
                "${it.name}: ${it.childrenIds.size - 1}\n"
            }
            else ""
        })
        println("Base station events")
        println(AccessPointEventsLogger.connections.groupBy { it.modelTime }.values.joinToString("\n") { logEntries ->
            logEntries.sortedBy { it.connectedDevice.mName }.joinToString("\n")
        })
        AccessPointEventsLogger.clear()
        println("Migrations")
        println(MigrationLogger.migrations.groupBy { it.modelTime }.values.joinToString("") { logEntries ->
            logEntries.sortedBy { it.migrationRequest.appId }.joinToString("") { logEntry ->
                if (logEntry.migrationRequest.type != MigrationRequest.Type.REMOVE_ALL_INSTANCES) {
                    logEntry.toString() + "\n"
                }
                else ""
            }
        })
        MigrationLogger.clear()
        println("Avg delay for loops: ${
            TimeKeeper.getInstance().loopIdToCurrentAverage.values.run {
            val delays = TimeKeeper.getInstance().loopIdToCurrentAverage.values.toDoubleArray()
            "${"%.3f".format(Mean().evaluate(delays))} (std ${"%.2f".format(StandardDeviation().evaluate(delays))})"
        }}")
        println("Total cost of execution: ${
            fogDevices.filter { it.totalCost.isFinite() }.sumOf { it.totalCost }
        }, cloud: ${cloud.totalCost}")
        println("Total network usage: ${NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME}")
    }
    applications.forEach { app ->
        controller.submitApplication(app, ModulePlacementMapping(fogDevices, app, mappings[app.appId]))
    }

    SimulationTimeTracker(30 * modelTimeUnitsPerSec)
    UtilizationHistoryCleaner(300 * modelTimeUnitsPerSec, controller)
    Config.RESOURCE_MGMT_INTERVAL = (0.1 * modelTimeUnitsPerSec)
    Config.MAX_SIMULATION_TIME = (
//            36000
            1000
                    * modelTimeUnitsPerSec).toInt()
    try {
        CloudSim.startSimulation()
    }
    catch (e: Exception) {
        controller.onStopSimulation()
        throw e
    }
}

fun createMedicalApplication3(i: Int, userId: Int): Application {
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
    app.addAppEdge("patient_state_analyzer$i", "patient_disease_history$i", 1000.0, 8192.0, "PATIENT_STATE_LOG$i", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("patient_disease_history$i", "patient_state_analyzer$i", 1000.0, 819200.0, "PATIENT_HISTORY_RECORD_RESPONSE$i", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("patient_state_analyzer$i", "heart_attack_determiner$i", 1000.0, 108344.0, "HEART_ATTACK_DETERMINATION$i", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("heart_attack_determiner$i", "client$i", 250.0, 800.0, "HEART_ATTACK_PATIENT_ALARM$i", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("client$i", "PATIENT_ALARM$i", 100.0, 80.0, "PATIENT_ALARM$i", Tuple.DOWN, AppEdge.ACTUATOR)

    app.addTupleMapping("client$i", "PATIENT_SENSOR$i", "PATIENT_STATE$i", FractionalSelectivity(1.0))
    app.addTupleMapping("client$i", "HEART_ATTACK_PATIENT_ALARM$i", "PATIENT_ALARM$i", FractionalSelectivity(1.0))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "PATIENT_HISTORY_RECORD_REQUEST$i", FractionalSelectivity(0.01))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(0.01))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(0.05))
    app.addTupleMapping("patient_disease_history$i", "PATIENT_HISTORY_RECORD_REQUEST$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", FractionalSelectivity(1.0))
    app.addTupleMapping("heart_attack_determiner$i", "HEART_ATTACK_DETERMINATION$i", "HEART_ATTACK_PATIENT_ALARM$i", FractionalSelectivity(1.0))

    app.loops.addAll(listOf(
            AppLoop(listOf("client$i", "patient_state_analyzer$i", "heart_attack_determiner$i", "client$i")),
//            AppLoop(listOf("client$i", "patient_state_analyzer$i", "patient_disease_history$i", "patient_state_analyzer$i"))
    ))

    return app
}

//fun createSmallMediaApplication(i: Int, userId: Int, periodSec: Double): Application {
//    val app = Application.createApplication("SmallMediaConsumingApplication$i", userId)
//    app.addAppModule("media_consumer_proxy$i", 0)
//    app.addAppModule("media_provider$i", 0)
//
//    app.addAppEdge("MEDIA_TRIGGER$i", "media_consumer_proxy$i", 0.0, 0.0, "MEDIA_TRIGGER$i", Tuple.UP, AppEdge.SENSOR)
//    app.addAppEdge("media_consumer_proxy$i", "media_provider$i", 0.0, 1.0 * 1024 * 1024 * 8 /*Up 1 Mbytes per sec*/ * periodSec, "MEDIA_REQUEST$i", Tuple.UP, AppEdge.MODULE)
//    app.addAppEdge("media_provider$i", "media_consumer_proxy$i", 0.0, 500.0 * 1024 * 1024 * 8/*Down 500 Mbytes per sec*/ * periodSec, "MEDIA_RESPONSE$i", Tuple.DOWN, AppEdge.MODULE)
//    app.addAppEdge("media_consumer_proxy$i", "MEDIA_CONSUMER$i", 0.0, 0.0, "MEDIA_CONSUMER$i", Tuple.DOWN, AppEdge.ACTUATOR)
//
//    app.addTupleMapping("media_consumer_proxy$i", "MEDIA_TRIGGER$i", "MEDIA_REQUEST$i", FractionalSelectivity(1.0))
//    app.addTupleMapping("media_provider$i", "MEDIA_REQUEST$i", "MEDIA_RESPONSE$i", FractionalSelectivity(1.0))
//    app.addTupleMapping("media_consumer_proxy$i", "MEDIA_RESPONSE$i", "MEDIA_CONSUMER$i", FractionalSelectivity(1.0))
//    //app loops
//    return app
//}
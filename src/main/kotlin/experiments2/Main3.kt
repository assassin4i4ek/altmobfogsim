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
import api.common.utils.DeterministicDistributionWithOffset
import api.migration.models.mapo.*
import api.migration.models.mapo.OnlyTriggeredCentralizedMapoModel
import api.migration.models.mapo.ideals.ClosestToEdgeStartEnvironmentBuilder
import api.migration.models.mapo.ideals.MinCostIdealEnvironmentBuilder
import api.migration.models.mapo.normalizers.MinMaxNormalizer
import api.migration.models.mapo.objectives.MinCostObjective
import api.migration.models.mapo.objectives.MinProcessingDelayObjective
import api.migration.models.mapo.objectives.MinNetworkUsageObjective
import api.migration.models.mapo.problems.SingleInstanceIdealInjectingModulePlacementProblem
import api.migration.models.timeprogression.FixedTimeProgression
import api.migration.models.timeprogression.FixedWithOffsetTimeProgression
import api.migration.utils.MigrationLogger
import api.migration.utils.MigrationRequest
import api.mobility.models.CsvInputMobilityModelFactory
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.Application
import org.fog.entities.*
import org.fog.placement.ModuleMapping
import org.fog.placement.ModulePlacementMapping
import org.fog.utils.Config
import org.fog.utils.Logger
import org.fog.utils.NetworkUsageMonitor
import org.fog.utils.TimeKeeper
import utils.BaseStationSpecification
import utils.MyTestController
import utils.SimulationTimeTracker
import utils.TimeKeeperCleaner
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

fun main() {
    globalExperiment3ParallelService()
}

fun globalExperiment3ParallelService() {
    val commandScanner = Scanner(System.`in`)
    while (commandScanner.hasNextLine()) {
        val command = commandScanner.nextLine()
        System.err.println(command)
        System.err.flush()
        globalExperiment3Parallel(command.split(" ").drop(1).toTypedArray())
    }
}

fun globalExperiment3Parallel(args: Array<String>) {
    val numMobiles = args[2].toInt()
    val populationSize = args[3].toInt()
    val mapoModelMaxIterations = args[4].toInt()
    val injectedSolutionsFraction = args[5].toDouble()
    val migrationModelType = args[6].toInt()
    val elapsedTime = measureTimeMillis {
        experiment3(numMobiles, populationSize, mapoModelMaxIterations, injectedSolutionsFraction, migrationModelType)
    }
    val delays = TimeKeeper.getInstance().loopIdToCurrentAverage.values.toDoubleArray()
    val (config, avgStd, elapsedSeconds) = Triple(
            listOf(numMobiles, populationSize, mapoModelMaxIterations, injectedSolutionsFraction),
            Mean().evaluate(delays) to StandardDeviation().evaluate(delays),
            elapsedTime.toDouble() / 1000
    )
    TimeKeeper.getInstance().apply {
        emitTimes.clear()
        endTimes.clear()
        loopIdToTupleIds.clear()
        tupleTypeToAverageCpuTime.clear()
        tupleTypeToExecutedTupleCount.clear()
        tupleIdToCpuStartTime.clear()
        loopIdToCurrentAverage.clear()
        loopIdToCurrentNum.clear()
    }
    println("Results so far")
    println("numMobiles: populationSize, mapoModelMaxEvaluations, injectedSolutionsFraction % = avg +- std (std %) (time seconds)")
    val resultAsString = "${config[0]}: ${config[1]}, ${config[2]}, ${"%.0f".format((config[3].toDouble()) * 100)}% = " +
            "${"%.3f".format(avgStd.first)} +- ${"%.2f".format(avgStd.second)} " +
            "(${"%.2f".format(100 * avgStd.second / avgStd.first)} %) " +
            "(${"%.3f".format(elapsedSeconds)})"
    println(resultAsString)
    println()
    val resultsTxtFile = PrintWriter(FileWriter(File(args[0]), true), true)
    resultsTxtFile.println(resultAsString)
    resultsTxtFile.close()
    val resultsCsvFile = PrintWriter(FileWriter(File(args[1]), true), true)
    resultsCsvFile.println(
            "${config[0]}\t${config[1]}\t${config[2]}\t${"%.0f".format((config[3].toDouble()) * 100)}\t" +
                    "${"%.3f".format(avgStd.first)}\t${"%.2f".format(avgStd.second)}\t" +
                    "${"%.2f".format(100 * avgStd.second / avgStd.first)}\t" +
                    "%.3f".format(elapsedSeconds)
    )
    resultsCsvFile.close()
}

fun experiment3(numMobiles: Int, populationSize: Int, mapoModelMaxIterations: Int, injectedSolutionsFraction: Double, strategyType: Int) {
    val randSeed = 12345L
    setMathRandSeed(randSeed)
    val modelTimeUnitsPerSec = 1000.0 // one CloudSim tick == 1 ms
    CloudSim.init(1, Calendar.getInstance(), false)
    Log.disable()
//    Logger.ENABLED = true
    Logger.ENABLED = false
    AccessPointEventsLogger.enabled = true
    MigrationLogger.enabled = true
    MigrationLogger.logEntryAsString = { logEntry ->
        if (logEntry.migrationRequest.type != MigrationRequest.Type.REMOVE_ALL_INSTANCES) {
            logEntry.toString()
        } else null
    }

    val migrationModel = run {
        val ignoredTimeProgression = FixedTimeProgression(Double.MAX_VALUE)
        val numOfInjectedSolutions = max(1, min((populationSize * injectedSolutionsFraction).toInt(), populationSize - 1))
        val normalizer = MinMaxNormalizer()
        val logProgress = false

        when (strategyType) {
            // min cost = cloud only
            1 -> CentralizedMapoModel(false)
            // min network = edge only
            2 -> OnlyTriggeredCentralizedMapoModel(true, { 1 }, { 1 }, ignoredTimeProgression, listOf(MinNetworkUsageObjective()),
                    SingleInstanceIdealInjectingModulePlacementProblem.Factory(listOf(
                            ClosestToEdgeStartEnvironmentBuilder()
                    ), 1), normalizer, randSeed, logProgress
            )
            // min cost, delay = multiobjective
            3, 4 -> PartitionedCentralizedMapoModel(
                    true,30,
                    FixedWithOffsetTimeProgression(CloudSim.getMinTimeBetweenEvents(), when (strategyType) {
                        3 -> 5.0
                        4 -> 1.0
                        else -> 0.0
                    } * 60 * modelTimeUnitsPerSec),
                    listOf(
                            MinCostObjective(),
                            MinProcessingDelayObjective()
                    ), SingleInstanceIdealInjectingModulePlacementProblem.Factory(listOf(
                    MinCostIdealEnvironmentBuilder(),
                    ClosestToEdgeStartEnvironmentBuilder(),
            ), numOfInjectedSolutions), mapoModelMaxIterations, populationSize, normalizer, randSeed, logProgress
            )
            else -> throw Exception("Unknown scenario type")
        }
    }

    val cloudNumPes = 16 * 128
    val cloudRatePerMips = 168050.0 /*USD per year */ / 15200 /* MIPS */ / (365 * 24 * 3600) /* sec */
    val cloud = createCharacteristicsAndAllocationPolicy(
            300000.0 /*Intel Core i7-5960x 3.5GHz 2014y*/ / 16 /*threads*/ / modelTimeUnitsPerSec, cloudNumPes,
            189.0 /*Watts 100% load for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec,
            69.0 /*Watts idle for Intel Core i7-5960x 8 cores 16 threads*/ / 16 /*threads*/ / modelTimeUnitsPerSec
    ).let {
        DynamicAddressingMigrationSupportingDeviceImpl("cloud", it.first, it.second, emptyList(), 10.0,
                2.5e9 /* 2.5 Gbps link*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                2.5e9 /* 2.5 Gbps link*/ * 0.85 /*85% efficiency for throughput*/ / modelTimeUnitsPerSec,
                0.0, cloudRatePerMips, AddressingDevice.AddressingType.HIERARCHICAL,
                migrationModel
        )
    }

    val migrationDecisionMakingDevice = when(strategyType) {
        2 -> cloud
        else -> null
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
                val apRatePerMips = cloudRatePerMips * 1.1
                val apNumPes = 16//max(cloudNumPes / 8, 1)
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
    val mobiles = CsvInputMobilityModelFactory.fromDirectory("input/gps_data", modelTimeUnitsPerSec, numMobiles, 3600.0).map { (name, mobilityModel) ->
        createCharacteristicsAndAllocationPolicy(
                2.65 /*Cortex-A55 DMIPS per MHz*/ * 2000.0 /*MHz*/ / modelTimeUnitsPerSec, 1,
                0.85 /* Watts in idle mode */ * 0.85 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec,
                0.02 /* Watts in idle mode */ * 0.9 /* A55 vs A53 efficiency*/ / modelTimeUnitsPerSec).let {
            MigrationStimulatorAddressingAccessPointConnectedDeviceImpl(name, it.first, it.second, emptyList(), 10.0,
                    0.0, 0.0, 0.0, 0.0,
                    Position(Coordinates(-1000.0, -1000.0), 0.0, 0.0),
                    mobilityModel, apm, migrationDecisionMakingDevice?.id
            )
        }
    }

    ispGateway.parentId = cloud.id
    hospital.parentId = ispGateway.id
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
        val app = createMedicalApplication2(i, broker.id)
        val sensor = Sensor("PatientSensor$i", "PATIENT_SENSOR$i", broker.id, app.appId,
                DeterministicDistributionWithOffset(Math.random() * 2.0 * modelTimeUnitsPerSec, 2.0 * modelTimeUnitsPerSec))
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
        migrationModel.allowMigrationForModule("patient_state_analyzer$i")
        migrationModel.allowMigrationForModule("heart_attack_determiner$i")
//        migrationModel.allowMigrationForModule("patient_disease_history$i")

        brokers.add(broker)
        applications.add(app)
        sensors.add(sensor)
        actuators.add(actuator)
        mappings[app.appId] = moduleMapping
    }
    val fogDevices = mutableListOf<FogDevice>()

    fogDevices.add(cloud)
    fogDevices.add(hospital)
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
        println("Total network usage: ${"%.0f".format(NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME)}")
    }
    applications.forEach { app ->
        controller.submitApplication(app, ModulePlacementMapping(fogDevices, app, mappings[app.appId]))
    }

    SimulationTimeTracker(
            when (strategyType) {
                1 -> 30
                else -> 300
            }
    * modelTimeUnitsPerSec, controller)
    TimeKeeperCleaner(300* modelTimeUnitsPerSec, controller)
    Config.RESOURCE_MGMT_INTERVAL = (0.1 * modelTimeUnitsPerSec)
    Config.MAX_SIMULATION_TIME = (4.75 * 60 * 60 * modelTimeUnitsPerSec).toInt()
    try {
        CloudSim.startSimulation()
    }
    catch (e: Exception) {
        controller.onStopSimulation(controller)
        throw e
    }
}
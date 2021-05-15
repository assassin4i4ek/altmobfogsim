package experiments2

import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.application.AppEdge
import org.fog.application.AppLoop
import org.fog.application.Application
import org.fog.application.selectivity.FractionalSelectivity
import org.fog.entities.FogDeviceCharacteristics
import org.fog.entities.Tuple
import org.fog.policy.AppModuleAllocationPolicy
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.FogUtils
import utils.PowerHostWithoutHistory
import java.util.*

fun createCharacteristicsAndAllocationPolicy(mips: Double, numPes: Int, maxPowerPerCore: Double, idlePowerPerCore: Double): Pair<FogDeviceCharacteristics, VmAllocationPolicy> {
    val peList = List(numPes) { i -> Pe(i, PeProvisionerOverbooking(mips)) }
    val hostId = FogUtils.generateEntityId()
    val ram: Int = Int.MAX_VALUE // host memory (MB)
    val storage: Long = Long.MAX_VALUE // host storage
    val bw: Long = Long.MAX_VALUE
    val host = PowerHostWithoutHistory(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw),storage,
            peList, VmSchedulerTimeSharedOverSubscription(peList),
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

fun createMedicalApplication1(i: Int, userId: Int): Application {
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

fun createMedicalApplication2(i: Int, userId: Int): Application {
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
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "PATIENT_HISTORY_RECORD_REQUEST$i", FractionalSelectivity(0.01))
    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_STATE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(1.0))
//    app.addTupleMapping("patient_state_analyzer$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", "HEART_ATTACK_DETERMINATION$i", FractionalSelectivity(0.05))
    app.addTupleMapping("patient_disease_history$i", "PATIENT_HISTORY_RECORD_REQUEST$i", "PATIENT_HISTORY_RECORD_RESPONSE$i", FractionalSelectivity(1.0))
    app.addTupleMapping("heart_attack_determiner$i", "HEART_ATTACK_DETERMINATION$i", "HEART_ATTACK_PATIENT_ALARM$i", FractionalSelectivity(1.0))

    app.loops.addAll(listOf(
            AppLoop(listOf("client$i", "patient_state_analyzer$i", "heart_attack_determiner$i", "client$i")),
    ))

    return app
}

fun setMathRandSeed(seed: Long) {
    val randField = Math::class.java.declaredClasses.find { it.simpleName == "RandomNumberGeneratorHolder" }!!
            .getDeclaredField("randomNumberGenerator")
    randField.isAccessible = true
    val randObj = randField.get(null) as Random
    randObj.setSeed(seed)
}
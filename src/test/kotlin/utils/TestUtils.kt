package utils

import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.VmAllocationPolicy
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.power.models.PowerModelLinear
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
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.FogLinearPowerModel
import org.fog.utils.FogUtils


fun createTwoModulesApp(userId: Int): Application {
    val app = Application.createApplication("App1", userId)
    app.addAppModule("AppModule1", 100)
    app.addAppModule("AppModule2", 100)

    app.addTupleMapping("AppModule1", "SENSOR", "MODULE2_INPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule2", "MODULE2_INPUT", "MODULE2_OUTPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule1", "MODULE2_OUTPUT", "ACTUATOR", FractionalSelectivity(1.0))

    app.addAppEdge("SENSOR", "AppModule1", 0.1, 0.2,
        "SENSOR", Tuple.UP, AppEdge.SENSOR)
    app.addAppEdge("AppModule1", "AppModule2", 0.1, 0.2,
        "MODULE2_INPUT", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("AppModule2", "AppModule1", 0.1, 0.2,
        "MODULE2_OUTPUT", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("AppModule1", "ACTUATOR", 0.1, 0.2,
        "ACTUATOR", Tuple.DOWN, AppEdge.ACTUATOR)

    app.loops.add(AppLoop(listOf("SENSOR", "AppModule1", "AppModule2", "AppModule1", "ACTUATOR")))
    return app
}

fun createThreeModulesApp(userId: Int): Application {
    val app = Application.createApplication("App1", userId)
    app.addAppModule("AppModule1", 100)
    app.addAppModule("AppModule2", 100)
    app.addAppModule("AppModule3", 100)

    app.addTupleMapping("AppModule1", "SENSOR", "MODULE2_INPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule2", "MODULE2_INPUT", "MODULE3_INPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule3", "MODULE3_INPUT", "MODULE3_OUTPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule2", "MODULE3_OUTPUT", "MODULE2_OUTPUT", FractionalSelectivity(1.0))
    app.addTupleMapping("AppModule1", "MODULE2_OUTPUT", "ACTUATOR", FractionalSelectivity(1.0))

    app.addAppEdge("SENSOR", "AppModule1", 0.1, 0.2,
            "SENSOR", Tuple.UP, AppEdge.SENSOR)
    app.addAppEdge("AppModule1", "AppModule2", 0.1, 0.2,
            "MODULE2_INPUT", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("AppModule2", "AppModule3", 0.1, 0.2,
            "MODULE3_INPUT", Tuple.UP, AppEdge.MODULE)
    app.addAppEdge("AppModule3", "AppModule2", 0.1, 0.2,
            "MODULE3_OUTPUT", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("AppModule2", "AppModule1", 0.1, 0.2,
            "MODULE2_OUTPUT", Tuple.DOWN, AppEdge.MODULE)
    app.addAppEdge("AppModule1", "ACTUATOR", 0.1, 0.2,
            "ACTUATOR", Tuple.DOWN, AppEdge.ACTUATOR)

    app.loops.add(AppLoop(listOf("SENSOR", "AppModule1", "AppModule2", "AppModule3", "AppModule2", "AppModule1", "ACTUATOR")))
    return app
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
    val time_zone = 10.0 // time zone this resource located
    val cost = 3.0 // the cost of using processing in this resource
    val costPerMem = 0.05 // the cost of using memory in this resource
    val costPerStorage = 0.001 // the cost of using storage in this resource
    val costPerBw = 0.0 // the cost of using bw in this resource

    return Pair(FogDeviceCharacteristics(
        arch, os, vmm, host, time_zone, cost, costPerMem,
        costPerStorage, costPerBw
    ), AppModuleAllocationPolicy(hostList))
}

fun createExperimentApp(userId: Int): Application {
    val app = Application.createApplication("App1", userId)

    app.addAppModule("client", 10) // adding module Client to the application model
    app.addAppModule("concentration_calculator", 10) // adding module Concentration Calculator to the application model

    /*
     * Connecting the application modules (vertices) in the application model (directed graph) with edges
     */
    app.addAppEdge("SENSOR", "client", 100.0, 0.1, "SENSOR", Tuple.UP, AppEdge.SENSOR)
    app.addAppEdge("client", "concentration_calculator", 1000.0, 0.1, "_SENSOR", Tuple.UP, AppEdge.MODULE) // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
    app.addAppEdge("concentration_calculator", "client", 14.0, 0.1, "CONCENTRATION", Tuple.DOWN, AppEdge.MODULE) // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
    app.addAppEdge("client", "ACTUATOR", 100.0, 0.1, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR) // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE

    /*
     * Defining the input-output relationships (represented by selectivity) of the application modules.
     */
    app.addTupleMapping("client", "SENSOR", "_SENSOR", FractionalSelectivity(1.0)) // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
    app.addTupleMapping("client", "CONCENTRATION", "SELF_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
    app.addTupleMapping("concentration_calculator", "_SENSOR", "CONCENTRATION", FractionalSelectivity(1.0)) // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
    app.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE", FractionalSelectivity(1.0)) // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE

    app.loops = listOf(AppLoop(listOf("SENSOR", "client", "concentration_calculator", "client", "ACTUATOR")))
    return app
}

//fun createMobileFogDevice(name: String, position: Position, mobilityModel: MobilityModel,
//                          mips: Double, uplinkBandwidth: Double, downlinkBandwidth: Double,
//                          latency: Double, ratePerMips: Double): MobileFogDevice {
//    val peList = listOf(Pe(0, PeProvisionerOverbooking(mips))) // need to store Pe id and MIPS Rating
//    val hostId = FogUtils.generateEntityId()
//    val ram = 2048 // host memory (MB)
//    val storage: Long = 1000000 // host storage
//    val bw = 10000
//    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw.toLong()),storage,
//        peList, StreamOperatorScheduler(peList), PowerModelLinear(100.0, 40.0)
//    )
//    val hostList = listOf(host)
//
//    val arch = "x86" // system architecture
//    val os = "Linux" // operating system
//    val vmm = "Xen"
//    val time_zone = 10.0 // time zone this resource located
//    val cost = 3.0 // the cost of using processing in this resource
//    val costPerMem = 0.05 // the cost of using memory in this resource
//    val costPerStorage = 0.001 // the cost of using storage in this resource
//    val costPerBw = 0.0 // the cost of using bw in this resource
//    val storageList = emptyList<Storage>() // we are not adding SAN devices by now
//
//    val characteristics = FogDeviceCharacteristics(
//        arch, os, vmm, host, time_zone, cost, costPerMem,
//        costPerStorage, costPerBw
//    )
//
//    var mobileFogDevice: MobileFogDevice? = null
//    try {
//        mobileFogDevice = MobileFogDevice(name,position, mobilityModel, characteristics, AppModuleAllocationPolicy(hostList),
//            storageList,10.0,uplinkBandwidth,downlinkBandwidth, latency,ratePerMips
//        )
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return mobileFogDevice!!
//}
//
//fun createSingleLinkAddressingDevice(
//    name: String, mips: Double, downlinkBandwidth: Double, ratePerMips: Double
//): SingleLinkAddressingFogDevice {
//    val peList = listOf(Pe(0, PeProvisionerOverbooking(mips))) // need to store Pe id and MIPS Rating
//    val hostId = FogUtils.generateEntityId()
//    val ram = 2048 // host memory (MB)
//    val storage: Long = 1000000 // host storage
//    val bw = 10000
//    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw.toLong()),storage,
//        peList, StreamOperatorScheduler(peList), PowerModelLinear(100.0, 40.0)
//    )
//    val hostList = listOf(host)
//
//    val arch = "x86" // system architecture
//    val os = "Linux" // operating system
//    val vmm = "Xen"
//    val time_zone = 10.0 // time zone this resource located
//    val cost = 3.0 // the cost of using processing in this resource
//    val costPerMem = 0.05 // the cost of using memory in this resource
//    val costPerStorage = 0.001 // the cost of using storage in this resource
//    val costPerBw = 0.0 // the cost of using bw in this resource
//    val storageList = emptyList<Storage>() // we are not adding SAN devices by now
//
//    val characteristics = FogDeviceCharacteristics(
//        arch, os, vmm, host, time_zone, cost, costPerMem,
//        costPerStorage, costPerBw
//    )
//
//    var addressingDevice: SingleLinkAddressingFogDevice? = null
//    try {
//        addressingDevice = SingleLinkAddressingFogDevice(
//            name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0,
//            downlinkBandwidth, ratePerMips
//        )
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return addressingDevice!!
//}
//
//fun createOriginalAddressingFogDevice(
//    name: String, mips: Double, uplinkBandwidth: Double, downlinkBandwidth: Double, ratePerMips: Double
//): OriginalAddressingFogDevice {
//    val peList = listOf(Pe(0, PeProvisionerOverbooking(mips))) // need to store Pe id and MIPS Rating
//    val hostId = FogUtils.generateEntityId()
//    val ram = 2048 // host memory (MB)
//    val storage: Long = 1000000 // host storage
//    val bw = 10000
//    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw.toLong()),storage,
//        peList, StreamOperatorScheduler(peList), PowerModelLinear(100.0, 40.0)
//    )
//    val hostList = listOf(host)
//
//    val arch = "x86" // system architecture
//    val os = "Linux" // operating system
//    val vmm = "Xen"
//    val time_zone = 10.0 // time zone this resource located
//    val cost = 3.0 // the cost of using processing in this resource
//    val costPerMem = 0.05 // the cost of using memory in this resource
//    val costPerStorage = 0.001 // the cost of using storage in this resource
//    val costPerBw = 0.0 // the cost of using bw in this resource
//    val storageList = emptyList<Storage>() // we are not adding SAN devices by now
//
//    val characteristics = FogDeviceCharacteristics(
//        arch, os, vmm, host, time_zone, cost, costPerMem,
//        costPerStorage, costPerBw
//    )
//
//    var addressingDevice: OriginalAddressingFogDevice? = null
//    try {
//        addressingDevice = OriginalAddressingFogDevice(
//            name, characteristics, AppModuleAllocationPolicy(hostList), storageList, 10.0,
//            uplinkBandwidth, downlinkBandwidth, ratePerMips
//        )
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return addressingDevice!!
//}
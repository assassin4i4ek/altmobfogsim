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
import org.fog.utils.FogUtils


fun createApp(userId: Int): Application {
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

fun createCharacteristicsAndAllocationPolicy(mips: Double): Pair<FogDeviceCharacteristics, VmAllocationPolicy> {
    val peList = listOf(Pe(0, PeProvisionerOverbooking(mips))) // need to store Pe id and MIPS Rating
    val hostId = FogUtils.generateEntityId()
    val ram = 2048 // host memory (MB)
    val storage: Long = 1000000 // host storage
    val bw = 10000
    val host = PowerHost(hostId, RamProvisionerSimple(ram), BwProvisionerOverbooking(bw.toLong()),storage,
        peList, StreamOperatorScheduler(peList), PowerModelLinear(100.0, 40.0)
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
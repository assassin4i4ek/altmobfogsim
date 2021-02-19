package api.accesspoint.entities

import api.dynamic.mobility.positioning.Coordinates
import api.dynamic.mobility.positioning.distance
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.power.PowerHost
import org.cloudbus.cloudsim.power.models.PowerModelLinear
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking
import org.fog.entities.FogDeviceCharacteristics
import org.fog.scheduler.StreamOperatorScheduler
import org.fog.utils.FogUtils

object AccessPointsMap {
    private val accessPoints: MutableList<AccessPoint> = mutableListOf()

    fun getClosestAccessPointsTo(deviceCoord: Coordinates): List<AccessPoint> {
        return accessPoints.sortedBy { distance(it.coordinates, deviceCoord) }
    }

    fun registerAccessPoint(ap : AccessPoint) {
        accessPoints.add(ap)
    }

    fun accessPointCharacteristics(): FogDeviceCharacteristics {
        val peList = listOf(Pe(0, PeProvisionerOverbooking(0.0))) // need to store Pe id and MIPS Rating
        val hostId = FogUtils.generateEntityId()
//        val ram = 2048 // host memory (MB)
//        val storage: Long = 1000000 // host storage
//        val bw = 10000
        val host = PowerHost(hostId, RamProvisionerSimple(0),
            BwProvisionerOverbooking(0), 0,
            peList, StreamOperatorScheduler(peList), PowerModelLinear(100.0, 40.0)
        )
//        val hostList = listOf(host)

        val arch = "x86" // system architecture
        val os = "Linux" // operating system
        val vmm = "Xen"
        val time_zone = 10.0 // time zone this resource located
        val cost = 3.0 // the cost of using processing in this resource
        val costPerMem = 0.05 // the cost of using memory in this resource
        val costPerStorage = 0.001 // the cost of using storage in this resource
        val costPerBw = 0.0 // the cost of using bw in this resource

        return FogDeviceCharacteristics(
            arch, os, vmm, host, time_zone, cost, costPerMem,
            costPerStorage, costPerBw
        )
    }
}
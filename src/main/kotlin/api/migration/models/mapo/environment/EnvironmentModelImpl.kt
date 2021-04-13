package api.migration.models.mapo.environment

import api.addressing.fixed.entities.AddressingDevice
import api.addressing.models.AddressingModel
import org.apache.commons.math3.util.Pair
import org.cloudbus.cloudsim.core.CloudSim
import org.fog.application.AppEdge
import org.fog.application.AppModule
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.entities.Tuple
import org.fog.placement.Controller
import java.lang.Exception

class EnvironmentModelImpl(private val controller: Controller) : MutableEnvironmentModel {
    // deviceId -> appId -> moduleName -> instanceCount
    private val fogDevicesToAppModulesInstances: MutableMap<Int, MutableMap<String, MutableMap<String, Int>>> = mutableMapOf()
    // appId -> moduleName -> [deviceId]
    override val appModuleToFogDevices: MutableMap<String, MutableMap<String, MutableList<Int>>> = mutableMapOf()

    override val fogDevicesToAppModules: MutableMap<Int, MutableMap<String, MutableList<String>>> = mutableMapOf()

    private var stateChanged: Boolean = true
    private lateinit var pathsCache: List<EnvironmentModelPath>

    override fun addFogDeviceWithModules(fogDeviceId: Int, modules: List<AppModule>) {
        stateChanged = true
        assert(!fogDevicesToAppModulesInstances.containsKey(fogDeviceId))
        fogDevicesToAppModulesInstances[fogDeviceId] = mutableMapOf()
        modules.forEach { module ->
            fogDevicesToAppModulesInstances
                    .getOrPut(fogDeviceId) { mutableMapOf() } //device -> appId
                    .getOrPut(module.appId) { mutableMapOf() }[module.name] = 0 //appId -> moduleName
            fogDevicesToAppModules
                    .getOrPut(fogDeviceId) { mutableMapOf() } //device -> appId
                    .getOrPut(module.appId) { mutableListOf() }.add(module.name) //appId -> moduleName
            appModuleToFogDevices
                    .getOrPut(module.appId) { mutableMapOf() } //appId -> moduleName
                    .getOrPut(module.name) { mutableListOf() } //moduleName -> [deviceId]
                    .add(fogDeviceId)
        }
    }

    override fun hasFogDevice(fogDeviceId: Int): Boolean {
        return fogDevicesToAppModulesInstances.containsKey(fogDeviceId)
    }

    override val allFogDevices: List<FogDevice> get() = fogDevicesToAppModulesInstances.keys.map {
        CloudSim.getEntity(it) as FogDevice
    }

    override fun getAllPaths(): List<EnvironmentModelPath> {
        if (stateChanged) {
            val paths = mutableListOf<EnvironmentModelPath>()
            // tuples initialized by sensors
            controller.sensors.forEach { sensor ->
                paths.add(EnvironmentModelPath(
                        processTupleGeneration(DeviceSuperposition(sensor), sensor.appId, sensor.app.edgeMap[sensor.tupleType]!!)
                ))
            }
            // modules with periodic tuples
            fogDevicesToAppModulesInstances.forEach { (deviceId, apps) ->
                apps.forEach { (appId, appModules) ->
                    appModules.forEach { (moduleName, moduleInstanceCountOnDevice) ->
                        controller.applications[appId]!!.getPeriodicEdges(moduleName).forEach { periodicEdge ->
                            val initDevice = CloudSim.getEntity(deviceId) as FogDevice
                            repeat(if (periodicEdge.direction == Tuple.UP) moduleInstanceCountOnDevice else 1) {
                                paths.add(EnvironmentModelPath(
                                        processTupleGeneration(DeviceSuperposition(initDevice), appId, periodicEdge)
                                ))
                            }
                        }
                    }
                }
            }

            stateChanged = false
            pathsCache = paths
        }

        return pathsCache
    }

    private fun processTupleGeneration(deviceSuperposition: DeviceSuperposition, appId: String, initAppEdge: AppEdge): List<EnvironmentModelPathLink> {
        val pathLinks = mutableListOf<EnvironmentModelPathLink>()
        // moduleName -> deviceId
//        val pathAppEdgeModuleCopyMap = mutableMapOf<String, Int>()

        var unhandledLinks = when (val srcDevice = deviceSuperposition.device) {
            is Sensor -> {
                val gatewayDevice = CloudSim.getEntity(srcDevice.gatewayDeviceId) as FogDevice
                val currentEdgeModuleMap = emptyMap<String, Int>()
                val processingModule = processingModuleOfTargetDevice(gatewayDevice.id, appId, initAppEdge, currentEdgeModuleMap)
                val nextEdgeModuleMap = HashMap(currentEdgeModuleMap)
                if (processingModule != null) {
                    nextEdgeModuleMap[processingModule.name] = gatewayDevice.id
                }
                mutableListOf(EnvironmentModelPathLink(
                        deviceSuperposition, DeviceSuperposition(gatewayDevice),
                        processingModule, initAppEdge, appId, srcDevice.transmitDistribution.meanInterTransmitTime,
                        1.0, nextEdgeModuleMap
                ))
            }
            is FogDevice -> {
                val currentEdgeModuleMap = mapOf(initAppEdge.source to srcDevice.id)
                addressEdge(srcDevice, initAppEdge, appId, currentEdgeModuleMap).map { nextDevice ->
                    val processingModule = processingModuleOfTargetDevice(nextDevice.id, appId, initAppEdge, currentEdgeModuleMap)
                    val nextEdgeModuleMap = HashMap(currentEdgeModuleMap)
                    if (processingModule != null) {
                        nextEdgeModuleMap[processingModule.name] = nextDevice.id
                    }
                    EnvironmentModelPathLink(
                            deviceSuperposition, DeviceSuperposition(nextDevice),
                            processingModule, initAppEdge, appId, initAppEdge.periodicity,
                            1.0, nextEdgeModuleMap
                    )
                }.toMutableList()
            }
            else -> throw Exception("Unknown type of device")
        }

        while (unhandledLinks.isNotEmpty()) {
            val currentUnhandledLinks = unhandledLinks
            unhandledLinks = mutableListOf()
            currentUnhandledLinks.forEach { currentLink ->
                pathLinks.add(currentLink)
                val currentEdgeModuleMap = currentLink.edgeModuleMap
                when (val currentDevice = currentLink.destDevice.device) {
                    is FogDevice -> {
                        val targetModule = controller.applications[appId]!!.getModuleByName(
                                fogDevicesToAppModulesInstances[currentDevice.id]!![appId]?.keys?.find { moduleName ->
                                    moduleName == currentLink.appEdge.destination
                                }
                        )
                        if (targetModule != null && targetModule == currentLink.destProcessingModule) {
                            // process app edge tuples and create new
                            val app = controller.applications[targetModule.appId]!!
                            // increment target module instance count
                            if (currentLink.appEdge.direction == Tuple.UP) {
                                fogDevicesToAppModulesInstances[currentDevice.id]!![appId]!!.let { it[targetModule.name] = it[targetModule.name]!! + 1 }
                            }
                            val nextEdges = app.edges.filter { it.source == targetModule.name && targetModule.selectivityMap.containsKey(Pair(currentLink.appEdge.tupleType, it.tupleType)) }
                            if (nextEdges.isNotEmpty()) {
                                nextEdges.forEach { nextEdge ->
                                    when (nextEdge.edgeType) {
                                        AppEdge.MODULE -> {
                                            addressEdge(currentDevice, nextEdge, currentLink.appId, currentEdgeModuleMap).forEach { nextDevice ->
                                                val processingModule = processingModuleOfTargetDevice(nextDevice.id, appId, nextEdge, currentEdgeModuleMap)
                                                val nextEdgeModuleMap = HashMap(currentEdgeModuleMap)
                                                if (processingModule != null) {
                                                    nextEdgeModuleMap[processingModule.name] = nextDevice.id
                                                }
                                                unhandledLinks.add(EnvironmentModelPathLink(
                                                        DeviceSuperposition(currentDevice),
                                                        DeviceSuperposition(nextDevice),
                                                        processingModule,
                                                        nextEdge,
                                                        currentLink.appId,
                                                        currentLink.timeInterval,
                                                        currentLink.selectivity * targetModule.selectivityMap[Pair(currentLink.appEdge.tupleType, nextEdge.tupleType)]!!.meanRate,
                                                        nextEdgeModuleMap
                                                ))
                                            }
                                        }
                                        AppEdge.ACTUATOR -> {
                                            val actuator = controller.actuators.find {
                                                it.gatewayDeviceId == currentDevice.id && it.actuatorType == nextEdge.destination
                                            }!!
                                            unhandledLinks.add(EnvironmentModelPathLink(
                                                    DeviceSuperposition(currentDevice),
                                                    DeviceSuperposition(actuator),
                                                    null,
                                                    nextEdge,
                                                    currentLink.appId,
                                                    currentLink.timeInterval,
                                                    currentLink.selectivity * targetModule.selectivityMap[Pair(currentLink.appEdge.tupleType, nextEdge.tupleType)]!!.meanRate,
                                                    HashMap(currentEdgeModuleMap)
                                            ))
                                        }
                                    }
                                }
                            }
//                            pathAppEdgeModuleCopyMap[targetModule.name] = currentDevice.id
                        }
                        else if (targetModule == null && currentLink.destProcessingModule == null) {
                            // address app edge tuples to next device
                            addressEdge(currentDevice, currentLink.appEdge, currentLink.appId, currentEdgeModuleMap)
                                    .forEach { nextDevice ->
                                        val processingModule = processingModuleOfTargetDevice(nextDevice.id, appId, currentLink.appEdge, currentEdgeModuleMap)
                                        val nextEdgeModuleMap = HashMap(currentEdgeModuleMap)
                                        if (processingModule != null) {
                                            nextEdgeModuleMap[processingModule.name] = nextDevice.id
                                        }
                                        unhandledLinks.add(EnvironmentModelPathLink(
                                                DeviceSuperposition(currentDevice), DeviceSuperposition(nextDevice),
                                                processingModule, currentLink.appEdge, currentLink.appId,
                                                currentLink.timeInterval, currentLink.selectivity, nextEdgeModuleMap
                                        ))
                                    }
                        }
                    }
                }
            }
        }

        return pathLinks
    }

    private fun processingModuleOfTargetDevice(deviceId: Int, appId: String, appEdge: AppEdge, moduleCopyMap: Map<String, Int>): AppModule? {
        return controller.applications[appId]!!.getModuleByName(
                fogDevicesToAppModulesInstances[deviceId]!![appId]?.keys?.find { moduleName ->
                    moduleName == appEdge.destination && !(moduleCopyMap.containsKey(moduleName) && moduleCopyMap[moduleName] != deviceId)
                }
        )
    }

    private fun addressEdge(srcDevice: FogDevice, appEdge: AppEdge, appId: String, moduleCopyMap: Map<String, Int>): List<FogDevice> {
        return if (srcDevice is AddressingDevice) {
            val deviceAddressingModel = srcDevice.addressingModel.copy()
            val devicesWithDestModule = appModuleToFogDevices[appId]!![appEdge.destination]
            if (devicesWithDestModule != null) {
                val (targetDeviceIds, quantifier) = if (moduleCopyMap.containsKey(appEdge.destination)) {
                    // if the tuples of edge have to be sent to specific module
                    listOf(moduleCopyMap[appEdge.destination]!!) to AddressingModel.Quantifier.SINGLE
                } else {
                    when (appEdge.direction) {
                        Tuple.UP -> devicesWithDestModule to AddressingModel.Quantifier.ANY
                        Tuple.DOWN -> {
                            when (srcDevice.addressingType) {
                                AddressingDevice.AddressingType.HIERARCHICAL -> {
                                    deviceAddressingModel.filterInChildren(srcDevice, devicesWithDestModule) to AddressingModel.Quantifier.ALL
                                }
                                AddressingDevice.AddressingType.PEER_TO_PEER -> {
                                    devicesWithDestModule to AddressingModel.Quantifier.ALL
                                }
                            }
                        }
                        else -> throw Exception("Unknown app edge direction ${appEdge.direction}")
                    }
                }

                val targetNextHopsMap = deviceAddressingModel.idsOfNextHopTo(srcDevice, targetDeviceIds, quantifier, appEdge.tupleNwLength.toLong())
                targetNextHopsMap.values.distinct().filter { it > 0 }.map { nextHopId -> CloudSim.getEntity(nextHopId) as FogDevice }
            }
            else {
                emptyList()
            }
        }
        else {
            TODO()
        }
    }
}
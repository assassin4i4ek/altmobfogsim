package api.migration.models.mapo.environment

import org.fog.entities.FogDevice

interface EnvironmentModel {
    fun hasFogDevice(fogDeviceId: Int): Boolean
    val allFogDevices: List<FogDevice>

    val fogDevicesToAppModules: Map<Int, Map<String, List<String>>>
    val appModuleToFogDevices: Map<String, Map<String, List<Int>>>
    fun getAllPaths(): List<EnvironmentModelPath>
}
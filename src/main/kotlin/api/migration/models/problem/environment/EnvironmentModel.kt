package api.migration.models.problem.environment

import org.fog.application.AppModule
import org.fog.entities.FogDevice

interface EnvironmentModel {
    fun hasFogDevice(fogDeviceId: Int): Boolean
    val allFogDevices: List<FogDevice>

    val fogDevicesToAppModules: Map<Int, Map<String, List<String>>>
    val appModuleToFogDevices: Map<String, Map<String, List<Int>>>
    fun getAllPaths(): List<EnvironmentModelPath>
}
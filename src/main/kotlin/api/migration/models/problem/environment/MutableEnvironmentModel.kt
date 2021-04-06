package api.migration.models.problem.environment

import org.fog.application.AppModule

interface MutableEnvironmentModel: EnvironmentModel {
    fun addFogDeviceWithModules(fogDeviceId: Int, modules: List<AppModule>)
}
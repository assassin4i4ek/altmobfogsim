package api.migration.models.mapo.environment

import org.fog.application.AppModule

interface MutableEnvironmentModel: EnvironmentModel {
    fun addFogDeviceWithModules(fogDeviceId: Int, modules: List<AppModule>)
}
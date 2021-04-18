package api.migration.models.mapo.ideals.utils

import api.migration.models.mapo.environment.EnvironmentModel
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.entities.FogDevice

interface ClosestToEdgeModulesMapping {
    enum class Edge {START, END}

    fun getClosestToEdgeModulesMapping(
            currentEnvironmentModel: EnvironmentModel,
            migrationSupportingDevices: List<MigrationSupportingDevice>,
            modulesToMigrate: List<AppModule>,
            edge: Edge
    ): Map<MigrationSupportingDevice, List<Pair<AppModule, Boolean>>> {
        val migrationSupportingDeviceIds = migrationSupportingDevices.associateBy { it.mId }
        val modulesMapping = currentEnvironmentModel.getAllPaths().mapNotNull { path ->
            val modulesToMigrateOnPath = mutableSetOf<Int>()
            var closestToSourceMigrationSupportingDeviceId: Int = -1
            path.links.run {
                when(edge) {
                    Edge.START -> this
                    Edge.END -> this.asReversed()
                }
            }.forEach { link ->
                if (link.destProcessingModule != null) {
                    val indexOfDestModule = modulesToMigrate.indexOf(link.destProcessingModule)
                    if (indexOfDestModule >= 0) {
                        modulesToMigrateOnPath.add(indexOfDestModule)
                    }
                }
                if (closestToSourceMigrationSupportingDeviceId <= 0) {
                    if (link.destDevice.device is FogDevice) {
                        val destDeviceId = (link.destDevice.device as FogDevice).id
                        if (migrationSupportingDeviceIds.containsKey(destDeviceId)) {
                            closestToSourceMigrationSupportingDeviceId = destDeviceId
                        }
                    }
                }
            }
            if (closestToSourceMigrationSupportingDeviceId > 0) {
                migrationSupportingDeviceIds[closestToSourceMigrationSupportingDeviceId]!! to modulesToMigrateOnPath
            } else null
        }.groupingBy {
            it.first
        }.fold({ _, first -> first.second }, { _, accumulator, next ->
            accumulator.addAll(next.second)
            accumulator
        }).mapValues { (_, moduleIndices) ->
            modulesToMigrate.mapIndexed { moduleIndex: Int, module: AppModule ->
                module to (moduleIndex in moduleIndices)
            }
        }

        return migrationSupportingDevices.associateWith { migrationSupportingDevice ->
            if (modulesMapping.containsKey(migrationSupportingDevice)) {
                modulesMapping[migrationSupportingDevice]!!
            }
            else {
                modulesToMigrate.map { it to false }
            }
        }
    }
}
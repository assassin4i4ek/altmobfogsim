package api.migration.entites

import api.common.entities.SimEntity
import org.fog.application.AppModule

interface ModuleLaunchingDevice: SimEntity {
    val mAppModuleList: List<AppModule>
    val mAppToModulesMap: Map<String, List<String>>
    val mParentId: Int
}
package api.migration.models.problem.environment

import org.fog.application.AppEdge
import org.fog.entities.Actuator

data class EnvironmentModelPath(
        val links: List<EnvironmentModelPathLink>
) {
    fun reduce(): EnvironmentModelPath {
        val newLinks = mutableListOf<EnvironmentModelPathLink>()
//        val prevSrcDevices = mutableMapOf<AppEdge, MutableList<DeviceSuperposition>>()
        val srcDeviceMapping = mutableMapOf<AppEdge, MutableMap<Int,DeviceSuperposition>>()
        links.forEach { nextLink ->
            if (!srcDeviceMapping.containsKey(nextLink.appEdge)) {
                srcDeviceMapping[nextLink.appEdge] = mutableMapOf()
            }
            if (!srcDeviceMapping[nextLink.appEdge]!!.containsKey(nextLink.srcDevice.device!!.id)) {
                srcDeviceMapping[nextLink.appEdge]!![nextLink.srcDevice.device!!.id] = nextLink.srcDevice
            }
            if (nextLink.destProcessingModule != null || nextLink.destDevice.device is Actuator) {
                newLinks.add(nextLink.copy(srcDevice = srcDeviceMapping[nextLink.appEdge]!![nextLink.srcDevice.device!!.id]!!))
            }
            srcDeviceMapping[nextLink.appEdge]!![nextLink.destDevice.device!!.id] =
                    srcDeviceMapping[nextLink.appEdge]!![nextLink.srcDevice.device!!.id]!!
        }
        return EnvironmentModelPath(newLinks)
    }
}

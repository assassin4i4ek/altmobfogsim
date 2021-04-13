package api.migration.models.mapo.environment

import org.fog.application.AppEdge
import org.fog.application.AppModule

data class EnvironmentModelPathLink(
        val srcDevice: DeviceSuperposition,
        val destDevice: DeviceSuperposition,
        val destProcessingModule: AppModule?,
        val appEdge: AppEdge,
        val appId: String,
        val timeInterval: Double,
        val selectivity: Double,
        val edgeModuleMap: Map<String, Int>
) {
    /*override fun equals(other: Any?): Boolean {
        return if (other is EnvironmentModelPathLink) {
            srcDevice.device == other.srcDevice.device && destDevice.device == other.destDevice.device &&
                    destProcessingModule == other.destProcessingModule && appEdge == other.appEdge &&
                    appId == other.appId && timeInterval == other.timeInterval && selectivity == other.selectivity
        }
        else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = srcDevice.hashCode()
        result = 31 * result + destDevice.hashCode()
        result = 31 * result + (destProcessingModule?.hashCode() ?: 0)
        result = 31 * result + appEdge.hashCode()
        result = 31 * result + appId.hashCode()
        result = 31 * result + timeInterval.hashCode()
        result = 31 * result + selectivity.hashCode()
        return result
    }*/
}

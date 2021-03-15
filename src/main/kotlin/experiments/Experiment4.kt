package experiments

import org.fog.application.Application
import org.fog.entities.Actuator
import org.fog.entities.FogDevice
import org.fog.entities.Sensor
import org.fog.placement.ModulePlacement

class Experiment4(resultsPath: String?, isWarmup: Boolean, isLog: Boolean, seed: Long, eegTransRates: DoubleArray, totalGatewaysCount: IntArray, numMobilesPerGateway: Int, isCloudCount: BooleanArray) : Experiment(resultsPath, isWarmup,
        isLog, seed, eegTransRates, totalGatewaysCount, numMobilesPerGateway, isCloudCount,
) {
    override fun placeModules(isCloud: Boolean, fogDevices: List<FogDevice>, app: Application, sensors: List<Sensor>, actuators: List<Actuator>): ModulePlacement {
        TODO()
    }

    override fun createAllDevices(numGateways: Int, numMobilesPerGateway: Int, brokerId: Int, appId: String, eegTransRate: Double): Triple<List<FogDevice>, List<Sensor>, List<Actuator>> {
        TODO("Not yet implemented")
    }
}
package api.migration.original

import api.common.utils.ConnectionUtils
import addons.migration.original.entities.DynamicGatewayConnectionModuleLaunchingDeviceImpl
import addons.migration.original.entities.MigrationSupportingNetworkDeviceImpl
import api.migration.models.MigrationModel
import api.migration.models.MigrationModelImpl
import api.migration.models.timeprogression.FixedTimeProgression
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.core.SimEntity
import org.cloudbus.cloudsim.core.SimEvent
import org.fog.application.AppModule
import org.fog.placement.ModulePlacementEdgewards
import org.fog.utils.*
import org.junit.jupiter.api.Test
import utils.*
import kotlin.math.round
import kotlin.test.assertEquals

class MigrationSupportingDeviceTest: BaseFogDeviceTest() {
    @Suppress("SameParameterValue")
    private fun createMobileDevice(
            name: String, schedulingInterval: Double,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double
    ): DynamicGatewayConnectionModuleLaunchingDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(1000.0).let {
            DynamicGatewayConnectionModuleLaunchingDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun createMigratingNetworkDevice(
            name: String, schedulingInterval: Double,
            uplinkBandwidth: Double, downlinkBandwidth: Double, uplinkLatency: Double, ratePerMips: Double,
            migrationModel: MigrationModel
    ): MigrationSupportingNetworkDeviceImpl {
        return createCharacteristicsAndAllocationPolicy(10000.0).let {
            MigrationSupportingNetworkDeviceImpl(
                    name, it.first, it.second, emptyList(), schedulingInterval,
                    uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, migrationModel
            )
        }
    }

    @Test
    fun test1() {
        init(1, ::createExperimentApp)
        val mob = createMobileDevice("Mob", 10.0, 1000.0, 1000.0, 0.1, 0.01)
        val serv1 = createMigratingNetworkDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                MigrationModelImpl(FixedTimeProgression(1.0))
        )
        val serv2 = createMigratingNetworkDevice("Serv2", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                MigrationModelImpl(FixedTimeProgression(1.0))
        )
        fogDeviceList.addAll(listOf(mob, serv1, serv2))

        mob.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(mob, 0)

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 4.9, 1, null)
                send(serv1.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
                send(serv2.mId, 9.9, FogEvents.RESOURCE_MGMT, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob)
                    ConnectionUtils.connectChildToParent(serv2, mob)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1.016, round( TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!! * 1000) / 1000)
            assertEquals(0, serv1.getVmList<AppModule>().size)
            assertEquals(1, serv2.getVmList<AppModule>().size)
            assertEquals(0.18, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }

    @Test
    fun test2() {
        init(2, ::createExperimentApp)
        val mob1 = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01)
        val mob2 = createMobileDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01)
        val serv1 = createMigratingNetworkDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01,
            MigrationModelImpl(FixedTimeProgression(1.0))
        )
        val serv2 = createMigratingNetworkDevice("Serv2", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                MigrationModelImpl(FixedTimeProgression(1.0))
        )
        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2))

        mob1.parentId = serv1.id
        mob2.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(mob1, 0)
        connectSensorsAndActuatorsToDevice(mob2, 1)

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 4.9, 1, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob2)
                    ConnectionUtils.connectChildToParent(serv2, mob2)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1.6947142857142856, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!!)
            assertEquals(1, serv1.getVmList<AppModule>().size)
            assertEquals(1, serv2.getVmList<AppModule>().size)
            assertEquals(0.39, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }

    @Test
    fun test3() {
        init(2, ::createExperimentApp)
        val mob1 = createMobileDevice("Mob1", 10.0, 1000.0, 1000.0, 0.1, 0.01)
        val mob2 = createMobileDevice("Mob2", 10.0, 1000.0, 1000.0, 0.1, 0.01)
        val serv1 = createMigratingNetworkDevice("Serv1", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                MigrationModelImpl(FixedTimeProgression(1.0))
        )
        val serv2 = createMigratingNetworkDevice("Serv2", 10.0, 1000.0, 1000.0, 0.1, 0.01,
                MigrationModelImpl(FixedTimeProgression(1.0))
        )
        fogDeviceList.addAll(listOf(mob1, mob2, serv1, serv2))

        mob1.parentId = serv1.id
        mob2.parentId = serv1.id
        serv1.parentId = cloud.id
        serv2.parentId = cloud.id
        connectSensorsAndActuatorsToDevice(mob1, 0)
        connectSensorsAndActuatorsToDevice(mob2, 1)

        object : SimEntity("test") {
            override fun startEntity() {
                send(id, 4.9, 1, null)
            }

            override fun processEvent(ev: SimEvent) {
                if (ev.tag == 1) {
                    ConnectionUtils.disconnectChildFromParent(serv1, mob1)
                    ConnectionUtils.disconnectChildFromParent(serv1, mob2)
                    ConnectionUtils.connectChildToParent(serv2, mob1)
                    ConnectionUtils.connectChildToParent(serv2, mob2)
                }
            }

            override fun shutdownEntity() {}
        }

        val startNetworkUsage = NetworkUsageMonitor.getNetworkUsage()
        launchTest {
            val loopId = app.loops[0].loopId
            assertEquals(1.7883571428571428, TimeKeeper.getInstance().loopIdToCurrentAverage[loopId]!!)
            assertEquals(0, serv1.getVmList<AppModule>().size)
            assertEquals(1, serv2.getVmList<AppModule>().size)
            assertEquals(2, serv2.getVmList<AppModule>().first()!!.numInstances)
            assertEquals(0.44, round((NetworkUsageMonitor.getNetworkUsage() - startNetworkUsage) / Config.MAX_SIMULATION_TIME * 100) / 100)
        }
    }

    override fun launchTest(onStopSimulation: () -> Unit) {
        controller = TestController("Controller", fogDeviceList, sensorList, actuatorList, onStopSimulation)
        controller.submitApplication(app, 0, ModulePlacementEdgewards(fogDeviceList, sensorList, actuatorList, app, mm))
        CloudSim.startSimulation()
    }
}
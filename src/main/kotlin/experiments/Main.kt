package experiments

import org.cloudbus.cloudsim.core.CloudSim
import org.fog.utils.Config
import java.io.FileReader
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.full.declaredMembers

fun main() {
//    Experiment1(null, false,false,1000,
//            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16),4, booleanArrayOf(false, true)
//    ).start()
    Experiment2("results/experiment2.txt", true, false, 1000,
            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false, true)).start()
    FileReader("results/experiment2.txt").readLines().forEach(::println)

//    Config.MAX_SIMULATION_TIME = 300
//    Experiment1(null, false, false,1000,
//            doubleArrayOf(10.0), intArrayOf(8), 4, booleanArrayOf(true)).start()
//    Experiment2(null, false,false,1000,
//            doubleArrayOf(10.0), intArrayOf(8), 4, booleanArrayOf(true)).start()
//    Experiment3(null, false, true, 1000,
//            doubleArrayOf(5.0), intArrayOf(2), 1, booleanArrayOf(true)).start()
}
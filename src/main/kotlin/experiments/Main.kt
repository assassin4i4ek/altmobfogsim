package experiments

import java.io.FileReader
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.full.declaredMembers

fun main() {
//    Experiment1("results/experiment1.txt", false,1000, doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16),
//            4, booleanArrayOf(false, true)).start()
    Experiment2("results/experiment2.txt", true, 1000,
            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false, true)).start()
//    Experiment1(null, false,1000, doubleArrayOf(10.0), intArrayOf(4), 4, booleanArrayOf(true, false)).start()
    FileReader("results/experiment2.txt").readLines().forEach(::println)
}
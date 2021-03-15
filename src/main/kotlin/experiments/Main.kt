package experiments

import java.io.FileReader

fun main() {
//    Experiment1(null, false,false,1000,
//            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16),4, booleanArrayOf(false, true)
//    ).start()
//    Experiment2("results/experiment2.txt", true, false, 1000,
//            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false, true)).start()
//    Experiment3("results/experiment3.txt", true, false, 1000,
//            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false, true)).start()
    Experiment4("results/experiment4.txt", true, false, 1000,
            doubleArrayOf(10.0, 5.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false, true)).start()
    FileReader("results/experiment4.txt").readLines().forEach(::println)

//    Config.MAX_SIMULATION_TIME = 00
//    Experiment1(null, false, false,1000,
//            doubleArrayOf(10.0), intArrayOf(1, 2, 4, 8, 16), 4, booleanArrayOf(false)).start()
//    Experiment2(null, false,true,1000,
//            doubleArrayOf(5.0), intArrayOf(1), 4, booleanArrayOf(true)).start()
//    Experiment3(null, false, true, 1000,
//            doubleArrayOf(5.0), intArrayOf(1), 4, booleanArrayOf(true)).start()
//    Experiment4(null, false, false, 1000,
//            doubleArrayOf(5.0), intArrayOf(2), 4, booleanArrayOf(true)).start()
}
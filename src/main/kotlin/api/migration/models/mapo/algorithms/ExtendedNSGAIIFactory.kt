package api.migration.models.mapo.algorithms

import org.moeaframework.algorithm.AdaptiveTimeContinuation
import org.moeaframework.algorithm.NSGAII
import org.moeaframework.analysis.sensitivity.EpsilonHelper
import org.moeaframework.core.*
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.DominanceComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.InjectedInitialization
import org.moeaframework.core.operator.RandomInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.UniformSelection
import org.moeaframework.core.operator.real.UM
import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.OperatorFactory
import org.moeaframework.util.TypedProperties
import java.util.*
import kotlin.math.max

class ExtendedNSGAIIFactory(private val injectedSolutions: List<Solution>): AlgorithmFactory() {
    override fun getAlgorithm(name: String, untypedProperties: Properties, problem: Problem): Algorithm {
        val properties = TypedProperties(untypedProperties)
        val initialization: Initialization = InjectedInitialization(
                problem, properties.getDouble("populationSize", 100.0).toInt(), injectedSolutions
        )
        val population = NondominatedSortingPopulation()
        val selection = if (properties.getBoolean("withReplacement", true)) {
            TournamentSelection(2, ChainedComparator(ParetoDominanceComparator(), CrowdingComparator()))
        } else null
        val variation: Variation = OperatorFactory.getInstance().getVariation(null as String?, properties, problem)

        return NSGAII(problem, population, null, selection, variation, initialization)

//        val properties = TypedProperties(untypedProperties)
//        val initialization = InjectedInitialization(problem, properties.getDouble("populationSize", 100.0).toInt(), injectedSolutions)
//        val nondominatedPopulation = NondominatedSortingPopulation(ParetoDominanceComparator())
//        val archive = EpsilonBoxDominanceArchive(properties.getDoubleArray("epsilon", doubleArrayOf(EpsilonHelper.getEpsilon(problem))))
//        val selection = TournamentSelection(2, ChainedComparator(ParetoDominanceComparator(), CrowdingComparator()))
//        val variation = OperatorFactory.getInstance().getVariation(null, properties, problem)
//        val nsgaii = NSGAII(problem, nondominatedPopulation, null, selection, variation, initialization)
//        return AdaptiveTimeContinuation(nsgaii,
//                properties.getInt("windowSize", 100),
//                max(
//                        properties.getInt("windowSize", 100),
//                        properties.getInt("maxWindowSize", 100)
//                ),
//                1.0 / properties.getDouble("injectionRate", 0.25),
//                properties.getInt("minimumPopulationSize", 100),
//                properties.getInt("maximumPopulationSize", 10000),
//                UniformSelection(), UM(1.0))
    }
}
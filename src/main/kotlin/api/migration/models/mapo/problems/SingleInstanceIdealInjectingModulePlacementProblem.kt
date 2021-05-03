package api.migration.models.mapo.problems

import api.migration.models.mapo.ideals.IdealEnvironmentBuilder
import api.migration.models.mapo.objectives.Objective
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.PRNG
import org.moeaframework.core.Solution
import org.moeaframework.core.operator.UniformCrossover
import org.moeaframework.core.operator.real.SBX
import org.moeaframework.core.operator.real.UNDX
import org.moeaframework.core.variable.EncodingUtils
import java.lang.Exception
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class SingleInstanceIdealInjectingModulePlacementProblem(
        private val idealEnvironments: List<IdealEnvironmentBuilder>,
        objectives: List<Objective>,
        devices: List<MigrationSupportingDevice>,
        modules: List<AppModule>,
        controller: Controller,
        private val numOfInjectedSolutions: Int = 1,
        private val confidenceLevel: Double = 0.66,
        ): SingleInstanceModulePlacementProblem(objectives, devices, modules, controller) {
    class Factory(
            private val idealEnvironments: List<IdealEnvironmentBuilder>,
            private val numOfInjectedSolutions: Int? = null
    ) : ModulePlacementProblemFactory {
        override fun newProblem(objectives: List<Objective>, devices: List<MigrationSupportingDevice>, modules: List<AppModule>, controller: Controller): ModulePlacementProblem<*> {
            return if(numOfInjectedSolutions != null) {
                SingleInstanceIdealInjectingModulePlacementProblem(idealEnvironments, objectives, devices, modules, controller, numOfInjectedSolutions)
            }
            else {
                SingleInstanceIdealInjectingModulePlacementProblem(idealEnvironments, objectives, devices, modules, controller)
            }
        }
    }

    override fun injectedSolutions(populationSize: Int): List<Solution> {
        val proposedSolutions = idealEnvironments.map { ideal ->
            ideal.getIdealModulesMapping(currentEnvironmentModel, devices, modules)
        }.map { idealModulesMapping ->
            val idealSolution = newSolution()
            idealModulesMapping.forEach { (device, modules) ->
                val deviceIndex = devices.indexOf(device)
                modules.forEachIndexed { j, moduleIsPresent ->
                    if (moduleIsPresent.second) {
                        EncodingUtils.setInt(idealSolution.getVariable(j), deviceIndex)
                    }
                }
            }
            if (validateSolution(idealSolution)) {
                idealSolution
            }
//          else null
            else throw Exception("Ideal solution is invalid") //
        }

        val injectedSolutions = mutableListOf<Solution>()
        val randomSolution = newSolution().apply {
            repeat(numberOfVariables) {
                getVariable(it).randomize()
            }
        }
        val extendedProposedSolutions = listOf(*proposedSolutions.toTypedArray(), randomSolution)
        val random = PRNG.getRandom().asKotlinRandom()

        repeat(numOfInjectedSolutions) { i ->
            injectedSolutions.add(when {
                i < confidenceLevel * numOfInjectedSolutions -> proposedSolutions[i % proposedSolutions.size].copy()
                else -> newSolution().apply {
                    repeat(numberOfVariables) { j ->
                        setVariable(j, extendedProposedSolutions.random(random).getVariable(j).copy())
                    }
                }
            })
        }
        return injectedSolutions
    }
}
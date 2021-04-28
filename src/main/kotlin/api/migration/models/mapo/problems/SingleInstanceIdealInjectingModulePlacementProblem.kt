package api.migration.models.mapo.problems

import api.migration.models.mapo.environment.EnvironmentModelImpl
import api.migration.models.mapo.ideals.IdealEnvironmentBuilder
import api.migration.models.mapo.objectives.Objective
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.PRNG
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.EncodingUtils
import java.lang.Exception
import kotlin.math.min

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

        repeat(numOfInjectedSolutions) { i ->
            injectedSolutions.add(when {
                i < confidenceLevel * numOfInjectedSolutions -> proposedSolutions[i % proposedSolutions.size]
                else -> newSolution().apply {
                    repeat(numberOfVariables) { j ->
                        val index = PRNG.nextInt(proposedSolutions.size + 1)
                        setVariable(j, proposedSolutions.getOrElse(index) { randomSolution }.getVariable(j).copy())
                    }
                }
            })
        }
//                injectedSolutions.size < proposedSolutions.size -> injectedSolutions.add(proposedSolutions[i].copy())
//                injectedSolutions.size == 1 -> injectedSolutions.add(
//                        newSolution().apply {
//                            repeat(numberOfVariables) { j ->
//                                getVariable(j).randomize()
//                            }
//                        }
//                )
//                else -> injectedSolutions.add(newSolution().apply {
//                    repeat(numberOfVariables) { j ->
//                        setVariable(j, injectedSolutions.random().getVariable(j).copy())
//                    }
//                })
//            }
//        }

//        while (injectedSolutions.size < numOfInjectedSolutions) {
//            if (injectedSolutions.size < proposedSolutions.size) {
//
//            }
//        }
//        repeat(min(numOfInjectedSolutions, proposedSolutions.size)) { i ->
//            injectedSolutions.add(proposedSolutions[i].copy())
//        }
//        repeat(numOfInjectedSolutions - injectedSolutions.size) { i ->
//
//        }
//        repeat(numOfInjectedSolutions) { i ->
//            injectedSolutions.add(proposedSolutions[i % proposedSolutions.size].copy())
//        }
        return injectedSolutions
    }
}
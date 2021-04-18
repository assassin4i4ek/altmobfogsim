package api.migration.models.mapo.problems

import api.migration.models.mapo.objectives.Objective
import api.migration.original.entites.MigrationSupportingDevice
import org.fog.application.AppModule
import org.fog.placement.Controller
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.EncodingUtils

class SingleInstanceCurrentSolutionInjectingModulePlacementProblem(
        objectives: List<Objective>,
        devices: List<MigrationSupportingDevice>,
        modules: List<AppModule>,
        controller: Controller
): SingleInstanceModulePlacementProblem(objectives, devices, modules, controller) {
    class Factory: ModulePlacementProblemFactory {
        override fun newProblem(objectives: List<Objective>, devices: List<MigrationSupportingDevice>, modules: List<AppModule>, controller: Controller): ModulePlacementProblem<*> {
            return SingleInstanceCurrentSolutionInjectingModulePlacementProblem(objectives, devices, modules, controller)
        }
    }

    override fun injectedSolutions(populationSize: Int): List<Solution> {
        return listOf(currentEnvironmentAsSolution())
    }
}
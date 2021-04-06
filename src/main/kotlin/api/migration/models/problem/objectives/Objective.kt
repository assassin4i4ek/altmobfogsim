package api.migration.models.problem.objectives

import api.migration.models.problem.environment.EnvironmentModel

interface Objective {
    fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double
}
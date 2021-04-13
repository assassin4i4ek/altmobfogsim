package api.migration.models.mapo.objectives

import api.migration.models.mapo.environment.EnvironmentModel

interface Objective {
    fun compute(currentEnvironmentModel: EnvironmentModel, newEnvironmentModel: EnvironmentModel): Double
}
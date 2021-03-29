package api.migration.original.utils

import org.fog.application.AppModule

data class OldNewModulePair(
        val oldModule: AppModule,
        val newModule: AppModule
)

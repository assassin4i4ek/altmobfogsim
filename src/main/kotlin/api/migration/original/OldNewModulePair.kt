package api.migration.original

import org.fog.application.AppModule

data class OldNewModulePair(
        val oldModule: AppModule,
        val newModule: AppModule
)

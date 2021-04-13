package api.migration.utils

import org.cloudbus.cloudsim.core.CloudSim

object MigrationLogger {
    data class LogEntry(
            val modelTime: Double,
            val decisionMakerName: String,
            val migrationRequest: MigrationRequest)

    private val _migrations: MutableList<LogEntry> = mutableListOf()

    val migrations: List<LogEntry> get() = _migrations

    fun clear() {
        _migrations.clear()
    }

    fun logMigrationDecision(decisionMakerName: String, migrationRequest: MigrationRequest) {
        _migrations.add(LogEntry(CloudSim.clock(), decisionMakerName, migrationRequest))
    }
}
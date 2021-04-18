package api.migration.utils

import api.accesspoint.utils.AccessPointEventsLogger
import org.cloudbus.cloudsim.core.CloudSim
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object MigrationLogger {
    data class LogEntry(
            val modelTime: Double,
            val decisionMakerName: String,
            val migrationRequest: MigrationRequest) {
        override fun toString(): String {
            return "${dateFormat.format(Date(modelTime.toLong()))}: ${migrationRequest.appModuleName} from ${migrationRequest.from} to ${migrationRequest.to}"
        }
    }

    var enabled: Boolean = false
    var dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
    private val _migrations: MutableList<LogEntry> = mutableListOf()

    val migrations: List<LogEntry> get() = _migrations

    fun clear() {
        _migrations.clear()
    }

    fun logMigrationDecision(decisionMakerName: String, migrationRequest: MigrationRequest) {
        val logEntry = LogEntry(CloudSim.clock(), decisionMakerName, migrationRequest)
        _migrations.add(logEntry)
        if (enabled) {
            println(logEntry)
        }
    }
}
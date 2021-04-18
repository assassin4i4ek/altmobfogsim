package api.accesspoint.utils

import api.accesspoint.original.entities.AccessPoint
import api.accesspoint.original.entities.AccessPointConnectedDevice
import org.cloudbus.cloudsim.core.CloudSim
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

object AccessPointEventsLogger {
    data class LogEntry(
            val modelTime: Double,
            val connectedDevice: AccessPointConnectedDevice,
            val previousAccessPoint: AccessPoint?,
            val nextAccessPoint: AccessPoint?
            ) {
        override fun toString(): String {
            return "${dateFormat.format(Date(modelTime.toLong()))}: $connectedDevice connected from $previousAccessPoint to $nextAccessPoint"
        }
    }

    var enabled: Boolean = false
    var dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
    private val _connections: MutableList<LogEntry> = mutableListOf()

    val connections: List<LogEntry> get() = _connections

    fun clear() {
        _connections.clear()
    }

    fun logAccessPointEvent(connectedDevice: AccessPointConnectedDevice, fromAccessPoint: AccessPoint?, toAccessPoint: AccessPoint?) {
        val logEntry = LogEntry(CloudSim.clock(), connectedDevice, fromAccessPoint, toAccessPoint)
        _connections.add(logEntry)
        if (enabled) {
            println(logEntry)
        }
    }
}
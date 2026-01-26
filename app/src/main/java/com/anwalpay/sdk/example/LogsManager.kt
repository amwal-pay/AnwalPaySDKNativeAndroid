package com.anwalpay.sdk.example

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.*

/**
 * Singleton class to manage logs across the application
 */
object LogsManager {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs.toList()

    fun addLog(message: String, type: LogType) {
        val entry = LogEntry(
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _logs.add(entry)
    }

    fun clearLogs() {
        _logs.clear()
    }
}

/**
 * Represents different types of logs
 */
enum class LogType {
    RESPONSE,
    CANCELLED,
    CUSTOMER_ID,
    ERROR,
    INFO,
    DEBUG
}

/**
 * Represents a single log entry
 */
data class LogEntry(
    val message: String,
    val type: LogType,
    val timestamp: Long
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
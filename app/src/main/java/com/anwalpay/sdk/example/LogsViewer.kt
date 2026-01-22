package com.anwalpay.sdk.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewer(
    onDismiss: () -> Unit
) {
    val logs by remember { derivedStateOf { LogsManager.logs } }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SDK Logs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row {
                        IconButton(onClick = { LogsManager.clearLogs() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear all logs"
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }

                Divider()

                // Logs list
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No logs yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SDK interactions will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            LogEntryItem(
                                log = log,
                                onClick = { selectedLog = log }
                            )
                        }
                    }
                }
            }
        }
    }

    // Log details dialog
    selectedLog?.let { log ->
        LogDetailsDialog(
            log = log,
            onDismiss = { selectedLog = null }
        )
    }
}

@Composable
fun LogEntryItem(
    log: LogEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = log.type.getIcon(),
                        contentDescription = null,
                        tint = log.type.getColor(),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.type.getDisplayName(),
                        fontWeight = FontWeight.Bold,
                        color = log.type.getColor()
                    )
                }
                Text(
                    text = log.formattedTimestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (log.message.length > 100) {
                    "${log.message.take(100)}..."
                } else {
                    log.message
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LogDetailsDialog(
    log: LogEntry,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = log.type.getIcon(),
                    contentDescription = null,
                    tint = log.type.getColor()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(log.type.getDisplayName())
            }
        },
        text = {
            Column {
                Text(
                    text = "Time: ${log.formattedTimestamp}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelectionContainer {
                    Text(
                        text = log.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Log Entry", log.message)
                    clipboard.setPrimaryClip(clip)
                    onDismiss()
                }
            ) {
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Extension functions for LogType
fun LogType.getIcon(): ImageVector {
    return when (this) {
        LogType.RESPONSE -> Icons.Default.CheckCircle
        LogType.CANCELLED -> Icons.Default.Close
        LogType.CUSTOMER_ID -> Icons.Default.Person
        LogType.ERROR -> Icons.Default.Warning
        LogType.INFO -> Icons.Default.Info
        LogType.DEBUG -> Icons.Default.Info
    }
}

fun LogType.getColor(): Color {
    return when (this) {
        LogType.RESPONSE -> Color(0xFF4CAF50)
        LogType.CANCELLED -> Color(0xFFFF9800)
        LogType.CUSTOMER_ID -> Color(0xFF2196F3)
        LogType.ERROR -> Color(0xFFF44336)
        LogType.INFO -> Color(0xFF2196F3)
        LogType.DEBUG -> Color(0xFF9E9E9E)
    }
}

fun LogType.getDisplayName(): String {
    return when (this) {
        LogType.RESPONSE -> "Response"
        LogType.CANCELLED -> "Cancelled"
        LogType.CUSTOMER_ID -> "Customer ID"
        LogType.ERROR -> "Error"
        LogType.INFO -> "Info"
        LogType.DEBUG -> "Debug"
    }
}
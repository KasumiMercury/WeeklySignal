package net.mercuryksm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Represents the status of an operation (deletion, alarm cancellation, etc.)
 */
sealed class OperationStatus {
    object Loading : OperationStatus()
    
    data class Success(
        val message: String,
        val details: List<String> = emptyList()
    ) : OperationStatus()
    
    data class Error(
        val message: String,
        val details: List<String> = emptyList()
    ) : OperationStatus()
    
    data class PartialSuccess(
        val message: String,
        val successDetails: List<String> = emptyList(),
        val failureDetails: List<String> = emptyList()
    ) : OperationStatus()
}

/**
 * Modal dialog that shows the status of operations like deletion and alarm cancellation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationStatusModal(
    status: OperationStatus?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (status != null) {
        AlertDialog(
            onDismissRequest = {
                if (status !is OperationStatus.Loading) {
                    onDismiss()
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (status) {
                        is OperationStatus.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        is OperationStatus.Success -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        is OperationStatus.Error -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        is OperationStatus.PartialSuccess -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Partial Success",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    Text(
                        text = when (status) {
                            is OperationStatus.Loading -> "Processing..."
                            is OperationStatus.Success -> "Success"
                            is OperationStatus.Error -> "Error"
                            is OperationStatus.PartialSuccess -> "Partial Success"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (status) {
                        is OperationStatus.Loading -> {
                            Text(
                                text = "Please wait while the operation completes...",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        is OperationStatus.Success -> {
                            Text(
                                text = status.message,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (status.details.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                status.details.forEach { detail ->
                                    Text(
                                        text = "• $detail",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        is OperationStatus.Error -> {
                            Text(
                                text = status.message,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            if (status.details.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                status.details.forEach { detail ->
                                    Text(
                                        text = "• $detail",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        is OperationStatus.PartialSuccess -> {
                            Text(
                                text = status.message,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (status.successDetails.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Successful operations:",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                status.successDetails.forEach { detail ->
                                    Text(
                                        text = "• $detail",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            if (status.failureDetails.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Failed operations:",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                status.failureDetails.forEach { detail ->
                                    Text(
                                        text = "• $detail",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (status !is OperationStatus.Loading) {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            },
            modifier = modifier
        )
    }
}

/**
 * Helper functions to create common operation status instances
 */
object OperationStatusHelper {
    fun loading(): OperationStatus = OperationStatus.Loading
    
    fun signalItemDeletedSuccessfully(alarmsDeleted: Boolean): OperationStatus {
        return if (alarmsDeleted) {
            OperationStatus.Success(
                message = "Signal item deleted successfully",
                details = listOf("All associated alarms have been cancelled")
            )
        } else {
            OperationStatus.PartialSuccess(
                message = "Signal item deleted with warnings",
                successDetails = listOf("Signal item removed from database"),
                failureDetails = listOf("Some alarms may not have been cancelled properly")
            )
        }
    }
    
    fun timeSlotDeletedSuccessfully(alarmDeleted: Boolean): OperationStatus {
        return if (alarmDeleted) {
            OperationStatus.Success(
                message = "Time slot deleted successfully",
                details = listOf("Associated alarm has been cancelled")
            )
        } else {
            OperationStatus.PartialSuccess(
                message = "Time slot deleted with warnings",
                successDetails = listOf("Time slot removed"),
                failureDetails = listOf("Alarm may not have been cancelled properly")
            )
        }
    }
    
    fun signalItemDeleteFailed(reason: String): OperationStatus {
        return OperationStatus.Error(
            message = "Failed to delete signal item",
            details = listOf(reason)
        )
    }
    
    fun timeSlotDeleteFailed(reason: String): OperationStatus {
        return OperationStatus.Error(
            message = "Failed to delete time slot",
            details = listOf(reason)
        )
    }
    
    fun signalItemCreateFailed(reason: String): OperationStatus {
        return OperationStatus.Error(
            message = "Failed to create signal item",
            details = listOf(
                reason,
                "Device alarm state takes priority over app data."
            )
        )
    }
    
    fun signalItemUpdateFailed(reason: String): OperationStatus {
        return OperationStatus.Error(
            message = "Failed to update signal item",
            details = listOf(
                reason,
                "Device alarm state takes priority over app data."
            )
        )
    }
    
    fun alarmOperationFailed(operation: String, reason: String): OperationStatus {
        return OperationStatus.Error(
            message = "Alarm operation failed",
            details = listOf(
                "Operation: $operation",
                "Reason: $reason",
                "Database was not modified to maintain consistency with device alarms."
            )
        )
    }
}
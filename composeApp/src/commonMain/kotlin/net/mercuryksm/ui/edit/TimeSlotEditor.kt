package net.mercuryksm.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.ui.components.OperationStatus
import net.mercuryksm.ui.components.OperationStatusModal
import net.mercuryksm.ui.weekly.WeeklySignalViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotEditor(
    timeSlots: List<TimeSlot>,
    onTimeSlotsChanged: (List<TimeSlot>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeeklySignalViewModel? = null,
    signalItem: SignalItem? = null
) {
    var showTimeSlotDialog by remember { mutableStateOf(false) }
    var editingTimeSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var operationStatus by remember { mutableStateOf<OperationStatus?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Time Slots",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (timeSlots.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No time slots configured",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { timeSlot ->
                    TimeSlotRow(
                        timeSlot = timeSlot,
                        onEdit = {
                            editingTimeSlot = timeSlot
                            showTimeSlotDialog = true
                        },
                        onDelete = {
                            onTimeSlotsChanged(timeSlots.filter { it.id != timeSlot.id })
                        }
                    )
                }
            }
        }

        // Add new time slot button
        OutlinedButton(
            onClick = {
                editingTimeSlot = null
                showTimeSlotDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add time slot"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Time Slot")
        }
    }

    // Time slot dialog
    if (showTimeSlotDialog) {
        TimeSlotDialog(
            timeSlot = editingTimeSlot,
            onConfirm = { newTimeSlot ->
                if (editingTimeSlot != null) {
                    // Edit existing
                    onTimeSlotsChanged(
                        timeSlots.map { 
                            if (it.id == editingTimeSlot?.id) {
                                newTimeSlot.copy(id = it.id)
                            } else it
                        }
                    )
                } else {
                    // Add new
                    onTimeSlotsChanged(
                        timeSlots + newTimeSlot.copy(id = UUID.randomUUID().toString())
                    )
                }
                showTimeSlotDialog = false
                editingTimeSlot = null
            },
            onDismiss = {
                showTimeSlotDialog = false
                editingTimeSlot = null
            }
        )
    }
    
    // Operation Status Modal
    OperationStatusModal(
        status = operationStatus,
        onDismiss = { operationStatus = null }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSlotRow(
    timeSlot: TimeSlot,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = timeSlot.getDisplayText(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete time slot",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

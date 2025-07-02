package net.mercuryksm.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.TimeSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotDialog(
    timeSlot: TimeSlot? = null,
    onConfirm: (TimeSlot) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDayOfWeek by remember { mutableStateOf(timeSlot?.dayOfWeek ?: DayOfWeekJp.MONDAY) }
    var selectedHour by remember { mutableIntStateOf(timeSlot?.hour ?: 9) }
    var selectedMinute by remember { mutableIntStateOf(timeSlot?.minute ?: 0) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (timeSlot == null) "Add Time Slot" else "Edit Time Slot",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Day of Week Selection
                Text(
                    text = "Day of Week",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DayOfWeekJp.values().forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedDayOfWeek == day),
                                    onClick = { selectedDayOfWeek = day },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedDayOfWeek == day),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${day.getShortDisplayName()} (${day.getDisplayName()})",
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Selection
                Text(
                    text = "Time",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTimeSlot = TimeSlot(
                        id = timeSlot?.id ?: "",
                        hour = selectedHour,
                        minute = selectedMinute,
                        dayOfWeek = selectedDayOfWeek
                    )
                    onConfirm(newTimeSlot)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
package net.mercuryksm.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import net.mercuryksm.data.DayOfWeekJp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRegistrationForm(
    name: String,
    onNameChange: (String) -> Unit,
    hour: Int,
    onHourChange: (Int) -> Unit,
    minute: Int,
    onMinuteChange: (Int) -> Unit,
    selectedDay: DayOfWeekJp,
    onDayChange: (DayOfWeekJp) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    sound: Boolean,
    onSoundChange: (Boolean) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Signal Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        TimePicker(
            hour = hour,
            minute = minute,
            onHourChange = onHourChange,
            onMinuteChange = onMinuteChange
        )
        
        DayOfWeekSelector(
            selectedDay = selectedDay,
            onDayChange = onDayChange
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        NotificationOptions(
            sound = sound,
            onSoundChange = onSoundChange,
            vibration = vibration,
            onVibrationChange = onVibrationChange
        )
    }
}

@Composable
private fun TimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Hour", style = MaterialTheme.typography.bodySmall)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                onHourChange(if (hour > 0) hour - 1 else 23)
                            }
                        ) {
                            Text("-")
                        }
                        
                        Text(
                            text = String.format("%02d", hour),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.widthIn(min = 48.dp)
                        )
                        
                        TextButton(
                            onClick = { 
                                onHourChange(if (hour < 23) hour + 1 else 0)
                            }
                        ) {
                            Text("+")
                        }
                    }
                }
                
                Text(":", style = MaterialTheme.typography.headlineMedium)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Minute", style = MaterialTheme.typography.bodySmall)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                onMinuteChange(if (minute > 0) minute - 1 else 59)
                            }
                        ) {
                            Text("-")
                        }
                        
                        Text(
                            text = String.format("%02d", minute),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.widthIn(min = 48.dp)
                        )
                        
                        TextButton(
                            onClick = { 
                                onMinuteChange(if (minute < 59) minute + 1 else 0)
                            }
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    selectedDay: DayOfWeekJp,
    onDayChange: (DayOfWeekJp) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text(
                text = "Day of Week",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            DayOfWeekJp.entries.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (day == selectedDay),
                            onClick = { onDayChange(day) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (day == selectedDay),
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${day.getDisplayName()} (${day.name})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationOptions(
    sound: Boolean,
    onSoundChange: (Boolean) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sound", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = sound,
                    onCheckedChange = onSoundChange
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibration", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = vibration,
                    onCheckedChange = onVibrationChange
                )
            }
        }
    }
}
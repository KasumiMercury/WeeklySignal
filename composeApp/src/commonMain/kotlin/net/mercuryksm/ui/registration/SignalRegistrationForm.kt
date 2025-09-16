package net.mercuryksm.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.ui.components.ColorPicker
import net.mercuryksm.ui.edit.TimeSlotEditor
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRegistrationForm(
    name: String,
    onNameChange: (String) -> Unit,
    timeSlots: List<TimeSlot>,
    onTimeSlotsChange: (List<TimeSlot>) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    sound: Boolean,
    onSoundChange: (Boolean) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    color: Long,
    onColorChange: (Long) -> Unit
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
        
        ColorPicker(
            selectedColor = color,
            onColorSelected = onColorChange
        )
        
        TimeSlotEditor(
            timeSlots = timeSlots,
            onTimeSlotsChanged = onTimeSlotsChange
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

@Preview
@Composable
private fun SignalRegistrationFormPreview() {
    MaterialTheme {
        SignalRegistrationForm(
            name = "Morning Routine",
            onNameChange = {},
            timeSlots = registrationFormPreviewTimeSlots(),
            onTimeSlotsChange = {},
            description = "Start the day with intent",
            onDescriptionChange = {},
            sound = true,
            onSoundChange = {},
            vibration = true,
            onVibrationChange = {},
            color = 0xFF81C784,
            onColorChange = {}
        )
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

@Preview
@Composable
private fun NotificationOptionsPreview() {
    MaterialTheme {
        NotificationOptions(
            sound = true,
            onSoundChange = {},
            vibration = false,
            onVibrationChange = {}
        )
    }
}

private fun registrationFormPreviewTimeSlots(): List<TimeSlot> {
    return listOf(
        TimeSlot(
            id = "registration-form-1",
            hour = 7,
            minute = 30,
            dayOfWeek = DayOfWeekJp.MONDAY
        ),
        TimeSlot(
            id = "registration-form-2",
            hour = 20,
            minute = 0,
            dayOfWeek = DayOfWeekJp.FRIDAY
        )
    )
}

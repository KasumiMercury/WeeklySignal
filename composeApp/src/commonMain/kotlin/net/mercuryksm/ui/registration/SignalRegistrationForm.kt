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
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.ui.edit.TimeSlotEditor

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
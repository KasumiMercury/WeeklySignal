package net.mercuryksm.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.ui.WeeklySignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalEditScreen(
    signalId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: WeeklySignalViewModel = viewModel { WeeklySignalViewModel() }
    val originalSignalItem = viewModel.getSignalItemById(signalId)

    if (originalSignalItem == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    var name by remember { mutableStateOf(originalSignalItem.name) }
    var description by remember { mutableStateOf(originalSignalItem.description) }
    var sound by remember { mutableStateOf(originalSignalItem.sound) }
    var vibration by remember { mutableStateOf(originalSignalItem.vibration) }
    var timeSlots by remember { mutableStateOf(originalSignalItem.timeSlots) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun saveSignalItem() {
        try {
            if (name.isBlank()) {
                errorMessage = "Signal name is required"
                showErrorDialog = true
                return
            }

            if (timeSlots.isEmpty()) {
                errorMessage = "At least one time slot is required"
                showErrorDialog = true
                return
            }

            val updatedSignalItem = SignalItem(
                id = signalId,
                name = name.trim(),
                timeSlots = timeSlots,
                description = description.trim(),
                sound = sound,
                vibration = vibration
            )

            viewModel.updateSignalItem(updatedSignalItem)
            onNavigateBack()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
            showErrorDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Signal",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveSignalItem() }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignalEditForm(
                name = name,
                onNameChange = { name = it },
                description = description,
                onDescriptionChange = { description = it },
                sound = sound,
                onSoundChange = { sound = it },
                vibration = vibration,
                onVibrationChange = { vibration = it },
                timeSlots = timeSlots,
                onTimeSlotsChange = { timeSlots = it }
            )
        }
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignalEditForm(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    sound: Boolean,
    onSoundChange: (Boolean) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    timeSlots: List<TimeSlot>,
    onTimeSlotsChange: (List<TimeSlot>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Signal Name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Signal Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Time Slots Editor
        TimeSlotEditor(
            timeSlots = timeSlots,
            onTimeSlotsChanged = onTimeSlotsChange
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        // Notification Settings
        Text(
            text = "Notification Settings",
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Sound switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sound",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = sound,
                onCheckedChange = onSoundChange
            )
        }

        // Vibration switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Vibration",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = vibration,
                onCheckedChange = onVibrationChange
            )
        }
    }
}
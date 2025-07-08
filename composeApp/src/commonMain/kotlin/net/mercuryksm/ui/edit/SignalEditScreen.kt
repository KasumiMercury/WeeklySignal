package net.mercuryksm.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.notification.AlarmResult
import net.mercuryksm.notification.NotificationPermissionDialog
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.notification.createTestAlarmSettings
import net.mercuryksm.ui.ColorPicker
import net.mercuryksm.ui.WeeklySignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalEditScreen(
    viewModel: WeeklySignalViewModel,
    signalId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    alarmManager: SignalAlarmManager? = null
) {
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
    var color by remember { mutableStateOf(originalSignalItem.color) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isPreviewLoading by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val showPreviewButton = alarmManager?.isAlarmSupported() == true
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    
    suspend fun testAlarm() {
        if (alarmManager == null) return
        
        if (name.isBlank()) {
            errorMessage = "Name is required for test alarm"
            showErrorDialog = true
            return
        }
        
        if (timeSlots.isEmpty()) {
            errorMessage = "At least one time slot is required for test alarm"
            showErrorDialog = true
            return
        }
        
        // Check permission first
        if (!alarmManager.hasAlarmPermission()) {
            showPermissionDialog = true
            return
        }
        
        val settings = createTestAlarmSettings(
            name = name.trim(),
            description = description.trim(),
            sound = sound,
            vibration = vibration,
            timeSlots = timeSlots
        )
        
        isPreviewLoading = true
        val result = alarmManager.showTestAlarm(settings)
        isPreviewLoading = false
        
        when (result) {
            AlarmResult.PERMISSION_DENIED -> {
                showPermissionDialog = true
            }
            AlarmResult.ERROR -> {
                errorMessage = "Failed to show test alarm"
                showErrorDialog = true
            }
            AlarmResult.NOT_SUPPORTED -> {
                errorMessage = "Alarms not supported on this platform"
                showErrorDialog = true
            }
            AlarmResult.SUCCESS -> {
                // Success - no need to show anything
            }
            else -> {
                // Handle other cases if needed
            }
        }
    }
    
    suspend fun requestPermissionAndTest() {
        if (alarmManager == null) return
        
        isPreviewLoading = true
        val permissionGranted = alarmManager.requestAlarmPermission()
        
        if (permissionGranted) {
            // Permission granted, now show the test alarm
            val settings = createTestAlarmSettings(
                name = name.trim(),
                description = description.trim(),
                sound = sound,
                vibration = vibration,
                timeSlots = timeSlots
            )
            
            val result = alarmManager.showTestAlarm(settings)
            if (result == AlarmResult.ERROR) {
                errorMessage = "Failed to show test alarm"
                showErrorDialog = true
            }
        } else {
            errorMessage = "Alarm permission was denied. Please enable exact alarms in device settings to use this feature."
            showErrorDialog = true
        }
        
        isPreviewLoading = false
        showPermissionDialog = false
    }

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
                vibration = vibration,
                color = color
            )

            viewModel.updateSignalItem(updatedSignalItem) { result ->
                result.onSuccess {
                    onNavigateBack()
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Unknown error occurred"
                    showErrorDialog = true
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
            showErrorDialog = true
        }
    }

    fun deleteSignalItem() {
        try {
            viewModel.removeSignalItem(originalSignalItem) { result ->
                result.onSuccess {
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(50)
                        onNavigateBack()
                    }
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Failed to delete signal"
                    showErrorDialog = true
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to delete signal"
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (showPreviewButton) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    testAlarm()
                                }
                            },
                            enabled = !isPreviewLoading
                        ) {
                            if (isPreviewLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 1.dp
                                )
                            } else {
                                Text("Test Alarm")
                            }
                        }
                    }
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
                onTimeSlotsChange = { timeSlots = it },
                color = color,
                onColorChange = { color = it }
            )
            
            // Delete Button
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Signal",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // Permission Dialog
    NotificationPermissionDialog(
        showDialog = showPermissionDialog,
        onDismiss = { showPermissionDialog = false },
        onRequestPermission = {
            coroutineScope.launch {
                requestPermissionAndTest()
            }
        }
    )

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Signal") },
            text = { Text("Are you sure you want to delete this signal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteSignalItem()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
    color: Long,
    onColorChange: (Long) -> Unit,
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

        // Color Picker
        ColorPicker(
            selectedColor = color,
            onColorSelected = onColorChange
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

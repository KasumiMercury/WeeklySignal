package net.mercuryksm.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.notification.AlarmResult
import net.mercuryksm.notification.NotificationPermissionDialog
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.notification.createTestAlarmSettings
import net.mercuryksm.notification.rememberPermissionHelper
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRegistrationScreen(
    onSignalSaved: (SignalItem, (Result<Unit>) -> Unit) -> Unit,
    onBackPressed: () -> Unit,
    alarmManager: SignalAlarmManager? = null
) {
    var name by remember { mutableStateOf("") }
    var timeSlots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }
    var description by remember { mutableStateOf("") }
    var sound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(0xFF6750A4L) }
    
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isPreviewLoading by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val showPreviewButton = alarmManager?.isAlarmSupported() == true
    val coroutineScope = rememberCoroutineScope()
    val permissionHelper = rememberPermissionHelper()
    
    // Setup permission helper with alarm manager if both are available
    LaunchedEffect(permissionHelper, alarmManager) {
        if (permissionHelper != null && alarmManager != null) {
            alarmManager.setPermissionHelper(permissionHelper)
        }
    }
    
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
            AlarmResult.ALREADY_SCHEDULED, AlarmResult.ALARM_NOT_FOUND -> {
                // These states shouldn't occur for test alarms, but handle gracefully
                errorMessage = "Unexpected alarm state"
                showErrorDialog = true
            }
        }
    }
    
    suspend fun requestAlarmPermissionAndTest() {
        if (alarmManager == null) return
        
        val alarmPermissionGranted = alarmManager.requestAlarmPermission()
        
        if (alarmPermissionGranted) {
            // Both permissions granted, now show the test alarm
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
    }
    
    suspend fun requestPermissionAndTest() {
        if (alarmManager == null) return
        
        isPreviewLoading = true
        showPermissionDialog = false
        
        // First, request notification permission if needed
        if (permissionHelper != null && !permissionHelper.hasNotificationPermission()) {
            permissionHelper.requestNotificationPermission { notificationGranted ->
                if (notificationGranted) {
                    // Notification permission granted, now request alarm permission
                    coroutineScope.launch {
                        requestAlarmPermissionAndTest()
                    }
                } else {
                    errorMessage = "Notification permission was denied. Please enable notifications in device settings to use this feature."
                    showErrorDialog = true
                    isPreviewLoading = false
                }
            }
        } else {
            // Notification permission already granted or not needed, check alarm permission
            requestAlarmPermissionAndTest()
        }
    }
    
    suspend fun requestAlarmPermissionAndSave() {
        if (alarmManager == null) return
        
        val alarmPermissionGranted = alarmManager.requestAlarmPermission()
        
        if (alarmPermissionGranted) {
            // Both permissions granted, now save the signal
            val newSignalItem = SignalItem(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                timeSlots = timeSlots,
                description = description.trim(),
                sound = sound,
                vibration = vibration,
                color = color
            )
            
            isLoading = true
            onSignalSaved(newSignalItem) { result ->
                isLoading = false
                result.onSuccess {
                    showSuccessDialog = true
                }.onFailure { exception ->
                    errorMessage = "Failed to save: ${exception.message}"
                    showErrorDialog = true
                }
            }
        } else {
            errorMessage = "Alarm permission was denied. Please enable exact alarms in device settings to save this signal."
            showErrorDialog = true
        }
    }
    
    suspend fun requestPermissionAndSave() {
        if (alarmManager == null) return
        
        showPermissionDialog = false
        
        // First, request notification permission if needed
        if (permissionHelper != null && !permissionHelper.hasNotificationPermission()) {
            permissionHelper.requestNotificationPermission { notificationGranted ->
                if (notificationGranted) {
                    // Notification permission granted, now request alarm permission
                    coroutineScope.launch {
                        requestAlarmPermissionAndSave()
                    }
                } else {
                    errorMessage = "Notification permission was denied. Please enable notifications in device settings to save this signal."
                    showErrorDialog = true
                }
            }
        } else {
            // Notification permission already granted or not needed, check alarm permission
            requestAlarmPermissionAndSave()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Signal") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignalRegistrationForm(
                name = name,
                onNameChange = { name = it },
                timeSlots = timeSlots,
                onTimeSlotsChange = { timeSlots = it },
                description = description,
                onDescriptionChange = { description = it },
                sound = sound,
                onSoundChange = { sound = it },
                vibration = vibration,
                onVibrationChange = { vibration = it },
                color = color,
                onColorChange = { color = it }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Test Alarm Button (Android only)
            if (showPreviewButton) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            testAlarm()
                        }
                    },
                    enabled = !isPreviewLoading && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isPreviewLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Test Alarm")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Name is required"
                        showErrorDialog = true
                        return@Button
                    }
                    
                    if (timeSlots.isEmpty()) {
                        errorMessage = "At least one time slot is required"
                        showErrorDialog = true
                        return@Button
                    }
                    
                    // Check permissions before saving
                    if (permissionHelper != null && !permissionHelper.hasAllPermissions()) {
                        coroutineScope.launch {
                            requestPermissionAndSave()
                        }
                        return@Button
                    }
                    
                    val newSignalItem = SignalItem(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        timeSlots = timeSlots,
                        description = description.trim(),
                        sound = sound,
                        vibration = vibration,
                        color = color
                    )
                    
                    isLoading = true
                    onSignalSaved(newSignalItem) { result ->
                        isLoading = false
                        result.onSuccess {
                            showSuccessDialog = true
                        }.onFailure { exception ->
                            errorMessage = "Failed to save: ${exception.message}"
                            showErrorDialog = true
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Signal")
                }
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
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onBackPressed()
            },
            title = { Text("Success") },
            text = { Text("Signal has been saved successfully!") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    onBackPressed()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

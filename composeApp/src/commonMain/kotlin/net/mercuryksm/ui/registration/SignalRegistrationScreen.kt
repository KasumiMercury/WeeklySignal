package net.mercuryksm.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.notification.*
import net.mercuryksm.getPlatform
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRegistrationScreen(
    onSignalSaved: (SignalItem, (Result<Unit>) -> Unit) -> Unit,
    onBackPressed: () -> Unit,
    notificationManager: SignalNotificationManager? = null
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
    
    val isAndroid = getPlatform().name.contains("Android")
    val showPreviewButton = isAndroid && notificationManager?.isNotificationSupported() == true
    val coroutineScope = rememberCoroutineScope()
    
    suspend fun previewNotification() {
        if (notificationManager == null) return
        
        if (name.isBlank()) {
            errorMessage = "Name is required for preview"
            showErrorDialog = true
            return
        }
        
        if (timeSlots.isEmpty()) {
            errorMessage = "At least one time slot is required for preview"
            showErrorDialog = true
            return
        }
        
        // Check permission first
        if (!notificationManager.hasNotificationPermission()) {
            showPermissionDialog = true
            return
        }
        
        val settings = createPreviewNotificationSettings(
            name = name.trim(),
            description = description.trim(),
            sound = sound,
            vibration = vibration,
            timeSlots = timeSlots
        )
        
        isPreviewLoading = true
        val result = notificationManager.showPreviewNotification(settings)
        isPreviewLoading = false
        
        when (result) {
            NotificationResult.PERMISSION_DENIED -> {
                showPermissionDialog = true
            }
            NotificationResult.ERROR -> {
                errorMessage = "Failed to show notification preview"
                showErrorDialog = true
            }
            NotificationResult.NOT_SUPPORTED -> {
                errorMessage = "Notifications not supported on this platform"
                showErrorDialog = true
            }
            NotificationResult.SUCCESS -> {
                // Success - no need to show anything
            }
        }
    }
    
    suspend fun requestPermissionAndPreview() {
        if (notificationManager == null) return
        
        isPreviewLoading = true
        val permissionGranted = notificationManager.requestNotificationPermission()
        
        if (permissionGranted) {
            // Permission granted, now show the notification
            val settings = createPreviewNotificationSettings(
                name = name.trim(),
                description = description.trim(),
                sound = sound,
                vibration = vibration,
                timeSlots = timeSlots
            )
            
            val result = notificationManager.showPreviewNotification(settings)
            if (result == NotificationResult.ERROR) {
                errorMessage = "Failed to show notification preview"
                showErrorDialog = true
            }
        } else {
            errorMessage = "Notification permission was denied. Please enable notifications in device settings to use this feature."
            showErrorDialog = true
        }
        
        isPreviewLoading = false
        showPermissionDialog = false
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
            
            // Preview Notification Button (Android only)
            if (showPreviewButton) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            previewNotification()
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
                        Text("Preview Notification")
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
                requestPermissionAndPreview()
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
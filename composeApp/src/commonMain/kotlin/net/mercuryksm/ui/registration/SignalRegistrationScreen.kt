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
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRegistrationScreen(
    onSignalSaved: (SignalItem) -> Unit,
    onBackPressed: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableStateOf(DayOfWeekJp.MONDAY) }
    var description by remember { mutableStateOf("") }
    var sound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
                hour = hour,
                onHourChange = { hour = it },
                minute = minute,
                onMinuteChange = { minute = it },
                selectedDay = selectedDay,
                onDayChange = { selectedDay = it },
                description = description,
                onDescriptionChange = { description = it },
                sound = sound,
                onSoundChange = { sound = it },
                vibration = vibration,
                onVibrationChange = { vibration = it }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Name is required"
                        showErrorDialog = true
                        return@Button
                    }
                    
                    val newSignalItem = SignalItem(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        hour = hour,
                        minute = minute,
                        dayOfWeek = selectedDay,
                        description = description.trim(),
                        sound = sound,
                        vibration = vibration
                    )
                    
                    onSignalSaved(newSignalItem)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Add Signal")
            }
        }
    }
    
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
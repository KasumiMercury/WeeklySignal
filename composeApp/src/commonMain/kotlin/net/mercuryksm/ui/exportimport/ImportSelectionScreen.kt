package net.mercuryksm.ui.exportimport

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.mercuryksm.data.*
import net.mercuryksm.ui.usecase.ImportSelectionUseCase
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSelectionScreen(
    importedItems: List<SignalItem>,
    existingItems: List<SignalItem>,
    onBackPressed: () -> Unit,
    onImportSelected: (ImportConflictResolutionResult) -> Unit
) {
    var selectionState by remember { mutableStateOf(ExportSelectionState()) }
    var showConflictDialog by remember { mutableStateOf(false) }
    
    val importSelectionUseCase = remember { ImportSelectionUseCase() }
    
    // Initialize selection state for imported items
    LaunchedEffect(importedItems) {
        selectionState = importSelectionUseCase.createInitialSelectionState(importedItems)
    }
    
    val conflicts = remember(existingItems, importedItems) {
        importSelectionUseCase.findConflicts(existingItems, importedItems)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Items to Import") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Bottom Action Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Import Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected: ${selectionState.selectedItemCount} items",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (conflicts.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Conflicts",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${conflicts.size} conflicts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    // Import Button
                    Button(
                        onClick = {
                            if (conflicts.isNotEmpty()) {
                                showConflictDialog = true
                            } else {
                                // No conflicts, all selected items are new items to insert
                                val result = importSelectionUseCase.handleNoConflictImport(selectionState)
                                onImportSelected(result)
                            }
                        },
                        enabled = selectionState.hasSelection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (conflicts.isNotEmpty()) {
                                "Import with Conflicts"
                            } else {
                                "Import Selected Items"
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Import Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Import Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Found ${importedItems.size} signal items to import",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (conflicts.isNotEmpty()) {
                        Text(
                            text = "⚠️ ${conflicts.size} items have conflicts with existing data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = "• Deselect items you don't want to import",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "• Expand items to select specific time slots",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selection List
            SelectableSignalItemList(
                selectionState = selectionState,
                onSignalItemSelectionChanged = { signalItemId ->
                    selectionState = SelectionStateManager.toggleSignalItemSelection(
                        selectionState,
                        signalItemId
                    )
                },
                onTimeSlotSelectionChanged = { signalItemId, timeSlotId ->
                    selectionState = SelectionStateManager.toggleTimeSlotSelection(
                        selectionState,
                        signalItemId,
                        timeSlotId
                    )
                },
                onSignalItemExpansionChanged = { signalItemId ->
                    selectionState = SelectionStateManager.toggleSignalItemExpansion(
                        selectionState,
                        signalItemId
                    )
                },
                onSelectAllChanged = { selected ->
                    selectionState = SelectionStateManager.selectAll(
                        selectionState,
                        selected
                    )
                }
            )
        }
    }
    
    // Conflict Resolution Dialog
    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Import Conflicts") },
            text = {
                Column {
                    Text("Found ${conflicts.size} conflicting signal items:")
                    Spacer(modifier = Modifier.height(8.dp))
                    conflicts.forEach { item ->
                        Text("• ${item.name}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("How would you like to resolve these conflicts?")
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showConflictDialog = false
                                val selectedItems = selectionState.selectedSignalItemsWithTimeSlots
                                val result = importSelectionUseCase.handleConflictResolution(
                                    existingItems,
                                    selectedItems,
                                    ConflictResolution.REPLACE_EXISTING
                                )
                                onImportSelected(result)
                            }
                        ) {
                            Text("Replace Existing")
                        }
                        
                        TextButton(
                            onClick = {
                                showConflictDialog = false
                                val selectedItems = selectionState.selectedSignalItemsWithTimeSlots
                                val result = importSelectionUseCase.handleConflictResolution(
                                    existingItems,
                                    selectedItems,
                                    ConflictResolution.KEEP_EXISTING
                                )
                                onImportSelected(result)
                            }
                        ) {
                            Text("Keep Existing")
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showConflictDialog = false
                                val selectedItems = selectionState.selectedSignalItemsWithTimeSlots
                                val result = importSelectionUseCase.handleConflictResolution(
                                    existingItems,
                                    selectedItems,
                                    ConflictResolution.MERGE_TIME_SLOTS
                                )
                                onImportSelected(result)
                            }
                        ) {
                            Text("Merge Time Slots")
                        }
                        
                        TextButton(onClick = { showConflictDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun ImportSelectionScreenPreview() {
    MaterialTheme {
        val imported = remember { importSelectionPreviewImported() }
        val existing = remember { importSelectionPreviewExisting() }
        ImportSelectionScreen(
            importedItems = imported,
            existingItems = existing,
            onBackPressed = {},
            onImportSelected = {}
        )
    }
}

private fun importSelectionPreviewImported(): List<SignalItem> {
    return listOf(
        SignalItem(
            id = "import-preview-1",
            name = "Morning Routine",
            description = "Stretch and hydrate",
            color = 0xFF81C784,
            timeSlots = listOf(
                TimeSlot(
                    id = "import-preview-1-mon",
                    hour = 7,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            )
        ),
        SignalItem(
            id = "import-preview-2",
            name = "Lunch Walk",
            description = "Walk outside",
            color = 0xFF4FC3F7,
            timeSlots = listOf(
                TimeSlot(
                    id = "import-preview-2-tue",
                    hour = 12,
                    minute = 0,
                    dayOfWeek = DayOfWeekJp.TUESDAY
                ),
                TimeSlot(
                    id = "import-preview-2-thu",
                    hour = 12,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.THURSDAY
                )
            )
        )
    )
}

private fun importSelectionPreviewExisting(): List<SignalItem> {
    return listOf(
        SignalItem(
            id = "import-preview-1",
            name = "Morning Routine",
            description = "Existing version",
            color = 0xFF81C784,
            timeSlots = listOf(
                TimeSlot(
                    id = "import-preview-existing-mon",
                    hour = 7,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            )
        ),
        SignalItem(
            id = "import-preview-3",
            name = "Evening Review",
            description = "Plan tomorrow",
            color = 0xFFFFB74D,
            timeSlots = listOf(
                TimeSlot(
                    id = "import-preview-3-fri",
                    hour = 20,
                    minute = 0,
                    dayOfWeek = DayOfWeekJp.FRIDAY
                )
            )
        )
    )
}

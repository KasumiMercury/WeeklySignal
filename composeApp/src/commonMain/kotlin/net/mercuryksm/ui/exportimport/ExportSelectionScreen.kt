package net.mercuryksm.ui.exportimport

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.mercuryksm.data.*
import net.mercuryksm.ui.weekly.WeeklySignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSelectionScreen(
    viewModel: WeeklySignalViewModel,
    onBackPressed: () -> Unit,
    onExportSelected: (ExportSelectionState) -> Unit
) {
    val signalItems by viewModel.signalItems.collectAsStateWithLifecycle()
    
    var selectionState by remember { mutableStateOf(ExportSelectionState()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Initialize selection state when signalItems change
    LaunchedEffect(signalItems) {
        selectionState = SelectionStateManager.createInitialState(signalItems)
    }
    
    val exportImportService = remember { ExportImportService() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Items to Export") },
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
                    // Export Summary
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
                        
                        Text(
                            text = "${selectionState.selectedTimeSlotCount} time slots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Export Button
                    Button(
                        onClick = {
                            if (selectionState.hasSelection) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = selectionState.hasSelection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Selected Items")
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
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select items to export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "• Tap the checkbox to select entire SignalItems",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "• Tap the SignalItem to expand and select individual time slots",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "• Use 'Select All' to quickly select everything",
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
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        val exportSummary = exportImportService.getExportSummary(selectionState)
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Export") },
            text = {
                Column {
                    Text("You are about to export:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• ${exportSummary.selectedSignalItemCount} signal items")
                    Text("• ${exportSummary.selectedTimeSlotCount} time slots")
                    
                    if (exportSummary.isPartialExport) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This is a partial export (${exportSummary.selectionPercentage.toInt()}% of total items)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onExportSelected(selectionState)
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
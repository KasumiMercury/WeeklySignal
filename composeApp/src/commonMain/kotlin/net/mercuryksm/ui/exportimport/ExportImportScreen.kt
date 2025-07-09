package net.mercuryksm.ui.exportimport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.mercuryksm.data.*
import net.mercuryksm.ui.weekly.WeeklySignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    viewModel: WeeklySignalViewModel,
    onBackPressed: () -> Unit
) {
    val signalItems by viewModel.signalItems.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboard.current
    
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    
    var showImportConflictDialog by remember { mutableStateOf(false) }
    var conflictingItems by remember { mutableStateOf<List<SignalItem>>(emptyList()) }
    var importedItems by remember { mutableStateOf<List<SignalItem>>(emptyList()) }
    
    // Selection state for export
    var exportSelectionState by remember { mutableStateOf(ExportSelectionState()) }
    var isSelectiveExportMode by remember { mutableStateOf(false) }
    
    val exportImportService = remember { ExportImportService() }
    val conflictResolver = remember { ImportConflictResolver() }
    val coroutineScope = rememberCoroutineScope()
    
    // Create file operations service based on platform
    val fileOperationsService = rememberFileOperationsService()
    
    // Initialize selection state when signalItems change
    LaunchedEffect(signalItems) {
        exportSelectionState = SelectionStateManager.createInitialState(signalItems)
    }
    
    suspend fun handleExport() {
        isExporting = true
        
        try {
            val exportResult = if (isSelectiveExportMode) {
                exportImportService.exportSelectedSignalItems(exportSelectionState)
            } else {
                exportImportService.exportSignalItems(signalItems)
            }
            
            when (exportResult) {
                is ExportResult.Success -> {
                    val fileName = if (isSelectiveExportMode) {
                        exportImportService.generateSelectiveFileName(exportSelectionState)
                    } else {
                        exportImportService.generateFileName()
                    }
                    val fileResult = fileOperationsService.exportToFile(exportResult.exportData, fileName)
                    
                    when (fileResult) {
                        is FileOperationResult.Success -> {
                            val summaryMessage = if (isSelectiveExportMode) {
                                val exportSummary = exportImportService.getExportSummary(exportSelectionState)
                                "Successfully exported ${exportSummary.selectedSignalItemCount} signal items with ${exportSummary.selectedTimeSlotCount} time slots"
                            } else {
                                "Successfully exported ${signalItems.size} signal items"
                            }
                            dialogMessage = "$summaryMessage\n\n${fileResult.message}"
                            showSuccessDialog = true
                        }
                        is FileOperationResult.Error -> {
                            dialogMessage = fileResult.message
                            showErrorDialog = true
                        }
                    }
                }
                is ExportResult.Error -> {
                    dialogMessage = exportResult.message
                    showErrorDialog = true
                }
            }
        } finally {
            isExporting = false
        }
    }
    
    suspend fun handleImport() {
        
        isImporting = true
        
        try {
            val fileResult = fileOperationsService.importFromFile()
            
            when (fileResult) {
                is FileReadResult.Success -> {
                    val importResult = exportImportService.importSignalItems(fileResult.content)
                    
                    when (importResult) {
                        is ImportResult.Success -> {
                            val conflicts = conflictResolver.findConflicts(signalItems, importResult.signalItems)
                            
                            if (conflicts.isNotEmpty()) {
                                conflictingItems = conflicts
                                importedItems = importResult.signalItems
                                showImportConflictDialog = true
                            } else {
                                // No conflicts, import directly
                                importResult.signalItems.forEach { signalItem ->
                                    viewModel.addSignalItem(signalItem)
                                }
                                dialogMessage = "Successfully imported ${importResult.signalItems.size} signal items"
                                showSuccessDialog = true
                            }
                        }
                        is ImportResult.Error -> {
                            dialogMessage = importResult.message
                            showErrorDialog = true
                        }
                    }
                }
                is FileReadResult.Error -> {
                    dialogMessage = fileResult.message
                    showErrorDialog = true
                }
            }
        } finally {
            isImporting = false
        }
    }
    
    
    fun handleConflictResolution(resolution: ConflictResolution) {
        val resolvedItems = conflictResolver.resolveConflicts(
            signalItems, 
            importedItems, 
            resolution
        )
        
        // Clear existing items and add resolved items
        viewModel.clearAllSignalItems()
        resolvedItems.forEach { signalItem ->
            viewModel.addSignalItem(signalItem)
        }
        
        showImportConflictDialog = false
        dialogMessage = "Successfully imported with conflict resolution"
        showSuccessDialog = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export & Import") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Export",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        // Export Mode Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isSelectiveExportMode,
                                onCheckedChange = { isSelectiveExportMode = it },
                                enabled = !isExporting && !isImporting
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Selective",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (isSelectiveExportMode) {
                        // Selective Export UI
                        SelectableSignalItemList(
                            selectionState = exportSelectionState,
                            onSignalItemSelectionChanged = { signalItemId ->
                                exportSelectionState = SelectionStateManager.toggleSignalItemSelection(
                                    exportSelectionState,
                                    signalItemId
                                )
                            },
                            onTimeSlotSelectionChanged = { signalItemId, timeSlotId ->
                                exportSelectionState = SelectionStateManager.toggleTimeSlotSelection(
                                    exportSelectionState,
                                    signalItemId,
                                    timeSlotId
                                )
                            },
                            onSignalItemExpansionChanged = { signalItemId ->
                                exportSelectionState = SelectionStateManager.toggleSignalItemExpansion(
                                    exportSelectionState,
                                    signalItemId
                                )
                            },
                            onSelectAllChanged = { selected ->
                                exportSelectionState = SelectionStateManager.selectAll(
                                    exportSelectionState,
                                    selected
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Selection Summary
                        SelectionSummary(
                            selectionState = exportSelectionState
                        )
                    } else {
                        // Full Export UI
                        Text(
                            text = "Export all your signal items to a file for backup or sharing with other devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "Current signal items: ${signalItems.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Export Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                handleExport()
                            }
                        },
                        enabled = !isExporting && !isImporting && signalItems.isNotEmpty() && 
                                (!isSelectiveExportMode || exportSelectionState.hasSelection),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (isSelectiveExportMode) {
                                "Export Selected Items"
                            } else {
                                "Export All Items"
                            }
                        )
                    }
                }
            }
            
            // Import Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Import signal items from a previously exported file. Any conflicting items will be handled based on your selection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                handleImport()
                            }
                        },
                        enabled = !isExporting && !isImporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Import from File")
                    }
                }
            }
            
            // Info Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "File Format",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "• Files use the .weeklysignal extension",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "• Each export includes metadata and timestamps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "• UUID-based identification prevents data corruption",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "• Files are human-readable JSON format",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Import Conflict Resolution Dialog
    if (showImportConflictDialog) {
        AlertDialog(
            onDismissRequest = { showImportConflictDialog = false },
            title = { Text("Import Conflicts") },
            text = {
                Column {
                    Text("Found ${conflictingItems.size} conflicting signal items:")
                    Spacer(modifier = Modifier.height(8.dp))
                    conflictingItems.forEach { item ->
                        Text("• ${item.name}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("How would you like to resolve these conflicts?")
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { handleConflictResolution(ConflictResolution.REPLACE_EXISTING) }
                    ) {
                        Text("Replace")
                    }
                    TextButton(
                        onClick = { handleConflictResolution(ConflictResolution.KEEP_EXISTING) }
                    ) {
                        Text("Keep")
                    }
                    TextButton(
                        onClick = { handleConflictResolution(ConflictResolution.MERGE_TIME_SLOTS) }
                    ) {
                        Text("Merge")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConflictDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

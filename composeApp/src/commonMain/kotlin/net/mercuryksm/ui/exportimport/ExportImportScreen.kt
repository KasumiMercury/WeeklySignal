package net.mercuryksm.ui.exportimport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.mercuryksm.data.*
import net.mercuryksm.ui.weekly.WeeklySignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    viewModel: WeeklySignalViewModel,
    onBackPressed: () -> Unit,
    onNavigateToExportSelection: () -> Unit,
    onNavigateToImportSelection: () -> Unit
) {
    val signalItems by viewModel.signalItems.collectAsStateWithLifecycle()
    val exportSelectionState by viewModel.exportSelectionState.collectAsStateWithLifecycle()
    val selectedImportResult by viewModel.selectedImportResult.collectAsStateWithLifecycle()
    
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    
    val exportService = remember { ExportService() }
    val importService = remember { ImportService() }
    val coroutineScope = rememberCoroutineScope()
    
    // Create file operations service based on platform
    val fileOperationsService = rememberFileOperationsService()
    
    suspend fun handleExportWithSelection(selectionState: ExportSelectionState) {
        isExporting = true
        
        try {
            val exportResult = exportService.exportSelectedSignalItems(selectionState)
            
            when (exportResult) {
                is ExportResult.Success -> {
                    val fileName = exportService.generateSelectiveFileName(selectionState)
                    val fileResult = fileOperationsService.exportToFile(exportResult.exportData, fileName)
                    
                    when (fileResult) {
                        is FileOperationResult.Success -> {
                            val exportSummary = exportService.getExportSummary(selectionState)
                            val summaryMessage = "Successfully exported ${exportSummary.selectedSignalItemCount} signal items with ${exportSummary.selectedTimeSlotCount} time slots"
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
    
    suspend fun handleImportWithSelection(result: ImportConflictResolutionResult) {
        isImporting = true
        
        try {
            // Use transaction-based import method for better reliability
            viewModel.importSignalItemsWithConflictResolution(
                result.itemsToInsert,
                result.itemsToUpdate
            ) { importResult ->
                coroutineScope.launch {
                    importResult.onSuccess {
                        val totalItems = result.itemsToInsert.size + result.itemsToUpdate.size
                        dialogMessage = "Successfully imported $totalItems signal items"
                        showSuccessDialog = true
                    }.onFailure { error ->
                        dialogMessage = "Failed to import signal items: ${error.message}"
                        showErrorDialog = true
                    }
                }
            }
        } catch (e: Exception) {
            dialogMessage = "Failed to import signal items: ${e.message}"
            showErrorDialog = true
        } finally {
            isImporting = false
            viewModel.clearImportedItems()
        }
    }

    // Handle export selection result
    LaunchedEffect(exportSelectionState) {
        exportSelectionState?.let { state ->
            coroutineScope.launch {
                handleExportWithSelection(state)
                viewModel.clearExportSelectionState()
            }
        }
    }
    
    // Handle import selection result
    LaunchedEffect(selectedImportResult) {
        selectedImportResult?.let { result ->
            coroutineScope.launch {
                handleImportWithSelection(result)
                viewModel.clearSelectedImportResult()
            }
        }
    }
    
    suspend fun handleImport() {
        isImporting = true
        
        try {
            val fileResult = fileOperationsService.importFromFile()
            
            when (fileResult) {
                is FileReadResult.Success -> {
                    // Use the new conflict check method to provide better user experience
                    val conflictCheckResult = importService.checkForConflicts(
                        fileResult.content,
                        signalItems
                    )
                    
                    when (conflictCheckResult) {
                        is ConflictCheckResult.Success -> {
                            // Store imported items in ViewModel and navigate to selection screen
                            viewModel.setImportedItems(conflictCheckResult.importedItems)
                            onNavigateToImportSelection()
                        }
                        is ConflictCheckResult.Error -> {
                            dialogMessage = conflictCheckResult.message
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
                    Text(
                        text = "Export",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Export your signal items to a file for backup or sharing with other devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Current signal items: ${signalItems.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Export Button
                    Button(
                        onClick = {
                            onNavigateToExportSelection()
                        },
                        enabled = !isExporting && !isImporting && signalItems.isNotEmpty(),
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
                        Text("Select Items to Export")
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
    
}

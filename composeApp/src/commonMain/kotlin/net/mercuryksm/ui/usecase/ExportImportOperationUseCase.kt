package net.mercuryksm.ui.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mercuryksm.data.*

/**
 * Use case for handling export/import file operations.
 * Extracts business logic from ExportImportScreen UI component.
 */
class ExportImportOperationUseCase(
    private val coroutineScope: CoroutineScope
) {
    
    private val exportService = ExportService()
    private val importService = ImportService()
    
    /**
     * Handle export operation with selection state
     */
    suspend fun handleExportWithSelection(
        selectionState: ExportSelectionState,
        fileOperationsService: FileOperationsService
    ): ExportOperationResult {
        return try {
            val exportResult = exportService.exportSelectedSignalItems(selectionState)
            
            when (exportResult) {
                is ExportResult.Success -> {
                    val fileName = exportService.generateSelectiveFileName(selectionState)
                    val fileResult = fileOperationsService.exportToFile(exportResult.exportData, fileName)
                    
                    when (fileResult) {
                        is FileOperationResult.Success -> {
                            val exportSummary = exportService.getExportSummary(selectionState)
                            val summaryMessage = "Successfully exported ${exportSummary.selectedSignalItemCount} signal items with ${exportSummary.selectedTimeSlotCount} time slots"
                            ExportOperationResult.Success("$summaryMessage\n\n${fileResult.message}")
                        }
                        is FileOperationResult.Error -> {
                            ExportOperationResult.Error(fileResult.message)
                        }
                    }
                }
                is ExportResult.Error -> {
                    ExportOperationResult.Error(exportResult.message)
                }
            }
        } catch (e: Exception) {
            ExportOperationResult.Error("Failed to export: ${e.message}")
        }
    }
    
    /**
     * Handle import file selection and conflict detection
     */
    suspend fun handleImportFromFile(
        fileOperationsService: FileOperationsService,
        existingSignalItems: List<SignalItem>
    ): ImportOperationResult {
        return try {
            val fileResult = fileOperationsService.importFromFile()
            
            when (fileResult) {
                is FileReadResult.Success -> {
                    val conflictCheckResult = importService.checkForConflicts(
                        fileResult.content,
                        existingSignalItems
                    )
                    
                    when (conflictCheckResult) {
                        is ConflictCheckResult.Success -> {
                            ImportOperationResult.Success(conflictCheckResult.importedItems)
                        }
                        is ConflictCheckResult.Error -> {
                            ImportOperationResult.Error(conflictCheckResult.message)
                        }
                    }
                }
                is FileReadResult.Error -> {
                    ImportOperationResult.Error(fileResult.message)
                }
            }
        } catch (e: Exception) {
            ImportOperationResult.Error("Failed to import: ${e.message}")
        }
    }
    
    /**
     * Validate if file has correct extension
     */
    fun isValidExportFile(fileName: String): Boolean {
        return exportService.isValidExportFile(fileName)
    }
}

/**
 * Result type for export operations
 */
sealed class ExportOperationResult {
    data class Success(val message: String) : ExportOperationResult()
    data class Error(val message: String) : ExportOperationResult()
}

/**
 * Result type for import operations
 */
sealed class ImportOperationResult {
    data class Success(val importedItems: List<SignalItem>) : ImportOperationResult()
    data class Error(val message: String) : ImportOperationResult()
}
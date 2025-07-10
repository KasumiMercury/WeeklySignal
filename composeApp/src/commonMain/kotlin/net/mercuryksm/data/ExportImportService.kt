package net.mercuryksm.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mercuryksm.data.ExportFormatConverter.toExportData
import net.mercuryksm.data.ExportFormatConverter.toSignalItems

sealed class ExportResult {
    data class Success(val exportData: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val signalItems: List<SignalItem>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

sealed class ConflictCheckResult {
    data class Success(
        val importedItems: List<SignalItem>,
        val conflictingItems: List<SignalItem>,
        val hasConflicts: Boolean
    ) : ConflictCheckResult()
    data class Error(val message: String) : ConflictCheckResult()
}

/**
 * Facade service that coordinates export and import operations.
 * Delegates specific responsibilities to specialized services.
 * 
 * @deprecated This class will be removed in future versions.
 * Use ExportService, ImportService, and ConflictResolutionService directly.
 */
@Deprecated("Use specialized services instead", ReplaceWith("ExportService, ImportService, ConflictResolutionService"))
class ExportImportService {
    
    private val exportService = ExportService()
    private val importService = ImportService()
    
    // Delegate to ExportService
    fun exportSignalItems(
        signalItems: List<SignalItem>,
        version: String = "1.0",
        appVersion: String = "1.0.0"
    ): ExportResult = exportService.exportSignalItems(signalItems, version, appVersion)
    
    fun exportSelectedSignalItems(
        selectionState: ExportSelectionState,
        version: String = "1.0",
        appVersion: String = "1.0.0"
    ): ExportResult = exportService.exportSelectedSignalItems(selectionState, version, appVersion)
    
    fun generateSelectiveFileName(selectionState: ExportSelectionState): String = 
        exportService.generateSelectiveFileName(selectionState)
    
    fun getExportSummary(selectionState: ExportSelectionState): ExportSummary = 
        exportService.getExportSummary(selectionState)
    
    fun generateFileName(): String = exportService.generateFileName()
    
    fun isValidExportFile(fileName: String): Boolean = exportService.isValidExportFile(fileName)
    
    // Delegate to ImportService
    fun importSignalItems(jsonString: String): ImportResult = 
        importService.importSignalItems(jsonString)
    
    fun importSignalItemsWithConflictResolution(
        jsonString: String,
        existingSignalItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult? = 
        importService.importSignalItemsWithConflictResolution(jsonString, existingSignalItems, conflictResolution)
    
    fun checkForConflicts(
        jsonString: String,
        existingSignalItems: List<SignalItem>
    ): ConflictCheckResult = 
        importService.checkForConflicts(jsonString, existingSignalItems)
}

data class ImportConflictResolution(
    val conflictingSignalItems: List<SignalItem>,
    val resolution: ConflictResolution
)

enum class ConflictResolution {
    REPLACE_EXISTING,
    KEEP_EXISTING,
    MERGE_TIME_SLOTS
}

data class ImportConflictResolutionResult(
    val itemsToInsert: List<SignalItem>,
    val itemsToUpdate: List<SignalItem>
)

/**
 * @deprecated Use ConflictResolutionService instead
 */
@Deprecated("Use ConflictResolutionService instead", ReplaceWith("ConflictResolutionService"))
class ImportConflictResolver {
    private val conflictResolutionService = ConflictResolutionService()
    
    fun findConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>
    ): List<SignalItem> {
        return conflictResolutionService.findConflicts(existingSignalItems, importedSignalItems)
    }
    
    fun resolveConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult {
        return conflictResolutionService.resolveConflicts(existingSignalItems, importedSignalItems, conflictResolution)
    }
}

/**
 * Summary information for export operations
 */
data class ExportSummary(
    val selectedSignalItemCount: Int,
    val totalSignalItemCount: Int,
    val selectedTimeSlotCount: Int,
    val totalTimeSlotCount: Int
) {
    val isFullExport: Boolean
        get() = selectedSignalItemCount == totalSignalItemCount
    
    val isPartialExport: Boolean
        get() = !isFullExport
    
    val selectionPercentage: Float
        get() = if (totalSignalItemCount > 0) {
            (selectedSignalItemCount.toFloat() / totalSignalItemCount.toFloat()) * 100f
        } else {
            0f
        }
}
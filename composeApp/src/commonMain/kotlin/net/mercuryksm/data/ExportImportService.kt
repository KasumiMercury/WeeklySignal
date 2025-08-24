package net.mercuryksm.data

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
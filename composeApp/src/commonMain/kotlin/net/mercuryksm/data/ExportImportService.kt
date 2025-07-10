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

class ExportImportService {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun exportSignalItems(
        signalItems: List<SignalItem>,
        version: String = "1.0",
        appVersion: String = "1.0.0"
    ): ExportResult {
        return try {
            val exportData = signalItems.toExportData(version, appVersion)
            val jsonString = json.encodeToString(exportData)
            ExportResult.Success(jsonString)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export signal items: ${e.message}")
        }
    }
    
    /**
     * Export selected SignalItems and TimeSlots based on selection state
     */
    fun exportSelectedSignalItems(
        selectionState: ExportSelectionState,
        version: String = "1.0",
        appVersion: String = "1.0.0"
    ): ExportResult {
        return try {
            if (!selectionState.hasSelection) {
                return ExportResult.Error("No items selected for export")
            }
            
            val selectedSignalItems = selectionState.selectedSignalItemsWithTimeSlots
            
            if (selectedSignalItems.isEmpty()) {
                return ExportResult.Error("No valid items selected for export")
            }
            
            val exportData = selectedSignalItems.toExportData(version, appVersion)
            val jsonString = json.encodeToString(exportData)
            ExportResult.Success(jsonString)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export selected signal items: ${e.message}")
        }
    }
    
    /**
     * Generate a filename for selective export with additional context
     */
    fun generateSelectiveFileName(selectionState: ExportSelectionState): String {
        val timestamp = System.currentTimeMillis()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(timestamp)
        val selectedCount = selectionState.selectedItemCount
        val totalCount = selectionState.signalItemSelections.size
        
        return if (selectedCount == totalCount) {
            "weekly_signal_export_$date.weeklysignal"
        } else {
            "weekly_signal_export_${selectedCount}of${totalCount}_$date.weeklysignal"
        }
    }
    
    /**
     * Get export summary information
     */
    fun getExportSummary(selectionState: ExportSelectionState): ExportSummary {
        val selectedSignalItems = selectionState.selectedSignalItemsWithTimeSlots
        val totalTimeSlots = selectedSignalItems.sumOf { it.timeSlots.size }
        
        return ExportSummary(
            selectedSignalItemCount = selectedSignalItems.size,
            totalSignalItemCount = selectionState.signalItemSelections.size,
            selectedTimeSlotCount = totalTimeSlots,
            totalTimeSlotCount = selectionState.signalItemSelections.sumOf { it.signalItem.timeSlots.size }
        )
    }
    
    fun importSignalItems(jsonString: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<WeeklySignalExportData>(jsonString)
            
            // Validate the imported data
            val validationResult = validateImportData(exportData)
            if (validationResult != null) {
                return ImportResult.Error(validationResult)
            }
            
            val signalItems = exportData.toSignalItems()
            ImportResult.Success(signalItems)
        } catch (e: Exception) {
            ImportResult.Error("Failed to import signal items: ${e.message}")
        }
    }
    
    /**
     * Import SignalItems with conflict resolution
     */
    fun importSignalItemsWithConflictResolution(
        jsonString: String,
        existingSignalItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult? {
        return try {
            val exportData = json.decodeFromString<WeeklySignalExportData>(jsonString)
            
            // Validate the imported data
            val validationResult = validateImportData(exportData)
            if (validationResult != null) {
                return null
            }
            
            val importedSignalItems = exportData.toSignalItems()
            val conflictResolver = ImportConflictResolver()
            
            // Resolve conflicts
            conflictResolver.resolveConflicts(
                existingSignalItems,
                importedSignalItems,
                conflictResolution
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check for conflicts between existing and imported SignalItems
     */
    fun checkForConflicts(
        jsonString: String,
        existingSignalItems: List<SignalItem>
    ): ConflictCheckResult {
        return try {
            val exportData = json.decodeFromString<WeeklySignalExportData>(jsonString)
            
            // Validate the imported data
            val validationResult = validateImportData(exportData)
            if (validationResult != null) {
                return ConflictCheckResult.Error(validationResult)
            }
            
            val importedSignalItems = exportData.toSignalItems()
            val conflictResolver = ImportConflictResolver()
            
            // Find conflicts
            val conflictingItems = conflictResolver.findConflicts(existingSignalItems, importedSignalItems)
            
            ConflictCheckResult.Success(
                importedItems = importedSignalItems,
                conflictingItems = conflictingItems,
                hasConflicts = conflictingItems.isNotEmpty()
            )
        } catch (e: Exception) {
            ConflictCheckResult.Error("Failed to check for conflicts: ${e.message}")
        }
    }
    
    private fun validateImportData(exportData: WeeklySignalExportData): String? {
        // Validate version compatibility
        if (exportData.version.isEmpty()) {
            return "Invalid export format: missing version"
        }
        
        // Validate signal items
        exportData.signalItems.forEach { signalItem ->
            if (signalItem.id.isEmpty()) {
                return "Invalid signal item: missing ID"
            }
            
            if (signalItem.name.isEmpty()) {
                return "Invalid signal item: missing name"
            }
            
            if (signalItem.timeSlots.isEmpty()) {
                return "Invalid signal item '${signalItem.name}': must have at least one time slot"
            }
            
            // Validate time slots
            signalItem.timeSlots.forEach { timeSlot ->
                if (timeSlot.id.isEmpty()) {
                    return "Invalid time slot: missing ID"
                }
                
                if (timeSlot.hour !in 0..23) {
                    return "Invalid time slot in '${signalItem.name}': hour must be between 0 and 23"
                }
                
                if (timeSlot.minute !in 0..59) {
                    return "Invalid time slot in '${signalItem.name}': minute must be between 0 and 59"
                }
                
                if (timeSlot.dayOfWeek !in 0..6) {
                    return "Invalid time slot in '${signalItem.name}': day of week must be between 0 and 6"
                }
            }
        }
        
        // Check for duplicate IDs
        val signalItemIds = exportData.signalItems.map { it.id }
        if (signalItemIds.size != signalItemIds.distinct().size) {
            return "Duplicate signal item IDs found in export data"
        }
        
        val timeSlotIds = exportData.signalItems.flatMap { it.timeSlots.map { ts -> ts.id } }
        if (timeSlotIds.size != timeSlotIds.distinct().size) {
            return "Duplicate time slot IDs found in export data"
        }
        
        return null
    }
    
    fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(timestamp)
        return "weekly_signal_export_$date.weeklysignal"
    }
    
    fun isValidExportFile(fileName: String): Boolean {
        return fileName.endsWith(".weeklysignal")
    }
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

class ImportConflictResolver {
    
    fun findConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>
    ): List<SignalItem> {
        val existingIds = existingSignalItems.map { it.id }.toSet()
        return importedSignalItems.filter { it.id in existingIds }
    }
    
    fun resolveConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult {
        val existingMap = existingSignalItems.associateBy { it.id }
        
        val itemsToInsert = mutableListOf<SignalItem>()
        val itemsToUpdate = mutableListOf<SignalItem>()
        
        // Process imported items
        importedSignalItems.forEach { importedItem ->
            val existingItem = existingMap[importedItem.id]
            
            if (existingItem == null) {
                // New item, add to insert list
                itemsToInsert.add(importedItem)
            } else {
                // Conflict, resolve based on strategy
                when (conflictResolution) {
                    ConflictResolution.REPLACE_EXISTING -> {
                        // Replace the existing item (update with imported item)
                        itemsToUpdate.add(importedItem)
                    }
                    ConflictResolution.KEEP_EXISTING -> {
                        // Keep existing, skip this imported item
                        // Do nothing - don't add to either list
                    }
                    ConflictResolution.MERGE_TIME_SLOTS -> {
                        // Merge time slots - keep existing SignalItem settings but add new TimeSlots
                        val existingTimeSlotIds = existingItem.timeSlots.map { it.id }.toSet()
                        val newTimeSlots = importedItem.timeSlots.filter { it.id !in existingTimeSlotIds }
                        
                        if (newTimeSlots.isNotEmpty()) {
                            val mergedItem = existingItem.copy(
                                timeSlots = existingItem.timeSlots + newTimeSlots
                            )
                            itemsToUpdate.add(mergedItem)
                        }
                        // If no new TimeSlots to add, skip this item
                    }
                }
            }
        }
        
        return ImportConflictResolutionResult(itemsToInsert, itemsToUpdate)
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
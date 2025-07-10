package net.mercuryksm.data

import kotlinx.serialization.json.Json
import net.mercuryksm.data.ExportFormatConverter.toSignalItems

/**
 * Service responsible for importing SignalItems from files.
 * Handles deserialization and basic validation.
 */
class ImportService {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val validator = ImportDataValidator()
    
    /**
     * Import SignalItems from JSON string
     */
    fun importSignalItems(jsonString: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<WeeklySignalExportData>(jsonString)
            
            // Validate the imported data
            val validationResult = validator.validateImportData(exportData)
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
            val validationResult = validator.validateImportData(exportData)
            if (validationResult != null) {
                return null
            }
            
            val importedSignalItems = exportData.toSignalItems()
            val conflictResolver = ConflictResolutionService()
            
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
            val validationResult = validator.validateImportData(exportData)
            if (validationResult != null) {
                return ConflictCheckResult.Error(validationResult)
            }
            
            val importedSignalItems = exportData.toSignalItems()
            val conflictResolver = ConflictResolutionService()
            
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
}
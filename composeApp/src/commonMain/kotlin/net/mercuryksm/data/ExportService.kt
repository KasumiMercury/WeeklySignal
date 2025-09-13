package net.mercuryksm.data

import kotlinx.serialization.json.Json
import net.mercuryksm.data.ExportFormatConverter.toExportData

/**
 * Service responsible for exporting SignalItems to files.
 * Handles serialization, file naming, and export summary generation.
 */
class ExportService {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Export all SignalItems to JSON format
     */
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
    
    /**
     * Generate a standard filename for full export
     */
    fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(timestamp)
        return "weekly_signal_export_$date.weeklysignal"
    }
    
    /**
     * Check if filename has valid export file extension
     */
    fun isValidExportFile(fileName: String): Boolean {
        return fileName.endsWith(".weeklysignal")
    }
}

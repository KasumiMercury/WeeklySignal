package net.mercuryksm.ui.coordination

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.mercuryksm.data.ExportSelectionState
import net.mercuryksm.data.ImportConflictResolutionResult
import net.mercuryksm.data.SignalItem

/**
 * Coordinates import/export operations and manages related UI state.
 * Extracted from WeeklySignalViewModel to follow single responsibility principle.
 */
class ImportExportCoordinator {
    
    // Export/Import selection state
    private val _exportSelectionState = MutableStateFlow<ExportSelectionState?>(null)
    val exportSelectionState: StateFlow<ExportSelectionState?> = _exportSelectionState.asStateFlow()
    
    private val _importedItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val importedItems: StateFlow<List<SignalItem>> = _importedItems.asStateFlow()
    
    private val _selectedImportResult = MutableStateFlow<ImportConflictResolutionResult?>(null)
    val selectedImportResult: StateFlow<ImportConflictResolutionResult?> = _selectedImportResult.asStateFlow()
    
    // Export state management
    fun setExportSelectionState(state: ExportSelectionState) {
        _exportSelectionState.value = state
    }
    
    fun clearExportSelectionState() {
        _exportSelectionState.value = null
    }
    
    // Import state management
    fun setImportedItems(items: List<SignalItem>) {
        _importedItems.value = items
    }
    
    fun clearImportedItems() {
        _importedItems.value = emptyList()
    }
    
    fun setSelectedImportResult(result: ImportConflictResolutionResult) {
        _selectedImportResult.value = result
    }
    
    fun clearSelectedImportResult() {
        _selectedImportResult.value = null
    }
    
    /**
     * Reset all import/export state
     */
    fun clearAllState() {
        clearExportSelectionState()
        clearImportedItems()
        clearSelectedImportResult()
    }
}

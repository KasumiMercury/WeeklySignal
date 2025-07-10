package net.mercuryksm.ui.usecase

import net.mercuryksm.data.*

/**
 * Use case for handling import selection business logic.
 * Extracts business logic from ImportSelectionScreen UI component.
 */
class ImportSelectionUseCase {
    
    private val conflictResolutionService = ConflictResolutionService()
    
    /**
     * Initialize selection state for imported items
     */
    fun createInitialSelectionState(importedItems: List<SignalItem>): ExportSelectionState {
        val initialState = SelectionStateManager.createInitialState(importedItems)
        // Initially select all imported items
        return SelectionStateManager.selectAll(initialState, true)
    }
    
    /**
     * Find conflicts between existing and imported items
     */
    fun findConflicts(
        existingItems: List<SignalItem>, 
        importedItems: List<SignalItem>
    ): List<SignalItem> {
        return conflictResolutionService.findConflicts(existingItems, importedItems)
    }
    
    /**
     * Handle import operation when no conflicts exist
     */
    fun handleNoConflictImport(selectionState: ExportSelectionState): ImportConflictResolutionResult {
        return ImportConflictResolutionResult(
            itemsToInsert = selectionState.selectedSignalItemsWithTimeSlots,
            itemsToUpdate = emptyList()
        )
    }
    
    /**
     * Handle import operation with conflict resolution
     */
    fun handleConflictResolution(
        existingItems: List<SignalItem>,
        selectedItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult {
        return conflictResolutionService.resolveConflicts(
            existingItems,
            selectedItems,
            conflictResolution
        )
    }
    
    /**
     * Get import summary information
     */
    fun getImportSummary(
        selectionState: ExportSelectionState,
        conflicts: List<SignalItem>
    ): ImportSummary {
        return ImportSummary(
            totalItems = selectionState.signalItemSelections.size,
            selectedItems = selectionState.selectedItemCount,
            conflictingItems = conflicts.size
        )
    }
}

/**
 * Summary information for import operations
 */
data class ImportSummary(
    val totalItems: Int,
    val selectedItems: Int,
    val conflictingItems: Int
) {
    val hasConflicts: Boolean get() = conflictingItems > 0
    val hasSelection: Boolean get() = selectedItems > 0
}
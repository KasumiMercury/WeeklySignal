package net.mercuryksm.data

/**
 * Represents the selection state of a single TimeSlot for export/import operations
 */
data class TimeSlotSelectionState(
    val timeSlot: TimeSlot,
    val isSelected: Boolean = false
)

/**
 * Represents the selection state of a single SignalItem for export/import operations
 */
data class SignalItemSelectionState(
    val signalItem: SignalItem,
    val isSelected: Boolean = false,
    val isExpanded: Boolean = false,
    val timeSlotSelections: List<TimeSlotSelectionState> = emptyList()
) {
    /**
     * Returns true if all TimeSlots in this SignalItem are selected
     */
    val isFullySelected: Boolean
        get() = timeSlotSelections.isNotEmpty() && timeSlotSelections.all { it.isSelected }
    
    /**
     * Returns true if some (but not all) TimeSlots in this SignalItem are selected
     */
    val isPartiallySelected: Boolean
        get() = timeSlotSelections.any { it.isSelected } && !isFullySelected
    
    /**
     * Returns the list of selected TimeSlots
     */
    val selectedTimeSlots: List<TimeSlot>
        get() = timeSlotSelections.filter { it.isSelected }.map { it.timeSlot }
    
    /**
     * Returns the count of selected TimeSlots
     */
    val selectedTimeSlotCount: Int
        get() = timeSlotSelections.count { it.isSelected }
}

/**
 * Manages the overall selection state for export/import operations
 */
data class ExportSelectionState(
    val signalItemSelections: List<SignalItemSelectionState> = emptyList(),
    val selectAllEnabled: Boolean = false
) {
    /**
     * Returns true if all SignalItems are fully selected
     */
    val isAllSelected: Boolean
        get() = signalItemSelections.isNotEmpty() && signalItemSelections.all { it.isFullySelected }
    
    /**
     * Returns true if some SignalItems or TimeSlots are selected
     */
    val hasSelection: Boolean
        get() = signalItemSelections.any { it.isSelected || it.timeSlotSelections.any { ts -> ts.isSelected } }
    
    /**
     * Returns the list of selected SignalItems (for full SignalItem selection)
     */
    val selectedSignalItems: List<SignalItem>
        get() = signalItemSelections.filter { it.isSelected }.map { it.signalItem }
    
    /**
     * Returns a list of SignalItems with only their selected TimeSlots
     */
    val selectedSignalItemsWithTimeSlots: List<SignalItem>
        get() = signalItemSelections.mapNotNull { selection ->
            when {
                selection.isSelected -> selection.signalItem
                selection.selectedTimeSlots.isNotEmpty() -> selection.signalItem.copy(
                    timeSlots = selection.selectedTimeSlots
                )
                else -> null
            }
        }
    
    /**
     * Returns the total count of selected items (SignalItems or individual TimeSlots)
     */
    val selectedItemCount: Int
        get() = signalItemSelections.sumOf { selection ->
            if (selection.isSelected) 1 else selection.selectedTimeSlotCount
        }
    
    /**
     * Returns the total count of selected TimeSlots across all SignalItems
     */
    val selectedTimeSlotCount: Int
        get() = signalItemSelections.sumOf { selection ->
            if (selection.isSelected) selection.signalItem.timeSlots.size else selection.selectedTimeSlotCount
        }
}

/**
 * Utility functions for managing selection state
 */
object SelectionStateManager {
    
    /**
     * Creates initial selection state from a list of SignalItems
     */
    fun createInitialState(signalItems: List<SignalItem>): ExportSelectionState {
        val signalItemSelections = signalItems.map { signalItem ->
            SignalItemSelectionState(
                signalItem = signalItem,
                timeSlotSelections = signalItem.timeSlots.map { timeSlot ->
                    TimeSlotSelectionState(timeSlot = timeSlot)
                }
            )
        }
        return ExportSelectionState(signalItemSelections = signalItemSelections)
    }
    
    /**
     * Toggles selection state for a specific SignalItem
     */
    fun toggleSignalItemSelection(
        state: ExportSelectionState,
        signalItemId: String
    ): ExportSelectionState {
        val updatedSelections = state.signalItemSelections.map { selection ->
            if (selection.signalItem.id == signalItemId) {
                val newSelectedState = !selection.isSelected
                selection.copy(
                    isSelected = newSelectedState,
                    timeSlotSelections = selection.timeSlotSelections.map { timeSlotSelection ->
                        timeSlotSelection.copy(isSelected = newSelectedState)
                    }
                )
            } else {
                selection
            }
        }
        return state.copy(signalItemSelections = updatedSelections)
    }
    
    /**
     * Toggles selection state for a specific TimeSlot
     */
    fun toggleTimeSlotSelection(
        state: ExportSelectionState,
        signalItemId: String,
        timeSlotId: String
    ): ExportSelectionState {
        val updatedSelections = state.signalItemSelections.map { selection ->
            if (selection.signalItem.id == signalItemId) {
                val updatedTimeSlotSelections = selection.timeSlotSelections.map { timeSlotSelection ->
                    if (timeSlotSelection.timeSlot.id == timeSlotId) {
                        timeSlotSelection.copy(isSelected = !timeSlotSelection.isSelected)
                    } else {
                        timeSlotSelection
                    }
                }
                
                // Update SignalItem selection based on TimeSlot selections
                val allTimeSlotSelected = updatedTimeSlotSelections.all { it.isSelected }
                val anyTimeSlotSelected = updatedTimeSlotSelections.any { it.isSelected }
                
                selection.copy(
                    isSelected = allTimeSlotSelected,
                    timeSlotSelections = updatedTimeSlotSelections
                )
            } else {
                selection
            }
        }
        return state.copy(signalItemSelections = updatedSelections)
    }
    
    /**
     * Toggles expansion state for a specific SignalItem
     */
    fun toggleSignalItemExpansion(
        state: ExportSelectionState,
        signalItemId: String
    ): ExportSelectionState {
        val updatedSelections = state.signalItemSelections.map { selection ->
            if (selection.signalItem.id == signalItemId) {
                selection.copy(isExpanded = !selection.isExpanded)
            } else {
                selection
            }
        }
        return state.copy(signalItemSelections = updatedSelections)
    }
    
    /**
     * Selects or deselects all items
     */
    fun selectAll(state: ExportSelectionState, selected: Boolean): ExportSelectionState {
        val updatedSelections = state.signalItemSelections.map { selection ->
            selection.copy(
                isSelected = selected,
                timeSlotSelections = selection.timeSlotSelections.map { timeSlotSelection ->
                    timeSlotSelection.copy(isSelected = selected)
                }
            )
        }
        return state.copy(
            signalItemSelections = updatedSelections,
            selectAllEnabled = selected
        )
    }
    
    /**
     * Expands or collapses all SignalItems
     */
    fun expandAll(state: ExportSelectionState, expanded: Boolean): ExportSelectionState {
        val updatedSelections = state.signalItemSelections.map { selection ->
            selection.copy(isExpanded = expanded)
        }
        return state.copy(signalItemSelections = updatedSelections)
    }
}
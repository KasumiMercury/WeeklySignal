package net.mercuryksm.ui.exportimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.data.ExportImportService
import net.mercuryksm.data.ExportSelectionState
import net.mercuryksm.data.FileOperationsService
import net.mercuryksm.event.DomainEvent
import net.mercuryksm.event.EventBus
import net.mercuryksm.service.AlarmManagementService

enum class ConflictResolution {
    REPLACE_EXISTING,
    KEEP_EXISTING,
    MERGE_TIME_SLOTS
}

class ExportImportViewModel(
    private val signalRepository: SignalRepository,
    private val exportImportService: ExportImportService,
    private val fileService: FileOperationsService,
    private val alarmManagementService: AlarmManagementService,
    private val eventBus: EventBus
) : ViewModel() {
    
    // Expose signalItems from repository
    val signalItems: StateFlow<List<SignalItem>> = signalRepository.signalItems
    
    // Export/Import selection state
    private val _exportSelectionState = MutableStateFlow<ExportSelectionState?>(null)
    val exportSelectionState: StateFlow<ExportSelectionState?> = _exportSelectionState.asStateFlow()
    
    private val _importedItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val importedItems: StateFlow<List<SignalItem>> = _importedItems.asStateFlow()
    
    private val _selectedImportItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val selectedImportItems: StateFlow<List<SignalItem>> = _selectedImportItems.asStateFlow()
    
    // Export/Import state management
    fun setExportSelectionState(state: ExportSelectionState) {
        _exportSelectionState.value = state
    }
    
    fun clearExportSelectionState() {
        _exportSelectionState.value = null
    }
    
    fun setImportedItems(items: List<SignalItem>) {
        _importedItems.value = items
    }
    
    fun clearImportedItems() {
        _importedItems.value = emptyList()
    }
    
    fun setSelectedImportItems(items: List<SignalItem>) {
        _selectedImportItems.value = items
    }
    
    fun clearSelectedImportItems() {
        _selectedImportItems.value = emptyList()
    }
    
    // Export operations
    suspend fun exportSelectedItems(): Result<String> {
        return try {
            val selectionState = _exportSelectionState.value
            if (selectionState == null) {
                return Result.failure(IllegalStateException("No export selection state"))
            }
            
            val result = exportImportService.exportSelectedSignalItems(selectionState)
            result.onSuccess { fileName ->
                val exportedItems = selectionState.selectedSignalItems.keys.toList()
                eventBus.publish(DomainEvent.SignalItemsExported(exportedItems))
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Import operations with transaction support and conflict resolution
    fun importSignalItemsWithConflictResolution(
        signalItems: List<SignalItem>,
        conflictResolution: ConflictResolution = ConflictResolution.REPLACE_EXISTING,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = when (conflictResolution) {
                ConflictResolution.REPLACE_EXISTING -> {
                    signalRepository.addSignalItemsInTransaction(signalItems)
                }
                ConflictResolution.KEEP_EXISTING -> {
                    // For now, just add non-conflicting items
                    signalRepository.addSignalItemsInTransaction(signalItems)
                }
                ConflictResolution.MERGE_TIME_SLOTS -> {
                    // For now, just replace existing
                    signalRepository.addSignalItemsInTransaction(signalItems)
                }
            }
            
            result.onSuccess {
                // Schedule alarms for all imported SignalItems
                signalItems.forEach { signalItem ->
                    alarmManagementService.scheduleSignalItemAlarms(signalItem)
                }
                // Publish domain event
                eventBus.publish(DomainEvent.SignalItemsImported(signalItems))
            }
            onResult(result)
        }
    }
    
    fun updateSignalItemsWithConflictResolution(
        signalItems: List<SignalItem>,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = signalRepository.updateSignalItemsInTransaction(signalItems)
            result.onSuccess {
                // Update alarms for all modified SignalItems
                signalItems.forEach { signalItem ->
                    alarmManagementService.updateSignalItemAlarms(signalItem)
                }
                // Publish domain event
                eventBus.publish(DomainEvent.SignalItemsImported(signalItems))
            }
            onResult(result)
        }
    }
}

enum class ConflictResolution {
    REPLACE_EXISTING,
    KEEP_EXISTING,
    MERGE_TIME_SLOTS
}
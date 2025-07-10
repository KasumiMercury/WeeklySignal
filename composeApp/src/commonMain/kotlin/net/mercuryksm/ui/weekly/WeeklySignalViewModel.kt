package net.mercuryksm.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.data.ExportSelectionState
import net.mercuryksm.data.ImportConflictResolutionResult
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.ui.coordination.ImportExportCoordinator
import net.mercuryksm.ui.coordination.AlarmCoordinator

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository,
    alarmManager: SignalAlarmManager? = null
) : ViewModel() {

    private val _signalItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val signalItems: StateFlow<List<SignalItem>> = _signalItems.asStateFlow()
    val isLoading: StateFlow<Boolean> = signalRepository.isLoading
    
    // Coordinators for specialized responsibilities
    private val importExportCoordinator = ImportExportCoordinator()
    private val alarmCoordinator = AlarmCoordinator(alarmManager, viewModelScope)
    
    // Expose coordinator state
    val exportSelectionState: StateFlow<ExportSelectionState?> = importExportCoordinator.exportSelectionState
    val importedItems: StateFlow<List<SignalItem>> = importExportCoordinator.importedItems
    val selectedImportResult: StateFlow<ImportConflictResolutionResult?> = importExportCoordinator.selectedImportResult

    init {
        // Observe repository changes and update StateFlow
        signalRepository.signalItems
            .onEach { items ->
                _signalItems.value = items
            }
            .launchIn(viewModelScope)
    }
    
    fun addSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.addSignalItem(signalItem)
            result.onSuccess {
                // Schedule alarms for the new SignalItem
                alarmCoordinator.scheduleSignalItemAlarms(signalItem)
            }
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val oldSignalItem = getSignalItemById(signalItem.id)
            val result = signalRepository.updateSignalItem(signalItem)
            result.onSuccess {
                // Update alarms for the modified SignalItem
                if (oldSignalItem != null) {
                    alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                } else {
                    alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                }
            }
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.removeSignalItem(signalItem)
            result.onSuccess {
                // Cancel alarms for the removed SignalItem
                alarmCoordinator.cancelSignalItemAlarms(signalItem)
            }
            onResult(result)
        }
    }
    
    fun refreshData(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.refreshFromDatabase()
            onResult(result)
        }
    }
    
    fun getSignalItemById(id: String): SignalItem? {
        return signalRepository.getSignalItemById(id)
    }
    
    fun getSignalItemsForDay(dayOfWeek: DayOfWeekJp): List<SignalItem> {
        return signalRepository.getSignalItemsForDay(dayOfWeek)
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return signalItems.value
    }
    
    fun clearAllSignalItems(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.clearAllSignalItems()
            result.onSuccess {
                // Cancel all alarms
                alarmCoordinator.cancelSignalItemsAlarms(signalItems.value)
            }
            onResult(result)
        }
    }
    
    // Alarm management methods - delegate to AlarmCoordinator
    suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return alarmCoordinator.isSignalItemAlarmsEnabled(signalItemId)
    }
    
    // Export/Import selection state management - delegate to ImportExportCoordinator
    fun setExportSelectionState(state: ExportSelectionState) {
        importExportCoordinator.setExportSelectionState(state)
    }
    
    fun clearExportSelectionState() {
        importExportCoordinator.clearExportSelectionState()
    }
    
    fun setImportedItems(items: List<SignalItem>) {
        importExportCoordinator.setImportedItems(items)
    }
    
    fun clearImportedItems() {
        importExportCoordinator.clearImportedItems()
    }
    
    fun setSelectedImportResult(result: ImportConflictResolutionResult) {
        importExportCoordinator.setSelectedImportResult(result)
    }
    
    fun clearSelectedImportResult() {
        importExportCoordinator.clearSelectedImportResult()
    }
    
    // Improved import methods with transaction support and conflict resolution
    fun importSignalItemsWithConflictResolution(
        itemsToInsert: List<SignalItem>,
        itemsToUpdate: List<SignalItem>,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = signalRepository.importSignalItemsWithConflictResolution(itemsToInsert, itemsToUpdate)
            result.onSuccess {
                // Schedule alarms for all imported SignalItems
                alarmCoordinator.scheduleSignalItemsAlarms(itemsToInsert + itemsToUpdate)
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
                    val oldSignalItem = getSignalItemById(signalItem.id)
                    if (oldSignalItem != null) {
                        alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                    } else {
                        alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                    }
                }
            }
            onResult(result)
        }
    }
}

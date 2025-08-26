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
import net.mercuryksm.ui.components.OperationStatus
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
    
    // Deletion status for WeeklySignalView modal display
    private val _deletionStatus = MutableStateFlow<OperationStatus?>(null)
    val deletionStatus: StateFlow<OperationStatus?> = _deletionStatus.asStateFlow()
    
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
                // Schedule alarms for the new SignalItem - await completion
                try {
                    alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log alarm scheduling error but don't fail the overall operation
                }
            }
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val oldSignalItem = getSignalItemById(signalItem.id)
            
            // First, update alarms before database update (as per requirements)
            try {
                if (oldSignalItem != null) {
                    alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                } else {
                    alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Log alarm update error but don't fail the overall operation
            }
            
            // Then update database
            val result = signalRepository.updateSignalItem(signalItem)
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Pair<Boolean, Boolean>>) -> Unit = {}) {
        viewModelScope.launch {
            // First, cancel alarms before removing from database and WAIT for completion
            val alarmsCancelled = try {
                alarmCoordinator.cancelSignalItemAlarms(signalItem) // Now properly awaits completion
            } catch (e: Exception) {
                e.printStackTrace()
                false // Alarm cancellation failed
                // Continue with database deletion even if alarm cancellation fails
            }
            
            // Then remove from database
            val databaseResult = signalRepository.removeSignalItem(signalItem)
            
            // Return both database deletion success and alarm cancellation success
            val combinedResult = databaseResult.map { 
                Pair(databaseResult.isSuccess, alarmsCancelled)
            }
            
            onResult(combinedResult)
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
                // Cancel all alarms - await completion
                try {
                    alarmCoordinator.cancelSignalItemsAlarms(signalItems.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log alarm cancellation error but don't fail the overall operation
                }
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
    
    // Deletion status management
    fun setDeletionStatus(status: OperationStatus?) {
        _deletionStatus.value = status
    }
    
    fun clearDeletionStatus() {
        _deletionStatus.value = null
    }
    
    fun removeTimeSlotFromSignalItem(
        signalItem: SignalItem, 
        timeSlot: net.mercuryksm.data.TimeSlot, 
        onResult: (Result<Pair<Boolean, Boolean>>) -> Unit = {}
    ) {
        viewModelScope.launch {
            // First, cancel alarm for the specific TimeSlot
            val alarmCancelled = alarmCoordinator.cancelTimeSlotAlarm(signalItem, timeSlot)
            
            // Then update the SignalItem in the database
            val updatedSignalItem = signalItem.copy(
                timeSlots = signalItem.timeSlots.filter { it.id != timeSlot.id }
            )
            
            val databaseResult = signalRepository.updateSignalItem(updatedSignalItem)
            
            // Return both database update success and alarm cancellation success
            val combinedResult = databaseResult.map { 
                Pair(databaseResult.isSuccess, alarmCancelled)
            }
            
            onResult(combinedResult)
        }
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
                // Schedule alarms for all imported SignalItems - await completion
                try {
                    alarmCoordinator.scheduleSignalItemsAlarms(itemsToInsert + itemsToUpdate)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log alarm scheduling error but don't fail the overall operation
                }
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
                // Update alarms for all modified SignalItems - await completion
                try {
                    signalItems.forEach { signalItem ->
                        val oldSignalItem = getSignalItemById(signalItem.id)
                        if (oldSignalItem != null) {
                            alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                        } else {
                            alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log alarm update error but don't fail the overall operation
                }
            }
            onResult(result)
        }
    }
}

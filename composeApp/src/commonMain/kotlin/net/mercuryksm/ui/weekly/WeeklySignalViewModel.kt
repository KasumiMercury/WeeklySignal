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
import net.mercuryksm.ui.components.OperationStatusHelper
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
    private val alarmCoordinator = AlarmCoordinator(alarmManager)
    
    // Deletion status for WeeklySignalView modal display
    private val _deletionStatus = MutableStateFlow<OperationStatus?>(null)
    val deletionStatus: StateFlow<OperationStatus?> = _deletionStatus.asStateFlow()
    
    // Operation status for alarm failures and other operations
    private val _operationStatus = MutableStateFlow<OperationStatus?>(null)
    val operationStatus: StateFlow<OperationStatus?> = _operationStatus.asStateFlow()
    
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
            // First, try to schedule alarms (device OS state priority)
            val schedulingResults = try {
                alarmCoordinator.scheduleSignalItemAlarms(signalItem)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            
            if (schedulingResults == null) {
                // Alarm operation failed - do not update database
                println("WeeklySignalViewModel: Alarm scheduling failed for SignalItem: ${signalItem.name}")
                _operationStatus.value = OperationStatusHelper.signalItemCreateFailed(
                    "Failed to schedule alarms on device. Signal item was not saved."
                )
                onResult(Result.failure(Exception("Alarm scheduling failed")))
                return@launch
            }
            
            // Only update database if alarm operations succeeded
            val result = signalRepository.addSignalItem(signalItem, schedulingResults)
            if (result.isFailure) {
                // Database failed after successful alarm scheduling - cancel alarms to maintain consistency
                try {
                    alarmCoordinator.cancelSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val oldSignalItem = getSignalItemById(signalItem.id)
            
            // First, update alarms (device OS state priority)
            val schedulingResults = try {
                if (oldSignalItem != null) {
                    alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                } else {
                    alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            
            if (schedulingResults == null) {
                // Alarm operation failed - do not update database
                println("WeeklySignalViewModel: Alarm update failed for SignalItem: ${signalItem.name}")
                _operationStatus.value = OperationStatusHelper.signalItemUpdateFailed(
                    "Failed to update alarms on device. Signal item was not updated."
                )
                onResult(Result.failure(Exception("Alarm update failed")))
                return@launch
            }
            
            // Only update database if alarm operations succeeded
            val result = signalRepository.updateSignalItem(signalItem, schedulingResults)
            if (result.isFailure) {
                // Database failed after successful alarm update - try to revert alarms
                if (oldSignalItem != null) {
                    try {
                        alarmCoordinator.updateSignalItemAlarms(signalItem, oldSignalItem)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Pair<Boolean, Boolean>>) -> Unit = {}) {
        viewModelScope.launch {
            // First, cancel alarms (device OS state priority)
            val alarmsCancelled = try {
                alarmCoordinator.cancelSignalItemAlarms(signalItem)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            
            if (!alarmsCancelled) {
                // Alarm cancellation failed - do not remove from database
                _operationStatus.value = OperationStatusHelper.signalItemDeleteFailed(
                    "Failed to cancel alarms on device. Signal item was not deleted to maintain consistency."
                )
                onResult(Result.failure(Exception("Alarm cancellation failed")))
                return@launch
            }
            
            // Only remove from database if alarm cancellation succeeded
            val databaseResult = signalRepository.removeSignalItem(signalItem)
            if (databaseResult.isFailure) {
                // Database deletion failed after successful alarm cancellation - try to reschedule alarms
                try {
                    alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
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
            // First, cancel all alarms (device OS state priority)
            val currentItems = signalItems.value
            val alarmSuccess = try {
                alarmCoordinator.cancelSignalItemsAlarms(currentItems)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            
            if (!alarmSuccess) {
                // Alarm cancellation failed - do not clear database
                _operationStatus.value = OperationStatusHelper.alarmOperationFailed(
                    "Clear All SignalItems",
                    "Failed to cancel all alarms on device. Clear operation cancelled."
                )
                onResult(Result.failure(Exception("Clear all alarms failed")))
                return@launch
            }
            
            // Only clear database if alarm cancellation succeeded
            val result = signalRepository.clearAllSignalItems()
            if (result.isFailure) {
                // Database clear failed after successful alarm cancellation - try to reschedule alarms
                try {
                    alarmCoordinator.scheduleSignalItemsAlarms(currentItems)
                } catch (e: Exception) {
                    e.printStackTrace()
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
    
    // Operation status management
    fun setOperationStatus(status: OperationStatus?) {
        _operationStatus.value = status
    }
    
    fun clearOperationStatus() {
        _operationStatus.value = null
    }
    
    

    // Improved import methods with transaction support and conflict resolution
    fun importSignalItemsWithConflictResolution(
        itemsToInsert: List<SignalItem>,
        itemsToUpdate: List<SignalItem>,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            // First, schedule alarms for all imported SignalItems (device OS state priority)
            val allItems = itemsToInsert + itemsToUpdate
            val alarmSuccess = try {
                alarmCoordinator.scheduleSignalItemsAlarms(allItems)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            
            if (!alarmSuccess) {
                // Alarm operations failed - do not import to database
                _operationStatus.value = OperationStatusHelper.alarmOperationFailed(
                    "Import SignalItems",
                    "Failed to schedule alarms on device. Import operation cancelled."
                )
                onResult(Result.failure(Exception("Import alarm scheduling failed")))
                return@launch
            }
            
            // Only import to database if alarm operations succeeded
            val result = signalRepository.importSignalItemsWithConflictResolution(itemsToInsert, itemsToUpdate)
            if (result.isFailure) {
                // Database import failed after successful alarm scheduling - cancel alarms to maintain consistency
                try {
                    alarmCoordinator.cancelSignalItemsAlarms(allItems)
                } catch (e: Exception) {
                    e.printStackTrace()
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
            val allSchedulingResults = mutableListOf<net.mercuryksm.notification.AlarmOperationResult>()
            var allAlarmsSuccess = true

            // First, update alarms for all SignalItems (device OS state priority)
            try {
                for (signalItem in signalItems) {
                    val oldSignalItem = getSignalItemById(signalItem.id)
                    val results = if (oldSignalItem != null) {
                        alarmCoordinator.updateSignalItemAlarms(oldSignalItem, signalItem)
                    } else {
                        alarmCoordinator.scheduleSignalItemAlarms(signalItem)
                    }

                    if (results == null) {
                        allAlarmsSuccess = false
                        break
                    } else {
                        allSchedulingResults.addAll(results)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                allAlarmsSuccess = false
            }
            
            if (!allAlarmsSuccess) {
                // Alarm operations failed - do not update database
                _operationStatus.value = OperationStatusHelper.alarmOperationFailed(
                    "Update SignalItems",
                    "Failed to update alarms on device. Update operation cancelled."
                )
                onResult(Result.failure(Exception("Batch alarm update failed")))
                return@launch
            }
            
            // Only update database if alarm operations succeeded
            val result = signalRepository.updateSignalItemsInTransaction(signalItems, allSchedulingResults)
            if (result.isFailure) {
                // Database update failed after successful alarm updates - try to revert alarms
                try {
                    signalItems.forEach { signalItem ->
                        val oldSignalItem = getSignalItemById(signalItem.id)
                        if (oldSignalItem != null) {
                            alarmCoordinator.updateSignalItemAlarms(signalItem, oldSignalItem)
                        } else {
                            alarmCoordinator.cancelSignalItemAlarms(signalItem)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onResult(result)
        }
    }
}

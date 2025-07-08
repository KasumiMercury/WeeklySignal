package net.mercuryksm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.notification.SignalAlarmManager

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository,
    private val alarmManager: SignalAlarmManager? = null
) : ViewModel() {

    private val _signalItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val signalItems: StateFlow<List<SignalItem>> = _signalItems.asStateFlow()
    val isLoading: StateFlow<Boolean> = signalRepository.isLoading

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
                scheduleSignalItemAlarms(signalItem)
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
                    updateSignalItemAlarms(oldSignalItem, signalItem)
                } else {
                    scheduleSignalItemAlarms(signalItem)
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
                cancelSignalItemAlarms(signalItem)
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
    
    // Alarm management methods
    
    private fun scheduleSignalItemAlarms(signalItem: SignalItem) {
        viewModelScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.scheduleSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    // Log alarm scheduling error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun cancelSignalItemAlarms(signalItem: SignalItem) {
        viewModelScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.cancelSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    // Log alarm cancellation error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem) {
        viewModelScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.updateSignalItemAlarms(oldSignalItem, newSignalItem)
                } catch (e: Exception) {
                    // Log alarm update error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return alarmManager?.let { manager ->
            try {
                manager.isSignalItemAlarmsEnabled(signalItemId)
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
}

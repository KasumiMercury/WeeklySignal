package net.mercuryksm.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.event.DomainEvent
import net.mercuryksm.event.EventBus
import net.mercuryksm.service.AlarmManagementService

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository,
    private val alarmManagementService: AlarmManagementService,
    private val eventBus: EventBus
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
                eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
            }
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.updateSignalItem(signalItem)
            result.onSuccess {
                eventBus.publish(DomainEvent.SignalItemUpdated(signalItem))
            }
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.removeSignalItem(signalItem)
            result.onSuccess {
                eventBus.publish(DomainEvent.SignalItemDeleted(signalItem.id))
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
                eventBus.publish(DomainEvent.AllSignalItemsCleared())
            }
            onResult(result)
        }
    }
    
    suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return alarmManagementService.isSignalItemAlarmsEnabled(signalItemId)
    }
}

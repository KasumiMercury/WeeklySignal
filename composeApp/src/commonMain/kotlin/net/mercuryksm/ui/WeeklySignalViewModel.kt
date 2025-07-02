package net.mercuryksm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository = SignalRepository()
) : ViewModel() {
    
    val signalItems = signalRepository.signalItems
    val isLoading: StateFlow<Boolean> = signalRepository.isLoading
    
    fun addSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.addSignalItem(signalItem)
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.updateSignalItem(signalItem)
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.removeSignalItem(signalItem)
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
        return signalRepository.getAllSignalItems()
    }
}
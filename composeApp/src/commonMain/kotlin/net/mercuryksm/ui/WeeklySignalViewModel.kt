package net.mercuryksm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository
) : ViewModel() {
    
    private val _signalItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val signalItems: StateFlow<List<SignalItem>> = _signalItems.asStateFlow()
    val isLoading: StateFlow<Boolean> = signalRepository.isLoading
    
    init {
        // Observe repository changes and update StateFlow
        viewModelScope.launch {
            // Initial load
            _signalItems.value = signalRepository.getAllSignalItems()
            
            // Listen for changes - since we're using mutableStateListOf in repository,
            // we need to periodically check or use a different approach
            // For now, we'll update after each operation
        }
    }
    
    fun addSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.addSignalItem(signalItem)
            if (result.isSuccess) {
                _signalItems.value = signalRepository.getAllSignalItems()
            }
            onResult(result)
        }
    }
    
    fun updateSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.updateSignalItem(signalItem)
            if (result.isSuccess) {
                _signalItems.value = signalRepository.getAllSignalItems()
            }
            onResult(result)
        }
    }
    
    fun removeSignalItem(signalItem: SignalItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.removeSignalItem(signalItem)
            if (result.isSuccess) {
                _signalItems.value = signalRepository.getAllSignalItems()
            }
            onResult(result)
        }
    }
    
    fun refreshData(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = signalRepository.refreshFromDatabase()
            if (result.isSuccess) {
                _signalItems.value = signalRepository.getAllSignalItems()
            }
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
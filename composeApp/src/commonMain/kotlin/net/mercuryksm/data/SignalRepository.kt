package net.mercuryksm.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.mercuryksm.data.database.SignalDatabaseService
import java.util.*

class SignalRepository(
    private val databaseService: SignalDatabaseService? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _signalItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val signalItems: StateFlow<List<SignalItem>> = _signalItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadFromDatabase()
    }
    
    suspend fun addSignalItem(signalItem: SignalItem): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = if (databaseService != null) {
                databaseService.saveSignalItem(signalItem)
            } else {
                Result.success(Unit)
            }
            
            if (result.isSuccess) {
                _signalItems.value = _signalItems.value + signalItem
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateSignalItem(updatedItem: SignalItem): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = if (databaseService != null) {
                databaseService.updateSignalItem(updatedItem)
            } else {
                Result.success(Unit)
            }
            
            if (result.isSuccess) {
                val index = _signalItems.value.indexOfFirst { it.id == updatedItem.id }
                if (index != -1) {
                    _signalItems.value = _signalItems.value.toMutableList().apply {
                        this[index] = updatedItem
                    }
                }
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun removeSignalItem(signalItem: SignalItem): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = if (databaseService != null) {
                databaseService.deleteSignalItem(signalItem.id)
            } else {
                Result.success(Unit)
            }
            
            if (result.isSuccess) {
                _signalItems.value = _signalItems.value.filterNot { it.id == signalItem.id }
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun getSignalItemsForDay(dayOfWeek: DayOfWeekJp): List<SignalItem> {
        return _signalItems.value.filter { signalItem ->
            signalItem.timeSlots.any { it.dayOfWeek == dayOfWeek }
        }
    }
    
    fun getSignalItemById(id: String): SignalItem? {
        return _signalItems.value.find { it.id == id }
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return _signalItems.value
    }
    
    suspend fun refreshFromDatabase(): Result<Unit> {
        return try {
            _isLoading.value = true
            
            if (databaseService != null) {
                val result = databaseService.getAllSignalItems()
                result.onSuccess { items ->
                    _signalItems.value = items
                }
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    private fun loadFromDatabase() {
        if (databaseService != null) {
            coroutineScope.launch {
                databaseService.getAllSignalItems()
                    .onSuccess { items ->
                        _signalItems.value = items
                    }
                    .onFailure {
                        loadSampleData()
                    }
            }
        } else {
            loadSampleData()
        }
    }
    
    private fun loadSampleData() {
        val sampleItems = listOf(
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Morning Meeting",
                timeSlots = listOf(
                    TimeSlot(hour = 9, minute = 0, dayOfWeek = DayOfWeekJp.MONDAY),
                    TimeSlot(hour = 9, minute = 0, dayOfWeek = DayOfWeekJp.WEDNESDAY),
                    TimeSlot(hour = 9, minute = 0, dayOfWeek = DayOfWeekJp.FRIDAY)
                ),
                description = "Weekly team meeting",
                sound = true,
                vibration = true,
                color = 0xFF2196F3L // Blue
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Lunch Break",
                timeSlots = listOf(
                    TimeSlot(hour = 12, minute = 30, dayOfWeek = DayOfWeekJp.MONDAY),
                    TimeSlot(hour = 12, minute = 30, dayOfWeek = DayOfWeekJp.TUESDAY),
                    TimeSlot(hour = 12, minute = 30, dayOfWeek = DayOfWeekJp.WEDNESDAY),
                    TimeSlot(hour = 12, minute = 30, dayOfWeek = DayOfWeekJp.THURSDAY),
                    TimeSlot(hour = 12, minute = 30, dayOfWeek = DayOfWeekJp.FRIDAY)
                ),
                description = "Time for lunch",
                sound = false,
                vibration = true,
                color = 0xFF4CAF50L // Green
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Project Review",
                timeSlots = listOf(
                    TimeSlot(hour = 15, minute = 0, dayOfWeek = DayOfWeekJp.WEDNESDAY)
                ),
                description = "Review project progress",
                sound = true,
                vibration = false,
                color = 0xFFFF9800L // Orange
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Exercise Time",
                timeSlots = listOf(
                    TimeSlot(hour = 18, minute = 30, dayOfWeek = DayOfWeekJp.TUESDAY),
                    TimeSlot(hour = 18, minute = 30, dayOfWeek = DayOfWeekJp.THURSDAY),
                    TimeSlot(hour = 19, minute = 0, dayOfWeek = DayOfWeekJp.SATURDAY)
                ),
                description = "Daily workout session",
                sound = true,
                vibration = true,
                color = 0xFFE91E63L // Pink
            )
        )
        
        _signalItems.value = sampleItems
    }
}

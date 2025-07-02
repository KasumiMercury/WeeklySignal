package net.mercuryksm.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import net.mercuryksm.data.database.SignalDatabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SignalRepository(
    private val databaseService: SignalDatabaseService? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _signalItems = mutableStateListOf<SignalItem>()
    val signalItems: SnapshotStateList<SignalItem> = _signalItems
    
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
                _signalItems.add(signalItem)
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
                val index = _signalItems.indexOfFirst { it.id == updatedItem.id }
                if (index != -1) {
                    _signalItems[index] = updatedItem
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
                _signalItems.remove(signalItem)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun getSignalItemsForDay(dayOfWeek: DayOfWeekJp): List<SignalItem> {
        return _signalItems.filter { signalItem ->
            signalItem.timeSlots.any { it.dayOfWeek == dayOfWeek }
        }
    }
    
    fun getSignalItemById(id: String): SignalItem? {
        return _signalItems.find { it.id == id }
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return _signalItems.toList()
    }
    
    suspend fun refreshFromDatabase(): Result<Unit> {
        return try {
            _isLoading.value = true
            
            if (databaseService != null) {
                val result = databaseService.getAllSignalItems()
                result.onSuccess { items ->
                    _signalItems.clear()
                    _signalItems.addAll(items)
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
                        _signalItems.clear()
                        _signalItems.addAll(items)
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
                vibration = true
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
                vibration = true
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Project Review",
                timeSlots = listOf(
                    TimeSlot(hour = 15, minute = 0, dayOfWeek = DayOfWeekJp.WEDNESDAY)
                ),
                description = "Review project progress",
                sound = true,
                vibration = false
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
                vibration = true
            )
        )
        
        _signalItems.addAll(sampleItems)
    }
}
package net.mercuryksm.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.UUID

class SignalRepository {
    private val _signalItems = mutableStateListOf<SignalItem>()
    val signalItems: SnapshotStateList<SignalItem> = _signalItems
    
    init {
        loadSampleData()
    }
    
    fun addSignalItem(signalItem: SignalItem) {
        _signalItems.add(signalItem)
    }
    
    fun removeSignalItem(signalItem: SignalItem) {
        _signalItems.remove(signalItem)
    }
    
    fun getSignalItemsForDay(dayOfWeek: DayOfWeekJp): List<SignalItem> {
        return _signalItems.filter { signalItem ->
            signalItem.timeSlots.any { it.dayOfWeek == dayOfWeek }
        }
    }
    
    fun getSignalItemById(id: String): SignalItem? {
        return _signalItems.find { it.id == id }
    }
    
    fun updateSignalItem(updatedItem: SignalItem) {
        val index = _signalItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            _signalItems[index] = updatedItem
        }
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return _signalItems.toList()
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
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
        return _signalItems.filter { it.dayOfWeek == dayOfWeek }
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return _signalItems.toList()
    }
    
    private fun loadSampleData() {
        val sampleItems = listOf(
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Morning Meeting",
                hour = 9,
                minute = 0,
                dayOfWeek = DayOfWeekJp.MONDAY,
                description = "Weekly team meeting",
                sound = true,
                vibration = true
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Lunch Break",
                hour = 12,
                minute = 30,
                dayOfWeek = DayOfWeekJp.TUESDAY,
                description = "Time for lunch",
                sound = false,
                vibration = true
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Project Review",
                hour = 15,
                minute = 0,
                dayOfWeek = DayOfWeekJp.WEDNESDAY,
                description = "Review project progress",
                sound = true,
                vibration = false
            ),
            SignalItem(
                id = UUID.randomUUID().toString(),
                name = "Exercise Time",
                hour = 18,
                minute = 30,
                dayOfWeek = DayOfWeekJp.FRIDAY,
                description = "Daily workout session",
                sound = true,
                vibration = true
            )
        )
        
        _signalItems.addAll(sampleItems)
    }
}
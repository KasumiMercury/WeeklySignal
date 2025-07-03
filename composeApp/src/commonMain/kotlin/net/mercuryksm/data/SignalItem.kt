package net.mercuryksm.data

import java.util.UUID

enum class DayOfWeekJp {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    fun getDisplayName(): String {
        return when (this) {
            MONDAY -> "月"
            TUESDAY -> "火"
            WEDNESDAY -> "水"
            THURSDAY -> "木"
            FRIDAY -> "金"
            SATURDAY -> "土"
            SUNDAY -> "日"
        }
    }

    fun getShortDisplayName(): String {
        return when (this) {
            MONDAY -> "Mon"
            TUESDAY -> "Tue"
            WEDNESDAY -> "Wed"
            THURSDAY -> "Thu"
            FRIDAY -> "Fri"
            SATURDAY -> "Sat"
            SUNDAY -> "Sun"
        }
    }
}

data class TimeSlot(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val dayOfWeek: DayOfWeekJp
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
    }
    
    fun getTimeDisplayText(): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getTimeInMinutes(): Int {
        return hour * 60 + minute
    }
    
    fun getDisplayText(): String {
        return "${dayOfWeek.getShortDisplayName()} ${getTimeDisplayText()}"
    }
}

data class SignalItem(
    val id: String,
    val name: String,
    val timeSlots: List<TimeSlot>,
    val description: String = "",
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val color: Long = 0xFF6750A4L // Default to Material 3 primary color
) {
    init {
        require(timeSlots.isNotEmpty()) { "SignalItem must have at least one time slot" }
    }
    
    fun getTruncatedName(maxLength: Int = 10): String {
        return if (name.length <= maxLength) {
            name
        } else {
            name.take(maxLength) + "..."
        }
    }
    
    fun getTimeSlotsForDay(dayOfWeek: DayOfWeekJp): List<TimeSlot> {
        return timeSlots.filter { it.dayOfWeek == dayOfWeek }
    }
    
    fun getAllTimeSlotsSorted(): List<TimeSlot> {
        return timeSlots.sortedWith(compareBy({ it.dayOfWeek.ordinal }, { it.getTimeInMinutes() }))
    }
    
    fun addTimeSlot(timeSlot: TimeSlot): SignalItem {
        return this.copy(timeSlots = timeSlots + timeSlot)
    }
    
    fun removeTimeSlot(timeSlotId: String): SignalItem {
        return this.copy(timeSlots = timeSlots.filter { it.id != timeSlotId })
    }
    
    fun updateTimeSlot(timeSlotId: String, newTimeSlot: TimeSlot): SignalItem {
        return this.copy(
            timeSlots = timeSlots.map { 
                if (it.id == timeSlotId) newTimeSlot.copy(id = timeSlotId) else it 
            }
        )
    }
}
package net.mercuryksm.ui

data class UITimeSlot(
    val hour: Int,
    val minute: Int,
    val hasItems: Boolean
) {
    fun getTimeInMinutes(): Int {
        return hour * 60 + minute
    }
    
    fun getDisplayText(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

sealed class TimeSlotItem {
    data class TimeSlot(val uiTimeSlot: UITimeSlot) : TimeSlotItem()
    data class Spacer(val width: Int, val startTime: Int) : TimeSlotItem() // width in dp, startTime in minutes
}
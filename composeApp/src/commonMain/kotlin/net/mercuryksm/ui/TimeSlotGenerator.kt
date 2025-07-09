package net.mercuryksm.ui

import net.mercuryksm.data.SignalItem

fun generateTimeSlotItems(allItems: List<SignalItem>): List<TimeSlotItem> {
    val itemTimes = allItems.flatMap { signalItem ->
        signalItem.timeSlots.map { it.getTimeInMinutes() }
    }.toSet().sorted()
    
    if (itemTimes.isEmpty()) {
        return emptyList()
    }
    
    val minTime = itemTimes.minOrNull() ?: 0
    val maxTime = itemTimes.maxOrNull() ?: WeeklyGridConstants.MAX_MINUTES_PER_DAY
    
    val timeSlotItems = mutableListOf<TimeSlotItem>()
    var currentTime = minTime
    
    while (currentTime <= maxTime) {
        val hour = currentTime / 60
        val minute = currentTime % 60
        val hasItems = itemTimes.contains(currentTime)
        
        if (hasItems) {
            // Add the actual time slot
            timeSlotItems.add(
                TimeSlotItem.TimeSlot(
                    UITimeSlot(hour, minute, true)
                )
            )
            currentTime += WeeklyGridConstants.TIME_INTERVAL_MINUTES
        } else {
            // Add memory marks for empty intervals
            val nextItemTime = itemTimes.filter { it > currentTime }.minOrNull()
            val gapToNextItem = nextItemTime?.let { it - currentTime } ?: WeeklyGridConstants.TIME_INTERVAL_MINUTES
            
            if (gapToNextItem >= WeeklyGridConstants.TIME_INTERVAL_MINUTES) {
                val memoryIntervals = (gapToNextItem / WeeklyGridConstants.TIME_INTERVAL_MINUTES).coerceAtLeast(1)
                val spacerWidth = memoryIntervals * WeeklyGridConstants.SPACER_WIDTH_PER_INTERVAL
                timeSlotItems.add(TimeSlotItem.Spacer(spacerWidth, currentTime))
                currentTime += memoryIntervals * WeeklyGridConstants.TIME_INTERVAL_MINUTES
            } else {
                currentTime += WeeklyGridConstants.TIME_INTERVAL_MINUTES
            }
        }
    }
    
    return timeSlotItems
}
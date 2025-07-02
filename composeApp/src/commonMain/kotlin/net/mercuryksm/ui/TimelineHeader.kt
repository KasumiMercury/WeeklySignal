package net.mercuryksm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.SignalItem

data class TimeSlot(
    val hour: Int,
    val minute: Int,
    val hasItems: Boolean
) {
    fun getDisplayText(): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getTimeInMinutes(): Int {
        return hour * 60 + minute
    }
}

@Composable
fun TimelineHeader(
    allItems: List<SignalItem>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val timeSlots = generateTimeSlots(allItems)
    
    LazyRow(
        state = scrollState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(timeSlots) { timeSlot ->
            TimeSlotHeader(
                timeSlot = timeSlot,
                modifier = Modifier
                    .width(if (timeSlot.hasItems) 120.dp else 20.dp)
                    .height(40.dp)
            )
        }
    }
}

@Composable
private fun TimeSlotHeader(
    timeSlot: TimeSlot,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (timeSlot.hasItems) {
            Text(
                text = timeSlot.getDisplayText(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = "|",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun generateTimeSlots(allItems: List<SignalItem>): List<TimeSlot> {
    if (allItems.isEmpty()) {
        return emptyList()
    }
    
    val itemTimes = allItems.map { it.getTimeInMinutes() }.toSet()
    val minTime = itemTimes.minOrNull() ?: 0
    val maxTime = itemTimes.maxOrNull() ?: 1440
    
    val slots = mutableListOf<TimeSlot>()
    var currentTime = minTime
    
    // 開始時間より前の時間は表示しない
    while (currentTime <= maxTime) {
        val hour = currentTime / 60
        val minute = currentTime % 60
        val hasItems = itemTimes.contains(currentTime)
        
        slots.add(TimeSlot(hour, minute, hasItems))
        
        if (hasItems) {
            // SignalItemがある時間は次の時間スロットまで30分間隔
            currentTime += 30
        } else {
            // SignalItemがない時間は15分間隔でコンパクトに
            val nextItemTime = itemTimes.filter { it > currentTime }.minOrNull()
            if (nextItemTime != null && nextItemTime - currentTime <= 15) {
                currentTime = nextItemTime
            } else {
                currentTime += 15
            }
        }
    }
    
    return slots
}
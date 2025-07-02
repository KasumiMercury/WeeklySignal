package net.mercuryksm.ui

import androidx.compose.foundation.background
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
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem

@Composable
fun DayColumn(
    dayOfWeek: DayOfWeekJp,
    items: List<SignalItem>,
    allItems: List<SignalItem>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    onItemClick: (SignalItem) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day label (fixed section)
        DayLabel(
            dayOfWeek = dayOfWeek,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
        )
        
        // SignalItems timeline (scrollable section)
        DayTimeline(
            items = items,
            allItems = allItems,
            scrollState = scrollState,
            onItemClick = onItemClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DayLabel(
    dayOfWeek: DayOfWeekJp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayOfWeek.getDisplayName(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DayTimeline(
    items: List<SignalItem>,
    allItems: List<SignalItem>,
    scrollState: LazyListState,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeSlots = generateTimeSlots(allItems)
    val itemsMap = items.associateBy { it.getTimeInMinutes() }
    
    LazyRow(
        state = scrollState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(timeSlots) { timeSlot ->
            val item = itemsMap[timeSlot.getTimeInMinutes()]
            
            if (item != null) {
                SignalItemCard(
                    item = item,
                    onClick = onItemClick,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                )
            } else {
                EmptyTimeSlot(
                    modifier = Modifier
                        .width(if (timeSlot.hasItems) 120.dp else 20.dp)
                        .height(80.dp)
                )
            }
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
    
    while (currentTime <= maxTime) {
        val hour = currentTime / 60
        val minute = currentTime % 60
        val hasItems = itemTimes.contains(currentTime)
        
        slots.add(TimeSlot(hour, minute, hasItems))
        
        if (hasItems) {
            currentTime += 30
        } else {
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
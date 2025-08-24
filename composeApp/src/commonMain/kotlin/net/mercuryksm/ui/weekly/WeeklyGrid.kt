package net.mercuryksm.ui.weekly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.weekly.components.timeslot.TimeSlotItem
import net.mercuryksm.ui.weekly.components.timeslot.UITimeSlot
import net.mercuryksm.ui.WeeklyGridConstants
import net.mercuryksm.ui.weekly.components.timeslot.generateTimeSlotItems
import net.mercuryksm.ui.weekly.components.DayCell
import net.mercuryksm.ui.weekly.components.timeslot.TimeSlotHeader

@Composable
fun WeeklyGrid(
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeSlotItems = generateTimeSlotItems(items)
    val scrollState = rememberLazyListState()
    
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Fixed left side with day labels
        DayLabelsColumn()
        
        // Scrollable content area
        ScrollableTimeSlots(
            timeSlotItems = timeSlotItems,
            items = items,
            onItemClick = onItemClick,
            scrollState = scrollState,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScrollableTimeSlots(
    timeSlotItems: List<TimeSlotItem>,
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
    ) {
        items(timeSlotItems) { timeSlotItem ->
            when (timeSlotItem) {
                is TimeSlotItem.TimeSlot -> {
                    TimeSlotWithHeader(
                        timeSlotItem = timeSlotItem,
                        items = items,
                        onItemClick = onItemClick,
                        modifier = Modifier.Companion.width(WeeklyGridConstants.SIGNAL_ITEM_WIDTH)
                    )
                }
                is TimeSlotItem.Spacer -> {
                    TimeSlotSpacer(
                        timeSlotItem = timeSlotItem,
                        modifier = Modifier.width(timeSlotItem.width.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSlotWithHeader(
    timeSlotItem: TimeSlotItem.TimeSlot,
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Time header
        TimeSlotHeader(
            timeSlot = timeSlotItem.uiTimeSlot,
            modifier = Modifier
                .fillMaxWidth()
                .height(WeeklyGridConstants.TIME_HEADER_HEIGHT)
        )
        
        // Time slot column content
        TimeSlotColumn(
            timeSlot = timeSlotItem.uiTimeSlot,
            allItems = items,
            onItemClick = onItemClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TimeSlotSpacer(
    timeSlotItem: TimeSlotItem.Spacer,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Time header with memory marks
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WeeklyGridConstants.TIME_HEADER_HEIGHT),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val numMemoryMarks = (timeSlotItem.width / WeeklyGridConstants.SPACER_WIDTH_PER_INTERVAL).coerceAtLeast(1)
                repeat(numMemoryMarks) { index ->
                    val timeOffset = index * WeeklyGridConstants.TIME_INTERVAL_MINUTES
                    val currentTime = timeSlotItem.startTime + timeOffset
                    val hour = currentTime / 60
                    val minute = currentTime % 60
                    
                    if (index == 0 && numMemoryMarks > 1) {
                        // First mark shows time for longer gaps
                        Text(
                            text = String.format("%02d:%02d", hour, minute),
                            fontSize = WeeklyGridConstants.TIME_LABEL_FONT_SIZE,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Other marks show memory line
                        Text(
                            text = "|",
                            fontSize = WeeklyGridConstants.MEMORY_MARK_FONT_SIZE,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Empty spacer for content area
        Spacer(
            modifier = Modifier.Companion.height(
                WeeklyGridConstants.CELL_TOTAL_HEIGHT * 7 +
                (0.5.dp * 7) // Account for dividers
            )
        )
    }
}

@Composable
private fun TimeSlotColumn(
    timeSlot: UITimeSlot,
    allItems: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTimeInMinutes = timeSlot.getTimeInMinutes()
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seven day cells
        DayOfWeekJp.values().forEach { dayOfWeek ->
            val items = allItems.filter { signalItem ->
                signalItem.timeSlots.any { timeSlot ->
                    timeSlot.dayOfWeek == dayOfWeek && timeSlot.getTimeInMinutes() == currentTimeInMinutes
                }
            }

            DayCell(
                dayOfWeek = dayOfWeek,
                items = items,
                onItemClick = onItemClick,
                timeSlot = timeSlot,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WeeklyGridConstants.CELL_TOTAL_HEIGHT)
            )
        }
    }
}

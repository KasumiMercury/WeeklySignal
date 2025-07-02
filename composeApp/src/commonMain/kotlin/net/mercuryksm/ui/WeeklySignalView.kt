package net.mercuryksm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem

@Composable
fun WeeklySignalView(
    modifier: Modifier = Modifier,
    items: List<SignalItem> = getSampleData(),
    onItemClick: (SignalItem) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Weekly Signal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Content
        if (items.isEmpty()) {
            EmptyState()
        } else {
            WeeklyGrid(
                items = items,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun WeeklyGrid(
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit
) {
    val timeSlots = generateTimeSlots(items)
    val scrollState = rememberLazyListState()
    
    Column {
        
        // Main content area
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fixed day label column
            Column(
                modifier = Modifier.width(60.dp)
            ) {
                DayOfWeekJp.values().forEach { dayOfWeek ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayOfWeek.getDisplayName(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (dayOfWeek != DayOfWeekJp.SUNDAY) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
            
            // Scrollable timeline
            LazyRow(
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(timeSlots) { timeSlot ->
                    TimeSlotColumn(
                        timeSlot = timeSlot,
                        allItems = items,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Signal Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add your first signal to get started",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
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

// Sample data for testing
private fun getSampleData(): List<SignalItem> {
    return listOf(
        SignalItem("1", "Morning Stretch", 7, 0, DayOfWeekJp.MONDAY, "Start your day with exercise"),
        SignalItem("2", "Lunch Prep", 12, 0, DayOfWeekJp.MONDAY, "Time for lunch preparation"),
        SignalItem("3", "Evening Walk", 17, 30, DayOfWeekJp.MONDAY, "Take a relaxing walk"),
        
        SignalItem("4", "Morning Coffee", 8, 0, DayOfWeekJp.TUESDAY, "Coffee break time"),
        SignalItem("5", "Meeting Preparation", 14, 0, DayOfWeekJp.TUESDAY, "Important meeting ahead"),
        SignalItem("6", "Reading Time", 20, 0, DayOfWeekJp.TUESDAY, "Time to read books"),
        
        SignalItem("7", "Yoga Session", 6, 30, DayOfWeekJp.WEDNESDAY, "Morning yoga practice"),
        SignalItem("8", "Lunch Meeting", 12, 30, DayOfWeekJp.WEDNESDAY, "Team lunch meeting"),
        
        SignalItem("9", "Presentation Review", 9, 0, DayOfWeekJp.THURSDAY, "Final check before presentation"),
        SignalItem("10", "Snack Time", 15, 0, DayOfWeekJp.THURSDAY, "Afternoon snack break"),
        SignalItem("11", "Gym Session", 18, 0, DayOfWeekJp.THURSDAY, "Time for workout"),
        
        SignalItem("12", "Weekend Planning", 13, 0, DayOfWeekJp.FRIDAY, "Plan for the weekend"),
        SignalItem("13", "Call Friends", 19, 0, DayOfWeekJp.FRIDAY, "Catch up with friends"),
        
        SignalItem("14", "House Cleaning", 10, 0, DayOfWeekJp.SATURDAY, "Clean the house"),
        SignalItem("15", "Grocery Shopping", 14, 30, DayOfWeekJp.SATURDAY, "Buy groceries"),
        
        SignalItem("16", "Relaxed Breakfast", 9, 30, DayOfWeekJp.SUNDAY, "Enjoy a leisurely breakfast"),
        SignalItem("17", "Family Time", 16, 0, DayOfWeekJp.SUNDAY, "Spend quality time with family")
    )
}
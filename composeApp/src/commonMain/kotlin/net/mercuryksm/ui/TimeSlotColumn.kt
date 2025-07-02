package net.mercuryksm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun TimeSlotColumn(
    timeSlot: TimeSlot,
    allItems: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val width = if (timeSlot.hasItems) 120.dp else 20.dp
    val itemsAtThisTime = allItems.filter { 
        it.getTimeInMinutes() == timeSlot.getTimeInMinutes() 
    }
    
    Column(
        modifier = modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time header
        TimeSlotHeader(
            timeSlot = timeSlot,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )
        
        // Seven day cells
        DayOfWeekJp.values().forEach { dayOfWeek ->
            val item = itemsAtThisTime.find { it.dayOfWeek == dayOfWeek }
            
            DayCell(
                dayOfWeek = dayOfWeek,
                item = item,
                onItemClick = onItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
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

@Composable
private fun DayCell(
    dayOfWeek: DayOfWeekJp,
    item: SignalItem?,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (item != null) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            SignalItemCard(
                item = item,
                onClick = onItemClick,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
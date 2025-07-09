package net.mercuryksm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.WeeklyGridConstants

@Composable
fun TimeSlotColumn(
    timeSlot: UITimeSlot,
    allItems: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val width = if (timeSlot.hasItems) 120.dp else 20.dp
    val currentTimeInMinutes = timeSlot.getTimeInMinutes()
    
    Column(
        modifier = modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time header
        TimeSlotHeader(
            timeSlot = timeSlot,
            modifier = Modifier
                .fillMaxWidth()
                .height(WeeklyGridConstants.TIME_HEADER_HEIGHT)
        )
        
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WeeklyGridConstants.CELL_TOTAL_HEIGHT)
            )
            
            // Add horizontal divider after each day cell
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun TimeSlotHeader(
    timeSlot: UITimeSlot,
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
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                if (items.isNotEmpty()) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                }
            ),
        verticalArrangement = Arrangement.Center
    ) {
        // Top spacing
        Spacer(modifier = Modifier.height(WeeklyGridConstants.CELL_SPACING))
        
        // SignalItems display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WeeklyGridConstants.CELL_CONTENT_HEIGHT),
            contentAlignment = Alignment.Center
        ) {
            if (items.isNotEmpty()) {
                MultipleSignalItemsCell(
                    items = items,
                    dayOfWeek = dayOfWeek,
                    onItemClick = onItemClick,
                    modifier = Modifier.padding(WeeklyGridConstants.CELL_PADDING)
                )
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(WeeklyGridConstants.CELL_SPACING))
    }
}

@Composable
private fun MultipleSignalItemsCell(
    items: List<SignalItem>,
    dayOfWeek: DayOfWeekJp,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }
    
    when (items.size) {
        1 -> {
            // Single item: display at top with consistent height
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                SignalItemCard(
                    item = items.first(),
                    onClick = onItemClick,
                    cornerRadius = RoundedCornerShape(WeeklyGridConstants.CORNER_RADIUS),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Time display at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.TIME_DISPLAY_HEIGHT)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(WeeklyGridConstants.CORNER_RADIUS)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val timeText = items.first().timeSlots.find { it.dayOfWeek == dayOfWeek }?.getTimeDisplayText() ?: "--:--"
                    Text(
                        text = timeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        2 -> {
            // Two items: stack vertically
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(WeeklyGridConstants.ITEM_SPACING)
            ) {
                items.forEachIndexed { index, item ->
                    SignalItemCard(
                        item = item,
                        onClick = onItemClick,
                        cornerRadius = if (index == 0) {
                            RoundedCornerShape(topStart = WeeklyGridConstants.CORNER_RADIUS, topEnd = WeeklyGridConstants.CORNER_RADIUS, bottomStart = 0.dp, bottomEnd = 0.dp)
                        } else {
                            RoundedCornerShape(0.dp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
                    )
                }
                
                // Time display at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.TIME_DISPLAY_HEIGHT)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = WeeklyGridConstants.CORNER_RADIUS, bottomEnd = WeeklyGridConstants.CORNER_RADIUS)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val timeText = items.first().timeSlots.find { it.dayOfWeek == dayOfWeek }?.getTimeDisplayText() ?: "--:--"
                    Text(
                        text = timeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            // Three or more items: show first item + button for additional items
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(WeeklyGridConstants.ITEM_SPACING)
            ) {
                // First item (smallest ID)
                val firstItem = items.minByOrNull { it.id } ?: items.first()
                SignalItemCard(
                    item = firstItem,
                    onClick = onItemClick,
                    cornerRadius = RoundedCornerShape(topStart = WeeklyGridConstants.CORNER_RADIUS, topEnd = WeeklyGridConstants.CORNER_RADIUS, bottomStart = 0.dp, bottomEnd = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
                )
                
                // Additional items button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .clickable { showModal = true }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${items.size - 1}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                // Time display at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WeeklyGridConstants.TIME_DISPLAY_HEIGHT)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = WeeklyGridConstants.CORNER_RADIUS, bottomEnd = WeeklyGridConstants.CORNER_RADIUS)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val timeText = items.first().timeSlots.find { it.dayOfWeek == dayOfWeek }?.getTimeDisplayText() ?: "--:--"
                    Text(
                        text = timeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Modal for showing additional items
    if (showModal) {
        SignalItemsModal(
            items = items,
            dayOfWeek = dayOfWeek,
            onItemClick = { item ->
                showModal = false
                onItemClick(item)
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
private fun SignalItemsModal(
    items: List<SignalItem>,
    dayOfWeek: DayOfWeekJp,
    onItemClick: (SignalItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select SignalItem",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        SignalItemCard(
                            item = item,
                            onClick = onItemClick,
                            showTime = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
                        )
                    }
                }
                
                // Unified time and day display at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(WeeklyGridConstants.CORNER_RADIUS)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val timeText = items.first().timeSlots.find { it.dayOfWeek == dayOfWeek }?.getTimeDisplayText() ?: "--:--"
                    Text(
                        text = "${dayOfWeek.getDisplayName()} ${timeText}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
package net.mercuryksm.ui.weekly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.WeeklyGridConstants
import net.mercuryksm.ui.components.SignalItemCard

@Composable
fun DayCell(
    dayOfWeek: DayOfWeekJp,
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier,
    timeSlot: net.mercuryksm.ui.weekly.components.timeslot.UITimeSlot? = null
) {
    Column(
        modifier = modifier
            .background(
                if (items.isNotEmpty()) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                }
            )
            .clickable { /* Empty cells are now clickable for better touch feedback */ },
        verticalArrangement = Arrangement.Center
    ) {
        // Top spacing
        Spacer(modifier = Modifier.Companion.height(WeeklyGridConstants.CELL_SPACING))
        
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
                    timeSlot = timeSlot,
                    modifier = Modifier.Companion.padding(WeeklyGridConstants.CELL_PADDING)
                )
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.Companion.height(WeeklyGridConstants.CELL_SPACING))
    }
}

@Composable
private fun MultipleSignalItemsCell(
    items: List<SignalItem>,
    dayOfWeek: DayOfWeekJp,
    onItemClick: (SignalItem) -> Unit,
    modifier: Modifier = Modifier,
    timeSlot: net.mercuryksm.ui.weekly.components.timeslot.UITimeSlot? = null
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
                    val timeText = timeSlot?.getDisplayText() ?: "--:--"
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
                            RoundedCornerShape(
                                topStart = WeeklyGridConstants.CORNER_RADIUS,
                                topEnd = WeeklyGridConstants.CORNER_RADIUS,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
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
                    val timeText = timeSlot?.getDisplayText() ?: "--:--"
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
                    cornerRadius = RoundedCornerShape(
                        topStart = WeeklyGridConstants.CORNER_RADIUS,
                        topEnd = WeeklyGridConstants.CORNER_RADIUS,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
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
                    val timeText = timeSlot?.getDisplayText() ?: "--:--"
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

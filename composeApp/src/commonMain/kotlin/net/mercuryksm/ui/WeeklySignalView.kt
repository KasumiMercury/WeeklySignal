package net.mercuryksm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.WeeklyGridConstants

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySignalView(
    viewModel: WeeklySignalViewModel,
    modifier: Modifier = Modifier,
    onAddSignalClick: () -> Unit = {},
    onItemClick: (SignalItem) -> Unit = {}
) {
    val items by viewModel.signalItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weekly Signal",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSignalClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Signal"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Content
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (items.isEmpty()) {
                    EmptyState()
                } else {
                    WeeklyGrid(
                        items = items,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
    
    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
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
                modifier = Modifier.width(WeeklyGridConstants.DAY_LABEL_WIDTH)
            ) {
                // Spacer to align with time header in TimeSlotColumn
                Spacer(modifier = Modifier.height(WeeklyGridConstants.TIME_HEADER_HEIGHT))
                
                DayOfWeekJp.values().forEach { dayOfWeek ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WeeklyGridConstants.CELL_TOTAL_HEIGHT)
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
                text = "Tap the + button to add your first signal",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun generateTimeSlots(allItems: List<SignalItem>): List<UITimeSlot> {
    if (allItems.isEmpty()) {
        return emptyList()
    }
    
    val itemTimes = allItems.flatMap { signalItem ->
        signalItem.timeSlots.map { it.getTimeInMinutes() }
    }.toSet()
    
    val minTime = itemTimes.minOrNull() ?: 0
    val maxTime = itemTimes.maxOrNull() ?: 1440
    
    val slots = mutableListOf<UITimeSlot>()
    var currentTime = minTime
    
    while (currentTime <= maxTime) {
        val hour = currentTime / 60
        val minute = currentTime % 60
        val hasItems = itemTimes.contains(currentTime)
        
        slots.add(UITimeSlot(hour, minute, hasItems))
        
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


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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.WeeklyGridConstants

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
    val timeSlotItems = generateTimeSlotItems(items)
    val scrollState = rememberLazyListState()
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Fixed left side with day labels
        Column(
            modifier = Modifier.width(WeeklyGridConstants.DAY_LABEL_WIDTH)
        ) {
            // Empty spacer for time header row
            Spacer(
                modifier = Modifier.height(WeeklyGridConstants.TIME_HEADER_HEIGHT)
            )
            
            // Day labels
            DayOfWeekJp.values().forEach { dayOfWeek ->
                Box(
                    modifier = Modifier
                        .width(WeeklyGridConstants.DAY_LABEL_WIDTH)
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
                
                // Divider after each day
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
        }
        
        // Scrollable content area
        LazyRow(
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(timeSlotItems) { timeSlotItem ->
                when (timeSlotItem) {
                    is TimeSlotItem.TimeSlot -> {
                        Column(
                            modifier = Modifier.width(WeeklyGridConstants.SIGNAL_ITEM_WIDTH)
                        ) {
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
                    is TimeSlotItem.Spacer -> {
                        Column(
                            modifier = Modifier.width(timeSlotItem.width.dp)
                        ) {
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
                                modifier = Modifier.height(
                                    WeeklyGridConstants.CELL_TOTAL_HEIGHT * 7 + 
                                    (0.5.dp * 7) // Account for dividers
                                )
                            )
                        }
                    }
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



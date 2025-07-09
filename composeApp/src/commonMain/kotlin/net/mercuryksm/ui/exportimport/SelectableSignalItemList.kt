package net.mercuryksm.ui.exportimport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.mercuryksm.data.*

@Composable
fun SelectableSignalItemList(
    selectionState: ExportSelectionState,
    onSignalItemSelectionChanged: (String) -> Unit,
    onTimeSlotSelectionChanged: (String, String) -> Unit,
    onSignalItemExpansionChanged: (String) -> Unit,
    onSelectAllChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Select All Header
        SelectAllHeader(
            isAllSelected = selectionState.isAllSelected,
            hasSelection = selectionState.hasSelection,
            selectedItemCount = selectionState.selectedItemCount,
            totalItemCount = selectionState.signalItemSelections.size,
            onSelectAllChanged = onSelectAllChanged
        )
        
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        
        // SignalItem List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectionState.signalItemSelections.forEach { selection ->
                SelectableSignalItemCard(
                    selectionState = selection,
                    onSignalItemSelectionChanged = onSignalItemSelectionChanged,
                    onTimeSlotSelectionChanged = onTimeSlotSelectionChanged,
                    onExpansionChanged = onSignalItemExpansionChanged
                )
            }
        }
    }
}

@Composable
private fun SelectAllHeader(
    isAllSelected: Boolean,
    hasSelection: Boolean,
    selectedItemCount: Int,
    totalItemCount: Int,
    onSelectAllChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = onSelectAllChanged
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Select All",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Selection Summary
            Text(
                text = "$selectedItemCount of $totalItemCount selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectableSignalItemCard(
    selectionState: SignalItemSelectionState,
    onSignalItemSelectionChanged: (String) -> Unit,
    onTimeSlotSelectionChanged: (String, String) -> Unit,
    onExpansionChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionState.isSelected || selectionState.isPartiallySelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column {
            // SignalItem Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpansionChanged(selectionState.signalItem.id) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Selection Checkbox
                    Checkbox(
                        checked = selectionState.isSelected,
                        onCheckedChange = { onSignalItemSelectionChanged(selectionState.signalItem.id) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Color Indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(selectionState.signalItem.color))
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // SignalItem Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = selectionState.signalItem.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (selectionState.signalItem.description.isNotEmpty()) {
                            Text(
                                text = selectionState.signalItem.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Selection Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectionState.isSelected) {
                                    "${selectionState.signalItem.timeSlots.size} time slots (all selected)"
                                } else if (selectionState.isPartiallySelected) {
                                    "${selectionState.selectedTimeSlotCount} of ${selectionState.signalItem.timeSlots.size} time slots selected"
                                } else {
                                    "${selectionState.signalItem.timeSlots.size} time slots"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Expand/Collapse Icon
                IconButton(
                    onClick = { onExpansionChanged(selectionState.signalItem.id) }
                ) {
                    Icon(
                        imageVector = if (selectionState.isExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (selectionState.isExpanded) "Collapse" else "Expand"
                    )
                }
            }
            
            // TimeSlot List (when expanded)
            AnimatedVisibility(
                visible = selectionState.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        text = "Time Slots",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    selectionState.timeSlotSelections.forEach { timeSlotSelection ->
                        SelectableTimeSlotItem(
                            timeSlotSelection = timeSlotSelection,
                            onSelectionChanged = { 
                                onTimeSlotSelectionChanged(
                                    selectionState.signalItem.id,
                                    timeSlotSelection.timeSlot.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableTimeSlotItem(
    timeSlotSelection: TimeSlotSelectionState,
    onSelectionChanged: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = timeSlotSelection.isSelected,
            onCheckedChange = { onSelectionChanged() }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = "${timeSlotSelection.timeSlot.dayOfWeek.getShortDisplayName()} ${
                    String.format(
                        "%02d:%02d",
                        timeSlotSelection.timeSlot.hour,
                        timeSlotSelection.timeSlot.minute
                    )
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SelectionSummary(
    selectionState: ExportSelectionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selection Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• ${selectionState.selectedItemCount} items selected",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "• ${selectionState.selectedTimeSlotCount} time slots total",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (selectionState.hasSelection) {
                Text(
                    text = "• Ready to export",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "• Select items to export",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
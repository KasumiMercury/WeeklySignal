package net.mercuryksm.ui.weekly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import net.mercuryksm.ui.components.SignalItemCard
import net.mercuryksm.data.TimeSlot
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SignalItemsModal(
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

@Preview
@Composable
private fun SignalItemsModalPreview() {
    MaterialTheme {
        SignalItemsModal(
            items = signalItemsModalPreviewItems(),
            dayOfWeek = DayOfWeekJp.MONDAY,
            onItemClick = {},
            onDismiss = {}
        )
    }
}

private fun signalItemsModalPreviewItems(): List<SignalItem> {
    return listOf(
        SignalItem(
            id = "modal-preview-1",
            name = "Morning Routine",
            description = "Stretch and hydrate",
            color = 0xFF81C784,
            timeSlots = listOf(
                TimeSlot(
                    id = "modal-preview-1-mon",
                    hour = 7,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            )
        ),
        SignalItem(
            id = "modal-preview-2",
            name = "Evening Review",
            description = "Plan tomorrow",
            color = 0xFFFFB74D,
            timeSlots = listOf(
                TimeSlot(
                    id = "modal-preview-2-mon",
                    hour = 20,
                    minute = 0,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            )
        )
    )
}

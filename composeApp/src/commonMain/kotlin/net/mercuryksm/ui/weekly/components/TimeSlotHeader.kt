package net.mercuryksm.ui.weekly.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import net.mercuryksm.ui.UITimeSlot
import net.mercuryksm.ui.WeeklyGridConstants

@Composable
fun TimeSlotHeader(
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
                fontSize = WeeklyGridConstants.MEMORY_MARK_FONT_SIZE,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

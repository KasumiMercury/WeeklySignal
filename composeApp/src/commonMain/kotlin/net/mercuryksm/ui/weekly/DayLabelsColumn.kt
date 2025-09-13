package net.mercuryksm.ui.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.ui.WeeklyGridConstants

@Composable
fun DayLabelsColumn(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(WeeklyGridConstants.DAY_LABEL_WIDTH)
    ) {
        // Empty spacer for time header row
        Spacer(
            modifier = Modifier.Companion.height(WeeklyGridConstants.TIME_HEADER_HEIGHT)
        )
        
        // Day labels
        DayOfWeekJp.values().forEach { dayOfWeek ->
            Box(
                modifier = Modifier.Companion
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
}

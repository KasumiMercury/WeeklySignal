package net.mercuryksm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.ui.WeeklyGridConstants

@Composable
fun SignalItemCard(
    item: SignalItem,
    modifier: Modifier = Modifier,
    onClick: (SignalItem) -> Unit = {},
    showTime: Boolean = false,
    cornerRadius: RoundedCornerShape = RoundedCornerShape(WeeklyGridConstants.CORNER_RADIUS)
) {
    Card(
        modifier = modifier
            .width(WeeklyGridConstants.SIGNAL_ITEM_WIDTH)
            .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT),
        shape = cornerRadius,
        colors = CardDefaults.cardColors(
            containerColor = Color(item.color),
            contentColor = Color.White
        ),
        onClick = { onClick(item) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = if (showTime) Arrangement.SpaceBetween else Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.getTruncatedName(6),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            if (showTime) {
                Text(
                    text = if (item.timeSlots.isNotEmpty()) {
                        item.timeSlots.first().getTimeDisplayText()
                    } else {
                        "--:--"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyTimeSlot(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .width(20.dp)
            .height(WeeklyGridConstants.SIGNAL_ITEM_HEIGHT)
    )
}

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

@Composable
fun SignalItemCard(
    item: SignalItem,
    modifier: Modifier = Modifier,
    onClick: (SignalItem) -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.getTruncatedName(10),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = if (item.timeSlots.isNotEmpty()) {
                    item.timeSlots.first().getTimeDisplayText()
                } else {
                    "--:--"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
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
            .height(80.dp)
    )
}

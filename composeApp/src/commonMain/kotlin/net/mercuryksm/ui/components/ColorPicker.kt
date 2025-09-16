package net.mercuryksm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

object ColorPalette {
    val predefinedColors = listOf(
        0xFF6750A4L, // Material Primary
        0xFFE91E63L, // Pink
        0xFF2196F3L, // Blue
        0xFF4CAF50L, // Green
        0xFFFF9800L, // Orange
        0xFF9C27B0L, // Purple
        0xFFFF5722L, // Deep Orange
        0xFF607D8BL, // Blue Grey
        0xFF795548L, // Brown
        0xFF009688L, // Teal
        0xFFFFEB3BL, // Yellow
        0xFFF44336L  // Red
    )
}

@Preview
@Composable
private fun ColorPickerPreview() {
    MaterialTheme {
        ColorPicker(
            selectedColor = ColorPalette.predefinedColors.first(),
            onColorSelected = {}
        )
    }
}

@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ColorPalette.predefinedColors) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ColorItemPreview() {
    MaterialTheme {
        ColorItem(
            color = ColorPalette.predefinedColors.first(),
            isSelected = true,
            onClick = {}
        )
    }
}

@Composable
private fun ColorItem(
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) 
        MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 3.dp else 0.dp
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(
                BorderStroke(borderWidth, borderColor),
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

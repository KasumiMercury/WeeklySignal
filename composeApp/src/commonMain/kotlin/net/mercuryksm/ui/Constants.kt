package net.mercuryksm.ui

import androidx.compose.ui.unit.dp

object WeeklyGridConstants {
    // SignalItem dimensions
    val SIGNAL_ITEM_WIDTH = 120.dp
    val SIGNAL_ITEM_HEIGHT = 40.dp
    
    // Time display dimensions
    val TIME_DISPLAY_HEIGHT = 28.dp
    
    // Padding and spacing
    val CELL_PADDING = 2.dp
    val CELL_SPACING = 2.dp
    val ITEM_SPACING = 1.dp
    
    // Cell dimensions
    val CELL_CONTENT_HEIGHT = (SIGNAL_ITEM_HEIGHT * 2) + ITEM_SPACING + TIME_DISPLAY_HEIGHT
    val CELL_TOTAL_HEIGHT = CELL_CONTENT_HEIGHT + (CELL_SPACING * 2) // Content + top/bottom spacing
    
    // Time header
    val TIME_HEADER_HEIGHT = 40.dp
    
    // Day label column
    val DAY_LABEL_WIDTH = 60.dp
    
    // Corner radius
    val CORNER_RADIUS = 8.dp
}
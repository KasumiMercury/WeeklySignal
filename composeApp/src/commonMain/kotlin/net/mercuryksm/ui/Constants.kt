package net.mercuryksm.ui

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WeeklyGridConstants {
    // SignalItem dimensions
    val SIGNAL_ITEM_WIDTH = 120.dp
    val SIGNAL_ITEM_HEIGHT = 44.dp
    
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
    
    // Time slot generation constants
    const val TIME_INTERVAL_MINUTES = 15
    const val SPACER_WIDTH_PER_INTERVAL = 40 // dp per 15-minute interval
    const val DEFAULT_START_HOUR = 8
    const val DEFAULT_END_HOUR = 22
    const val MAX_MINUTES_PER_DAY = 1440 // 24 * 60
    
    // Time label font sizes
    val TIME_LABEL_FONT_SIZE = 8.sp
    val MEMORY_MARK_FONT_SIZE = 8.sp
}
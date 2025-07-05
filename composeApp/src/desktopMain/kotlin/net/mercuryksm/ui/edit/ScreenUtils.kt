package net.mercuryksm.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@Composable
actual fun shouldUseCompactTimeInput(): Boolean {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    
    // Convert window height to dp
    val windowHeightDp = with(density) { windowInfo.containerSize.height.toDp() }
    
    // Use TimeInput instead of TimePicker when window height is limited
    return windowHeightDp < 600.dp
}
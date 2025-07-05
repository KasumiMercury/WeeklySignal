package net.mercuryksm.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
actual fun shouldUseCompactTimeInput(): Boolean {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightDp = with(density) { configuration.screenHeightDp.dp }
    
    // Use TimeInput instead of TimePicker when screen height is limited
    return screenHeightDp < 600.dp
}
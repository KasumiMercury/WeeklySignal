package net.mercuryksm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import net.mercuryksm.ui.WeeklySignalView

@Composable
@Preview
fun App() {
    MaterialTheme {
        WeeklySignalView(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            onItemClick = { item ->
                // TODO: Implement navigation to detail page here
                println("Signal item clicked: ${item.name}")
            }
        )
    }
}
package net.mercuryksm

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import net.mercuryksm.data.database.DatabaseServiceFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "WeeklySignal",
        state = WindowState(
            width = 1200.dp,
            height = 800.dp
        )
    ) {
        val databaseService = DatabaseServiceFactory().createSignalDatabaseService()
        App(databaseService)
    }
}
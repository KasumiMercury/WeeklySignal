package net.mercuryksm

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.mercuryksm.data.database.DatabaseServiceFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "WeeklySignal",
    ) {
        val databaseService = DatabaseServiceFactory().createSignalDatabaseService()
        App(databaseService)
    }
}
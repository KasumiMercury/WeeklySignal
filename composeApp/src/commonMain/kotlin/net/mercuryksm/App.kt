package net.mercuryksm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import net.mercuryksm.navigation.NavGraph
import net.mercuryksm.ui.WeeklySignalViewModel
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.data.database.SignalDatabaseService
import net.mercuryksm.notification.SignalNotificationManager
import net.mercuryksm.notification.createNotificationServiceFactory

@Composable
@Preview
fun App(
    databaseService: SignalDatabaseService? = null,
    notificationManager: SignalNotificationManager? = null
) {
    MaterialTheme {
        val navController = rememberNavController()
        val repository = remember(databaseService) { SignalRepository(databaseService) }
        val viewModel: WeeklySignalViewModel = viewModel { WeeklySignalViewModel(repository) }
        val notificationService = remember(notificationManager) { 
            notificationManager ?: try {
                createNotificationServiceFactory().createNotificationManager()
            } catch (e: Exception) {
                null
            }
        }
        
        NavGraph(
            navController = navController,
            viewModel = viewModel,
            notificationManager = notificationService
        )
    }
}
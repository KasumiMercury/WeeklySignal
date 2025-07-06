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
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.notification.createAlarmServiceFactory

@Composable
@Preview
fun App(
    databaseService: SignalDatabaseService? = null,
    alarmManager: SignalAlarmManager? = null
) {
    MaterialTheme {
        val navController = rememberNavController()
        val repository = remember(databaseService) { SignalRepository(databaseService) }
        val alarmService = remember(alarmManager) { 
            alarmManager ?: try {
                createAlarmServiceFactory().createAlarmManager()
            } catch (e: Exception) {
                null
            }
        }
        val viewModel: WeeklySignalViewModel = viewModel { WeeklySignalViewModel(repository, alarmService) }
        
        NavGraph(
            navController = navController,
            viewModel = viewModel,
            alarmManager = alarmService
        )
    }
}
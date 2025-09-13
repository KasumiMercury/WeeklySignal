package net.mercuryksm

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import net.mercuryksm.data.SignalRepository
import net.mercuryksm.data.database.SignalDatabaseService
import net.mercuryksm.navigation.NavGraph
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.notification.createAlarmServiceFactory
import net.mercuryksm.ui.startup.StartupPermissionCheck
import net.mercuryksm.ui.weekly.WeeklySignalViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    databaseService: SignalDatabaseService? = null,
    alarmManager: SignalAlarmManager? = null
) {
    MaterialTheme {
        var permissionCheckCompleted by remember { mutableStateOf(false) }
        
        if (!permissionCheckCompleted) {
            StartupPermissionCheck(
                onPermissionCheckComplete = {
                    permissionCheckCompleted = true
                }
            )
        } else {
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
}

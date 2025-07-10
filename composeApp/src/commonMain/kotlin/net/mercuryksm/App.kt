package net.mercuryksm

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import net.mercuryksm.data.database.SignalDatabaseService
import net.mercuryksm.di.appModule
import net.mercuryksm.navigation.NavGraph
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.ui.startup.StartupPermissionCheck
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.context.GlobalContext

@Composable
@Preview
fun App(
    databaseService: SignalDatabaseService? = null,
    alarmManager: SignalAlarmManager? = null
) {
    KoinApplication(application = {
        if (GlobalContext.getOrNull() == null) {
            modules(appModule)
        }
    }) {
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
                
                NavGraph(
                    navController = navController
                )
            }
        }
    }
}

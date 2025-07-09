package net.mercuryksm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import net.mercuryksm.notification.SignalAlarmManager
import net.mercuryksm.ui.weekly.WeeklySignalView
import net.mercuryksm.ui.edit.SignalEditScreen
import net.mercuryksm.ui.registration.SignalRegistrationScreen
import net.mercuryksm.ui.weekly.WeeklySignalViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: WeeklySignalViewModel,
    alarmManager: SignalAlarmManager? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WeeklySignal.route
    ) {
        composable(Screen.WeeklySignal.route) {
            WeeklySignalView(
                viewModel = viewModel,
                onAddSignalClick = {
                    navController.navigate(Screen.SignalRegistration.route)
                },
                onItemClick = { signalItem ->
                    navController.navigate(Screen.SignalEdit.createRoute(signalItem.id))
                }
            )
        }
        
        composable(Screen.SignalRegistration.route) {
            SignalRegistrationScreen(
                onSignalSaved = { signalItem, onResult ->
                    viewModel.addSignalItem(signalItem) { result ->
                        onResult(result)
                        if (result.isSuccess) {
                            navController.popBackStack()
                        }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                },
                alarmManager = alarmManager
            )
        }
        
        composable(Screen.SignalEdit.route) { backStackEntry ->
            val signalId = backStackEntry.arguments?.getString("signalId") ?: ""
            SignalEditScreen(
                viewModel = viewModel,
                signalId = signalId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                alarmManager = alarmManager
            )
        }
    }
}

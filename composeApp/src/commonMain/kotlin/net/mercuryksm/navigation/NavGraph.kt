package net.mercuryksm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import net.mercuryksm.ui.WeeklySignalView
import net.mercuryksm.ui.registration.SignalRegistrationScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: net.mercuryksm.ui.WeeklySignalViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WeeklySignal.route
    ) {
        composable(Screen.WeeklySignal.route) {
            WeeklySignalView(
                onAddSignalClick = {
                    navController.navigate(Screen.SignalRegistration.route)
                }
            )
        }
        
        composable(Screen.SignalRegistration.route) {
            SignalRegistrationScreen(
                onSignalSaved = { signalItem ->
                    viewModel.addSignalItem(signalItem)
                    navController.popBackStack()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
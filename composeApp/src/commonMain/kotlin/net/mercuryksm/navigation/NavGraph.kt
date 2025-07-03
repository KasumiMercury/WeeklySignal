package net.mercuryksm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import net.mercuryksm.ui.WeeklySignalView
import net.mercuryksm.ui.registration.SignalRegistrationScreen
import net.mercuryksm.ui.edit.SignalEditScreen

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
                }
            )
        }
        
        composable(Screen.SignalEdit.route) { backStackEntry ->
            val signalId = backStackEntry.arguments?.getString("signalId") ?: ""
            SignalEditScreen(
                viewModel = viewModel,
                signalId = signalId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
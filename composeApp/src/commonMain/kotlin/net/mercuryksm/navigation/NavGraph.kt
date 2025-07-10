package net.mercuryksm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import net.mercuryksm.ui.weekly.WeeklySignalView
import net.mercuryksm.ui.edit.SignalEditScreen
import net.mercuryksm.ui.registration.SignalRegistrationScreen
import net.mercuryksm.ui.exportimport.ExportImportScreen
import net.mercuryksm.ui.exportimport.ExportSelectionScreen
import net.mercuryksm.ui.exportimport.ImportSelectionScreen
import net.mercuryksm.ui.weekly.WeeklySignalViewModel
import net.mercuryksm.ui.exportimport.ExportImportViewModel
import org.koin.compose.koinInject

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val weeklySignalViewModel: WeeklySignalViewModel = koinInject()
    NavHost(
        navController = navController,
        startDestination = Screen.WeeklySignal.route
    ) {
        composable(Screen.WeeklySignal.route) {
            WeeklySignalView(
                viewModel = weeklySignalViewModel,
                onAddSignalClick = {
                    navController.navigate(Screen.SignalRegistration.route)
                },
                onItemClick = { signalItem ->
                    navController.navigate(Screen.SignalEdit.createRoute(signalItem.id))
                },
                onExportImportClick = {
                    navController.navigate(Screen.ExportImport.route)
                }
            )
        }
        
        composable(Screen.SignalRegistration.route) {
            SignalRegistrationScreen(
                onSignalSaved = { signalItem, onResult ->
                    weeklySignalViewModel.addSignalItem(signalItem) { result ->
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
                viewModel = weeklySignalViewModel,
                signalId = signalId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ExportImport.route) {
            ExportImportScreen(
                weeklyViewModel = weeklySignalViewModel,
                onBackPressed = {
                    navController.popBackStack()
                },
                onNavigateToExportSelection = {
                    navController.navigate(Screen.ExportSelection.route)
                },
                onNavigateToImportSelection = {
                    navController.navigate(Screen.ImportSelection.route)
                }
            )
        }
        
        composable(Screen.ExportSelection.route) {
            ExportSelectionScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                onExportSelected = { selectionState ->
                    // Pass the selection state back to the ExportImportScreen
                    // The ExportImportViewModel will be accessed via koinViewModel() inside ExportSelectionScreen
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ImportSelection.route) {
            val exportImportViewModel: ExportImportViewModel = koinInject()
            val importedItems by exportImportViewModel.importedItems.collectAsStateWithLifecycle()
            ImportSelectionScreen(
                importedItems = importedItems,
                onBackPressed = {
                    navController.popBackStack()
                },
                onImportCompleted = { result ->
                    // Handle import completion and navigate back
                    navController.popBackStack()
                }
            )
        }
    }
}

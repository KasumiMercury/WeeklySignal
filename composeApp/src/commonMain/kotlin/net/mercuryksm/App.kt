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

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val viewModel: WeeklySignalViewModel = viewModel { WeeklySignalViewModel() }
        
        NavGraph(
            navController = navController,
            viewModel = viewModel
        )
    }
}
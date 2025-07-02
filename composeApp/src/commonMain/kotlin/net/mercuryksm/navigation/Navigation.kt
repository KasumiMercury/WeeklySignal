package net.mercuryksm.navigation

sealed class Screen(val route: String) {
    data object WeeklySignal : Screen("weekly_signal")
    data object SignalRegistration : Screen("signal_registration")
    data object SignalEdit : Screen("signal_edit/{signalId}") {
        fun createRoute(signalId: String) = "signal_edit/$signalId"
    }
}
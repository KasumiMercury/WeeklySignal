package net.mercuryksm.notification

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPermissionHelper(): PermissionHelper? {
    val context = LocalContext.current
    return remember {
        if (context is ComponentActivity) {
            NotificationPermissionHelper(context)
        } else {
            null
        }
    }
}
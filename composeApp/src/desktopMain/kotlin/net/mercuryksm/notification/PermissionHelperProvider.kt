package net.mercuryksm.notification

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionHelper(): PermissionHelper? {
    // Desktop doesn't need permission handling yet
    return null
}
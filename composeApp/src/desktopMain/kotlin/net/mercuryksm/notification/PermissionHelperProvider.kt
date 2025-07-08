package net.mercuryksm.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPermissionHelper(): PermissionHelper? {
    // Desktop always has permissions granted for future alarm support
    return remember { DesktopPermissionHelper() }
}

class DesktopPermissionHelper : PermissionHelper {
    override fun hasNotificationPermission(): Boolean = true
    override fun hasAlarmPermission(): Boolean = true
    override fun hasAllPermissions(): Boolean = true
    override fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        callback(true) // Always granted on desktop
    }
    override fun requestAlarmPermission(): Boolean = true
}
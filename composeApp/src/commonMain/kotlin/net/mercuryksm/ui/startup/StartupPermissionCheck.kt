package net.mercuryksm.ui.startup

import androidx.compose.runtime.*
import net.mercuryksm.notification.StartupPermissionDialog
import net.mercuryksm.notification.PermissionHelper
import net.mercuryksm.notification.rememberPermissionHelper

@Composable
fun StartupPermissionCheck(
    onPermissionCheckComplete: () -> Unit
) {
    val permissionHelper = rememberPermissionHelper()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionCheckCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(permissionHelper) {
        if (permissionHelper != null) {
            // Check if permissions are already granted
            if (permissionHelper.hasAllPermissions()) {
                permissionCheckCompleted = true
                onPermissionCheckComplete()
            } else {
                // Show permission dialog on startup
                showPermissionDialog = true
            }
        } else {
            // No permission helper means no permissions needed (desktop without implementation)
            permissionCheckCompleted = true
            onPermissionCheckComplete()
        }
    }

    if (showPermissionDialog && !permissionCheckCompleted) {
        StartupPermissionDialog(
            showDialog = true,
            onDismiss = {
                showPermissionDialog = false
                permissionCheckCompleted = true
                onPermissionCheckComplete()
            },
            onRequestPermission = {
                permissionHelper?.requestNotificationPermission { granted ->
                    if (granted && permissionHelper.hasAlarmPermission()) {
                        showPermissionDialog = false
                        permissionCheckCompleted = true
                        onPermissionCheckComplete()
                    } else if (!permissionHelper.hasAlarmPermission()) {
                        // If notification permission is granted but alarm permission is not
                        val alarmGranted = permissionHelper.requestAlarmPermission()
                        if (alarmGranted) {
                            showPermissionDialog = false
                            permissionCheckCompleted = true
                            onPermissionCheckComplete()
                        } else {
                            // Let user proceed even without alarm permission
                            showPermissionDialog = false
                            permissionCheckCompleted = true
                            onPermissionCheckComplete()
                        }
                    } else {
                        // User declined, let them proceed anyway
                        showPermissionDialog = false
                        permissionCheckCompleted = true
                        onPermissionCheckComplete()
                    }
                }
            }
        )
    }
}
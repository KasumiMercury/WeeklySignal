package net.mercuryksm.notification

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StartupPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Setup Permissions",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "WeeklySignal needs notification and alarm permissions to function properly. " +
                            "These permissions allow the app to send you scheduled notifications and set alarms."
                )
            },
            confirmButton = {
                TextButton(onClick = onRequestPermission) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Skip for Now")
                }
            }
        )
    }
}
package net.mercuryksm.notification

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NotificationPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Notification Permission Required",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "To preview notifications, WeeklySignal needs permission to send notifications. " +
                            "This will allow you to test how your signal notifications will appear."
                )
            },
            confirmButton = {
                TextButton(onClick = onRequestPermission) {
                    Text("Allow Notifications")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

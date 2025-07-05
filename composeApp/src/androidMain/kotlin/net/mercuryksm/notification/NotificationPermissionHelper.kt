package net.mercuryksm.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val activity: ComponentActivity
) {
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionCallback?.invoke(isGranted)
        permissionCallback = null
    }
    
    fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionCallback = callback
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            callback(true) // Permissions are granted by default on older versions
        }
    }
    
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

@Composable
fun rememberNotificationPermissionHelper(): NotificationPermissionHelper? {
    val context = LocalContext.current
    return remember {
        if (context is ComponentActivity) {
            NotificationPermissionHelper(context)
        } else {
            null
        }
    }
}
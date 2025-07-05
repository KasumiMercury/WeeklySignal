package net.mercuryksm

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.mercuryksm.data.database.DatabaseServiceFactory
import net.mercuryksm.data.database.setDatabaseContext
import net.mercuryksm.notification.AndroidSignalNotificationManagerSafe

class MainActivity : ComponentActivity() {
    
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionCallback?.invoke(isGranted)
        permissionCallback = null
    }
    
    private fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionCallback = callback
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            callback(true) // Permissions are granted by default on older versions
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize database context
        setDatabaseContext(this)

        setContent {
            val databaseService = DatabaseServiceFactory(this@MainActivity).createSignalDatabaseService()
            val notificationManager = AndroidSignalNotificationManagerSafe(
                context = this@MainActivity,
                requestPermission = ::requestNotificationPermission
            )
            App(databaseService, notificationManager)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
package net.mercuryksm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.mercuryksm.data.database.DatabaseServiceFactory
import net.mercuryksm.data.database.setDatabaseContext
import net.mercuryksm.notification.createNotificationServiceFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize database context
        setDatabaseContext(this)

        setContent {
            val databaseService = DatabaseServiceFactory(this@MainActivity).createSignalDatabaseService()
            val notificationManager = createNotificationServiceFactory(this@MainActivity).createNotificationManager()
            App(databaseService, notificationManager)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
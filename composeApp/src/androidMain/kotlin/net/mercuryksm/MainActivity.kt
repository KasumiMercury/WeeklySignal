package net.mercuryksm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.mercuryksm.data.database.DatabaseServiceFactory
import net.mercuryksm.data.database.setDatabaseContext
import net.mercuryksm.notification.AndroidSignalAlarmManager

class MainActivity : ComponentActivity() {
    
    // Permission handling is now done by the AlarmManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize database context
        setDatabaseContext(this)

        setContent {
            val databaseService = DatabaseServiceFactory(this@MainActivity).createSignalDatabaseService()
            val alarmManager = AndroidSignalAlarmManager(
                context = this@MainActivity
            )
            App(databaseService, alarmManager)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
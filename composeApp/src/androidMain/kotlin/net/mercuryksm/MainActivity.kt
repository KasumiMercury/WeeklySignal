package net.mercuryksm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.mercuryksm.data.database.DatabaseServiceFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val databaseService = DatabaseServiceFactory(this).createSignalDatabaseService()
            App(databaseService)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
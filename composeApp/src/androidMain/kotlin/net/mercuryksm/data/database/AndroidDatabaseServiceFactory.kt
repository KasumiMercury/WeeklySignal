package net.mercuryksm.data.database

import android.content.Context

actual class DatabaseServiceFactory(private val context: Context) {
    actual fun createSignalDatabaseService(): SignalDatabaseService {
        val databaseRepository = AndroidDatabaseRepository(context)
        return AndroidSignalDatabaseService(databaseRepository)
    }
}
package net.mercuryksm.notification

import android.content.Context
import net.mercuryksm.data.database.DatabaseServiceFactory

class AndroidAlarmServiceFactory(
    private val context: Context
) : AlarmServiceFactory {
    
    override fun createAlarmManager(): SignalAlarmManager {
        val databaseService = DatabaseServiceFactory(context).createSignalDatabaseService()
        return AndroidSignalAlarmManager(context, databaseService)
    }
}

actual fun createAlarmServiceFactory(): AlarmServiceFactory {
    throw IllegalStateException("Android alarm service factory requires Context. Use AndroidAlarmServiceFactory(context) instead.")
}

fun createAlarmServiceFactory(context: Context): AlarmServiceFactory {
    return AndroidAlarmServiceFactory(context)
}
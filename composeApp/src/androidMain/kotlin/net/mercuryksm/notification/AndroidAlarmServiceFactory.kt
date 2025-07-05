package net.mercuryksm.notification

import android.content.Context

class AndroidAlarmServiceFactory(
    private val context: Context
) : AlarmServiceFactory {
    
    override fun createAlarmManager(): SignalAlarmManager {
        return AndroidSignalAlarmManager(context)
    }
}

actual fun createAlarmServiceFactory(): AlarmServiceFactory {
    throw IllegalStateException("Android alarm service factory requires Context. Use AndroidAlarmServiceFactory(context) instead.")
}

fun createAlarmServiceFactory(context: Context): AlarmServiceFactory {
    return AndroidAlarmServiceFactory(context)
}
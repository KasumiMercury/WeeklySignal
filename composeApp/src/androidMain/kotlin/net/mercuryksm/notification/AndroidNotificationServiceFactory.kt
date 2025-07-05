package net.mercuryksm.notification

import android.content.Context

class AndroidNotificationServiceFactory(
    private val context: Context
) : NotificationServiceFactory {
    
    override fun createNotificationManager(): SignalNotificationManager {
        return AndroidSignalNotificationManager(context)
    }
}

actual fun createNotificationServiceFactory(): NotificationServiceFactory {
    throw IllegalStateException("Android notification service factory requires Context. Use AndroidNotificationServiceFactory(context) instead.")
}

fun createNotificationServiceFactory(context: Context): NotificationServiceFactory {
    return AndroidNotificationServiceFactory(context)
}
package net.mercuryksm.notification

interface NotificationServiceFactory {
    fun createNotificationManager(): SignalNotificationManager
}

expect fun createNotificationServiceFactory(): NotificationServiceFactory
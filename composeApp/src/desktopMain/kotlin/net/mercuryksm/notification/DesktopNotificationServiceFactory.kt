package net.mercuryksm.notification

class DesktopNotificationServiceFactory : NotificationServiceFactory {
    
    override fun createNotificationManager(): SignalNotificationManager {
        return DesktopSignalNotificationManager()
    }
}

actual fun createNotificationServiceFactory(): NotificationServiceFactory {
    return DesktopNotificationServiceFactory()
}
package net.mercuryksm.notification

class DesktopSignalNotificationManager : SignalNotificationManager {
    
    override suspend fun showPreviewNotification(settings: NotificationSettings): NotificationResult {
        return NotificationResult.NOT_SUPPORTED
    }
    
    override suspend fun hasNotificationPermission(): Boolean {
        return false
    }
    
    override suspend fun requestNotificationPermission(): Boolean {
        return false
    }
    
    override fun isNotificationSupported(): Boolean = false
}
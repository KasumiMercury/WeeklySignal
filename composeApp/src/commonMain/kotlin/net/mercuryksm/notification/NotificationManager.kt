package net.mercuryksm.notification

interface SignalNotificationManager {
    /**
     * Shows an immediate notification for preview purposes
     */
    suspend fun showPreviewNotification(settings: NotificationSettings): NotificationResult
    
    /**
     * Checks if notification permissions are granted
     */
    suspend fun hasNotificationPermission(): Boolean
    
    /**
     * Requests notification permissions (Android 13+)
     */
    suspend fun requestNotificationPermission(): Boolean
    
    /**
     * Checks if notification features are supported on this platform
     */
    fun isNotificationSupported(): Boolean
}
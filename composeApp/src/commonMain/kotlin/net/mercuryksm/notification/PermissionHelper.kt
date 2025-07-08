package net.mercuryksm.notification

interface PermissionHelper {
    fun hasNotificationPermission(): Boolean
    fun hasAlarmPermission(): Boolean
    fun hasAllPermissions(): Boolean
    fun requestNotificationPermission(callback: (Boolean) -> Unit)
    fun requestAlarmPermission(): Boolean
}
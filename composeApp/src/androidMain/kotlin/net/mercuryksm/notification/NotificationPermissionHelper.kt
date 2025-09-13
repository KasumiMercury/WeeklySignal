package net.mercuryksm.notification

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val activity: ComponentActivity
) : PermissionHelper {
    private val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    override fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission()) {
                callback(true)
            } else {
                // Navigate to app settings instead of using registerForActivityResult
                // This avoids lifecycle issues and is more reliable
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
                callback(false) // Return false indicating user needs to grant permission manually
            }
        } else {
            callback(true) // Permissions are granted by default on older versions
        }
    }
    
    override fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Requires POST_NOTIFICATIONS permission
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 7-12: Check if notifications are enabled system-wide
            NotificationManagerCompat.from(activity).areNotificationsEnabled()
        }
    }
    
    override fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            // For Android 7-11, exact alarms don't require special permission
            true
        }
    }
    
    override fun requestAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                return false
            }
        }
        return true
    }
    
    override fun hasAllPermissions(): Boolean {
        return hasNotificationPermission() && hasAlarmPermission()
    }
}

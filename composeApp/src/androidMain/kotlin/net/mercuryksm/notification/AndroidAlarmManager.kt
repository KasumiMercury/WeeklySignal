package net.mercuryksm.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mercuryksm.data.TimeSlot
import kotlin.coroutines.resume

class AndroidSignalAlarmManager(
    private val context: Context
) : SignalAlarmManager {
    
    companion object {
        private const val CHANNEL_ID = "weekly_signal_alarms"
        private const val CHANNEL_NAME = "Weekly Signal Test Alarms"
        private const val CHANNEL_DESCRIPTION = "Test alarms for WeeklySignal reminders"
        private const val NOTIFICATION_ID = 1001
    }
    
    private var permissionHelper: NotificationPermissionHelper? = null
    
    init {
        createNotificationChannel()
    }
    
    override suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmResult {
        // Android版では現在スケジューリング機能をサポートしない（将来実装予定）
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAlarm(alarmId: String): AlarmResult {
        // Android版では現在スケジューリング機能をサポートしない（将来実装予定）
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAllAlarms(): AlarmResult {
        // Android版では現在スケジューリング機能をサポートしない（将来実装予定）
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun getScheduledAlarms(): List<String> {
        // Android版では現在スケジューリング機能をサポートしない（将来実装予定）
        return emptyList()
    }
    
    override suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAlarmPermission()) {
                    return@withContext AlarmResult.PERMISSION_DENIED
                }
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(settings.title)
                    .setContentText(settings.message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .apply {
                        if (settings.sound) {
                            setDefaults(NotificationCompat.DEFAULT_SOUND)
                        }
                    }
                    .build()
                
                val notificationManager = NotificationManagerCompat.from(context)
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
                
                if (settings.vibration) {
                    triggerVibration()
                }
                
                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }
    
    override suspend fun hasAlarmPermission(): Boolean {
        // Android版では現在テスト通知のみサポート、スケジューリングはサポートしない
        return hasNotificationPermission()
    }
    
    override suspend fun requestAlarmPermission(): Boolean {
        // Android版では現在テスト通知のみサポート、スケジューリングはサポートしない
        return suspendCancellableCoroutine { continuation ->
            try {
                // テスト通知用の通知権限のみリクエスト
                if (permissionHelper == null && context is ComponentActivity) {
                    try {
                        permissionHelper = NotificationPermissionHelper(context)
                    } catch (e: IllegalStateException) {
                        continuation.resume(false)
                        return@suspendCancellableCoroutine
                    }
                }
                
                permissionHelper?.requestNotificationPermission { isGranted ->
                    continuation.resume(isGranted)
                } ?: continuation.resume(false)
            } catch (e: Exception) {
                continuation.resume(false)
            }
        }
    }
    
    override fun isAlarmSupported(): Boolean {
        // Android版では現在テスト通知のみサポート、スケジューリングはサポートしない
        // テストボタンを表示するためにtrueを返す
        return true
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setSound(null, audioAttributes)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}
package net.mercuryksm.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
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
        private const val CHANNEL_ID_BASE = "weekly_signal_alarms"
        private const val CHANNEL_NAME = "Weekly Signal Test Alarms"
        private const val CHANNEL_DESCRIPTION = "Test alarms for WeeklySignal reminders"
        private const val NOTIFICATION_ID = 1001
    }
    
    private var permissionHelper: NotificationPermissionHelper? = null
    
    init {
        // Initialize with default settings
        createNotificationChannel(sound = true, vibration = true)
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
                
                // Create channel with appropriate settings
                val channelId = createNotificationChannel(settings.sound, settings.vibration)
                
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(settings.title)
                    .setContentText(settings.message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .apply {
                        if (settings.sound) {
                            // Use alarm sound instead of default notification sound
                            val alarmUri = getAlarmSoundUri()
                            setSound(alarmUri)
                        }
                        if (settings.vibration) {
                            // Set vibration pattern for alarm
                            setVibrate(longArrayOf(0, 300, 200, 300))
                        }
                    }
                    .build()
                
                val notificationManager = NotificationManagerCompat.from(context)
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
                
                // Additional vibration for devices that support it
                if (settings.vibration) {
                    triggerVibration()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    // Cancel the notification after a short delay
                    notificationManager.cancel(NOTIFICATION_ID)
                }, 5000)

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
    
    private fun createNotificationChannel(sound: Boolean, vibration: Boolean): String {
        val channelId = "${CHANNEL_ID_BASE}_${if (sound) "s" else "n"}_${if (vibration) "v" else "n"}"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            
            // Check if channel already exists
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel != null) {
                return channelId
            }
            
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            val channel = NotificationChannel(
                channelId,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(vibration)
                
                if (sound) {
                    // Use alarm sound for the channel
                    val alarmUri = getAlarmSoundUri()
                    setSound(alarmUri, audioAttributes)
                } else {
                    // Disable sound for the channel
                    setSound(null, null)
                }
                
                if (vibration) {
                    // Set vibration pattern for alarm
                    this.vibrationPattern = longArrayOf(0, 300, 200, 300)
                }
            }
            
            notificationManager.createNotificationChannel(channel)
        }
        
        return channelId
    }
    
    private fun getAlarmSoundUri(): Uri {
        return try {
            // Try to get the default alarm sound
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: Uri.parse("content://settings/system/notification_sound")
        } catch (e: Exception) {
            // Fallback to notification sound if alarm sound is not available
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: Uri.parse("content://settings/system/notification_sound")
        }
    }
    
    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                // Use alarm-style vibration pattern
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Use alarm-style vibration pattern
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
                } else {
                    @Suppress("DEPRECATION")
                    // For older devices, use a simple vibration pattern
                    vibrator.vibrate(longArrayOf(0, 300, 200, 300), -1)
                }
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}

package net.mercuryksm.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        // Legacy base channel ID kept for migration; new multi-channel IDs live in AndroidSignalAlarmManager
        private const val LEGACY_CHANNEL_ID = "weekly_signal_alarms"
        private const val NOTIFICATION_ID_BASE = 3000
        private const val DISMISS_ACTION = "DISMISS_ALARM"
        private const val DISMISS_TEST_ALARM_ACTION = "DISMISS_TEST_ALARM"
        private const val DELETE_ALARM_ACTION = "DELETE_ALARM"
        private const val DELETE_TEST_ALARM_ACTION = "DELETE_TEST_ALARM"
        private const val ALARM_AUTO_STOP_DELAY_MS = 60000L // 1 minute
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 500) // 0.5s on, 0.5s off

        // Static management of Ringtone objects for alarm sound control
        val activeRingtones = ConcurrentHashMap<String, Ringtone>()
        // Static management of Vibrator objects for continuous vibration control
        val activeVibrators = ConcurrentHashMap<String, Vibrator>()
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                DISMISS_ACTION -> handleDismiss(context, intent)
                DISMISS_TEST_ALARM_ACTION -> handleTestAlarmDismiss(context, intent)
                DELETE_ALARM_ACTION -> handleDelete(context, intent)
                DELETE_TEST_ALARM_ACTION -> handleTestAlarmDelete(context, intent)
                else -> handleAlarm(context, intent)
            }
        } catch (e: Exception) {
            // Log the error to prevent receiver from crashing
            e.printStackTrace()
        } finally {
            // Clean up finished ringtones to prevent memory leaks
            cleanupFinishedRingtones()
        }
    }
    
    private fun handleAlarm(context: Context, intent: Intent) {
        val alarmInfoJson = intent.getStringExtra(AndroidSignalAlarmManager.EXTRA_ALARM_INFO) ?: return
        val isRepeating = intent.getBooleanExtra(AndroidSignalAlarmManager.EXTRA_IS_REPEATING, false)
        
        val alarmInfo = try {
            json.decodeFromString<AndroidSignalAlarmManager.AlarmInfo>(alarmInfoJson)
        } catch (e: Exception) {
            // If JSON parsing fails, ignore the alarm as we no longer support legacy format
            return
        }
        
        // Show notification
        showAlarmNotification(context, alarmInfo)
        
        // For modern Android (API 24+), we use single alarms and reschedule manually
        // This provides consistent behavior across all supported Android versions
        if (isRepeating) {
            scheduleNextWeekAlarm(context, alarmInfo)
        }
    }
    
    private fun showAlarmNotification(
        context: Context,
        alarmInfo: AndroidSignalAlarmManager.AlarmInfo
    ) {
        ensureNotificationChannels(context)
        // Generate notification ID
        val notificationId = NOTIFICATION_ID_BASE + alarmInfo.alarmId.hashCode()

        val channelId = if (alarmInfo.vibration) {
            AndroidSignalAlarmManager.CHANNEL_ID_VIBRATE
        } else {
            AndroidSignalAlarmManager.CHANNEL_ID_SILENT
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(alarmInfo.title)
            .setContentText(alarmInfo.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityIntent(context))
            .setSound(null) // Always disable notification sound, handle manually
            .setVibrate(null) // Use channel vibration settings
            .setDeleteIntent(createDeleteIntent(context, alarmInfo.alarmId, notificationId))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                createDismissIntent(context, alarmInfo.alarmId, notificationId)
            )
        
        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
        
        // Handle sound manually - only if enabled
        if (alarmInfo.sound) {
            playAndManageAlarmSound(context, alarmInfo.alarmId)
        }
        
        // Set auto-stop timer for 1 minute
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            stopAlarmSound(alarmInfo.alarmId)
            stopVibration(alarmInfo.alarmId)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
        }, ALARM_AUTO_STOP_DELAY_MS)
    }
    
    private fun scheduleNextWeekAlarm(
        context: Context,
        alarmInfo: AndroidSignalAlarmManager.AlarmInfo
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }
        
        // Calculate next week's alarm time
        val nextAlarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmInfo.hour)
            set(Calendar.MINUTE, alarmInfo.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Move from current day to target day of week
            val targetDayOfWeek = convertOrdinalToCalendarDay(alarmInfo.dayOfWeek)
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
            
            // Set to one week later
            add(Calendar.WEEK_OF_YEAR, 1)
        }.timeInMillis
        
        // Create Intent
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "net.mercuryksm.WEEKLY_ALARM"
            putExtra(AndroidSignalAlarmManager.EXTRA_ALARM_INFO, json.encodeToString(AndroidSignalAlarmManager.AlarmInfo.serializer(), alarmInfo))
            putExtra(AndroidSignalAlarmManager.EXTRA_IS_REPEATING, true)
        }
        
        val alarmId = alarmInfo.alarmId.hashCode() and 0x7FFFFFFF
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Use modern alarm scheduling for all supported Android versions
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime,
            pendingIntent
        )
    }
    
    private fun convertOrdinalToCalendarDay(dayOrdinal: Int): Int {
        return when (dayOrdinal) {
            0 -> Calendar.MONDAY
            1 -> Calendar.TUESDAY
            2 -> Calendar.WEDNESDAY
            3 -> Calendar.THURSDAY
            4 -> Calendar.FRIDAY
            5 -> Calendar.SATURDAY
            6 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
    
    
    private fun handleDismiss(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        val alarmId = intent.getStringExtra("alarm_id") ?: ""
        
        // Stop alarm sound and vibration if they're playing
        stopAlarmSound(alarmId)
        stopVibration(alarmId)
        
        // Clear notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
    
    private fun handleTestAlarmDismiss(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        
        // Stop test alarm sound and vibration if they're playing
        stopAlarmSound("test_alarm")
        stopVibration("test_alarm")
        
        // Clear notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
    
    private fun handleTestAlarmDelete(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        
        // Stop test alarm sound and vibration when notification is swiped away
        stopAlarmSound("test_alarm")
        stopVibration("test_alarm")
    }
    
    private fun handleDelete(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        val alarmId = intent.getStringExtra("alarm_id") ?: ""
        
        // Stop alarm sound and vibration when notification is swiped away
        stopAlarmSound(alarmId)
        stopVibration(alarmId)
    }
    
    private fun createMainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, net.mercuryksm.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDismissIntent(context: Context, alarmId: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = DISMISS_ACTION
            putExtra("alarm_id", alarmId)
            putExtra("notification_id", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "${alarmId}_dismiss".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun createTestAlarmDismissIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = DISMISS_TEST_ALARM_ACTION
            putExtra("notification_id", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "test_alarm_dismiss".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDeleteIntent(context: Context, alarmId: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = DELETE_ALARM_ACTION
            putExtra("alarm_id", alarmId)
            putExtra("notification_id", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "${alarmId}_delete".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createTestAlarmDeleteIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = DELETE_TEST_ALARM_ACTION
            putExtra("notification_id", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "test_alarm_delete".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun playAndManageAlarmSound(context: Context, alarmId: String) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return
            
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            if (ringtone != null) {
                // Explicitly set audio attributes to use the alarm stream
                ringtone.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                activeRingtones[alarmId] = ringtone
                ringtone.play()
            }
        } catch (e: Exception) {
            // Failed to play alarm sound, ignore
        }
    }
    
    private fun stopAlarmSound(alarmId: String) {
        try {
            val ringtone = activeRingtones.remove(alarmId)
            if (ringtone?.isPlaying == true) {
                ringtone.stop()
            }
        } catch (e: Exception) {
            // Failed to stop alarm sound, ignore
        }
    }
    
    private fun stopVibration(alarmId: String) {
        try {
            val vibrator = activeVibrators.remove(alarmId)
            vibrator?.cancel()
        } catch (e: Exception) {
            // Failed to stop vibration, ignore
        }
    }
    
    private fun stopAllAlarmSounds() {
        try {
            activeRingtones.values.forEach { ringtone ->
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
            }
            activeRingtones.clear()
        } catch (e: Exception) {
            // Failed to stop all alarm sounds, ignore
        }
    }
    
    private fun stopAllVibrations() {
        try {
            activeVibrators.values.forEach { vibrator ->
                vibrator.cancel()
            }
            activeVibrators.clear()
        } catch (e: Exception) {
            // Failed to stop all vibrations, ignore
        }
    }
    
    fun showTestAlarmNotification(context: Context, alarmInfo: AndroidSignalAlarmManager.AlarmInfo) {
        ensureNotificationChannels(context)
        val notificationId = 1001 // TEST_NOTIFICATION_ID

        val channelId = if (alarmInfo.vibration) {
            AndroidSignalAlarmManager.CHANNEL_ID_VIBRATE
        } else {
            AndroidSignalAlarmManager.CHANNEL_ID_SILENT
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(alarmInfo.title)
            .setContentText(alarmInfo.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityIntent(context))
            .setSound(null) // Always disable notification sound, handle manually
            .setVibrate(null) // Use channel vibration settings
            .setDeleteIntent(createTestAlarmDeleteIntent(context, notificationId))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                createTestAlarmDismissIntent(context, notificationId)
            )
        
        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
        
        // Handle sound manually - only if enabled
        if (alarmInfo.sound) {
            playAndManageAlarmSound(context, "test_alarm")
        }
        
    }

    private fun ensureNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Remove legacy single channel if lingering
        // mgr.deleteNotificationChannel(LEGACY_CHANNEL_ID)

        fun ensureChannel(id: String, name: String, importance: Int, enableVibrate: Boolean) {
            if (mgr.getNotificationChannel(id) != null) return
            val ch = android.app.NotificationChannel(id, name, importance).apply {
                description = "Weekly recurring alarms for WeeklySignal reminders"
                enableLights(true)
                setSound(null, null)
                if (enableVibrate) {
                    if (Build.VERSION.SDK_INT >= 35) {
                        try {
                            val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, -1)
                            this.javaClass.getMethod("setVibrationEffect", VibrationEffect::class.java)
                                .invoke(this, effect)
                        } catch (_: Throwable) {
                            enableVibration(true)
                            vibrationPattern = VIBRATION_PATTERN
                        }
                    } else {
                        enableVibration(true)
                        vibrationPattern = VIBRATION_PATTERN
                    }
                } else {
                    enableVibration(false)
                }
            }
            mgr.createNotificationChannel(ch)
        }

        ensureChannel(AndroidSignalAlarmManager.CHANNEL_ID_VIBRATE, "Alarms: Vibrate", android.app.NotificationManager.IMPORTANCE_HIGH, true)
        ensureChannel(AndroidSignalAlarmManager.CHANNEL_ID_SILENT, "Alarms: Silent", android.app.NotificationManager.IMPORTANCE_DEFAULT, false)
    }
    
    private fun cleanupFinishedRingtones() {
        try {
            val iterator = activeRingtones.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val ringtone = entry.value
                if (!ringtone.isPlaying) {
                    iterator.remove()
                }
            }
        } catch (e: Exception) {
            // Failed to cleanup ringtones, ignore
        }
    }
}

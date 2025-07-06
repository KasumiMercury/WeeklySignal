package net.mercuryksm.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.media.RingtoneManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "weekly_signal_alarms"
        private const val NOTIFICATION_ID_BASE = 3000
        private const val DISMISS_ACTION = "DISMISS_ALARM"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DISMISS_ACTION -> handleDismiss(context, intent)
            else -> handleAlarm(context, intent)
        }
    }
    
    private fun handleAlarm(context: Context, intent: Intent) {
        val alarmInfoJson = intent.getStringExtra(AndroidSignalAlarmManager.EXTRA_ALARM_INFO) ?: return
        val isRepeating = intent.getBooleanExtra(AndroidSignalAlarmManager.EXTRA_IS_REPEATING, false)
        
        val alarmInfo = try {
            json.decodeFromString<AndroidSignalAlarmManager.AlarmInfo>(alarmInfoJson)
        } catch (e: Exception) {
            // For legacy format compatibility
            handleLegacyAlarm(context, intent)
            return
        }
        
        // Show notification
        showAlarmNotification(context, alarmInfo)
        
        // For Android 12+, setRepeating became inaccurate, so manually set next alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isRepeating) {
            scheduleNextWeekAlarm(context, alarmInfo)
        }
    }
    
    private fun showAlarmNotification(
        context: Context,
        alarmInfo: AndroidSignalAlarmManager.AlarmInfo
    ) {
        // Generate notification ID
        val notificationId = NOTIFICATION_ID_BASE + alarmInfo.alarmId.hashCode()
        
        // Create notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(alarmInfo.title)
            .setContentText(alarmInfo.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(createFullScreenIntent(context, alarmInfo.alarmId), true)
            .apply {
                if (alarmInfo.sound) {
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                }
                
                // Dismiss action (no snooze per requirements)
                addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Dismiss",
                    createDismissIntent(context, alarmInfo.alarmId, notificationId)
                )
            }
        
        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
        
        // Vibration
        if (alarmInfo.vibration) {
            triggerVibration(context)
        }
    }
    
    private fun scheduleNextWeekAlarm(
        context: Context,
        alarmInfo: AndroidSignalAlarmManager.AlarmInfo
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check permissions
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
        
        // Set next alarm
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
    
    private fun handleLegacyAlarm(context: Context, intent: Intent) {
        // Legacy alarm processing (for compatibility)
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val title = intent.getStringExtra("title") ?: "WeeklySignal Alarm"
        val message = intent.getStringExtra("message") ?: "Time for your scheduled task"
        val sound = intent.getBooleanExtra("sound", true)
        val vibration = intent.getBooleanExtra("vibration", true)
        
        val alarmInfo = AndroidSignalAlarmManager.AlarmInfo(
            alarmId = alarmId,
            signalItemId = "",
            timeSlotId = "",
            title = title,
            message = message,
            sound = sound,
            vibration = vibration,
            hour = 0,
            minute = 0,
            dayOfWeek = 0
        )
        
        showAlarmNotification(context, alarmInfo)
    }
    
    private fun handleDismiss(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        
        // Clear notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
    
    private fun createFullScreenIntent(context: Context, alarmId: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("action", "full_screen")
        }
        
        return PendingIntent.getBroadcast(
            context,
            "${alarmId}_fullscreen".hashCode(),
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
    
    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                    vibrator.vibrate(vibrationPattern, -1)
                }
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}
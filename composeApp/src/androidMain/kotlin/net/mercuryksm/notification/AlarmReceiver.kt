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
import android.media.Ringtone
import androidx.annotation.RequiresApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "weekly_signal_alarms"
        private const val NOTIFICATION_ID_BASE = 3000
        private const val DISMISS_ACTION = "DISMISS_ALARM"
        
        // Static management of Ringtone objects for alarm sound control
        private val activeRingtones = ConcurrentHashMap<String, Ringtone>()
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DISMISS_ACTION -> handleDismiss(context, intent)
            else -> handleAlarm(context, intent)
        }
        
        // Clean up finished ringtones to prevent memory leaks
        cleanupFinishedRingtones()
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
    
    @RequiresApi(Build.VERSION_CODES.O)
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
            .setContentIntent(createMainActivityIntent(context))
            .apply {
                if (alarmInfo.sound) {
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    // Start playing alarm sound and manage it for potential stopping
                    playAndManageAlarmSound(context, alarmInfo.alarmId)
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
        
        // Stop alarm sound if it's playing
        stopAlarmSound(alarmId)
        
        // Clear notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
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
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun triggerVibration(context: Context) {
        try {
            val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+: Use VibratorManager for better control
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                // Android 7-11: Use VibrationEffect with legacy Vibrator
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
    
    private fun playAndManageAlarmSound(context: Context, alarmId: String) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return
            
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            if (ringtone != null) {
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

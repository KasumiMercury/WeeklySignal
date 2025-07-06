package net.mercuryksm.notification

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
import android.app.PendingIntent
import android.content.Intent as AndroidIntent

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "weekly_signal_alarms"
        private const val NOTIFICATION_ID_BASE = 3000
        private const val SNOOZE_ACTION = "SNOOZE_ALARM"
        private const val DISMISS_ACTION = "DISMISS_ALARM"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SNOOZE_ACTION -> handleSnooze(context, intent)
            DISMISS_ACTION -> handleDismiss(context, intent)
            else -> handleAlarm(context, intent)
        }
    }
    
    private fun handleAlarm(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val title = intent.getStringExtra("title") ?: "WeeklySignal Alarm"
        val message = intent.getStringExtra("message") ?: "Time for your scheduled task"
        val sound = intent.getBooleanExtra("sound", true)
        val vibration = intent.getBooleanExtra("vibration", true)
        val snoozeEnabled = intent.getBooleanExtra("snooze_enabled", false)
        val snoozeDuration = intent.getIntExtra("snooze_duration", 5)
        
        // Create notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(createFullScreenIntent(context, alarmId), true)
            .apply {
                if (sound) {
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                }
                
                // Add action buttons
                if (snoozeEnabled) {
                    addAction(
                        android.R.drawable.ic_media_pause,
                        "Snooze",
                        createSnoozeIntent(context, alarmId, snoozeDuration)
                    )
                }
                
                addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Dismiss",
                    createDismissIntent(context, alarmId)
                )
            }
        
        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(alarmId.hashCode(), notificationBuilder.build())
        }
        
        // Trigger vibration
        if (vibration) {
            triggerVibration(context)
        }
    }
    
    private fun handleSnooze(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val snoozeDuration = intent.getIntExtra("snooze_duration", 5)
        
        // Dismiss current notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(alarmId.hashCode())
        
        // Schedule snooze alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val snoozeIntent = AndroidIntent(context, AlarmReceiver::class.java).apply {
            putExtras(intent.extras ?: return)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            "${alarmId}_snooze".hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeTime = System.currentTimeMillis() + (snoozeDuration * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            snoozeTime,
            snoozePendingIntent
        )
    }
    
    private fun handleDismiss(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        
        // Dismiss notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(alarmId.hashCode())
    }
    
    private fun createFullScreenIntent(context: Context, alarmId: String): PendingIntent {
        val intent = AndroidIntent(context, AlarmReceiver::class.java).apply {
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
    
    private fun createSnoozeIntent(context: Context, alarmId: String, snoozeDuration: Int): PendingIntent {
        val intent = AndroidIntent(context, AlarmReceiver::class.java).apply {
            action = SNOOZE_ACTION
            putExtra("alarm_id", alarmId)
            putExtra("snooze_duration", snoozeDuration)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "${alarmId}_snooze".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDismissIntent(context: Context, alarmId: String): PendingIntent {
        val intent = AndroidIntent(context, AlarmReceiver::class.java).apply {
            action = DISMISS_ACTION
            putExtra("alarm_id", alarmId)
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
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                    vibrator.vibrate(vibrationPattern, 0)
                }
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}
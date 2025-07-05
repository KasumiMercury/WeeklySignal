package net.mercuryksm.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AndroidSignalNotificationManagerSafe(
    private val context: Context,
    private val requestPermission: ((Boolean) -> Unit) -> Unit
) : SignalNotificationManager {
    
    companion object {
        private const val CHANNEL_ID = "weekly_signal_notifications"
        private const val CHANNEL_NAME = "Weekly Signal"
        private const val CHANNEL_DESCRIPTION = "Notifications for WeeklySignal reminders"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    override suspend fun showPreviewNotification(settings: NotificationSettings): NotificationResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasNotificationPermission()) {
                    return@withContext NotificationResult.PERMISSION_DENIED
                }
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(settings.title)
                    .setContentText(settings.message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
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
                
                NotificationResult.SUCCESS
            } catch (e: Exception) {
                NotificationResult.ERROR
            }
        }
    }
    
    override suspend fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    override suspend fun requestNotificationPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            requestPermission { isGranted ->
                continuation.resume(isGranted)
            }
        }
    }
    
    override fun isNotificationSupported(): Boolean = true
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
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
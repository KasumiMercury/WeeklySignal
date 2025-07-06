package net.mercuryksm.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.DayOfWeekJp
import kotlin.coroutines.resume
import androidx.core.net.toUri
import java.util.Calendar
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class AndroidSignalAlarmManager(
    private val context: Context
) : SignalAlarmManager {

    companion object {
        private const val CHANNEL_ID_BASE = "weekly_signal_alarms"
        private const val CHANNEL_NAME = "Weekly Signal Alarms"
        private const val CHANNEL_DESCRIPTION = "Weekly recurring alarms for WeeklySignal reminders"
        private const val ALARM_SOUND_URI = "content://settings/system/notification_sound"
        private val VIBRATION_PATTERN = longArrayOf(0, 300, 200, 300)
        private const val TEST_NOTIFICATION_ID = 1001
        private const val TEST_NOTIFICATION_DELAY_MS = 5000L
        
        // SharedPreferences keys
        private const val PREFS_NAME = "weekly_signal_alarms"
        private const val PREFS_KEY_ALARM_INFO = "alarm_info_"
        private const val PREFS_KEY_ALL_ALARMS = "all_alarm_ids"
        
        // Intent extras
        const val EXTRA_ALARM_INFO = "alarm_info"
        const val EXTRA_IS_REPEATING = "is_repeating"
    }

    private var permissionHelper: NotificationPermissionHelper? = null
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        createNotificationChannel(sound = true, vibration = true)
    }

    // Data class for persisting alarm information
    @Serializable
    data class AlarmInfo(
        val alarmId: String,
        val signalItemId: String,
        val timeSlotId: String,
        val title: String,
        val message: String,
        val sound: Boolean,
        val vibration: Boolean,
        val hour: Int,
        val minute: Int,
        val dayOfWeek: Int
    )

    override suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check alarm permissions
                if (!hasAlarmPermission()) {
                    return@withContext AlarmResult.PERMISSION_DENIED
                }

                val alarmId = generateAlarmId(settings.alarmId, timeSlot)
                
                // Cancel existing alarm
                cancelExistingAlarm(alarmId)

                // Create alarm information
                val alarmInfo = AlarmInfo(
                    alarmId = settings.alarmId,
                    signalItemId = extractSignalItemId(settings.alarmId),
                    timeSlotId = timeSlot.id,
                    title = settings.title,
                    message = settings.message,
                    sound = settings.sound,
                    vibration = settings.vibration,
                    hour = timeSlot.hour,
                    minute = timeSlot.minute,
                    dayOfWeek = timeSlot.dayOfWeek.ordinal
                )

                // Calculate first alarm time
                val firstAlarmTime = calculateNextAlarmTime(timeSlot)
                
                // Create PendingIntent
                val alarmIntent = createAlarmIntent(alarmInfo)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Set weekly recurring alarm
                setRepeatingAlarmWithPermissionCheck(firstAlarmTime, pendingIntent)
                
                // Save alarm information
                saveAlarmInfo(alarmId, alarmInfo)
                
                AlarmResult.SUCCESS
            } catch (e: SecurityException) {
                AlarmResult.PERMISSION_DENIED
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun cancelAlarm(alarmId: String): AlarmResult {
        return withContext(Dispatchers.IO) {
            try {
                val alarmIdInt = alarmId.toIntOrNull() ?: return@withContext AlarmResult.ALARM_NOT_FOUND
                
                cancelExistingAlarm(alarmIdInt)
                removeAlarmInfo(alarmIdInt)
                
                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun cancelAllAlarms(): AlarmResult {
        return withContext(Dispatchers.IO) {
            try {
                val allAlarmIds = getAllAlarmIds()
                allAlarmIds.forEach { alarmId ->
                    cancelExistingAlarm(alarmId)
                }
                clearAllAlarmInfo()
                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun getScheduledAlarms(): List<String> {
        return withContext(Dispatchers.IO) {
            getAllAlarmIds().map { it.toString() }
        }
    }

    override suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult {
        // Maintain existing implementation
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAlarmPermission()) {
                    return@withContext AlarmResult.PERMISSION_DENIED
                }

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
                            val alarmUri = getAlarmSoundUri()
                            setSound(alarmUri)
                        }
                        if (settings.vibration) {
                            setVibrate(VIBRATION_PATTERN)
                        }
                    }
                    .build()

                val notificationManager = NotificationManagerCompat.from(context)
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(TEST_NOTIFICATION_ID, notification)
                }

                if (settings.vibration) {
                    triggerVibration()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    notificationManager.cancel(TEST_NOTIFICATION_ID)
                }, TEST_NOTIFICATION_DELAY_MS)

                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override suspend fun requestAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                return false
            }
        }
        return true
    }

    override fun isAlarmSupported(): Boolean {
        return true
    }

    // Batch scheduling function for SignalItem
    override suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): List<AlarmResult> {
        val results = mutableListOf<AlarmResult>()
        
        signalItem.timeSlots.forEach { timeSlot ->
            val settings = AlarmSettings(
                sound = signalItem.sound,
                vibration = signalItem.vibration,
                title = signalItem.name,
                message = signalItem.description.ifEmpty { 
                    "Weekly reminder: ${timeSlot.getDisplayText()}" 
                },
                alarmId = "${signalItem.id}_${timeSlot.id}"
            )
            
            val result = scheduleAlarm(timeSlot, settings)
            results.add(result)
        }
        
        return results
    }

    // Batch cancellation function for SignalItem
    override suspend fun cancelSignalItemAlarms(signalItem: SignalItem): List<AlarmResult> {
        val results = mutableListOf<AlarmResult>()
        
        signalItem.timeSlots.forEach { timeSlot ->
            val alarmId = generateAlarmId("${signalItem.id}_${timeSlot.id}", timeSlot)
            val result = cancelAlarm(alarmId.toString())
            results.add(result)
        }
        
        return results
    }

    // Process for TimeSlot updates
    override suspend fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem): List<AlarmResult> {
        // Cancel all old alarms
        cancelSignalItemAlarms(oldSignalItem)
        
        // Set new alarms
        return scheduleSignalItemAlarms(newSignalItem)
    }

    // Check alarm status for specific SignalItem
    override suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val allAlarmIds = getAllAlarmIds()
            allAlarmIds.any { alarmId ->
                val alarmInfo = getAlarmInfo(alarmId)
                alarmInfo?.signalItemId == signalItemId
            }
        }
    }

    // Private helper functions

    private fun calculateNextAlarmTime(timeSlot: TimeSlot): Long {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeSlot.hour)
            set(Calendar.MINUTE, timeSlot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Set to target day of week
            val targetDayOfWeek = convertDayOfWeekJpToCalendar(timeSlot.dayOfWeek)
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
            
            // If already past, set to next week
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        return alarm.timeInMillis
    }

    private fun convertDayOfWeekJpToCalendar(dayOfWeek: DayOfWeekJp): Int {
        return when (dayOfWeek) {
            DayOfWeekJp.SUNDAY -> Calendar.SUNDAY
            DayOfWeekJp.MONDAY -> Calendar.MONDAY
            DayOfWeekJp.TUESDAY -> Calendar.TUESDAY
            DayOfWeekJp.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeekJp.THURSDAY -> Calendar.THURSDAY
            DayOfWeekJp.FRIDAY -> Calendar.FRIDAY
            DayOfWeekJp.SATURDAY -> Calendar.SATURDAY
        }
    }

    private fun createAlarmIntent(alarmInfo: AlarmInfo): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = "net.mercuryksm.WEEKLY_ALARM"
            putExtra(EXTRA_ALARM_INFO, json.encodeToString(alarmInfo))
            putExtra(EXTRA_IS_REPEATING, true)
        }
    }

    private fun generateAlarmId(baseId: String, timeSlot: TimeSlot): Int {
        // Generate unique alarm ID
        val uniqueString = "${baseId}_${timeSlot.dayOfWeek.ordinal}_${timeSlot.hour}_${timeSlot.minute}"
        return uniqueString.hashCode() and 0x7FFFFFFF // Ensure positive integer
    }

    private fun extractSignalItemId(alarmId: String): String {
        // Extract signalItemId from "signalItemId_timeSlotId" format
        return alarmId.substringBefore("_")
    }

    private fun setRepeatingAlarmWithPermissionCheck(firstAlarmTime: Long, pendingIntent: PendingIntent) {
        val interval = AlarmManager.INTERVAL_DAY * 7 // One week

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // Android 12+: setRepeating became inaccurate, use setExactAndAllowWhileIdle
                // However, this is a one-time alarm, so AlarmReceiver needs to reschedule the next one
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstAlarmTime,
                    pendingIntent
                )
            } else {
                throw SecurityException("Exact alarm permission not granted")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+: Use setRepeating with Doze mode support
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstAlarmTime,
                interval,
                pendingIntent
            )
        } else {
            // Android 6.0-: Use normal setRepeating
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstAlarmTime,
                interval,
                pendingIntent
            )
        }
    }

    private fun cancelExistingAlarm(alarmId: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    // Alarm information save/load operations

    private fun saveAlarmInfo(alarmId: Int, alarmInfo: AlarmInfo) {
        val key = "$PREFS_KEY_ALARM_INFO$alarmId"
        val jsonString = json.encodeToString(alarmInfo)
        
        sharedPrefs.edit().apply {
            putString(key, jsonString)
            
            // Add to alarm ID list
            val allIds = getAllAlarmIds().toMutableSet()
            allIds.add(alarmId)
            putStringSet(PREFS_KEY_ALL_ALARMS, allIds.map { it.toString() }.toSet())
            
            apply()
        }
    }

    private fun removeAlarmInfo(alarmId: Int) {
        val key = "$PREFS_KEY_ALARM_INFO$alarmId"
        
        sharedPrefs.edit().apply {
            remove(key)
            
            // Remove from alarm ID list
            val allIds = getAllAlarmIds().toMutableSet()
            allIds.remove(alarmId)
            putStringSet(PREFS_KEY_ALL_ALARMS, allIds.map { it.toString() }.toSet())
            
            apply()
        }
    }

    private fun getAlarmInfo(alarmId: Int): AlarmInfo? {
        val key = "$PREFS_KEY_ALARM_INFO$alarmId"
        val jsonString = sharedPrefs.getString(key, null) ?: return null
        
        return try {
            json.decodeFromString<AlarmInfo>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private fun getAllAlarmIds(): Set<Int> {
        return sharedPrefs.getStringSet(PREFS_KEY_ALL_ALARMS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    private fun clearAllAlarmInfo() {
        sharedPrefs.edit().apply {
            // Delete all alarm information
            getAllAlarmIds().forEach { alarmId ->
                remove("$PREFS_KEY_ALARM_INFO$alarmId")
            }
            remove(PREFS_KEY_ALL_ALARMS)
            apply()
        }
    }

    // Notification channel and other helper methods maintain existing implementation

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
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

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
                    val alarmUri = getAlarmSoundUri()
                    setSound(alarmUri, audioAttributes)
                } else {
                    setSound(null, null)
                }

                if (vibration) {
                    this.vibrationPattern = VIBRATION_PATTERN
                }
            }

            notificationManager.createNotificationChannel(channel)
        }

        return channelId
    }

    private fun getAlarmSoundUri(): Uri {
        return try {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: ALARM_SOUND_URI.toUri()
        } catch (e: Exception) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: ALARM_SOUND_URI.toUri()
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(VIBRATION_PATTERN, -1)
                }
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}
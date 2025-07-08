package net.mercuryksm.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.database.AlarmStateEntity
import net.mercuryksm.data.database.SignalDatabaseService
import net.mercuryksm.data.database.getRoomDatabase
import java.util.*
import android.app.NotificationManager as AndroidNotificationManager

class AndroidSignalAlarmManager(
    private val context: Context,
    private val databaseService: SignalDatabaseService
) : SignalAlarmManager {

    companion object {
        private const val CHANNEL_ID_BASE = "weekly_signal_alarms"
        private const val CHANNEL_NAME = "Weekly Signal Alarms"
        private const val CHANNEL_DESCRIPTION = "Weekly recurring alarms for WeeklySignal reminders"
        private const val ALARM_SOUND_URI = "content://settings/system/notification_sound"
        private val VIBRATION_PATTERN = longArrayOf(0, 300, 200, 300)
        private const val TEST_NOTIFICATION_ID = 1001
        private const val TEST_NOTIFICATION_DELAY_MS = 5000L
        
        // SharedPreferences keys (for migration from legacy data only)
        private const val PREFS_NAME = "weekly_signal_alarms"
        private const val PREFS_KEY_ALARM_INFO = "alarm_info_"
        private const val PREFS_KEY_ALL_ALARMS = "all_alarm_ids"
        
        // Intent extras
        const val EXTRA_ALARM_INFO = "alarm_info"
        const val EXTRA_IS_REPEATING = "is_repeating"
    }

    private var permissionHelper: NotificationPermissionHelper? = null
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // SharedPreferences only used for one-time migration from legacy data
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        // Migrate existing SharedPreferences data to Room database on first run
        migrateSharedPreferencesToRoom()
    }

    init {
        createNotificationChannel()
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

                // Calculate first alarm time
                val firstAlarmTime = calculateNextAlarmTime(timeSlot)
                
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
                
                // Save alarm state to database
                saveAlarmStateToDatabase(timeSlot.id, alarmInfo.signalItemId, alarmId, firstAlarmTime)
                
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
                removeAlarmStateFromDatabase(alarmId)
                
                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun cancelAllAlarms(): AlarmResult {
        return withContext(Dispatchers.IO) {
            try {
                val database = getRoomDatabase()
                val allAlarmStates = database.alarmStateDao().getAllScheduledAlarms()
                allAlarmStates.forEach { alarmState ->
                    val alarmId = generateAlarmIdFromTimeSlotId(alarmState.timeSlotId)
                    cancelExistingAlarm(alarmId)
                }
                clearAllAlarmStates()
                AlarmResult.SUCCESS
            } catch (e: Exception) {
                AlarmResult.ERROR
            }
        }
    }

    override suspend fun getScheduledAlarms(): List<String> {
        return withContext(Dispatchers.IO) {
            val database = getRoomDatabase()
            database.alarmStateDao().getAllScheduledAlarms()
                .map { it.timeSlotId }
        }
    }

    override suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult {
        // Maintain existing implementation
        return withContext(Dispatchers.IO) {
            try {
                if (!hasAlarmPermission()) {
                    return@withContext AlarmResult.PERMISSION_DENIED
                }

                val notification = NotificationCompat.Builder(context, CHANNEL_ID_BASE)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(settings.title)
                    .setContentText(settings.message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .apply {
                        if (!settings.sound) {
                            setSound(null)
                        }
                        if (!settings.vibration) {
                            setVibrate(null)
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
            // For Android 7-11, exact alarms don't require special permission
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
            val database = getRoomDatabase()
            val timeSlots = database.timeSlotDao().getBySignalId(signalItemId)
            timeSlots.any { timeSlot ->
                val alarmState = database.alarmStateDao().getByTimeSlotId(timeSlot.id)
                alarmState?.isAlarmScheduled == true
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
        // Modern Android (API 24+): Use setExactAndAllowWhileIdle for precise timing
        // Note: For Android 12+, we use single alarms and reschedule in AlarmReceiver
        // For Android 7-11, we can still use setRepeating but prefer exact alarms for consistency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstAlarmTime,
                    pendingIntent
                )
            } else {
                throw SecurityException("Exact alarm permission not granted")
            }
        } else {
            // Android 7-11: Use setExactAndAllowWhileIdle for consistent behavior
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                firstAlarmTime,
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

    // Database alarm state management

    private suspend fun saveAlarmStateToDatabase(timeSlotId: String, signalItemId: String, alarmId: Int, nextAlarmTime: Long) {
        val alarmState = AlarmStateEntity(
            timeSlotId = timeSlotId,
            signalItemId = signalItemId,
            isAlarmScheduled = true,
            pendingIntentRequestCode = alarmId,
            scheduledAt = System.currentTimeMillis(),
            nextAlarmTime = nextAlarmTime
        )
        val database = getRoomDatabase()
        database.alarmStateDao().insertOrUpdate(alarmState)
    }

    private suspend fun removeAlarmStateFromDatabase(alarmId: String) {
        // Find alarm state by pending intent request code or timeSlotId
        val alarmIdInt = alarmId.toIntOrNull()
        if (alarmIdInt != null) {
            val database = getRoomDatabase()
            val allStates = database.alarmStateDao().getAll()
            val targetState = allStates.find { it.pendingIntentRequestCode == alarmIdInt }
            targetState?.let { state ->
                database.alarmStateDao().delete(state.timeSlotId)
            }
        } else {
            // Try to delete by timeSlotId directly
            val database = getRoomDatabase()
            database.alarmStateDao().delete(alarmId)
        }
    }

    private suspend fun clearAllAlarmStates() {
        val database = getRoomDatabase()
        val allStates = database.alarmStateDao().getAll()
        allStates.forEach { state ->
            database.alarmStateDao().delete(state.timeSlotId)
        }
    }

    private fun generateAlarmIdFromTimeSlotId(timeSlotId: String): Int {
        return timeSlotId.hashCode() and 0x7FFFFFFF
    }

    // Migration from SharedPreferences to Room database
    private fun migrateSharedPreferencesToRoom() {
        try {
            // Check if migration has already been done by looking for migration flag
            val migrationDone = sharedPrefs.getBoolean("migration_to_room_completed", false)
            if (migrationDone) {
                return
            }
            
            // Get all saved alarm IDs from SharedPreferences
            val allAlarmIds = sharedPrefs.getStringSet("all_alarm_ids", emptySet()) ?: emptySet()
            
            if (allAlarmIds.isNotEmpty()) {
                // Migrate alarm data to Room database
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        allAlarmIds.forEach { alarmIdStr ->
                            val alarmId = alarmIdStr.toIntOrNull() ?: return@forEach
                            val alarmInfoJson = sharedPrefs.getString("alarm_info_$alarmId", null) ?: return@forEach
                            
                            // Decode alarm information
                            val alarmInfo = json.decodeFromString<AlarmInfo>(alarmInfoJson)
                            
                            // Create AlarmStateEntity for Room database
                            val alarmState = AlarmStateEntity(
                                timeSlotId = alarmInfo.timeSlotId,
                                signalItemId = alarmInfo.signalItemId,
                                isAlarmScheduled = true, // If it was in SharedPrefs, it was scheduled
                                pendingIntentRequestCode = alarmId,
                                scheduledAt = System.currentTimeMillis(), // Use current time as fallback
                                nextAlarmTime = System.currentTimeMillis() // Use current time as fallback
                            )
                            
                            // Save to Room database
                            val result = databaseService.saveAlarmState(alarmState)
                            if (result.isFailure) {
                                // Log migration error for this specific alarm
                                result.exceptionOrNull()?.printStackTrace()
                            }
                        }
                        
                        // Mark migration as completed
                        sharedPrefs.edit().putBoolean("migration_to_room_completed", true).apply()
                        
                        // Optionally clear old SharedPreferences data after successful migration
                        clearSharedPreferencesAlarmData()
                        
                    } catch (e: Exception) {
                        // Log migration error but don't crash
                        e.printStackTrace()
                    }
                }
            } else {
                // No old data to migrate, just mark migration as done
                sharedPrefs.edit().putBoolean("migration_to_room_completed", true).apply()
            }
        } catch (e: Exception) {
            // Log error but don't crash the application
            e.printStackTrace()
        }
    }
    
    private fun clearSharedPreferencesAlarmData() {
        try {
            val editor = sharedPrefs.edit()
            
            // Get all alarm IDs to clear their data
            val allAlarmIds = sharedPrefs.getStringSet("all_alarm_ids", emptySet()) ?: emptySet()
            
            allAlarmIds.forEach { alarmIdStr ->
                val alarmId = alarmIdStr.toIntOrNull() ?: return@forEach
                editor.remove("alarm_info_$alarmId")
            }
            
            // Clear the alarm IDs set
            editor.remove("all_alarm_ids")
            
            // Apply changes
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notification channel and other helper methods maintain existing implementation

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Requires POST_NOTIFICATIONS permission
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 7-12: Check if notifications are enabled system-wide
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            val channelId = CHANNEL_ID_BASE // Use fixed channel ID
            
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    
                    // Set default sound and audio attributes
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(getAlarmSoundUri(), audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
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
                // Android 12+: Use VibratorManager for better control
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
            } else {
                // Android 7-11: Use VibrationEffect with legacy Vibrator
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
            }
        } catch (e: Exception) {
            // Vibration failed, ignore
        }
    }
}

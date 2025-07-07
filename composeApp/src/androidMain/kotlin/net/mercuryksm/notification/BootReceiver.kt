package net.mercuryksm.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.database.DatabaseServiceFactory
import net.mercuryksm.data.database.getRoomDatabase

class BootReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val pendingResult = goAsync()
            
            scope.launch {
                try {
                    rescheduleAllAlarms(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
    
    private suspend fun rescheduleAllAlarms(context: Context) {
        try {
            val databaseService = DatabaseServiceFactory(context).createSignalDatabaseService()
            val alarmManager = AndroidSignalAlarmManager(context, databaseService)
            val sharedPrefs = context.getSharedPreferences("weekly_signal_alarms", Context.MODE_PRIVATE)
            
            // Get all saved alarm IDs
            val allAlarmIds = sharedPrefs.getStringSet("all_alarm_ids", emptySet()) ?: emptySet()
            
            allAlarmIds.forEach { alarmIdStr ->
                val alarmId = alarmIdStr.toIntOrNull() ?: return@forEach
                val alarmInfoJson = sharedPrefs.getString("alarm_info_$alarmId", null) ?: return@forEach
                
                try {
                    // Restore alarm information
                    val alarmInfo = json.decodeFromString<AndroidSignalAlarmManager.AlarmInfo>(alarmInfoJson)
                    
                    // Reconstruct TimeSlot
                    val timeSlot = TimeSlot(
                        id = alarmInfo.timeSlotId,
                        hour = alarmInfo.hour,
                        minute = alarmInfo.minute,
                        dayOfWeek = DayOfWeekJp.entries[alarmInfo.dayOfWeek]
                    )
                    
                    // Reconstruct AlarmSettings
                    val settings = AlarmSettings(
                        sound = alarmInfo.sound,
                        vibration = alarmInfo.vibration,
                        title = alarmInfo.title,
                        message = alarmInfo.message,
                        alarmId = alarmInfo.alarmId
                    )
                    
                    // Reschedule alarm
                    alarmManager.scheduleAlarm(timeSlot, settings)
                    
                } catch (e: Exception) {
                    // Log error and continue if individual alarm restoration fails
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            // For general errors
            e.printStackTrace()
        }
    }
}
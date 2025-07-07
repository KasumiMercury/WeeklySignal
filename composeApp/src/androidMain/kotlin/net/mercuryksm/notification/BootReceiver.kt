package net.mercuryksm.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mercuryksm.data.database.DatabaseServiceFactory

class BootReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
            
            // Get all scheduled alarm states from Room database
            val scheduledAlarmsResult = databaseService.getAllScheduledAlarmStates()
            
            if (scheduledAlarmsResult.isSuccess) {
                val scheduledAlarms = scheduledAlarmsResult.getOrNull() ?: emptyList()
                
                scheduledAlarms.forEach { alarmState ->
                    try {
                        // Get the SignalItem for this alarm state
                        val signalItemResult = databaseService.getSignalItemById(alarmState.signalItemId)
                        
                        if (signalItemResult.isSuccess) {
                            val signalItem = signalItemResult.getOrNull()
                            
                            if (signalItem != null) {
                                // Find the TimeSlot that matches this alarm state
                                val timeSlot = signalItem.timeSlots.find { it.id == alarmState.timeSlotId }
                                
                                if (timeSlot != null) {
                                    // Reconstruct AlarmSettings
                                    val settings = AlarmSettings(
                                        sound = signalItem.sound,
                                        vibration = signalItem.vibration,
                                        title = signalItem.name,
                                        message = signalItem.description,
                                        alarmId = "${signalItem.id}_${timeSlot.id}"
                                    )
                                    
                                    // Reschedule alarm
                                    alarmManager.scheduleAlarm(timeSlot, settings)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Log error and continue if individual alarm restoration fails
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            // For general errors
            e.printStackTrace()
        }
    }
}
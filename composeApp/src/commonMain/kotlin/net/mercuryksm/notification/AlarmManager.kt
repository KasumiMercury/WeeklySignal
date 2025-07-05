package net.mercuryksm.notification

import net.mercuryksm.data.TimeSlot

interface SignalAlarmManager {
    /**
     * Schedules an alarm for the specified time slot
     */
    suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmResult
    
    /**
     * Cancels a specific alarm
     */
    suspend fun cancelAlarm(alarmId: String): AlarmResult
    
    /**
     * Cancels all scheduled alarms
     */
    suspend fun cancelAllAlarms(): AlarmResult
    
    /**
     * Gets list of scheduled alarms
     */
    suspend fun getScheduledAlarms(): List<String>
    
    /**
     * Shows an immediate test alarm for preview purposes
     */
    suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult
    
    /**
     * Checks if alarm permissions are granted
     */
    suspend fun hasAlarmPermission(): Boolean
    
    /**
     * Requests alarm permissions (Android 12+)
     */
    suspend fun requestAlarmPermission(): Boolean
    
    /**
     * Checks if alarm features are supported on this platform
     */
    fun isAlarmSupported(): Boolean
}
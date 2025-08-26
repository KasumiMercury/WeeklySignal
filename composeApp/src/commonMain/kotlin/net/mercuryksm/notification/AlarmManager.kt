package net.mercuryksm.notification

import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot

interface SignalAlarmManager {
    /**
     * Schedules an alarm for the specified time slot
     */
    suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmSchedulingInfo
    
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
    
    /**
     * Sets the permission helper for managing notification and alarm permissions
     */
    fun setPermissionHelper(helper: PermissionHelper) {
        // Default implementation - platform specific overrides will provide actual functionality
    }
    
    /**
     * Schedules alarms for all time slots in a SignalItem
     */
    suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): List<AlarmSchedulingInfo> {
        return signalItem.timeSlots.map { 
            AlarmSchedulingInfo(
                timeSlotId = it.id,
                pendingIntentRequestCode = -1,
                nextAlarmTime = -1,
                result = AlarmResult.NOT_SUPPORTED
            )
        } // Default implementation - platform specific overrides will provide actual functionality
    }
    
    /**
     * Cancels all alarms for a SignalItem
     */
    suspend fun cancelSignalItemAlarms(signalItem: SignalItem): List<AlarmResult> {
        return signalItem.timeSlots.map { AlarmResult.NOT_SUPPORTED } // Default implementation
    }
    
    /**
     * Updates alarms when a SignalItem is modified
     */
    suspend fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem): List<AlarmSchedulingInfo> {
        return newSignalItem.timeSlots.map { 
            AlarmSchedulingInfo(
                timeSlotId = it.id,
                pendingIntentRequestCode = -1,
                nextAlarmTime = -1,
                result = AlarmResult.NOT_SUPPORTED
            )
        } // Default implementation
    }
    
    /**
     * Checks if alarms are enabled for a specific SignalItem
     */
    suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return false // Default implementation
    }
}

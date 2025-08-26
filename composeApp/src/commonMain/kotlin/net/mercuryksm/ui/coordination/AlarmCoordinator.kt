package net.mercuryksm.ui.coordination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.notification.AlarmOperationResult
import net.mercuryksm.notification.AlarmResult
import net.mercuryksm.notification.SignalAlarmManager

/**
 * Coordinates alarm management operations.
 * Extracted from WeeklySignalViewModel to follow single responsibility principle.
 */
class AlarmCoordinator(
    private val alarmManager: SignalAlarmManager?,
) {
    
    /**
     * Schedule alarms for a SignalItem
     * Returns true if all alarms were successfully scheduled, false otherwise
     */
    suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): List<AlarmOperationResult>? {
        return try {
            alarmManager?.let { manager ->
                val results = manager.scheduleSignalItemAlarms(signalItem)
                // Check if all alarm scheduling operations were successful or not supported (desktop)
                if (results.all { it.result == AlarmResult.SUCCESS || it.result == AlarmResult.NOT_SUPPORTED }) {
                    results
                } else {
                    null
                }
            } // Return null if no alarm manager (e.g., on desktop), though the logic above handles it
        } catch (e: Exception) {
            // Log alarm scheduling error
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Cancel alarms for a SignalItem
     * Returns true if all alarms were successfully cancelled, false otherwise
     */
    suspend fun cancelSignalItemAlarms(signalItem: SignalItem): Boolean {
        return try {
            alarmManager?.let { manager ->
                val results = manager.cancelSignalItemAlarms(signalItem)
                // Check if all alarm cancellations were successful or not supported (desktop)
                results.all { result ->
                    result == AlarmResult.SUCCESS ||
                    result == AlarmResult.NOT_SUPPORTED
                }
            } ?: true // Return true if no alarm manager (e.g., on desktop)
        } catch (e: Exception) {
            // Log alarm cancellation error
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Update alarms when a SignalItem is modified
     * Returns true if all alarms were successfully updated, false otherwise
     */
    suspend fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem): List<AlarmOperationResult>? {
        return try {
            alarmManager?.let { manager ->
                val results = manager.updateSignalItemAlarms(oldSignalItem, newSignalItem)
                if (results.all { it.result == AlarmResult.SUCCESS || it.result == AlarmResult.NOT_SUPPORTED }) {
                    results
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            // Log alarm update error
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Schedule alarms for multiple SignalItems (batch operation)
     * Returns true if all alarms were successfully scheduled, false otherwise
     */
    suspend fun scheduleSignalItemsAlarms(signalItems: List<SignalItem>): Boolean {
        return try {
            var allSuccessful = true
            signalItems.forEach { signalItem ->
                val results = scheduleSignalItemAlarms(signalItem)
                if (results == null) allSuccessful = false
            }
            allSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Cancel alarms for multiple SignalItems (batch operation)
     * Returns true if all alarms were successfully cancelled, false otherwise
     */
    suspend fun cancelSignalItemsAlarms(signalItems: List<SignalItem>): Boolean {
        return try {
            var allSuccessful = true
            signalItems.forEach { signalItem ->
                val success = cancelSignalItemAlarms(signalItem)
                if (!success) allSuccessful = false
            }
            allSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Cancel alarm for a specific TimeSlot within a SignalItem
     */
    suspend fun cancelTimeSlotAlarm(signalItem: SignalItem, timeSlot: TimeSlot): Boolean {
        return try {
            alarmManager?.let { manager ->
                // Create a temporary SignalItem with only the specific TimeSlot
                val tempSignalItem = signalItem.copy(timeSlots = listOf(timeSlot))
                val results = manager.cancelSignalItemAlarms(tempSignalItem)
                // Check if the alarm cancellation was successful or not supported (desktop)
                results.all { result ->
                    result == AlarmResult.SUCCESS ||
                    result == AlarmResult.NOT_SUPPORTED
                }
            } ?: true // Return true if no alarm manager (e.g., on desktop)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Check if alarms are enabled for a specific SignalItem
     */
    suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return alarmManager?.let { manager ->
            try {
                manager.isSignalItemAlarmsEnabled(signalItemId)
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
}

package net.mercuryksm.ui.coordination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mercuryksm.data.SignalItem
import net.mercuryksm.notification.SignalAlarmManager

/**
 * Coordinates alarm management operations.
 * Extracted from WeeklySignalViewModel to follow single responsibility principle.
 */
class AlarmCoordinator(
    private val alarmManager: SignalAlarmManager?,
    private val coroutineScope: CoroutineScope
) {
    
    /**
     * Schedule alarms for a SignalItem
     */
    fun scheduleSignalItemAlarms(signalItem: SignalItem) {
        coroutineScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.scheduleSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    // Log alarm scheduling error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Cancel alarms for a SignalItem
     */
    fun cancelSignalItemAlarms(signalItem: SignalItem) {
        coroutineScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.cancelSignalItemAlarms(signalItem)
                } catch (e: Exception) {
                    // Log alarm cancellation error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Update alarms when a SignalItem is modified
     */
    fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem) {
        coroutineScope.launch {
            alarmManager?.let { manager ->
                try {
                    manager.updateSignalItemAlarms(oldSignalItem, newSignalItem)
                } catch (e: Exception) {
                    // Log alarm update error but don't fail the overall operation
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Schedule alarms for multiple SignalItems (batch operation)
     */
    fun scheduleSignalItemsAlarms(signalItems: List<SignalItem>) {
        signalItems.forEach { signalItem ->
            scheduleSignalItemAlarms(signalItem)
        }
    }
    
    /**
     * Cancel alarms for multiple SignalItems (batch operation)
     */
    fun cancelSignalItemsAlarms(signalItems: List<SignalItem>) {
        signalItems.forEach { signalItem ->
            cancelSignalItemAlarms(signalItem)
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
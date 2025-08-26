package net.mercuryksm.data.database

import net.mercuryksm.data.SignalItem

interface SignalDatabaseService {
    suspend fun saveSignalItemWithAlarms(signalItem: SignalItem, schedulingResults: List<net.mercuryksm.notification.AlarmOperationResult>): Result<Unit>
    suspend fun updateSignalItemWithAlarms(signalItem: SignalItem, schedulingResults: List<net.mercuryksm.notification.AlarmOperationResult>): Result<Unit>
    suspend fun deleteSignalItem(signalId: String): Result<Unit>
    suspend fun getSignalItemById(signalId: String): Result<SignalItem?>
    suspend fun getAllSignalItems(): Result<List<SignalItem>>
    suspend fun clearAllSignalItems(): Result<Unit>
    suspend fun clearAllData(): Result<Unit>
    
    // Batch operations with transaction support
    suspend fun saveSignalItemsInTransaction(signalItems: List<SignalItem>): Result<Unit>
    suspend fun updateSignalItemsWithAlarms(signalItems: List<SignalItem>, schedulingResults: List<net.mercuryksm.notification.AlarmOperationResult>): Result<Unit>
    suspend fun deleteSignalItemsInTransaction(signalIds: List<String>): Result<Unit>
    suspend fun importSignalItemsWithConflictResolution(
        itemsToInsert: List<SignalItem>,
        itemsToUpdate: List<SignalItem>
    ): Result<Unit>
    
    // Alarm state management methods
    suspend fun saveAlarmState(alarmState: AlarmStateEntity): Result<Unit>
    suspend fun getAlarmStateByTimeSlotId(timeSlotId: String): Result<AlarmStateEntity?>
    suspend fun getAlarmStatesBySignalItemId(signalItemId: String): Result<List<AlarmStateEntity>>
    suspend fun getAllScheduledAlarmStates(): Result<List<AlarmStateEntity>>
    suspend fun deleteAlarmState(timeSlotId: String): Result<Unit>
    suspend fun deleteAlarmStatesBySignalItemId(signalItemId: String): Result<Unit>
    suspend fun updateAlarmScheduledStatus(timeSlotId: String, isScheduled: Boolean): Result<Unit>
    suspend fun updateAlarmNextTime(timeSlotId: String, nextAlarmTime: Long): Result<Unit>
}
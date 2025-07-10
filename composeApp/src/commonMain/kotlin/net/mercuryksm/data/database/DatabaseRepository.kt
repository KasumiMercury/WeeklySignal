package net.mercuryksm.data.database

interface DatabaseRepository {
    suspend fun insertSignal(signalEntity: SignalEntity): Long
    suspend fun insertTimeSlot(timeSlotEntity: TimeSlotEntity): Long
    suspend fun updateSignal(signalEntity: SignalEntity)
    suspend fun updateTimeSlot(timeSlotEntity: TimeSlotEntity)
    suspend fun deleteSignal(signalId: String)
    suspend fun deleteTimeSlot(timeSlotId: String)
    suspend fun deleteTimeSlotsBySignalId(signalId: String)
    suspend fun getSignalById(signalId: String): SignalEntity?
    suspend fun getAllSignals(): List<SignalEntity>
    suspend fun getTimeSlotsBySignalId(signalId: String): List<TimeSlotEntity>
    suspend fun getAllTimeSlots(): List<TimeSlotEntity>
    
    // Alarm state management methods
    suspend fun insertOrUpdateAlarmState(alarmState: AlarmStateEntity)
    suspend fun getAlarmStateByTimeSlotId(timeSlotId: String): AlarmStateEntity?
    suspend fun getAlarmStatesBySignalItemId(signalItemId: String): List<AlarmStateEntity>
    suspend fun getAllScheduledAlarmStates(): List<AlarmStateEntity>
    suspend fun deleteAlarmState(timeSlotId: String)
    suspend fun deleteAlarmStatesBySignalItemId(signalItemId: String)
    suspend fun updateAlarmScheduledStatus(timeSlotId: String, isScheduled: Boolean)
    suspend fun updateAlarmNextTime(timeSlotId: String, nextAlarmTime: Long)
    
    // Transaction support using Room KMP 2.7.2 API
    suspend fun <T> withTransaction(block: suspend () -> T): T
    
    // DAO access for @Transaction methods
    fun getSignalDao(): SignalDao
}
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
}
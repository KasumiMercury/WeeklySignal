package net.mercuryksm.data.database

import android.content.Context

class AndroidDatabaseRepository(private val context: Context) : DatabaseRepository {
    
    override suspend fun insertSignal(signalEntity: SignalEntity): Long {
        TODO("Implement")
    }
    
    override suspend fun insertTimeSlot(timeSlotEntity: TimeSlotEntity): Long {
        TODO("Implement")
    }
    
    override suspend fun updateSignal(signalEntity: SignalEntity) {
        TODO("Implement")
    }
    
    override suspend fun updateTimeSlot(timeSlotEntity: TimeSlotEntity) {
        TODO("Implement")
    }
    
    override suspend fun deleteSignal(signalId: String) {
        TODO("Implement")
    }
    
    override suspend fun deleteTimeSlot(timeSlotId: String) {
        TODO("Implement")
    }
    
    override suspend fun deleteTimeSlotsBySignalId(signalId: String) {
        TODO("Implement")
    }
    
    override suspend fun getSignalById(signalId: String): SignalEntity? {
        TODO("Implement")
    }
    
    override suspend fun getAllSignals(): List<SignalEntity> {
        TODO("Implement")
    }
    
    override suspend fun getTimeSlotsBySignalId(signalId: String): List<TimeSlotEntity> {
        TODO("Implement")
    }
    
    override suspend fun getAllTimeSlots(): List<TimeSlotEntity> {
        TODO("Implement")
    }
}

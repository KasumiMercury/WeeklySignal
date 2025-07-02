package net.mercuryksm.data.database

class MockDatabaseRepository : DatabaseRepository {
    
    private val signals = mutableMapOf<String, SignalEntity>()
    private val timeSlots = mutableMapOf<String, TimeSlotEntity>()
    
    override suspend fun insertSignal(signalEntity: SignalEntity): Long {
        signals[signalEntity.id] = signalEntity
        return 1L
    }
    
    override suspend fun insertTimeSlot(timeSlotEntity: TimeSlotEntity): Long {
        timeSlots[timeSlotEntity.id] = timeSlotEntity
        return 1L
    }
    
    override suspend fun updateSignal(signalEntity: SignalEntity) {
        signals[signalEntity.id] = signalEntity
    }
    
    override suspend fun updateTimeSlot(timeSlotEntity: TimeSlotEntity) {
        timeSlots[timeSlotEntity.id] = timeSlotEntity
    }
    
    override suspend fun deleteSignal(signalId: String) {
        signals.remove(signalId)
    }
    
    override suspend fun deleteTimeSlot(timeSlotId: String) {
        timeSlots.remove(timeSlotId)
    }
    
    override suspend fun deleteTimeSlotsBySignalId(signalId: String) {
        timeSlots.values.removeAll { it.signalId == signalId }
    }
    
    override suspend fun getSignalById(signalId: String): SignalEntity? {
        return signals[signalId]
    }
    
    override suspend fun getAllSignals(): List<SignalEntity> {
        return signals.values.toList()
    }
    
    override suspend fun getTimeSlotsBySignalId(signalId: String): List<TimeSlotEntity> {
        return timeSlots.values.filter { it.signalId == signalId }
    }
    
    override suspend fun getAllTimeSlots(): List<TimeSlotEntity> {
        return timeSlots.values.toList()
    }
    
    fun clear() {
        signals.clear()
        timeSlots.clear()
    }
}
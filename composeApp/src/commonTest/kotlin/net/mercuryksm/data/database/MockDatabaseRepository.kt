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
    
    // Alarm state management methods - minimal implementation for testing
    override suspend fun insertOrUpdateAlarmState(alarmState: AlarmStateEntity) {
        // Mock implementation - do nothing
    }
    
    override suspend fun getAlarmStateByTimeSlotId(timeSlotId: String): AlarmStateEntity? {
        return null
    }
    
    override suspend fun getAlarmStatesBySignalItemId(signalItemId: String): List<AlarmStateEntity> {
        return emptyList()
    }
    
    override suspend fun getAllScheduledAlarmStates(): List<AlarmStateEntity> {
        return emptyList()
    }
    
    override suspend fun deleteAlarmState(timeSlotId: String) {
        // Mock implementation - do nothing
    }
    
    override suspend fun deleteAlarmStatesBySignalItemId(signalItemId: String) {
        // Mock implementation - do nothing
    }
    
    override suspend fun updateAlarmScheduledStatus(timeSlotId: String, isScheduled: Boolean) {
        // Mock implementation - do nothing
    }
    
    override suspend fun updateAlarmNextTime(timeSlotId: String, nextAlarmTime: Long) {
        // Mock implementation - do nothing
    }
    
    override suspend fun <T> withTransaction(block: suspend () -> T): T {
        // Mock implementation - simulates transaction by executing the block
        // In a real test environment, this could track transaction calls
        return block()
    }
    
    override fun getSignalDao(): SignalDao {
        // Mock implementation - return a mock DAO
        return object : SignalDao {
            override suspend fun insert(signal: SignalEntity): Long = 1L
            override suspend fun update(signal: SignalEntity) {}
            override suspend fun delete(signalId: String) {}
            override suspend fun getById(signalId: String): SignalEntity? = null
            override suspend fun getAll(): List<SignalEntity> = emptyList()
            override suspend fun insertSignalWithTimeSlots(signal: SignalEntity, timeSlots: List<TimeSlotEntity>) {}
            override suspend fun updateSignalWithTimeSlots(signal: SignalEntity, timeSlots: List<TimeSlotEntity>) {}
            override suspend fun deleteSignalWithTimeSlots(signalId: String) {}
            override suspend fun insertMultipleSignalsWithTimeSlots(signalsWithTimeSlots: List<Pair<SignalEntity, List<TimeSlotEntity>>>) {}
            override suspend fun updateMultipleSignalsWithTimeSlots(signalsWithTimeSlots: List<Pair<SignalEntity, List<TimeSlotEntity>>>) {}
            override suspend fun insertTimeSlot(timeSlot: TimeSlotEntity): Long = 1L
            override suspend fun deleteTimeSlotsBySignalId(signalId: String) {}
        }
    }
}
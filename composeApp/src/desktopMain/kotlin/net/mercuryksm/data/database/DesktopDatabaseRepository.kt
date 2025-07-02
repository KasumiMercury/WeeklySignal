package net.mercuryksm.data.database

class DesktopDatabaseRepository : DatabaseRepository {
    
    private val database: AppDatabase by lazy {
        getDatabaseBuilder().build()
    }
    
    private val signalDao: SignalDao by lazy { database.signalDao() }
    private val timeSlotDao: TimeSlotDao by lazy { database.timeSlotDao() }
    
    override suspend fun insertSignal(signalEntity: SignalEntity): Long {
        return signalDao.insert(signalEntity)
    }
    
    override suspend fun insertTimeSlot(timeSlotEntity: TimeSlotEntity): Long {
        return timeSlotDao.insert(timeSlotEntity)
    }
    
    override suspend fun updateSignal(signalEntity: SignalEntity) {
        signalDao.update(signalEntity)
    }
    
    override suspend fun updateTimeSlot(timeSlotEntity: TimeSlotEntity) {
        timeSlotDao.update(timeSlotEntity)
    }
    
    override suspend fun deleteSignal(signalId: String) {
        signalDao.delete(signalId)
    }
    
    override suspend fun deleteTimeSlot(timeSlotId: String) {
        timeSlotDao.delete(timeSlotId)
    }
    
    override suspend fun deleteTimeSlotsBySignalId(signalId: String) {
        timeSlotDao.deleteBySignalId(signalId)
    }
    
    override suspend fun getSignalById(signalId: String): SignalEntity? {
        return signalDao.getById(signalId)
    }
    
    override suspend fun getAllSignals(): List<SignalEntity> {
        return signalDao.getAll()
    }
    
    override suspend fun getTimeSlotsBySignalId(signalId: String): List<TimeSlotEntity> {
        return timeSlotDao.getBySignalId(signalId)
    }
    
    override suspend fun getAllTimeSlots(): List<TimeSlotEntity> {
        return timeSlotDao.getAll()
    }
}

package net.mercuryksm.data.database

import android.content.Context

class AndroidDatabaseRepository(private val context: Context) : DatabaseRepository {
    
    private val database: AppDatabase by lazy {
        getDatabaseBuilder(context).build()
    }
    
    private val _signalDao: SignalDao by lazy { database.signalDao() }
    private val timeSlotDao: TimeSlotDao by lazy { database.timeSlotDao() }
    private val alarmStateDao: AlarmStateDao by lazy { database.alarmStateDao() }
    
    override suspend fun insertSignal(signalEntity: SignalEntity): Long {
        return _signalDao.insert(signalEntity)
    }
    
    override suspend fun insertTimeSlot(timeSlotEntity: TimeSlotEntity): Long {
        return timeSlotDao.insert(timeSlotEntity)
    }
    
    override suspend fun updateSignal(signalEntity: SignalEntity) {
        _signalDao.update(signalEntity)
    }
    
    override suspend fun updateTimeSlot(timeSlotEntity: TimeSlotEntity) {
        timeSlotDao.update(timeSlotEntity)
    }
    
    override suspend fun deleteSignal(signalId: String) {
        _signalDao.delete(signalId)
    }
    
    override suspend fun deleteTimeSlot(timeSlotId: String) {
        timeSlotDao.delete(timeSlotId)
    }
    
    override suspend fun deleteTimeSlotsBySignalId(signalId: String) {
        timeSlotDao.deleteBySignalId(signalId)
    }
    
    override suspend fun getSignalById(signalId: String): SignalEntity? {
        return _signalDao.getById(signalId)
    }
    
    override suspend fun getAllSignals(): List<SignalEntity> {
        return _signalDao.getAll()
    }
    
    override suspend fun getTimeSlotsBySignalId(signalId: String): List<TimeSlotEntity> {
        return timeSlotDao.getBySignalId(signalId)
    }
    
    override suspend fun getAllTimeSlots(): List<TimeSlotEntity> {
        return timeSlotDao.getAll()
    }
    
    // Alarm state management methods
    override suspend fun insertOrUpdateAlarmState(alarmState: AlarmStateEntity) {
        alarmStateDao.insertOrUpdate(alarmState)
    }
    
    override suspend fun getAlarmStateByTimeSlotId(timeSlotId: String): AlarmStateEntity? {
        return alarmStateDao.getByTimeSlotId(timeSlotId)
    }
    
    override suspend fun getAlarmStatesBySignalItemId(signalItemId: String): List<AlarmStateEntity> {
        return alarmStateDao.getAlarmStatesBySignalItemId(signalItemId)
    }
    
    override suspend fun getAllScheduledAlarmStates(): List<AlarmStateEntity> {
        return alarmStateDao.getAllScheduledAlarms()
    }
    
    override suspend fun deleteAlarmState(timeSlotId: String) {
        alarmStateDao.delete(timeSlotId)
    }
    
    override suspend fun deleteAlarmStatesBySignalItemId(signalItemId: String) {
        alarmStateDao.deleteBySignalId(signalItemId)
    }
    
    override suspend fun updateAlarmScheduledStatus(timeSlotId: String, isScheduled: Boolean) {
        alarmStateDao.updateScheduledStatus(timeSlotId, isScheduled)
    }
    
    override suspend fun updateAlarmNextTime(timeSlotId: String, nextAlarmTime: Long) {
        alarmStateDao.updateNextAlarmTime(timeSlotId, nextAlarmTime)
    }
    
    override suspend fun <T> withTransaction(block: suspend () -> T): T {
        // Room KMP 2.7.2: Transaction support is implemented at DAO level using @Transaction
        // This method is maintained for interface compatibility but not used in current implementation
        // All database operations use @Transaction annotated DAO methods for true ACID semantics
        return try {
            block()
        } catch (e: Exception) {
            throw e
        }
    }
    
    override fun getSignalDao(): SignalDao {
        return _signalDao
    }
}

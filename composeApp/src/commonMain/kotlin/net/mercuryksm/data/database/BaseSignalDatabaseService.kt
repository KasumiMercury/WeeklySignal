package net.mercuryksm.data.database

import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.database.EntityMappers.toSignalEntity
import net.mercuryksm.data.database.EntityMappers.toSignalItems
import net.mercuryksm.data.database.EntityMappers.toTimeSlotEntity

/**
 * Base implementation for SignalDatabaseService that provides common database operations.
 * Platform-specific implementations only need to provide the DatabaseRepository instance.
 */
abstract class BaseSignalDatabaseService : SignalDatabaseService {
    
    /**
     * Platform-specific database repository instance.
     * Must be implemented by concrete platform classes.
     */
    protected abstract val databaseRepository: DatabaseRepository
    
    /**
     * Helper function to execute database operations with consistent error handling.
     */
    protected inline fun <T> executeWithResult(operation: () -> T): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveSignalItem(signalItem: SignalItem): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            val signalEntity = signalItem.toSignalEntity()
            val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                timeSlot.toTimeSlotEntity(signalItem.id)
            }
            signalDao.insertSignalWithTimeSlots(signalEntity, timeSlotEntities)
        }
    }
    
    override suspend fun updateSignalItem(signalItem: SignalItem): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            val signalEntity = signalItem.toSignalEntity()
            val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                timeSlot.toTimeSlotEntity(signalItem.id)
            }
            signalDao.updateSignalWithTimeSlots(signalEntity, timeSlotEntities)
        }
    }
    
    override suspend fun deleteSignalItem(signalId: String): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            signalDao.deleteSignalWithTimeSlots(signalId)
        }
    }
    
    override suspend fun getSignalItemById(signalId: String): Result<SignalItem?> {
        return executeWithResult {
            val signalEntity = databaseRepository.getSignalById(signalId)
            if (signalEntity == null) {
                null
            } else {
                val timeSlotEntities = databaseRepository.getTimeSlotsBySignalId(signalId)
                val timeSlotsBySignalId = mapOf(signalId to timeSlotEntities)
                listOf(signalEntity).toSignalItems(timeSlotsBySignalId).firstOrNull()
            }
        }
    }
    
    override suspend fun getAllSignalItems(): Result<List<SignalItem>> {
        return executeWithResult {
            val signalEntities = databaseRepository.getAllSignals()
            val allTimeSlots = databaseRepository.getAllTimeSlots()
            val timeSlotsBySignalId = allTimeSlots.groupBy { it.signalId }
            
            signalEntities.toSignalItems(timeSlotsBySignalId)
        }
    }
    
    override suspend fun clearAllSignalItems(): Result<Unit> {
        return executeWithResult {
            val signalEntities = databaseRepository.getAllSignals()
            signalEntities.forEach { signal ->
                databaseRepository.deleteTimeSlotsBySignalId(signal.id)
                databaseRepository.deleteSignal(signal.id)
            }
        }
    }
    
    override suspend fun clearAllData(): Result<Unit> {
        return executeWithResult {
            val signalEntities = databaseRepository.getAllSignals()
            signalEntities.forEach { signal ->
                databaseRepository.deleteTimeSlotsBySignalId(signal.id)
                databaseRepository.deleteSignal(signal.id)
            }
        }
    }
    
    // Alarm state management methods
    override suspend fun saveAlarmState(alarmState: AlarmStateEntity): Result<Unit> {
        return executeWithResult {
            databaseRepository.insertOrUpdateAlarmState(alarmState)
        }
    }
    
    override suspend fun getAlarmStateByTimeSlotId(timeSlotId: String): Result<AlarmStateEntity?> {
        return executeWithResult {
            databaseRepository.getAlarmStateByTimeSlotId(timeSlotId)
        }
    }
    
    override suspend fun getAlarmStatesBySignalItemId(signalItemId: String): Result<List<AlarmStateEntity>> {
        return executeWithResult {
            databaseRepository.getAlarmStatesBySignalItemId(signalItemId)
        }
    }
    
    override suspend fun getAllScheduledAlarmStates(): Result<List<AlarmStateEntity>> {
        return executeWithResult {
            databaseRepository.getAllScheduledAlarmStates()
        }
    }
    
    override suspend fun deleteAlarmState(timeSlotId: String): Result<Unit> {
        return executeWithResult {
            databaseRepository.deleteAlarmState(timeSlotId)
        }
    }
    
    override suspend fun deleteAlarmStatesBySignalItemId(signalItemId: String): Result<Unit> {
        return executeWithResult {
            databaseRepository.deleteAlarmStatesBySignalItemId(signalItemId)
        }
    }
    
    override suspend fun updateAlarmScheduledStatus(timeSlotId: String, isScheduled: Boolean): Result<Unit> {
        return executeWithResult {
            databaseRepository.updateAlarmScheduledStatus(timeSlotId, isScheduled)
        }
    }
    
    override suspend fun updateAlarmNextTime(timeSlotId: String, nextAlarmTime: Long): Result<Unit> {
        return executeWithResult {
            databaseRepository.updateAlarmNextTime(timeSlotId, nextAlarmTime)
        }
    }
    
    // Batch operations using @Transaction annotated DAO methods for true ACID semantics
    override suspend fun saveSignalItemsInTransaction(signalItems: List<SignalItem>): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            val signalsWithTimeSlots = signalItems.map { signalItem ->
                val signalEntity = signalItem.toSignalEntity()
                val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                    timeSlot.toTimeSlotEntity(signalItem.id)
                }
                Pair(signalEntity, timeSlotEntities)
            }
            signalDao.insertMultipleSignalsWithTimeSlots(signalsWithTimeSlots)
        }
    }
    
    override suspend fun updateSignalItemsInTransaction(signalItems: List<SignalItem>): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            val signalsWithTimeSlots = signalItems.map { signalItem ->
                val signalEntity = signalItem.toSignalEntity()
                val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                    timeSlot.toTimeSlotEntity(signalItem.id)
                }
                Pair(signalEntity, timeSlotEntities)
            }
            signalDao.updateMultipleSignalsWithTimeSlots(signalsWithTimeSlots)
        }
    }
    
    override suspend fun deleteSignalItemsInTransaction(signalIds: List<String>): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            // Use individual @Transaction methods for each deletion
            // This ensures proper transaction semantics for each SignalItem
            signalIds.forEach { signalId ->
                signalDao.deleteSignalWithTimeSlots(signalId)
            }
        }
    }
    
    override suspend fun importSignalItemsWithConflictResolution(
        itemsToInsert: List<SignalItem>,
        itemsToUpdate: List<SignalItem>
    ): Result<Unit> {
        return executeWithResult {
            val signalDao = databaseRepository.getSignalDao()
            
            // Insert new items using @Transaction method
            if (itemsToInsert.isNotEmpty()) {
                val insertsWithTimeSlots = itemsToInsert.map { signalItem ->
                    val signalEntity = signalItem.toSignalEntity()
                    val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                        timeSlot.toTimeSlotEntity(signalItem.id)
                    }
                    Pair(signalEntity, timeSlotEntities)
                }
                signalDao.insertMultipleSignalsWithTimeSlots(insertsWithTimeSlots)
            }
            
            // Update existing items using @Transaction method
            if (itemsToUpdate.isNotEmpty()) {
                val updatesWithTimeSlots = itemsToUpdate.map { signalItem ->
                    val signalEntity = signalItem.toSignalEntity()
                    val timeSlotEntities = signalItem.timeSlots.map { timeSlot ->
                        timeSlot.toTimeSlotEntity(signalItem.id)
                    }
                    Pair(signalEntity, timeSlotEntities)
                }
                signalDao.updateMultipleSignalsWithTimeSlots(updatesWithTimeSlots)
            }
        }
    }
}
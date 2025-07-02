package net.mercuryksm.data.database

import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.database.EntityMappers.toSignalEntity
import net.mercuryksm.data.database.EntityMappers.toSignalItems
import net.mercuryksm.data.database.EntityMappers.toTimeSlotEntity

class AndroidSignalDatabaseService(
    private val databaseRepository: DatabaseRepository
) : SignalDatabaseService {
    
    override suspend fun saveSignalItem(signalItem: SignalItem): Result<Unit> {
        return try {
            val signalEntity = signalItem.toSignalEntity()
            databaseRepository.insertSignal(signalEntity)
            
            signalItem.timeSlots.forEach { timeSlot ->
                val timeSlotEntity = timeSlot.toTimeSlotEntity(signalItem.id)
                databaseRepository.insertTimeSlot(timeSlotEntity)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSignalItem(signalItem: SignalItem): Result<Unit> {
        return try {
            val signalEntity = signalItem.toSignalEntity()
            databaseRepository.updateSignal(signalEntity)
            
            databaseRepository.deleteTimeSlotsBySignalId(signalItem.id)
            
            signalItem.timeSlots.forEach { timeSlot ->
                val timeSlotEntity = timeSlot.toTimeSlotEntity(signalItem.id)
                databaseRepository.insertTimeSlot(timeSlotEntity)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSignalItem(signalId: String): Result<Unit> {
        return try {
            databaseRepository.deleteTimeSlotsBySignalId(signalId)
            databaseRepository.deleteSignal(signalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSignalItemById(signalId: String): Result<SignalItem?> {
        return try {
            val signalEntity = databaseRepository.getSignalById(signalId)
            if (signalEntity == null) {
                Result.success(null)
            } else {
                val timeSlotEntities = databaseRepository.getTimeSlotsBySignalId(signalId)
                val timeSlotsBySignalId = mapOf(signalId to timeSlotEntities)
                val signalItem = listOf(signalEntity).toSignalItems(timeSlotsBySignalId).firstOrNull()
                Result.success(signalItem)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllSignalItems(): Result<List<SignalItem>> {
        return try {
            val signalEntities = databaseRepository.getAllSignals()
            val allTimeSlots = databaseRepository.getAllTimeSlots()
            val timeSlotsBySignalId = allTimeSlots.groupBy { it.signalId }
            
            val signalItems = signalEntities.toSignalItems(timeSlotsBySignalId)
            Result.success(signalItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllData(): Result<Unit> {
        return try {
            val signalEntities = databaseRepository.getAllSignals()
            signalEntities.forEach { signal ->
                databaseRepository.deleteTimeSlotsBySignalId(signal.id)
                databaseRepository.deleteSignal(signal.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
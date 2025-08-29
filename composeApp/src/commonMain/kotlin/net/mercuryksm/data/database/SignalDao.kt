package net.mercuryksm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SignalDao {
    @Insert
    suspend fun insert(signal: SignalEntity): Long

    @Update
    suspend fun update(signal: SignalEntity)

    @Query("DELETE FROM signals WHERE id = :signalId")
    suspend fun delete(signalId: String)

    @Query("SELECT * FROM signals WHERE id = :signalId")
    suspend fun getById(signalId: String): SignalEntity?

    @Query("SELECT * FROM signals")
    suspend fun getAll(): List<SignalEntity>
    
    // Transaction methods for batch operations
    @Transaction
    suspend fun insertSignalWithWithAlarms(
        signal: SignalEntity,
        timeSlots: List<TimeSlotEntity>,
        alarmStates: List<AlarmStateEntity>
    ) {
        insert(signal)
        timeSlots.forEach { timeSlot ->
            insertTimeSlot(timeSlot)
        }
        alarmStates.forEach { alarmState ->
            insertAlarmState(alarmState)
        }
    }

    @Transaction
    suspend fun insertSignalWithTimeSlots(
        signal: SignalEntity,
        timeSlots: List<TimeSlotEntity>
    ) {
        insert(signal)
        timeSlots.forEach { timeSlot ->
            insertTimeSlot(timeSlot)
        }
    }
    
    @Transaction
    suspend fun updateSignalWithWithAlarms(
        signal: SignalEntity,
        timeSlots: List<TimeSlotEntity>,
        alarmStates: List<AlarmStateEntity>
    ) {
        update(signal)
        deleteTimeSlotsBySignalId(signal.id)
        // This will also delete corresponding alarm states due to CASCADE DELETE
        timeSlots.forEach { timeSlot ->
            insertTimeSlot(timeSlot)
        }
        alarmStates.forEach { alarmState ->
            insertAlarmState(alarmState)
        }
    }

    @Transaction
    suspend fun updateSignalWithTimeSlots(
        signal: SignalEntity,
        timeSlots: List<TimeSlotEntity>
    ) {
        update(signal)
        deleteTimeSlotsBySignalId(signal.id)
        timeSlots.forEach { timeSlot ->
            insertTimeSlot(timeSlot)
        }
    }
    
    @Transaction
    suspend fun deleteSignalWithTimeSlots(signalId: String) {
        deleteTimeSlotsBySignalId(signalId)
        delete(signalId)
    }
    
    @Transaction
    suspend fun insertMultipleSignalsWithTimeSlots(
        signalsWithTimeSlots: List<Pair<SignalEntity, List<TimeSlotEntity>>>
    ) {
        signalsWithTimeSlots.forEach { (signal, timeSlots) ->
            insert(signal)
            timeSlots.forEach { timeSlot ->
                insertTimeSlot(timeSlot)
            }
        }
    }
    
    @Transaction
    suspend fun updateMultipleSignalsWithWithAlarms(
        signalsWithTimeSlots: List<Pair<SignalEntity, List<TimeSlotEntity>>>,
        alarmStates: List<AlarmStateEntity>
    ) {
        signalsWithTimeSlots.forEach { (signal, timeSlots) ->
            update(signal)
            deleteTimeSlotsBySignalId(signal.id)
            timeSlots.forEach { timeSlot ->
                insertTimeSlot(timeSlot)
            }
        }
        // Assuming old alarm states are deleted by CASCADE
        alarmStates.forEach { alarmState ->
            insertAlarmState(alarmState)
        }
    }

    @Transaction
    suspend fun updateMultipleSignalsWithTimeSlots(
        signalsWithTimeSlots: List<Pair<SignalEntity, List<TimeSlotEntity>>>
    ) {
        signalsWithTimeSlots.forEach { (signal, timeSlots) ->
            update(signal)
            deleteTimeSlotsBySignalId(signal.id)
            timeSlots.forEach { timeSlot ->
                insertTimeSlot(timeSlot)
            }
        }
    }
    
    @Insert
    suspend fun insertAlarmState(alarmState: AlarmStateEntity)

    // Helper methods for time slot operations (these will be called from transaction methods)
    @Insert
    suspend fun insertTimeSlot(timeSlot: TimeSlotEntity): Long
    
    @Query("DELETE FROM time_slots WHERE signalId = :signalId")
    suspend fun deleteTimeSlotsBySignalId(signalId: String)
}
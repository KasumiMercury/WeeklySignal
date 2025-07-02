package net.mercuryksm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimeSlotDao {
    @Insert
    suspend fun insert(timeSlot: TimeSlotEntity): Long

    @Update
    suspend fun update(timeSlot: TimeSlotEntity)

    @Query("DELETE FROM time_slots WHERE id = :timeSlotId")
    suspend fun delete(timeSlotId: String)

    @Query("DELETE FROM time_slots WHERE signalId = :signalId")
    suspend fun deleteBySignalId(signalId: String)

    @Query("SELECT * FROM time_slots WHERE signalId = :signalId")
    suspend fun getBySignalId(signalId: String): List<TimeSlotEntity>

    @Query("SELECT * FROM time_slots")
    suspend fun getAll(): List<TimeSlotEntity>
}
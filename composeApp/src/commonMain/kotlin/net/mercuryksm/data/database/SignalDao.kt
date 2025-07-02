package net.mercuryksm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
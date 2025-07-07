package net.mercuryksm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlarmStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(alarmState: AlarmStateEntity)

    @Query("SELECT * FROM alarm_states WHERE timeSlotId = :timeSlotId")
    suspend fun getByTimeSlotId(timeSlotId: String): AlarmStateEntity?

    @Query("DELETE FROM alarm_states WHERE timeSlotId = :timeSlotId")
    suspend fun delete(timeSlotId: String)

    @Query("DELETE FROM alarm_states WHERE timeSlotId IN (SELECT id FROM time_slots WHERE signalId = :signalId)")
    suspend fun deleteBySignalId(signalId: String)

    @Query("SELECT * FROM alarm_states WHERE isAlarmScheduled = 1")
    suspend fun getAllScheduledAlarms(): List<AlarmStateEntity>

    @Query("SELECT * FROM alarm_states")
    suspend fun getAll(): List<AlarmStateEntity>

    @Query("UPDATE alarm_states SET isAlarmScheduled = :isScheduled WHERE timeSlotId = :timeSlotId")
    suspend fun updateScheduledStatus(timeSlotId: String, isScheduled: Boolean)

    @Query("UPDATE alarm_states SET nextAlarmTime = :nextAlarmTime WHERE timeSlotId = :timeSlotId")
    suspend fun updateNextAlarmTime(timeSlotId: String, nextAlarmTime: Long)

    @Query("SELECT COUNT(*) FROM alarm_states WHERE isAlarmScheduled = 1")
    suspend fun getScheduledAlarmsCount(): Int

    @Query("SELECT * FROM alarm_states WHERE signalItemId = :signalItemId")
    suspend fun getAlarmStatesBySignalItemId(signalItemId: String): List<AlarmStateEntity>

    @Query("SELECT * FROM alarm_states WHERE signalItemId = :signalItemId AND isAlarmScheduled = 1")
    suspend fun getScheduledAlarmsBySignalItemId(signalItemId: String): List<AlarmStateEntity>
}
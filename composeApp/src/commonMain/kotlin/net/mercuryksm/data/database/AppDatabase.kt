package net.mercuryksm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SignalEntity::class, TimeSlotEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
    abstract fun timeSlotDao(): TimeSlotDao
}

// Platform-specific database builder
expect fun getRoomDatabase(): AppDatabase
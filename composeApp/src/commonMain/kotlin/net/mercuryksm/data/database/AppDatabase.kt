package net.mercuryksm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.AutoMigration

@Database(
    entities = [SignalEntity::class, TimeSlotEntity::class, AlarmStateEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun alarmStateDao(): AlarmStateDao
}

// Platform-specific database builder
expect fun getRoomDatabase(): AppDatabase

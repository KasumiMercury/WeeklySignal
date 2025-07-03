package net.mercuryksm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [SignalEntity::class, TimeSlotEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
    abstract fun timeSlotDao(): TimeSlotDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE signals ADD COLUMN color INTEGER NOT NULL DEFAULT 4288423076") // 0xFF6750A4L
    }
}

// Platform-specific database builder
expect fun getRoomDatabase(): AppDatabase
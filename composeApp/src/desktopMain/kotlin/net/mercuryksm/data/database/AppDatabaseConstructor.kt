package net.mercuryksm.data.database

import androidx.room.Room
import androidx.room.RoomDatabaseConstructor
import java.io.File

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase {
        return getDatabaseBuilder().build()
    }
}
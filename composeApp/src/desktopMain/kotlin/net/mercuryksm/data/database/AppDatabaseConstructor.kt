package net.mercuryksm.data.database

import androidx.room.Room
import java.io.File

actual fun getRoomDatabase(): AppDatabase {
    return getDatabaseBuilder().build()
}
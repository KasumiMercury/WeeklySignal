package net.mercuryksm.data.database

import android.content.Context
import androidx.room.Room

actual fun getRoomDatabase(): AppDatabase {
    val context = getDatabaseContext() as? Context
        ?: throw IllegalStateException("Expected Context but got ${getDatabaseContext()::class.simpleName}")

    return getDatabaseBuilder(context).build()
}
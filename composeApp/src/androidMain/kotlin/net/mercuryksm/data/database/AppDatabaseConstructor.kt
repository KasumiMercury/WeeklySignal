package net.mercuryksm.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabaseConstructor

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase {
        // Get the context from the application
        val context = getDatabaseContext() as? Context 
            ?: throw IllegalStateException("Expected Context but got ${getDatabaseContext()::class.simpleName}")
        
        return getDatabaseBuilder(context).build()
    }
}
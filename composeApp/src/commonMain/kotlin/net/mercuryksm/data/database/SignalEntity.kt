package net.mercuryksm.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val sound: Boolean,
    val vibration: Boolean,
    val color: Long = 0xFF6750A4L // Default to Material 3 primary color
)
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
    val vibration: Boolean
)
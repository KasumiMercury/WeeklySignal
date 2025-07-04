package net.mercuryksm.data.database

import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "4288423076") // 0xFF6750A4L as string
    val color: Long = 0xFF6750A4L
)
package net.mercuryksm.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_slots",
    foreignKeys = [
        ForeignKey(
            entity = SignalEntity::class,
            parentColumns = ["id"],
            childColumns = ["signalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["signalId"])]
)
data class TimeSlotEntity(
    @PrimaryKey
    val id: String,
    val signalId: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
        require(dayOfWeek in 0..6) { "DayOfWeek must be between 0 and 6" }
    }
}
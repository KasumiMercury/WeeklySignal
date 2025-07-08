package net.mercuryksm.data.database

import androidx.room.*

@Entity(
    tableName = "alarm_states",
    foreignKeys = [
        ForeignKey(
            entity = TimeSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeSlotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["timeSlotId"]), Index(value = ["signalItemId"])]
)
data class AlarmStateEntity(
    @PrimaryKey
    val timeSlotId: String,
    @ColumnInfo(defaultValue = "")
    val signalItemId: String,
    val isAlarmScheduled: Boolean,
    val pendingIntentRequestCode: Int,
    val scheduledAt: Long,
    val nextAlarmTime: Long
) {
    init {
        require(pendingIntentRequestCode >= 0) { "PendingIntent request code must be non-negative" }
        require(scheduledAt >= 0) { "Scheduled timestamp must be non-negative" }
        require(nextAlarmTime >= 0) { "Next alarm time must be non-negative" }
    }
}

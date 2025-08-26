package net.mercuryksm.notification

/**
 * Data class to hold the result of an alarm scheduling operation.
 */
data class AlarmSchedulingInfo(
    val timeSlotId: String,
    val pendingIntentRequestCode: Int,
    val nextAlarmTime: Long,
    val result: AlarmResult
)
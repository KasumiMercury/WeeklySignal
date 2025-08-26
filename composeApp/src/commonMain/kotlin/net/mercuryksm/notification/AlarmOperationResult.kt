package net.mercuryksm.notification

/**
 * Data class to hold the result of an alarm operation (scheduling, cancellation, etc.).
 */
data class AlarmOperationResult(
    val timeSlotId: String,
    val pendingIntentRequestCode: Int,
    val nextAlarmTime: Long,
    val result: AlarmResult
)
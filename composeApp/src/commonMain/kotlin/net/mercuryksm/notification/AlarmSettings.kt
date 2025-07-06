package net.mercuryksm.notification

data class AlarmSettings(
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val title: String = "",
    val message: String = "",
    val snoozeEnabled: Boolean = false,
    val snoozeDurationMinutes: Int = 5,
    val alarmId: String = ""
)

enum class AlarmResult {
    SUCCESS,
    PERMISSION_DENIED,
    ERROR,
    NOT_SUPPORTED,
    ALREADY_SCHEDULED,
    ALARM_NOT_FOUND
}
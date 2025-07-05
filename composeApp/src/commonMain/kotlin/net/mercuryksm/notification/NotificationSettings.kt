package net.mercuryksm.notification

data class NotificationSettings(
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val title: String = "",
    val message: String = ""
)

enum class NotificationResult {
    SUCCESS,
    PERMISSION_DENIED,
    ERROR,
    NOT_SUPPORTED
}
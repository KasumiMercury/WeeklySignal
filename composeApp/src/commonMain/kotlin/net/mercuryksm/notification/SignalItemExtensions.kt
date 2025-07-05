package net.mercuryksm.notification

import net.mercuryksm.data.SignalItem

fun SignalItem.toNotificationSettings(): NotificationSettings {
    return NotificationSettings(
        sound = this.sound,
        vibration = this.vibration,
        title = this.name,
        message = if (this.description.isNotBlank()) {
            this.description
        } else {
            "Time slots: ${this.timeSlots.joinToString(", ") { it.getDisplayText() }}"
        }
    )
}

fun createPreviewNotificationSettings(
    name: String,
    description: String,
    sound: Boolean,
    vibration: Boolean,
    timeSlots: List<net.mercuryksm.data.TimeSlot>
): NotificationSettings {
    return NotificationSettings(
        sound = sound,
        vibration = vibration,
        title = name.ifBlank { "Signal Preview" },
        message = if (description.isNotBlank()) {
            description
        } else {
            "Time slots: ${timeSlots.joinToString(", ") { it.getDisplayText() }}"
        }
    )
}
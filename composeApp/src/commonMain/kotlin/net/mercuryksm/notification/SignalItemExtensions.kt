package net.mercuryksm.notification

import net.mercuryksm.data.SignalItem

fun SignalItem.toAlarmSettings(): AlarmSettings {
    return AlarmSettings(
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

fun createTestAlarmSettings(
    name: String,
    description: String,
    sound: Boolean,
    vibration: Boolean,
    timeSlots: List<net.mercuryksm.data.TimeSlot>
): AlarmSettings {
    return AlarmSettings(
        sound = sound,
        vibration = vibration,
        title = name.ifBlank { "Signal Test Alarm" },
        message = if (description.isNotBlank()) {
            description
        } else {
            "Time slots: ${timeSlots.joinToString(", ") { it.getDisplayText() }}"
        }
    )
}
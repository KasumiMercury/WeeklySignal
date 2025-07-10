package net.mercuryksm.data

import kotlinx.serialization.Serializable

@Serializable
data class SignalItemExportFormat(
    val id: String,
    val name: String,
    val description: String,
    val sound: Boolean,
    val vibration: Boolean,
    val color: Long,
    val timeSlots: List<TimeSlotExportFormat>
)

@Serializable
data class TimeSlotExportFormat(
    val id: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int // Using Int for serialization compatibility
)

@Serializable
data class WeeklySignalExportData(
    val version: String,
    val exportedAt: Long, // Unix timestamp
    val appVersion: String,
    val signalItems: List<SignalItemExportFormat>
)

object ExportFormatConverter {
    
    fun SignalItem.toExportFormat(): SignalItemExportFormat {
        return SignalItemExportFormat(
            id = this.id,
            name = this.name,
            description = this.description,
            sound = this.sound,
            vibration = this.vibration,
            color = this.color,
            timeSlots = this.timeSlots.map { it.toExportFormat() }
        )
    }
    
    fun TimeSlot.toExportFormat(): TimeSlotExportFormat {
        return TimeSlotExportFormat(
            id = this.id,
            hour = this.hour,
            minute = this.minute,
            dayOfWeek = this.dayOfWeek.ordinal
        )
    }
    
    fun SignalItemExportFormat.toSignalItem(): SignalItem {
        return SignalItem(
            id = this.id,
            name = this.name,
            description = this.description,
            sound = this.sound,
            vibration = this.vibration,
            color = this.color,
            timeSlots = this.timeSlots.map { it.toTimeSlot() }
        )
    }
    
    fun TimeSlotExportFormat.toTimeSlot(): TimeSlot {
        return TimeSlot(
            id = this.id,
            hour = this.hour,
            minute = this.minute,
            dayOfWeek = DayOfWeekJp.values()[this.dayOfWeek]
        )
    }
    
    fun List<SignalItem>.toExportData(
        version: String = "1.0",
        appVersion: String = "1.0.0"
    ): WeeklySignalExportData {
        return WeeklySignalExportData(
            version = version,
            exportedAt = System.currentTimeMillis(),
            appVersion = appVersion,
            signalItems = this.map { it.toExportFormat() }
        )
    }
    
    fun WeeklySignalExportData.toSignalItems(): List<SignalItem> {
        return this.signalItems.map { it.toSignalItem() }
    }
}
package net.mercuryksm.data.database

import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot

object EntityMappers {
    
    fun SignalItem.toSignalEntity(): SignalEntity {
        return SignalEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            sound = this.sound,
            vibration = this.vibration
        )
    }
    
    fun TimeSlot.toTimeSlotEntity(signalId: String): TimeSlotEntity {
        return TimeSlotEntity(
            id = this.id,
            signalId = signalId,
            hour = this.hour,
            minute = this.minute,
            dayOfWeek = this.dayOfWeek.ordinal
        )
    }
    
    fun SignalEntity.toSignalItem(timeSlots: List<TimeSlot>): SignalItem {
        return SignalItem(
            id = this.id,
            name = this.name,
            timeSlots = timeSlots,
            description = this.description,
            sound = this.sound,
            vibration = this.vibration
        )
    }
    
    fun TimeSlotEntity.toTimeSlot(): TimeSlot {
        return TimeSlot(
            id = this.id,
            hour = this.hour,
            minute = this.minute,
            dayOfWeek = DayOfWeekJp.values()[this.dayOfWeek]
        )
    }
    
    fun List<SignalEntity>.toSignalItems(timeSlotsBySignalId: Map<String, List<TimeSlotEntity>>): List<SignalItem> {
        return this.map { signalEntity ->
            val timeSlots = timeSlotsBySignalId[signalEntity.id]?.map { it.toTimeSlot() } ?: emptyList()
            signalEntity.toSignalItem(timeSlots)
        }
    }
}
package net.mercuryksm.data.database

import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.database.EntityMappers.toSignalEntity
import net.mercuryksm.data.database.EntityMappers.toSignalItem
import net.mercuryksm.data.database.EntityMappers.toTimeSlot
import net.mercuryksm.data.database.EntityMappers.toTimeSlotEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityMappersTest {
    
    @Test
    fun `SignalItem to SignalEntity mapping should preserve all properties`() {
        val signalItem = SignalItem(
            id = "test-id",
            name = "Test Signal",
            timeSlots = listOf(
                TimeSlot(
                    id = "ts1",
                    hour = 9,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            ),
            description = "Test Description",
            sound = true,
            vibration = false
        )
        
        val signalEntity = signalItem.toSignalEntity()
        
        assertEquals("test-id", signalEntity.id)
        assertEquals("Test Signal", signalEntity.name)
        assertEquals("Test Description", signalEntity.description)
        assertEquals(true, signalEntity.sound)
        assertEquals(false, signalEntity.vibration)
    }
    
    @Test
    fun `TimeSlot to TimeSlotEntity mapping should preserve all properties`() {
        val timeSlot = TimeSlot(
            id = "ts1",
            hour = 14,
            minute = 45,
            dayOfWeek = DayOfWeekJp.FRIDAY
        )
        
        val timeSlotEntity = timeSlot.toTimeSlotEntity("signal-id")
        
        assertEquals("ts1", timeSlotEntity.id)
        assertEquals("signal-id", timeSlotEntity.signalId)
        assertEquals(14, timeSlotEntity.hour)
        assertEquals(45, timeSlotEntity.minute)
        assertEquals(DayOfWeekJp.FRIDAY.ordinal, timeSlotEntity.dayOfWeek)
    }
    
    @Test
    fun `SignalEntity to SignalItem mapping should preserve all properties`() {
        val signalEntity = SignalEntity(
            id = "test-id",
            name = "Test Signal",
            description = "Test Description",
            sound = true,
            vibration = false
        )
        
        val timeSlots = listOf(
            TimeSlot(
                id = "ts1",
                hour = 9,
                minute = 30,
                dayOfWeek = DayOfWeekJp.MONDAY
            )
        )
        
        val signalItem = signalEntity.toSignalItem(timeSlots)
        
        assertEquals("test-id", signalItem.id)
        assertEquals("Test Signal", signalItem.name)
        assertEquals("Test Description", signalItem.description)
        assertEquals(true, signalItem.sound)
        assertEquals(false, signalItem.vibration)
        assertEquals(1, signalItem.timeSlots.size)
        assertEquals("ts1", signalItem.timeSlots[0].id)
    }
    
    @Test
    fun `TimeSlotEntity to TimeSlot mapping should preserve all properties`() {
        val timeSlotEntity = TimeSlotEntity(
            id = "ts1",
            signalId = "signal-id",
            hour = 14,
            minute = 45,
            dayOfWeek = DayOfWeekJp.FRIDAY.ordinal
        )
        
        val timeSlot = timeSlotEntity.toTimeSlot()
        
        assertEquals("ts1", timeSlot.id)
        assertEquals(14, timeSlot.hour)
        assertEquals(45, timeSlot.minute)
        assertEquals(DayOfWeekJp.FRIDAY, timeSlot.dayOfWeek)
    }
}
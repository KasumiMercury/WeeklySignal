package net.mercuryksm.event

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.DayOfWeekJp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventBusTest {
    
    private val testScope = TestScope(UnconfinedTestDispatcher())
    
    @Test
    fun `should publish and receive events`() = testScope.runTest {
        // Given
        val eventBus = EventBusImpl()
        val signalItem = createTestSignalItem()
        
        // When
        eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
        
        // Then
        val receivedEvent = eventBus.subscribeAll().first()
        assertTrue(receivedEvent is DomainEvent.SignalItemCreated)
        assertEquals(signalItem.id, receivedEvent.signalItem.id)
    }
    
    @Test
    fun `should subscribe to specific event types`() = testScope.runTest {
        // Given
        val eventBus = EventBusImpl()
        val signalItem = createTestSignalItem()
        val events = mutableListOf<DomainEvent.SignalItemCreated>()
        
        // When
        val collectJob = launch {
            eventBus.subscribe(DomainEvent.SignalItemCreated::class.java)
                .toList(events)
        }
        
        eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
        eventBus.publish(DomainEvent.SignalItemDeleted("other-id"))
        eventBus.publish(DomainEvent.SignalItemCreated(signalItem.copy(id = "second-id")))
        
        collectJob.cancel()
        
        // Then
        assertEquals(2, events.size)
        assertEquals(signalItem.id, events[0].signalItem.id)
        assertEquals("second-id", events[1].signalItem.id)
    }
    
    @Test
    fun `should handle multiple event types`() = testScope.runTest {
        // Given
        val eventBus = EventBusImpl()
        val signalItem = createTestSignalItem()
        val allEvents = mutableListOf<DomainEvent>()
        
        // When
        val collectJob = launch {
            eventBus.subscribeAll().toList(allEvents)
        }
        
        eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
        eventBus.publish(DomainEvent.SignalItemUpdated(signalItem))
        eventBus.publish(DomainEvent.SignalItemDeleted(signalItem.id))
        eventBus.publish(DomainEvent.AllSignalItemsCleared())
        
        collectJob.cancel()
        
        // Then
        assertEquals(4, allEvents.size)
        assertTrue(allEvents[0] is DomainEvent.SignalItemCreated)
        assertTrue(allEvents[1] is DomainEvent.SignalItemUpdated)
        assertTrue(allEvents[2] is DomainEvent.SignalItemDeleted)
        assertTrue(allEvents[3] is DomainEvent.AllSignalItemsCleared)
    }
    
    @Test
    fun `should handle alarm events`() = testScope.runTest {
        // Given
        val eventBus = EventBusImpl()
        val signalItemId = "test-id"
        val timeSlotIds = listOf("slot-1", "slot-2")
        
        // When
        eventBus.publish(DomainEvent.AlarmScheduled(signalItemId, timeSlotIds))
        
        // Then
        val receivedEvent = eventBus.subscribeAll().first()
        assertTrue(receivedEvent is DomainEvent.AlarmScheduled)
        assertEquals(signalItemId, receivedEvent.signalItemId)
        assertEquals(timeSlotIds, receivedEvent.timeSlotIds)
    }
    
    private fun createTestSignalItem(): SignalItem {
        return SignalItem(
            id = "test-id",
            name = "Test Signal",
            description = "Test Description",
            sound = true,
            vibration = false,
            color = 0xFF0000FF.toInt(),
            timeSlots = listOf(
                TimeSlot(
                    id = "slot-1",
                    hour = 9,
                    minute = 0,
                    dayOfWeek = DayOfWeekJp.MONDAY
                )
            )
        )
    }
}
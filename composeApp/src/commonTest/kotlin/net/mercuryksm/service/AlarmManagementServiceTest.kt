package net.mercuryksm.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.TimeSlot
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.event.DomainEvent
import net.mercuryksm.event.EventBus
import net.mercuryksm.event.EventBusImpl
import net.mercuryksm.notification.SignalAlarmManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlarmManagementServiceTest {
    
    private val testScope = TestScope(UnconfinedTestDispatcher())
    
    @Test
    fun `should schedule alarms and update state`() = testScope.runTest {
        // Given
        val mockAlarmManager = MockAlarmManager()
        val eventBus = EventBusImpl()
        val service = AlarmManagementServiceImpl(mockAlarmManager, eventBus, testScope)
        val signalItem = createTestSignalItem()
        
        // When
        val result = service.scheduleSignalItemAlarms(signalItem)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockAlarmManager.scheduledItems.contains(signalItem))
        
        val alarmState = service.alarmStates.first()[signalItem.id]
        assertTrue(alarmState?.isEnabled == true)
        assertEquals(signalItem.timeSlots.map { it.id }, alarmState?.scheduledTimeSlotIds)
    }
    
    @Test
    fun `should cancel alarms and remove state`() = testScope.runTest {
        // Given
        val mockAlarmManager = MockAlarmManager()
        val eventBus = EventBusImpl()
        val service = AlarmManagementServiceImpl(mockAlarmManager, eventBus, testScope)
        val signalItem = createTestSignalItem()
        
        // First schedule an alarm
        service.scheduleSignalItemAlarms(signalItem)
        
        // When
        val result = service.cancelSignalItemAlarms(signalItem.id)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockAlarmManager.cancelledItemIds.contains(signalItem.id))
        
        val alarmState = service.alarmStates.first()[signalItem.id]
        assertEquals(null, alarmState)
    }
    
    @Test
    fun `should handle null alarm manager gracefully`() = testScope.runTest {
        // Given
        val eventBus = EventBusImpl()
        val service = AlarmManagementServiceImpl(null, eventBus, testScope)
        val signalItem = createTestSignalItem()
        
        // When
        val result = service.scheduleSignalItemAlarms(signalItem)
        
        // Then
        assertTrue(result.isSuccess)
        
        val alarmState = service.alarmStates.first()[signalItem.id]
        assertFalse(alarmState?.isEnabled == true)
    }
    
    @Test
    fun `should respond to domain events`() = testScope.runTest {
        // Given
        val mockAlarmManager = MockAlarmManager()
        val eventBus = EventBusImpl()
        val service = AlarmManagementServiceImpl(mockAlarmManager, eventBus, testScope)
        val signalItem = createTestSignalItem()
        
        // When
        eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
        
        // Allow event to be processed
        testScope.testScheduler.advanceUntilIdle()
        
        // Then
        assertTrue(mockAlarmManager.scheduledItems.contains(signalItem))
    }
    
    @Test
    fun `should publish alarm events`() = testScope.runTest {
        // Given
        val mockAlarmManager = MockAlarmManager()
        val eventBus = EventBusImpl()
        val service = AlarmManagementServiceImpl(mockAlarmManager, eventBus, testScope)
        val signalItem = createTestSignalItem()
        
        val alarmEvents = mutableListOf<DomainEvent>()
        val collectJob = launch {
            eventBus.subscribe(DomainEvent.AlarmScheduled::class.java)
                .toList(alarmEvents)
        }
        
        // When
        service.scheduleSignalItemAlarms(signalItem)
        
        collectJob.cancel()
        
        // Then
        assertEquals(1, alarmEvents.size)
        val event = alarmEvents[0] as DomainEvent.AlarmScheduled
        assertEquals(signalItem.id, event.signalItemId)
        assertEquals(signalItem.timeSlots.map { it.id }, event.timeSlotIds)
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
                ),
                TimeSlot(
                    id = "slot-2",
                    hour = 15,
                    minute = 30,
                    dayOfWeek = DayOfWeekJp.FRIDAY
                )
            )
        )
    }
}

class MockAlarmManager : SignalAlarmManager {
    val scheduledItems = mutableListOf<SignalItem>()
    val cancelledItemIds = mutableListOf<String>()
    val updatedItems = mutableListOf<Pair<SignalItem, SignalItem>>()
    
    override suspend fun scheduleSignalItemAlarms(signalItem: SignalItem) {
        scheduledItems.add(signalItem)
    }
    
    override suspend fun cancelSignalItemAlarms(signalItemId: String) {
        cancelledItemIds.add(signalItemId)
    }
    
    override suspend fun cancelSignalItemAlarms(signalItem: SignalItem) {
        cancelledItemIds.add(signalItem.id)
    }
    
    override suspend fun updateSignalItemAlarms(oldSignalItem: SignalItem, newSignalItem: SignalItem) {
        updatedItems.add(oldSignalItem to newSignalItem)
    }
    
    override suspend fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return scheduledItems.any { it.id == signalItemId }
    }
    
    override suspend fun cancelAllAlarms() {
        scheduledItems.clear()
        cancelledItemIds.clear()
    }
}
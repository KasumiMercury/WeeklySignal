package net.mercuryksm.service

import net.mercuryksm.data.SignalItem
import net.mercuryksm.event.DomainEvent
import net.mercuryksm.event.EventBus
import net.mercuryksm.notification.SignalAlarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class AlarmState(
    val signalItemId: String,
    val isEnabled: Boolean,
    val scheduledTimeSlotIds: List<String>,
    val lastUpdateTime: Long
)

interface AlarmManagementService {
    suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): Result<Unit>
    suspend fun cancelSignalItemAlarms(signalItemId: String): Result<Unit>
    suspend fun updateSignalItemAlarms(signalItem: SignalItem): Result<Unit>
    fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean
    val alarmStates: StateFlow<Map<String, AlarmState>>
}

class AlarmManagementServiceImpl(
    private val alarmManager: SignalAlarmManager?,
    private val eventBus: EventBus,
    private val serviceScope: CoroutineScope
) : AlarmManagementService {
    
    private val _alarmStates = MutableStateFlow<Map<String, AlarmState>>(emptyMap())
    override val alarmStates: StateFlow<Map<String, AlarmState>> = _alarmStates.asStateFlow()
    
    init {
        subscribeToSignalItemEvents()
    }
    
    private fun subscribeToSignalItemEvents() {
        eventBus.subscribe(DomainEvent.SignalItemCreated::class.java)
            .onEach { event ->
                scheduleSignalItemAlarms(event.signalItem)
            }
            .launchIn(serviceScope)
        
        eventBus.subscribe(DomainEvent.SignalItemUpdated::class.java)
            .onEach { event ->
                updateSignalItemAlarms(event.signalItem)
            }
            .launchIn(serviceScope)
        
        eventBus.subscribe(DomainEvent.SignalItemDeleted::class.java)
            .onEach { event ->
                cancelSignalItemAlarms(event.signalItemId)
            }
            .launchIn(serviceScope)
        
        eventBus.subscribe(DomainEvent.AllSignalItemsCleared::class.java)
            .onEach {
                cancelAllAlarms()
            }
            .launchIn(serviceScope)
    }
    
    override suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): Result<Unit> {
        return try {
            if (alarmManager == null) {
                updateAlarmState(signalItem.id, false, emptyList())
                return Result.success(Unit)
            }
            
            val timeSlotIds = signalItem.timeSlots.map { it.id }
            alarmManager.scheduleSignalItemAlarms(signalItem)
            updateAlarmState(signalItem.id, true, timeSlotIds)
            
            eventBus.publish(DomainEvent.AlarmScheduled(signalItem.id, timeSlotIds))
            Result.success(Unit)
        } catch (e: Exception) {
            updateAlarmState(signalItem.id, false, emptyList())
            Result.failure(e)
        }
    }
    
    override suspend fun cancelSignalItemAlarms(signalItemId: String): Result<Unit> {
        return try {
            if (alarmManager == null) {
                removeAlarmState(signalItemId)
                return Result.success(Unit)
            }
            
            alarmManager.cancelSignalItemAlarms(signalItemId)
            removeAlarmState(signalItemId)
            
            eventBus.publish(DomainEvent.AlarmCancelled(signalItemId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSignalItemAlarms(signalItem: SignalItem): Result<Unit> {
        return try {
            if (alarmManager == null) {
                updateAlarmState(signalItem.id, false, emptyList())
                return Result.success(Unit)
            }
            
            val timeSlotIds = signalItem.timeSlots.map { it.id }
            alarmManager.updateSignalItemAlarms(signalItem)
            updateAlarmState(signalItem.id, true, timeSlotIds)
            
            eventBus.publish(DomainEvent.AlarmUpdated(signalItem.id, timeSlotIds))
            Result.success(Unit)
        } catch (e: Exception) {
            updateAlarmState(signalItem.id, false, emptyList())
            Result.failure(e)
        }
    }
    
    override fun isSignalItemAlarmsEnabled(signalItemId: String): Boolean {
        return _alarmStates.value[signalItemId]?.isEnabled ?: false
    }
    
    private fun updateAlarmState(signalItemId: String, isEnabled: Boolean, timeSlotIds: List<String>) {
        val currentStates = _alarmStates.value.toMutableMap()
        currentStates[signalItemId] = AlarmState(
            signalItemId = signalItemId,
            isEnabled = isEnabled,
            scheduledTimeSlotIds = timeSlotIds,
            lastUpdateTime = System.currentTimeMillis()
        )
        _alarmStates.value = currentStates
    }
    
    private fun removeAlarmState(signalItemId: String) {
        val currentStates = _alarmStates.value.toMutableMap()
        currentStates.remove(signalItemId)
        _alarmStates.value = currentStates
    }
    
    private suspend fun cancelAllAlarms() {
        val currentStates = _alarmStates.value
        currentStates.keys.forEach { signalItemId ->
            cancelSignalItemAlarms(signalItemId)
        }
    }
}
package net.mercuryksm.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.reflect.KClass

interface EventBus {
    fun publish(event: DomainEvent)
    fun <T : DomainEvent> subscribe(eventType: KClass<T>): Flow<T>
    fun subscribeAll(): Flow<DomainEvent>
}

class EventBusImpl : EventBus {
    private val _events = MutableSharedFlow<DomainEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    private val events: SharedFlow<DomainEvent> = _events.asSharedFlow()

    override fun publish(event: DomainEvent) {
        _events.tryEmit(event)
    }

    override fun <T : DomainEvent> subscribe(eventType: KClass<T>): Flow<T> {
        return events.filterIsInstance(eventType)
    }

    override fun subscribeAll(): Flow<DomainEvent> {
        return events
    }
}
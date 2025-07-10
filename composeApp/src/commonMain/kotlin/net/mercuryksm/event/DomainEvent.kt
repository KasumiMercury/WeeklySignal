package net.mercuryksm.event

import net.mercuryksm.data.SignalItem

sealed class DomainEvent {
    data class SignalItemCreated(val signalItem: SignalItem) : DomainEvent()
    data class SignalItemUpdated(val signalItem: SignalItem) : DomainEvent()
    data class SignalItemDeleted(val signalItemId: String) : DomainEvent()
    data class SignalItemsImported(val signalItems: List<SignalItem>) : DomainEvent()
    data class SignalItemsExported(val signalItems: List<SignalItem>) : DomainEvent()
    data object AllSignalItemsCleared : DomainEvent()
    
    data class AlarmScheduled(val signalItemId: String, val timeSlotIds: List<String>) : DomainEvent()
    data class AlarmCancelled(val signalItemId: String) : DomainEvent()
    data class AlarmUpdated(val signalItemId: String, val timeSlotIds: List<String>) : DomainEvent()
}
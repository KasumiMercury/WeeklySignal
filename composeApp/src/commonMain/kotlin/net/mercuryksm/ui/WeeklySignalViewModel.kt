package net.mercuryksm.ui

import androidx.lifecycle.ViewModel
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem
import net.mercuryksm.data.SignalRepository

class WeeklySignalViewModel(
    private val signalRepository: SignalRepository = SignalRepository()
) : ViewModel() {
    
    val signalItems = signalRepository.signalItems
    
    fun addSignalItem(signalItem: SignalItem) {
        signalRepository.addSignalItem(signalItem)
    }
    
    fun removeSignalItem(signalItem: SignalItem) {
        signalRepository.removeSignalItem(signalItem)
    }
    
    fun updateSignalItem(signalItem: SignalItem) {
        signalRepository.updateSignalItem(signalItem)
    }
    
    fun getSignalItemById(id: String): SignalItem? {
        return signalRepository.getSignalItemById(id)
    }
    
    fun getSignalItemsForDay(dayOfWeek: DayOfWeekJp): List<SignalItem> {
        return signalRepository.getSignalItemsForDay(dayOfWeek)
    }
    
    fun getAllSignalItems(): List<SignalItem> {
        return signalRepository.getAllSignalItems()
    }
}
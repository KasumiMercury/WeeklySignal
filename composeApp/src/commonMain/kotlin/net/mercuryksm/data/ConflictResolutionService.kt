package net.mercuryksm.data

/**
 * Service responsible for resolving conflicts between existing and imported SignalItems.
 * Provides different conflict resolution strategies.
 */
class ConflictResolutionService {
    
    /**
     * Find conflicts between existing and imported SignalItems
     */
    fun findConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>
    ): List<SignalItem> {
        val existingIds = existingSignalItems.map { it.id }.toSet()
        return importedSignalItems.filter { it.id in existingIds }
    }
    
    /**
     * Resolve conflicts between existing and imported SignalItems
     */
    fun resolveConflicts(
        existingSignalItems: List<SignalItem>,
        importedSignalItems: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): ImportConflictResolutionResult {
        val existingMap = existingSignalItems.associateBy { it.id }
        
        val itemsToInsert = mutableListOf<SignalItem>()
        val itemsToUpdate = mutableListOf<SignalItem>()
        
        // Process imported items
        importedSignalItems.forEach { importedItem ->
            val existingItem = existingMap[importedItem.id]
            
            if (existingItem == null) {
                // New item, add to insert list
                itemsToInsert.add(importedItem)
            } else {
                // Conflict, resolve based on strategy
                when (conflictResolution) {
                    ConflictResolution.REPLACE_EXISTING -> {
                        // Replace the existing item (update with imported item)
                        itemsToUpdate.add(importedItem)
                    }
                    ConflictResolution.KEEP_EXISTING -> {
                        // Keep existing, skip this imported item
                        // Do nothing - don't add to either list
                    }
                    ConflictResolution.MERGE_TIME_SLOTS -> {
                        // Merge time slots - keep existing SignalItem settings but add new TimeSlots
                        val existingTimeSlotIds = existingItem.timeSlots.map { it.id }.toSet()
                        val newTimeSlots = importedItem.timeSlots.filter { it.id !in existingTimeSlotIds }
                        
                        if (newTimeSlots.isNotEmpty()) {
                            val mergedItem = existingItem.copy(
                                timeSlots = existingItem.timeSlots + newTimeSlots
                            )
                            itemsToUpdate.add(mergedItem)
                        }
                        // If no new TimeSlots to add, skip this item
                    }
                }
            }
        }
        
        return ImportConflictResolutionResult(itemsToInsert, itemsToUpdate)
    }
}
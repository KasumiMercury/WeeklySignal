package net.mercuryksm.data

/**
 * Service responsible for validating imported data structure and content.
 * Ensures data integrity before processing.
 */
class ImportDataValidator {
    
    /**
     * Validate the structure and content of imported data
     */
    fun validateImportData(exportData: WeeklySignalExportData): String? {
        // Validate version compatibility
        if (exportData.version.isEmpty()) {
            return "Invalid export format: missing version"
        }
        
        // Validate signal items
        exportData.signalItems.forEach { signalItem ->
            if (signalItem.id.isEmpty()) {
                return "Invalid signal item: missing ID"
            }
            
            if (signalItem.name.isEmpty()) {
                return "Invalid signal item: missing name"
            }
            
            if (signalItem.timeSlots.isEmpty()) {
                return "Invalid signal item '${signalItem.name}': must have at least one time slot"
            }
            
            // Validate time slots
            signalItem.timeSlots.forEach { timeSlot ->
                if (timeSlot.id.isEmpty()) {
                    return "Invalid time slot: missing ID"
                }
                
                if (timeSlot.hour !in 0..23) {
                    return "Invalid time slot in '${signalItem.name}': hour must be between 0 and 23"
                }
                
                if (timeSlot.minute !in 0..59) {
                    return "Invalid time slot in '${signalItem.name}': minute must be between 0 and 59"
                }
                
                if (timeSlot.dayOfWeek !in 0..6) {
                    return "Invalid time slot in '${signalItem.name}': day of week must be between 0 and 6"
                }
            }
        }
        
        // Check for duplicate IDs
        val signalItemIds = exportData.signalItems.map { it.id }
        if (signalItemIds.size != signalItemIds.distinct().size) {
            return "Duplicate signal item IDs found in export data"
        }
        
        val timeSlotIds = exportData.signalItems.flatMap { it.timeSlots.map { ts -> ts.id } }
        if (timeSlotIds.size != timeSlotIds.distinct().size) {
            return "Duplicate time slot IDs found in export data"
        }
        
        return null
    }
}
package net.mercuryksm.data.database

data class TimeSlotEntity(
    val id: String,
    val signalId: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
        require(dayOfWeek in 0..6) { "DayOfWeek must be between 0 and 6" }
    }
}
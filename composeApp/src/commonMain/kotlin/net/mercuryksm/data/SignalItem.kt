package net.mercuryksm.data

enum class DayOfWeekJp {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    fun getDisplayName(): String {
        return when (this) {
            MONDAY -> "月"
            TUESDAY -> "火"
            WEDNESDAY -> "水"
            THURSDAY -> "木"
            FRIDAY -> "金"
            SATURDAY -> "土"
            SUNDAY -> "日"
        }
    }
}

data class SignalItem(
    val id: String,
    val name: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: DayOfWeekJp,
    val description: String = "",
    val sound: Boolean = true,
    val vibration: Boolean = true
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
    }
    
    fun getTimeDisplayText(): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getTruncatedName(maxLength: Int = 10): String {
        return if (name.length <= maxLength) {
            name
        } else {
            name.take(maxLength) + "..."
        }
    }
    
    fun getTimeInMinutes(): Int {
        return hour * 60 + minute
    }
}
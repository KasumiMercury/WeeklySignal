package net.mercuryksm.data.database

data class SignalEntity(
    val id: String,
    val name: String,
    val description: String,
    val sound: Boolean,
    val vibration: Boolean
)
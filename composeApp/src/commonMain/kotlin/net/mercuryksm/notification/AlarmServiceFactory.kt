package net.mercuryksm.notification

interface AlarmServiceFactory {
    fun createAlarmManager(): SignalAlarmManager
}

expect fun createAlarmServiceFactory(): AlarmServiceFactory
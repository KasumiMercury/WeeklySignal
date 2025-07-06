package net.mercuryksm.notification

class DesktopAlarmServiceFactory : AlarmServiceFactory {
    
    override fun createAlarmManager(): SignalAlarmManager {
        return DesktopSignalAlarmManager()
    }
}

actual fun createAlarmServiceFactory(): AlarmServiceFactory {
    return DesktopAlarmServiceFactory()
}
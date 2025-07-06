package net.mercuryksm.notification

import net.mercuryksm.data.TimeSlot
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Image
import java.awt.Toolkit

class DesktopSignalAlarmManager : SignalAlarmManager {
    
    private var trayIcon: TrayIcon? = null
    
    init {
        initSystemTray()
    }
    
    override suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmResult {
        // Desktop版ではスケジューリング機能をサポートしない
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAlarm(alarmId: String): AlarmResult {
        // Desktop版ではスケジューリング機能をサポートしない
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAllAlarms(): AlarmResult {
        // Desktop版ではスケジューリング機能をサポートしない
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun getScheduledAlarms(): List<String> {
        // Desktop版ではスケジューリング機能をサポートしない
        return emptyList()
    }
    
    override suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult {
        return try {
            showDesktopNotification(settings)
            AlarmResult.SUCCESS
        } catch (e: Exception) {
            AlarmResult.ERROR
        }
    }
    
    override suspend fun hasAlarmPermission(): Boolean {
        // Desktop版ではテスト通知のみサポート、スケジューリングはサポートしない
        return SystemTray.isSupported()
    }
    
    override suspend fun requestAlarmPermission(): Boolean {
        // Desktop版ではテスト通知のみサポート、スケジューリングはサポートしない
        return SystemTray.isSupported()
    }
    
    override fun isAlarmSupported(): Boolean {
        // Desktop版ではテスト通知のみサポート、スケジューリングはサポートしない
        // テストボタンを表示するためにtrueを返す
        return SystemTray.isSupported()
    }
    
    private fun initSystemTray() {
        if (SystemTray.isSupported()) {
            try {
                val tray = SystemTray.getSystemTray()
                val image = createTrayIcon()
                trayIcon = TrayIcon(image, "WeeklySignal Test Notifications")
                trayIcon?.isImageAutoSize = true
                tray.add(trayIcon)
            } catch (e: Exception) {
                // System tray not available or failed to initialize
            }
        }
    }
    
    private fun createTrayIcon(): Image {
        return try {
            // Try to load a default icon, fallback to a simple 16x16 image
            Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon.png"))
        } catch (e: Exception) {
            // Create a simple default icon
            Toolkit.getDefaultToolkit().createImage(ByteArray(16 * 16 * 4))
        }
    }
    
    private fun showDesktopNotification(settings: AlarmSettings) {
        trayIcon?.displayMessage(
            settings.title.ifEmpty { "WeeklySignal Test" },
            settings.message.ifEmpty { "Test notification" },
            TrayIcon.MessageType.INFO
        )
        
        // Play system sound if requested
        if (settings.sound) {
            try {
                Toolkit.getDefaultToolkit().beep()
            } catch (e: Exception) {
                // Sound not available
            }
        }
    }
    
    fun cleanup() {
        trayIcon?.let { 
            try {
                SystemTray.getSystemTray().remove(it)
            } catch (e: Exception) {
                // Cleanup failed
            }
        }
    }
}
package net.mercuryksm.notification

import net.mercuryksm.data.TimeSlot
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class DesktopSignalAlarmManager : SignalAlarmManager {
    
    private var trayIcon: TrayIcon? = null
    
    init {
        initSystemTray()
    }
    
    override suspend fun scheduleAlarm(timeSlot: TimeSlot, settings: AlarmSettings): AlarmResult {
        // Desktop version does not support scheduling functionality
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAlarm(alarmId: String): AlarmResult {
        // Desktop version does not support scheduling functionality
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun cancelAllAlarms(): AlarmResult {
        // Desktop version does not support scheduling functionality
        return AlarmResult.NOT_SUPPORTED
    }
    
    override suspend fun getScheduledAlarms(): List<String> {
        // Desktop version does not support scheduling functionality
        return emptyList()
    }
    
    override suspend fun showTestAlarm(settings: AlarmSettings): AlarmResult {
        return try {
            if (!SystemTray.isSupported()) {
                return AlarmResult.NOT_SUPPORTED
            }
            showDesktopNotification(settings)
            AlarmResult.SUCCESS
        } catch (e: Exception) {
            AlarmResult.ERROR
        }
    }
    
    override suspend fun hasAlarmPermission(): Boolean {
        // Desktop version always treats permissions as granted for future alarm implementation
        return true
    }
    
    override suspend fun requestAlarmPermission(): Boolean {
        // Desktop version always treats permissions as granted for future alarm implementation
        return true
    }
    
    override fun isAlarmSupported(): Boolean {
        // Desktop version only supports test notifications, not scheduling
        // Returns true to display the test button
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

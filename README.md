# WeeklySignal

WeeklySignal is a cross-platform weekly schedule management application that helps you organize and track your recurring tasks, meetings, and reminders throughout the week.

## Features

### 📅 Weekly Schedule View
- **Visual Weekly Grid**: View your schedule in an intuitive weekly layout with days of the week and time slots
- **Horizontal Scrolling**: Navigate through different times of the day with smooth scrolling
- **Color-Coded Items**: Each SignalItem has a customizable color for easy visual identification
- **Multiple Items Support**: Handle multiple SignalItems in the same time slot with smart display modes

### 🔔 Smart Notifications
- **Alarm Integration**: Set up recurring alarms for your SignalItems
- **Customizable Alerts**: Configure sound and vibration settings for each item
- **Weekly Recurring**: Automatically schedule alarms for recurring weekly events
- **Cross-Platform**: Works on both Android and Desktop platforms

### 📊 Signal Management
- **Easy Registration**: Add new SignalItems with multiple time slots
- **Flexible Editing**: Modify existing items including time slots and settings
- **Batch Operations**: Select and manage multiple items at once
- **Time Slot Control**: Add multiple time slots to a single SignalItem

### 💾 Import/Export System
- **Selective Export**: Choose specific SignalItems and time slots to export
- **Conflict Resolution**: Smart handling of duplicate items during import
- **Data Backup**: Export your schedule data as `.weeklysignal` files
- **Cross-Device Sync**: Share your schedule between different devices

## Platform Support

- **Android**: Android 6.0 (API 24) and above
- **Desktop**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **Architecture**: Kotlin Multiplatform with Compose UI

## File Format

WeeklySignal uses `.weeklysignal` files for import/export operations. These are JSON-based files that contain:
- SignalItem data (name, description, settings)
- Time slot information
- Metadata and version information

## License

WeeklySignal is released under the MIT License. See the LICENSE file for details.

---

*WeeklySignal - Your personal weekly schedule companion*

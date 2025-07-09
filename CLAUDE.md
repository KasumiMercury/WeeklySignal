# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WeeklySignal is a Kotlin Multiplatform project using Compose Multiplatform, targeting Android and Desktop platforms. It displays a weekly schedule interface showing SignalItems (user-defined notification items) across days of the week with time-based horizontal scrolling, and includes alarm/notification functionality.

## Architecture

- **Package**: `net.mercuryksm`
- **Multiplatform Structure**:
  - `commonMain/`: Shared code for all platforms (UI, business logic, data models)
  - `androidMain/`: Android-specific implementations (alarms, database, notifications)
  - `desktopMain/`: Desktop-specific implementations (database, mock alarms)
  - `commonTest/`: Shared test code

## Application Structure

### Data Layer (`data/`)
- **SignalItem.kt**: Core data model for notification items with multiple time slots, containing `List<TimeSlot>`
- **TimeSlot.kt**: Individual time/day combination data class with hour, minute, and dayOfWeek
- **DayOfWeekJp.kt**: Japanese day-of-week enum with display names and short English names (Mon, Tue, etc.)
- **SignalRepository.kt**: Repository pattern for CRUD operations on SignalItems with reactive state management

#### Database Layer (`data/database/`)
- **Room KMP Implementation**: Cross-platform database using Room 2.7.2 with KSP annotation processing
- **AppDatabase.kt**: Main Room database class with `@Database` annotation and expect/actual constructor pattern
- **SignalDao.kt/TimeSlotDao.kt/AlarmStateDao.kt**: Room DAO interfaces with SQL query annotations
- **SignalEntity.kt/TimeSlotEntity.kt/AlarmStateEntity.kt**: Room entity models with proper annotations and foreign key constraints
- **EntityMappers.kt**: Conversion utilities between domain models and database entities
- **DatabaseServiceFactory.kt**: Expect/actual factory pattern for platform-specific database services

### Notification System (`notification/`)
- **SignalAlarmManager.kt**: Common interface for alarm/notification management
- **AlarmSettings.kt**: Configuration for alarm behavior (sound, vibration, message)
- **Platform-Specific Implementations**:
  - **Android**: AndroidAlarmManager with system AlarmManager integration, notification channels, permissions
  - **Desktop**: DesktopAlarmManager with mock implementation for development

### UI Layer (`ui/`)
- **WeeklySignalView.kt**: Main weekly schedule view with synchronized horizontal scrolling and full-width row dividers
- **SignalItemCard.kt**: Individual signal item display component (120dp width, 44dp height, color-coded, configurable corner radius)
- **TimeSlotColumn.kt**: Contains TimeSlotHeader and DayCell components for time-based grid layout
- **Constants.kt**: Centralized UI dimension constants for consistent styling across components
- **WeeklySignalViewModel.kt**: ViewModel managing SignalItem state and alarm scheduling

#### Registration Flow (`ui/registration/`)
- **SignalRegistrationScreen.kt**: Screen for creating new SignalItems with multiple time slots
- **SignalRegistrationForm.kt**: Form component with TimeSlotEditor integration

#### Edit Flow (`ui/edit/`)
- **SignalEditScreen.kt**: Screen for editing existing SignalItems
- **TimeSlotEditor.kt**: Component for managing multiple time slots in a list format
- **TimeSlotDialog.kt**: Dialog for selecting individual day/time combinations

### Navigation (`navigation/`)
- **Navigation.kt**: Screen route definitions including SignalEdit route with parameters
- **NavGraph.kt**: Navigation graph with routes for main view, registration, and editing

## Development Commands

### Build and Run
```bash
# RECOMMENDED: Desktop-only compilation for development and verification
./gradlew :composeApp:compileKotlinDesktop

# Run desktop application
./gradlew run

# Full build (may fail due to Android SDK environment issues)
./gradlew build

# Android builds (may fail in current environment due to SDK configuration)
./gradlew installDebug        # Requires emulator/device and proper Android SDK setup
./gradlew assembleDebug       # May fail due to missing Android SDK
```

### Room Database Development
```bash
# Clean and rebuild to regenerate Room code
./gradlew clean && ./gradlew :composeApp:compileKotlinDesktop

# Generate Room database schema (exports to composeApp/schemas/)
./gradlew :composeApp:kspCommonMainKotlinMetadata

# Check Room annotation processing for desktop
./gradlew :composeApp:kspDesktopKotlin
```

### Testing
```bash
# RECOMMENDED: Desktop-specific tests (reliable in current environment)
./gradlew desktopTest

# Full test suite (may fail due to Android environment issues)
./gradlew test
```

### Distribution
```bash
# Create desktop distribution packages
./gradlew createDistributable

# Package for specific formats (DMG, MSI, DEB)
./gradlew packageDmg
./gradlew packageMsi
./gradlew packageDeb
```

## Configuration Files

- **gradle/libs.versions.toml**: Centralized dependency version management including Room 2.7.2, KSP 2.2.0-2.0.2, and SQLite 2.5.2
- **composeApp/build.gradle.kts**: Main module configuration with Compose Multiplatform setup, Room plugin, and KSP configuration
- **build.gradle.kts**: Root project configuration with plugin declarations

### Room Database Configuration
- **Dependencies**: Room runtime, SQLite bundled, and Room compiler for annotation processing
- **KSP Setup**: Configured for commonMainMetadata, desktop, and Android targets
- **Schema Export**: Enabled to `composeApp/schemas/` directory for version control and migration planning

## UI Implementation Details

### WeeklySignal Interface Design
- **Layout**: 7-day vertical axis × time horizontal axis grid with full-width horizontal dividers
- **Scrolling**: Synchronized horizontal scrolling across all rows using shared LazyListState
- **Components**: Fixed day labels (60dp width) + scrollable time slots
- **SignalItem Cards**: Fixed 120dp width, 44dp height, color-coded, 6-character name truncation
- **Time Display**: 24-hour format (HH:MM), displayed at bottom of each cell with consistent styling
- **Multiple SignalItems**: Same cell can contain multiple SignalItems with adaptive display modes
- **Cell Dimensions**: 116dp content height + 4dp spacing = 120dp total height per row

### Scroll Synchronization Architecture
**Critical Implementation Note**: Uses restructured layout with synchronized LazyRows:

1. **Row-Based Structure**: Each day has its own row with synchronized horizontal scrolling
2. **Shared LazyListState**: Single scroll state synchronized across time header and all day rows
3. **Fixed Headers**: Time labels and day labels remain stationary during scroll
4. **Full-Width Dividers**: Horizontal dividers span the entire screen width for visual separation

### Key Design Patterns
- **Time Slot Generation**: Dynamic time slots based on actual SignalItem times (15-30min intervals)
- **Data Organization**: Items grouped by time, then distributed across days
- **Text Truncation**: `getTruncatedName(6)` with "..." for longer names
- **Material 3 Theming**: Color-coded SignalItems with custom colors
- **Color Picker**: UI component for selecting custom SignalItem colors

### Multiple SignalItems Display System
- **Single Item**: Full-height display with unified corner radius (8dp all corners)
- **Two Items**: Vertically stacked with first item having top corners rounded, second item no corners
- **Three+ Items**: First item + "+N" button, with modal dialog for selection
- **Time Display**: Unified at bottom of each cell with consistent styling (14sp font, 28dp height)
- **Corner Radius System**: Adaptive corner radius based on item count for visual cohesion
- **Modal Dialog**: Shows all items in a cell with unified day/time display at bottom

### UI Constants (Constants.kt)
Centralized dimension management for consistent styling:

```kotlin
object WeeklyGridConstants {
    // SignalItem dimensions
    val SIGNAL_ITEM_WIDTH = 120.dp
    val SIGNAL_ITEM_HEIGHT = 44.dp
    
    // Time display dimensions
    val TIME_DISPLAY_HEIGHT = 28.dp
    
    // Padding and spacing
    val CELL_PADDING = 2.dp
    val CELL_SPACING = 2.dp
    val ITEM_SPACING = 1.dp
    
    // Cell dimensions
    val CELL_CONTENT_HEIGHT = 116.dp // (44dp × 2) + 1dp + 28dp
    val CELL_TOTAL_HEIGHT = 120.dp   // Content + (2dp × 2) spacing
    
    // Layout dimensions
    val TIME_HEADER_HEIGHT = 40.dp
    val DAY_LABEL_WIDTH = 60.dp
    val CORNER_RADIUS = 8.dp
}
```

### Component Architecture Details
- **TimeSlotHeader**: Displays time labels (HH:MM format) with responsive width
- **DayCell**: Container for multiple SignalItems with adaptive layout
- **MultipleSignalItemsCell**: Handles 1/2/3+ item display logic with corner radius adaptation
- **SignalItemsModal**: Modal dialog for selecting from 3+ items with unified time display
- **SignalItemCard**: Individual item display with configurable corner radius and show/hide time option

## Alarm/Notification System

### Android Implementation
- **System Integration**: Uses Android AlarmManager for weekly recurring alarms
- **Notification Channels**: Creates dedicated notification channels for alarms
- **Permission Handling**: Manages exact alarm permissions (Android 12+) and notification permissions
- **Database Persistence**: Stores alarm state in Room database with AlarmStateEntity
- **Broadcast Receivers**: AlarmReceiver for handling alarm events, BootReceiver for alarm restoration

### Desktop Implementation
- **Mock Implementation**: DesktopAlarmManager provides interface compatibility without actual alarms
- **Development Support**: Allows UI testing without system alarm dependencies

### Key Features
- **Weekly Recurring Alarms**: Schedules alarms for each TimeSlot in a SignalItem
- **Batch Operations**: Schedule/cancel/update alarms for entire SignalItems
- **Permission Management**: Handles Android notification and exact alarm permissions
- **Alarm State Tracking**: Persists alarm scheduling state in database
- **Sound and Vibration**: Configurable alarm behavior per SignalItem

## Database Architecture

### Current Status: ✅ FULLY IMPLEMENTED
- **Database Integration**: Complete Room database implementation with cross-platform support
- **Data Persistence**: All SignalItems, TimeSlots, and AlarmStates persist across app restarts
- **Platform Storage**: 
  - Desktop: `${TEMP_DIR}/weekly_signal.db` (e.g., `/tmp/weekly_signal.db`)
  - Android: `/data/data/net.mercuryksm/databases/weekly_signal.db`

### Database Schema
- **signals**: Main SignalItem data (id, name, description, sound, vibration, color)
- **time_slots**: TimeSlot data with foreign key to signals table
- **alarm_states**: Alarm scheduling state with timestamps and request codes

### Key Architecture Points
1. **Repository Pattern**: SignalRepository manages all database operations with reactive StateFlow
2. **Platform-Specific Services**: DatabaseServiceFactory creates appropriate database service per platform
3. **Fallback Behavior**: Loads sample data only when database is empty (first run)
4. **Type-Safe Operations**: Room DAO interfaces with compile-time SQL validation

## Development Notes

- **Environment Constraints**: Android SDK not available in current development environment
- **Recommended Workflow**: Use `./gradlew :composeApp:compileKotlinDesktop` for all verification and development
- **Testing Strategy**: Focus on desktop platform for UI development and validation
- **Code Standards**: All comments and test data written in English
- **Hot Reload**: Uses Compose Hot Reload for faster development iteration
- **Targets**: Android API 24+ and JVM 11+

### Common Issues and Solutions
- **Build Issues**: Always use desktop-only builds for verification when Android SDK not configured
- **Scroll Sync Problems**: Ensure shared LazyListState across all row LazyRows, avoid nested scrollable components
- **Performance**: Use `remember` for expensive calculations, avoid recreating TimeSlots unnecessarily
- **Nested Scrolling**: Use regular `Column` instead of `LazyColumn` when inside scrollable containers
- **Multiple Items Display**: Use MultipleSignalItemsCell for adaptive display based on item count
- **UI Dimension Changes**: Modify Constants.kt for consistent styling across all components

## Key Implementation Insights

### Technical Decisions
1. **Row-Based Layout Architecture**: Restructured from single LazyRow to row-based layout with synchronized scrolling
2. **Multiple SignalItems Support**: Adaptive display system for 1, 2, and 3+ items in same cell
3. **Centralized UI Constants**: All dimensions managed in Constants.kt for consistent styling
4. **Color-Coded SignalItems**: Each SignalItem has configurable color for visual distinction
5. **Repository Pattern**: Centralized data management with reactive state updates
6. **Platform-Specific Database Services**: Unified interface with platform-appropriate implementations
7. **Alarm State Persistence**: Tracks alarm scheduling state in database for reliability
8. **Batch Alarm Operations**: Efficiently manage multiple alarms per SignalItem

### Sample Data Patterns
- **Morning Meeting**: Monday, Wednesday, Friday at 9:00 AM
- **Lunch Break**: Monday through Friday at 12:30 PM
- **Project Review**: Wednesday at 3:00 PM only
- **Exercise Time**: Tuesday/Thursday at 6:30 PM, Saturday at 7:00 PM
- **Multiple Items**: Same time slot can contain multiple SignalItems with adaptive display

## Navigation Flow
- **Main View**: WeeklySignalView displays all SignalItems in weekly grid
- **Add Flow**: FloatingActionButton → SignalRegistrationScreen → back to main view
- **Edit Flow**: Click SignalItem → SignalEditScreen → back to main view
- **Alarm Management**: Automatic alarm scheduling/cancellation on SignalItem CRUD operations

## Testing Architecture
- **Unit Tests**: EntityMappers, data model validation
- **Mock Services**: MockDatabaseRepository for testing without database
- **Platform Testing**: Desktop-focused testing due to environment constraints
- **Integration Tests**: Room database operations with in-memory database
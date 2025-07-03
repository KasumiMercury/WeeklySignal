# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WeeklySignal is a Kotlin Multiplatform project using Compose Multiplatform, targeting Android and Desktop platforms. It displays a weekly schedule interface showing SignalItems (user-defined notification items) across days of the week with time-based horizontal scrolling.

## Architecture

- **Package**: `net.mercuryksm`
- **Multiplatform Structure**:
  - `commonMain/`: Shared code for all platforms (UI, business logic, data models)
  - `androidMain/`: Android-specific implementations
  - `desktopMain/`: Desktop-specific implementations
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
- **SignalDao.kt/TimeSlotDao.kt**: Room DAO interfaces with SQL query annotations for type-safe database operations
- **SignalEntity.kt/TimeSlotEntity.kt**: Room entity models with `@Entity`, `@PrimaryKey`, and foreign key constraints
- **DatabaseRepository.kt**: Common interface for database operations implemented by platform-specific repositories
- **EntityMappers.kt**: Conversion utilities between domain models (SignalItem/TimeSlot) and database entities
- **Platform-Specific Services**: DesktopSignalDatabaseService, AndroidSignalDatabaseService for platform-specific database operations
- **DatabaseServiceFactory.kt**: Expect/actual factory pattern for creating platform-specific database services
- **Platform-Specific Builders**: AndroidDatabaseBuilder (Context-based) and DesktopDatabaseBuilder (temp directory)

**⚠️ Current Status**: Database service not initialized in application - all operations fall back to in-memory sample data. See "Current Implementation Issues" section for details.

### UI Layer (`ui/`)
- **WeeklySignalView.kt**: Main weekly schedule view with synchronized horizontal scrolling, supports multiple time slots per SignalItem
- **SignalItemCard.kt**: Individual signal item display component (120dp width, text truncation)
- **TimeSlotColumn.kt**: Vertical column showing all days for a specific time slot
- **WeeklySignalViewModel.kt**: ViewModel for managing SignalItem state and repository operations

#### Registration Flow (`ui/registration/`)
- **SignalRegistrationScreen.kt**: Screen for creating new SignalItems with multiple time slots
- **SignalRegistrationForm.kt**: Form component with TimeSlotEditor integration

#### Edit Flow (`ui/edit/`)
- **SignalEditScreen.kt**: Screen for editing existing SignalItems
- **TimeSlotEditor.kt**: Component for managing multiple time slots in a list format
- **TimeSlotDialog.kt**: Dialog for selecting individual day/time combinations

### Main Components
- **App.kt**: Main Compose UI entry point integrating WeeklySignalView
- **Platform.kt**: Platform abstraction interface
- **main.kt** (desktop): Desktop application entry point with Window configuration

### Navigation (`navigation/`)
- **Navigation.kt**: Screen route definitions including SignalEdit route with parameters
- **NavGraph.kt**: Navigation graph with routes for main view, registration, and editing

## Development Commands

### Build and Run
```bash
# Build the project (full build - may fail due to Android SDK environment issues)
./gradlew build

# RECOMMENDED: Desktop-only compilation for development and verification
./gradlew :composeApp:compileKotlinDesktop

# Run desktop application
./gradlew run

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

# Verify database operations (after fixing database service initialization)
./gradlew run  # Desktop app with database persistence
```

### Database Service Verification
```bash
# Test database service integration after fixes
./gradlew :composeApp:compileKotlinDesktop  # Verify compilation
./gradlew run  # Run desktop app and test SignalItem persistence

# Check for database file creation
# Desktop: Check ${TEMP_DIR}/weekly_signal.db exists after creating SignalItems
# Android: Use Android Studio Database Inspector or adb shell
```

### Testing
```bash
# RECOMMENDED: Desktop-specific tests (reliable in current environment)
./gradlew desktopTest

# Full test suite (may fail due to Android environment issues)
./gradlew test

# Android unit tests (may fail due to SDK configuration)
./gradlew testDebugUnitTest
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

- **gradle/libs.versions.toml**: Centralized dependency version management including Room 2.7.2, KSP 2.1.21-2.0.1, and SQLite 2.5.2
- **composeApp/build.gradle.kts**: Main module configuration with Compose Multiplatform setup, Room plugin, and KSP configuration
- **build.gradle.kts**: Root project configuration with plugin declarations

### Room Database Configuration
- **Dependencies**: Room runtime, SQLite bundled, and Room compiler for annotation processing
- **KSP Setup**: Configured for commonMainMetadata and desktop targets for cross-platform code generation
- **Schema Export**: Enabled to `composeApp/schemas/` directory for version control and migration planning

## UI Implementation Details

### WeeklySignal Interface Design
- **Layout**: 7-day vertical axis × time horizontal axis grid
- **Scrolling**: Single LazyRow for synchronized horizontal scrolling across all days
- **Components**: Fixed day labels (60dp width) + scrollable time slots
- **SignalItem Cards**: Fixed 120dp width, 80dp height, 10-character name truncation
- **Time Display**: 24-hour format (HH:MM)
- **Empty Slots**: Compact 20dp width for time periods without SignalItems
- **Multiple Time Slots**: Same SignalItem appears in multiple time/day combinations as configured

### Scroll Synchronization Architecture
**Critical Implementation Note**: Compose Multiplatform has limitations with multiple LazyRows inside LazyColumn - only the last LazyRow becomes scrollable. The solution uses:

1. **Single LazyRow Structure**: One horizontal scrolling container for all time slots
2. **TimeSlotColumn Pattern**: Each time slot contains a vertical column with 7 day cells
3. **Fixed Headers**: Time labels and day labels remain stationary during scroll
4. **Shared State**: Single `LazyListState` manages scroll position

### Key Design Patterns
- **Time Slot Generation**: Dynamic time slots based on actual SignalItem times (15-30min intervals)
- **Data Organization**: Items grouped by time, then distributed across days, with multiple appearances per SignalItem
- **Text Truncation**: `getTruncatedName(10)` with "..." for longer names
- **Material 3 Theming**: Primary container colors for SignalItems, outline colors for dividers

### Multiple Time Slot Architecture
- **SignalItem Structure**: Each SignalItem contains `List<TimeSlot>` instead of single time/day
- **TimeSlot Data Class**: Individual combination of hour, minute, and dayOfWeek with unique ID
- **UI Rendering**: TimeSlotColumn searches for SignalItems matching specific time/day combinations
- **CRUD Operations**: Add, edit, delete individual time slots within a SignalItem

## Development Notes

- Uses Compose Hot Reload for faster development iteration
- Targets Android API 24+ and JVM 11+
- Material 3 design system implementation
- Resource management through Compose Resources
- Lifecycle-aware ViewModels for state management
- **Code Standards**: All comments and test data should be written in English
- **Sample Data**: Test data uses English names and descriptions for better international readability
- **Navigation Flow**: WeeklySignalView → SignalRegistrationScreen (create) or SignalEditScreen (edit) → back to WeeklySignalView

## Testing and Validation

### Desktop Development
```bash
# Fast compilation check (desktop only)
./gradlew :composeApp:compileKotlinDesktop
```

### Common Issues and Solutions
- **Scroll Sync Problems**: Ensure single LazyRow architecture, avoid nested scrollable components
- **Build Issues**: Android SDK not configured in current environment - ALWAYS use desktop-only builds for verification
- **Performance**: Use `remember` for expensive calculations, avoid recreating TimeSlots unnecessarily
- **Nested Scrolling Issues**: Use regular `Column` instead of `LazyColumn` when inside scrollable containers to avoid infinite height constraints
- **Legacy Components**: `DayColumn.kt` and `TimelineHeader.kt` were removed as they conflicted with the new multiple time slot architecture

### Environment Constraints
- **Android SDK**: Not available in current development environment
- **Recommended Workflow**: Use `./gradlew :composeApp:compileKotlinDesktop` for all verification and development
- **Testing Strategy**: Focus on desktop platform for UI development and validation
- **Room Database**: Works on both platforms but development/testing should use desktop compilation
- **KSP Compatibility**: May show warnings about version compatibility but compilation succeeds

## Features Implemented

### Core Functionality
- **Multiple Time Slots per SignalItem**: Each SignalItem can have multiple day/time combinations
- **Interactive Time Slot Management**: Add, edit, and delete time slots through intuitive UI
- **Weekly Grid Display**: Shows all SignalItems across their configured time slots
- **Click-to-Edit**: Tap SignalItems in the weekly view to open edit screen
- **Database Architecture Ready**: Complete Room database implementation for both Android and Desktop platforms
- **Cross-Platform Database Service**: Unified database operations with platform-specific file storage
- **⚠️ Database Integration**: Currently operates on sample data - database service requires initialization (see "Current Implementation Issues")

### User Interface
- **TimeSlot Display Format**: "Wed 12:00" format for easy readability
- **Time/Day Selection Dialog**: Material 3 TimePicker with day selection radio buttons
- **Dynamic Time Slot List**: Shows configured time slots with delete option
- **Form Validation**: Requires at least one time slot and signal name

### Data Model
- **Flexible TimeSlot Structure**: Independent time/day combinations with unique IDs
- **Immutable Updates**: Uses copy() pattern for state updates
- **Repository Pattern**: Centralized data management with reactive state
- **Sample Data**: Demonstrates various scheduling patterns (daily, specific days, different times)
- **Room Entities**: Database entities with proper annotations, constraints, and foreign key relationships
- **Type-Safe Database Operations**: Room DAO interfaces with compile-time SQL validation

### Sample Scheduling Patterns
- **Morning Meeting**: Monday, Wednesday, Friday at 9:00 AM
- **Lunch Break**: Monday through Friday at 12:30 PM
- **Project Review**: Wednesday at 3:00 PM only
- **Exercise Time**: Tuesday/Thursday at 6:30 PM, Saturday at 7:00 PM

## Database Operations Documentation

For comprehensive database operations documentation, see `docs/db.md` which covers:

### Screen-Specific Database Operations
- **WeeklySignalView**: Read operations (display all SignalItems, filter by day)
  - `WeeklySignalView.kt:43-44` → `viewModel.getAllSignalItems()` → Repository → Database
- **SignalRegistrationScreen**: Create operations (save new SignalItems)
  - `SignalRegistrationScreen.kt:97` → `onSignalSaved()` → ViewModel → Repository → Database INSERT
- **SignalEditScreen**: Update/Delete operations (modify existing SignalItems)
  - `SignalEditScreen.kt:26` → `viewModel.getSignalItemById()` → Repository → Database SELECT
  - Update flow: ViewModel → Repository → Database UPDATE

### Complete Database Operation Chain
```
UI Screen Action → Navigation/Callback → ViewModel Method → Repository Method → 
Database Service → Platform-Specific Service → Database Repository → Room DAO → SQLite
```

### Platform Storage Locations
- **Desktop**: `${TEMP_DIR}/weekly_signal.db` (e.g., `/tmp/weekly_signal.db`)
- **Android**: `/data/data/net.mercuryksm/databases/weekly_signal.db`

## Current Implementation Issues

### Database Service Integration Status

**⚠️ Critical Issue**: Database service is not properly initialized in the application, causing all database operations to fall back to in-memory sample data only.

#### Problem Details
1. **WeeklySignalViewModel.kt:12**: Uses default `SignalRepository()` constructor without database service
2. **SignalRepository.kt:15**: `databaseService: SignalDatabaseService? = null` defaults to null
3. **App.kt**: Does not initialize database service for ViewModel
4. **Result**: All CRUD operations work in memory only, data is lost on app restart

#### Expected Fix
To enable proper database persistence, the application needs to initialize the database service:

```kotlin
// In App.kt or main entry points
val databaseService = DatabaseServiceFactory().createSignalDatabaseService()
val repository = SignalRepository(databaseService)
val viewModel = WeeklySignalViewModel(repository)
```

#### Fix Implementation Status
- ✅ Database service architecture exists and is fully implemented
- ✅ Platform-specific factories (Desktop/Android) are implemented
- ❌ Application entry points do not initialize database service
- ❌ ViewModels are created without database service parameter

### Workaround for Testing
For development and testing, the application loads sample data that demonstrates all features:
- 4 sample SignalItems with various scheduling patterns
- Multiple time slots per item (Morning Meeting: Mon/Wed/Fri, Lunch: Daily, etc.)
- All UI functionality works correctly with sample data

## Implementation Insights

### Key Technical Decisions
1. **Regular Column over LazyColumn**: Avoided nested scrolling issues in TimeSlotEditor
2. **UITimeSlot vs TimeSlot**: Separate data classes to avoid naming conflicts between UI and data models
3. **Removal of Legacy Components**: Cleaned up outdated DayColumn and TimelineHeader files
4. **Material 3 Integration**: Full adoption of Material 3 components and theming
5. **Database Service Separation**: Platform-agnostic database operations with expect/actual pattern
6. **Repository Pattern**: Centralized data management with fallback to sample data when database unavailable
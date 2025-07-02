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
- **SignalItem.kt**: Core data model for notification items with time, day, and content
- **DayOfWeekJp.kt**: Japanese day-of-week enum with display names

### UI Layer (`ui/`)
- **WeeklySignalView.kt**: Main weekly schedule view with synchronized horizontal scrolling
- **SignalItemCard.kt**: Individual signal item display component (120dp width, text truncation)
- **TimeSlotColumn.kt**: Vertical column showing all days for a specific time slot
- **DayColumn.kt**: Individual day row component (legacy, may be removed)
- **TimelineHeader.kt**: Time axis header component (legacy, may be removed)

### Main Components
- **App.kt**: Main Compose UI entry point integrating WeeklySignalView
- **Platform.kt**: Platform abstraction interface
- **main.kt** (desktop): Desktop application entry point with Window configuration

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

- **gradle/libs.versions.toml**: Centralized dependency version management
- **composeApp/build.gradle.kts**: Main module configuration with Compose Multiplatform setup
- **build.gradle.kts**: Root project configuration with plugin declarations

## UI Implementation Details

### WeeklySignal Interface Design
- **Layout**: 7-day vertical axis × time horizontal axis grid
- **Scrolling**: Single LazyRow for synchronized horizontal scrolling across all days
- **Components**: Fixed day labels (60dp width) + scrollable time slots
- **SignalItem Cards**: Fixed 120dp width, 80dp height, 10-character name truncation
- **Time Display**: 24-hour format (HH:MM)
- **Empty Slots**: Compact 20dp width for time periods without SignalItems

### Scroll Synchronization Architecture
**Critical Implementation Note**: Compose Multiplatform has limitations with multiple LazyRows inside LazyColumn - only the last LazyRow becomes scrollable. The solution uses:

1. **Single LazyRow Structure**: One horizontal scrolling container for all time slots
2. **TimeSlotColumn Pattern**: Each time slot contains a vertical column with 7 day cells
3. **Fixed Headers**: Time labels and day labels remain stationary during scroll
4. **Shared State**: Single `LazyListState` manages scroll position

### Key Design Patterns
- **Time Slot Generation**: Dynamic time slots based on actual SignalItem times (15-30min intervals)
- **Data Organization**: Items grouped by time, then distributed across days
- **Text Truncation**: `getTruncatedName(10)` with "..." for longer names
- **Material 3 Theming**: Primary container colors for SignalItems, outline colors for dividers

## Development Notes

- Uses Compose Hot Reload for faster development iteration
- Targets Android API 24+ and JVM 11+
- Material 3 design system implementation
- Resource management through Compose Resources
- Lifecycle-aware ViewModels for state management

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

### Environment Constraints
- **Android SDK**: Not available in current development environment
- **Recommended Workflow**: Use `./gradlew :composeApp:compileKotlinDesktop` for all verification and development
- **Testing Strategy**: Focus on desktop platform for UI development and validation
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WeeklySignal is a Kotlin Multiplatform project using Compose Multiplatform, targeting Android and Desktop platforms. The project structure follows Kotlin Multiplatform conventions with shared common code and platform-specific implementations.

## Architecture

- **Package**: `net.mercuryksm`
- **Multiplatform Structure**:
  - `commonMain/`: Shared code for all platforms (UI, business logic)
  - `androidMain/`: Android-specific implementations
  - `desktopMain/`: Desktop-specific implementations
  - `commonTest/`: Shared test code

## Key Components

- **App.kt**: Main Compose UI entry point with Material 3 theming
- **Greeting.kt**: Platform-aware greeting logic
- **Platform.kt**: Platform abstraction interface
- **main.kt** (desktop): Desktop application entry point with Window configuration

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run desktop application
./gradlew run

# Run Android (requires emulator/device)
./gradlew installDebug

# Build Android APK
./gradlew assembleDebug
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific platform tests
./gradlew desktopTest
./gradlew testDebugUnitTest  # Android unit tests
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

## Development Notes

- Uses Compose Hot Reload for faster development iteration
- Targets Android API 24+ and JVM 11+
- Material 3 design system implementation
- Resource management through Compose Resources
- Lifecycle-aware ViewModels for state management
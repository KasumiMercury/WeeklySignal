# Database Layer Implementation

This directory contains the database persistence layer for WeeklySignal using Room Kotlin Multiplatform. The architecture provides unified database operations across Android and Desktop platforms with platform-specific implementations for file storage.

## Architecture Overview

### Common Layer (commonMain)
- **DatabaseRepository**: Interface defining database operations
- **SignalDatabaseService**: High-level service interface for business operations
- **SignalEntity/TimeSlotEntity**: Room entity models with annotations
- **SignalDao/TimeSlotDao**: Room DAO interfaces with SQL query annotations
- **AppDatabase**: Room database class with `@Database` and `@ConstructedBy` annotations
- **EntityMappers**: Conversion utilities between domain models and entities
- **DatabaseServiceFactory**: Platform-agnostic factory (expect/actual pattern)

### Platform-Specific Implementations
- **Android**: AndroidDatabaseRepository + AndroidDatabaseServiceFactory + DatabaseBuilder
- **Desktop**: DesktopDatabaseRepository + DesktopDatabaseServiceFactory + DesktopDatabaseBuilder

## Room Database Implementation

### Technology Stack
- **Room Version**: 2.7.2 (Kotlin Multiplatform compatible)
- **SQLite**: androidx.sqlite 2.5.2 (bundled for cross-platform support)
- **KSP**: 2.1.21-2.0.1 (Kotlin Symbol Processing for annotation processing)

### Database Schema

#### Signal Table (`signals`)
```sql
CREATE TABLE signals (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    sound INTEGER NOT NULL,  -- BOOLEAN stored as INTEGER
    vibration INTEGER NOT NULL  -- BOOLEAN stored as INTEGER
);
```

#### TimeSlot Table (`time_slots`)
```sql
CREATE TABLE time_slots (
    id TEXT PRIMARY KEY NOT NULL,
    signalId TEXT NOT NULL,
    hour INTEGER NOT NULL,
    minute INTEGER NOT NULL,
    dayOfWeek INTEGER NOT NULL,
    FOREIGN KEY(signalId) REFERENCES signals(id) ON DELETE CASCADE
);
```

### Platform-Specific Database Storage

#### Android Implementation
- **Location**: Internal app database directory via `Context.getDatabasePath()`
- **File**: `/data/data/net.mercuryksm/databases/weekly_signal.db`
- **Builder**: Uses Android Context for proper database initialization

#### Desktop Implementation
- **Location**: System temporary directory via `System.getProperty("java.io.tmpdir")`
- **File**: `${TEMP_DIR}/weekly_signal.db` (e.g., `/tmp/weekly_signal.db` on Linux)
- **Builder**: Uses Java File API for cross-platform compatibility

## Usage Example

```kotlin
// Platform-specific initialization (Android)
val context: Context = // Android Context
val androidRepository = AndroidDatabaseRepository(context)

// Platform-specific initialization (Desktop)
val desktopRepository = DesktopDatabaseRepository()

// Common usage through factory
val databaseServiceFactory = DatabaseServiceFactory() // Platform-specific actual implementation
val databaseService = databaseServiceFactory.createSignalDatabaseService()

// Initialize repository with database service
val repository = SignalRepository(databaseService)

// Initialize ViewModel with repository
val viewModel = WeeklySignalViewModel(repository)

// Use persistent operations in UI
viewModel.saveSignalItem(signalItem) { result ->
    result.onSuccess {
        // Data persisted to Room database
    }.onFailure { exception ->
        // Handle database error
    }
}
```

## Room DAO Operations

### SignalDao
- `insert(signal: SignalEntity): Long` - Insert new signal
- `update(signal: SignalEntity)` - Update existing signal
- `delete(signalId: String)` - Delete signal by ID
- `getById(signalId: String): SignalEntity?` - Get signal by ID
- `getAll(): List<SignalEntity>` - Get all signals

### TimeSlotDao
- `insert(timeSlot: TimeSlotEntity): Long` - Insert new time slot
- `update(timeSlot: TimeSlotEntity)` - Update existing time slot
- `delete(timeSlotId: String)` - Delete time slot by ID
- `deleteBySignalId(signalId: String)` - Delete all time slots for a signal (cascade)
- `getBySignalId(signalId: String): List<TimeSlotEntity>` - Get time slots for signal
- `getAll(): List<TimeSlotEntity>` - Get all time slots

## Build Configuration

### Gradle Dependencies
```kotlin
// Room Kotlin Multiplatform
implementation("androidx.room:room-runtime:2.7.2")
implementation("androidx.sqlite:sqlite-bundled:2.5.2")

// KSP for annotation processing
id("com.google.devtools.ksp") version "2.1.21-2.0.1"
id("androidx.room") version "2.7.2"

// Platform-specific KSP configurations
add("kspCommonMainMetadata", "androidx.room:room-compiler:2.7.2")
add("kspDesktop", "androidx.room:room-compiler:2.7.2")
```

### Room Configuration
```kotlin
room {
    schemaDirectory("$projectDir/schemas")
}
```

## Testing

- **EntityMappersTest**: Tests domain model ↔ Room entity conversions
- **MockDatabaseRepository**: Test double for unit testing without database
- **Room Database Tests**: Can test actual Room operations with in-memory database
- **Cross-Platform Tests**: Verify consistent behavior between Android and Desktop

## Database Migration Strategy

### Current State
- **Version 1**: Initial schema with signals and time_slots tables
- **Schema Export**: Enabled for version control and migration planning

### Future Migration Considerations
1. Use Room's `@Database(version = X, exportSchema = true)` for versioning
2. Implement `Migration` classes for schema changes
3. Test migrations on both Android and Desktop platforms
4. Consider data validation and constraint handling improvements

## Implementation Benefits

### Room KMP Advantages
- **Type Safety**: Compile-time SQL query validation
- **Code Generation**: Automatic DAO implementations
- **Cross-Platform**: Same database code for Android and Desktop
- **Performance**: Optimized SQLite operations
- **Maintenance**: Single source of truth for database schema

### Architecture Benefits
- **Separation of Concerns**: Repository pattern isolates database logic
- **Platform Abstraction**: Common interface with platform-specific storage
- **Testability**: Easy to mock and test database operations
- **Consistency**: Identical CRUD operations across platforms

## Troubleshooting

### Common Issues
1. **KSP Version Compatibility**: Ensure KSP version matches Kotlin version
2. **Schema Export**: Check `schemas/` directory for generated database schema
3. **Platform-Specific Paths**: Verify database file locations are accessible
4. **Foreign Key Constraints**: Ensure proper cascade deletion behavior

### Build Commands
```bash
# Desktop compilation (recommended for development)
./gradlew :composeApp:compileKotlinDesktop

# Full build (may require Android SDK setup)
./gradlew build

# Clean Room generated code
./gradlew clean
```
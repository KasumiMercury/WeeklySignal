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

## Database Operations by Screen

This section documents how each UI screen interacts with the database, including the complete flow from user action to database persistence.

### WeeklySignalView - Read Operations

**Screen**: `src/commonMain/kotlin/net/mercuryksm/ui/WeeklySignalView.kt`

#### Database Operations Performed

1. **Initial Data Load**
   - **Trigger**: Screen composition/initialization
   - **Function Chain**:
     - `WeeklySignalView.kt:43` → `viewModel { WeeklySignalViewModel() }`
     - `WeeklySignalViewModel.kt:12` → `SignalRepository()` (default constructor)
     - `SignalRepository.kt:25` → `loadFromDatabase()`
     - `SignalRepository.kt:144-146` → Falls back to `loadSampleData()` (no database service)

2. **Display All SignalItems**
   - **Trigger**: UI composition with reactive state
   - **Function Chain**:
     - `WeeklySignalView.kt:44` → `viewModel.getAllSignalItems()`
     - `WeeklySignalViewModel.kt:54` → `signalRepository.getAllSignalItems()`
     - `SignalRepository.kt:107` → `_signalItems.toList()`
   - **Database Operation**: **READ** - Returns all SignalItems from in-memory list

3. **Get SignalItems by Day**
   - **Trigger**: UI rendering for specific day columns
   - **Function Chain**:
     - `WeeklySignalView.kt` (TimeSlotColumn rendering) → `viewModel.getSignalItemsForDay(dayOfWeek)`
     - `WeeklySignalViewModel.kt:50` → `signalRepository.getSignalItemsForDay(dayOfWeek)`
     - `SignalRepository.kt:97-101` → Filters items by TimeSlot.dayOfWeek
   - **Database Operation**: **READ** - Filters in-memory data by day

**Current State**: WeeklySignalView operates on **sample data only** due to missing database service initialization.

### SignalRegistrationScreen - Create Operations

**Screen**: `src/commonMain/kotlin/net/mercuryksm/ui/registration/SignalRegistrationScreen.kt`

#### Database Operations Performed

1. **Save New SignalItem**
   - **Trigger**: User clicks "Add Signal" button
   - **Validation** (lines 75-85):
     - Name is required (not blank)
     - At least one time slot is required
   - **Function Chain**:
     - `SignalRegistrationScreen.kt:97` → `onSignalSaved(newSignalItem) { result ->`
     - `NavGraph.kt:33-39` → `viewModel.addSignalItem(signalItem) { result ->`
     - `WeeklySignalViewModel.kt:18-23` → `signalRepository.addSignalItem(signalItem)`
     - `SignalRepository.kt:28-48` → `databaseService?.saveSignalItem(signalItem)` OR fallback
     - **If database service exists**:
       - `DesktopSignalDatabaseService.kt:saveSignalItem()` → `databaseRepository.saveSignalItem()`
       - `DesktopDatabaseRepository.kt` → Room DAO operations
       - `SignalDao.kt:insert()` + `TimeSlotDao.kt:insert()` (multiple)
     - **If no database service**: `Result.success(Unit)` (lines 35)
     - `SignalRepository.kt:39` → `_signalItems.add(signalItem)` (in-memory update)

   - **Database Operations**:
     - **INSERT** signal entity into `signals` table
     - **INSERT** multiple time slot entities into `time_slots` table
     - **UPDATE** in-memory reactive state

2. **Success/Error Handling**
   - **Success**: `SignalRegistrationScreen.kt:100` → Show success dialog, navigate back
   - **Error**: `SignalRegistrationScreen.kt:102-104` → Show error dialog with exception message

**Current State**: Creates new SignalItems in **memory only** due to missing database service initialization.

### SignalEditScreen - Update Operations

**Screen**: `src/commonMain/kotlin/net/mercuryksm/ui/edit/SignalEditScreen.kt`

#### Database Operations Performed

1. **Load Existing SignalItem**
   - **Trigger**: Screen initialization with signalId parameter
   - **Function Chain**:
     - `SignalEditScreen.kt:26` → `viewModel.getSignalItemById(signalId)`
     - `WeeklySignalViewModel.kt:46` → `signalRepository.getSignalItemById(id)`
     - `SignalRepository.kt:103-105` → `_signalItems.find { it.id == id }`
   - **Database Operation**: **READ** - Find SignalItem by ID from in-memory list

2. **Update Existing SignalItem**
   - **Trigger**: User clicks "Save Changes" button (in SignalEditForm)
   - **Function Chain**:
     - Similar to registration but calls `updateSignalItem` instead of `addSignalItem`
     - `WeeklySignalViewModel.kt:25-30` → `signalRepository.updateSignalItem(signalItem)`
     - `SignalRepository.kt:50-73` → `databaseService?.updateSignalItem(updatedItem)` OR fallback
     - **If database service exists**:
       - `DesktopSignalDatabaseService.kt:updateSignalItem()` → `databaseRepository.updateSignalItem()`
       - Room DAO: `SignalDao.kt:update()` + TimeSlot cascade operations
     - **If no database service**: `Result.success(Unit)` (line 57)
     - `SignalRepository.kt:61-64` → Update item in `_signalItems` list by index

   - **Database Operations**:
     - **UPDATE** signal entity in `signals` table
     - **DELETE** + **INSERT** time slot entities (cascade update pattern)
     - **UPDATE** in-memory reactive state

3. **Delete SignalItem** (if delete functionality exists)
   - **Function Chain**:
     - `WeeklySignalViewModel.kt:32-37` → `signalRepository.removeSignalItem(signalItem)`
     - `SignalRepository.kt:75-95` → `databaseService?.deleteSignalItem(signalItem.id)` OR fallback
     - **If database service exists**:
       - `DesktopSignalDatabaseService.kt:deleteSignalItem()` → `databaseRepository.deleteSignalItem()`
       - Room DAO: `SignalDao.kt:delete()` (cascades to time_slots via foreign key)
     - `SignalRepository.kt:86` → `_signalItems.remove(signalItem)`

   - **Database Operations**:
     - **DELETE** signal entity from `signals` table
     - **CASCADE DELETE** all related time slot entities from `time_slots` table
     - **UPDATE** in-memory reactive state

**Current State**: Edits SignalItems in **memory only** due to missing database service initialization.

## Database Service Architecture Flow

### Complete Operation Chain

```
UI Screen Action
    ↓
Navigation/Callback (NavGraph.kt)
    ↓
ViewModel Method (WeeklySignalViewModel.kt)
    ↓
Repository Method (SignalRepository.kt)
    ↓
Database Service Method (SignalDatabaseService interface)
    ↓
Platform-Specific Service (Desktop/AndroidSignalDatabaseService.kt)
    ↓
Database Repository (Desktop/AndroidDatabaseRepository.kt)
    ↓
Room DAO (SignalDao.kt / TimeSlotDao.kt)
    ↓
SQLite Database (weekly_signal.db)
```

### Platform-Specific Initialization

#### Desktop Platform
- **Entry Point**: `src/desktopMain/kotlin/net/mercuryksm/main.kt:12`
- **Factory**: `DesktopDatabaseServiceFactory.kt:4-7`
- **Repository**: `DesktopDatabaseRepository.kt`
- **Storage**: `${TEMP_DIR}/weekly_signal.db`

#### Android Platform
- **Entry Point**: `src/androidMain/kotlin/net/mercuryksm/MainActivity.kt:16`
- **Factory**: `AndroidDatabaseServiceFactory.kt:5-9` (requires Context)
- **Repository**: `AndroidDatabaseRepository.kt`
- **Storage**: `/data/data/net.mercuryksm/databases/weekly_signal.db`

### Current Implementation Status

**⚠️ Database Service Not Initialized**: The current implementation creates `SignalRepository()` without a database service parameter, causing all operations to fall back to in-memory sample data.

**Expected Fix**: Initialize `WeeklySignalViewModel` with database service:
```kotlin
val databaseService = DatabaseServiceFactory().createSignalDatabaseService()
val repository = SignalRepository(databaseService)
val viewModel = WeeklySignalViewModel(repository)
```

### Error Handling Pattern

All database operations use `Result<T>` for consistent error handling:

1. **Success Path**: `Result.success(data)` → UI shows success state
2. **Failure Path**: `Result.failure(exception)` → UI shows error dialog
3. **Loading State**: `_isLoading.value = true/false` → UI shows progress indicator

### Sample Data Fallback

When no database service is available:
- `SignalRepository.kt:145` → `loadSampleData()`
- Creates 4 sample SignalItems with various scheduling patterns
- Data persists only during app session (not saved to database)

## Database Operation Summary

| Screen | Operation | Current Status | Database Tables | Functions |
|--------|-----------|----------------|-----------------|-----------|
| WeeklySignalView | READ all items | ✅ Memory only | - | `getAllSignalItems()` |
| WeeklySignalView | READ by day | ✅ Memory only | - | `getSignalItemsForDay()` |
| SignalRegistrationScreen | CREATE new item | ✅ Memory only | signals, time_slots | `addSignalItem()` |
| SignalEditScreen | READ by ID | ✅ Memory only | - | `getSignalItemById()` |
| SignalEditScreen | UPDATE item | ✅ Memory only | signals, time_slots | `updateSignalItem()` |
| SignalEditScreen | DELETE item | ✅ Memory only | signals, time_slots | `removeSignalItem()` |

**Legend**:
- ✅ **Memory only**: Function works but uses sample data, no database persistence
- 🔧 **Database ready**: Architecture exists but requires service initialization
- ❌ **Not implemented**: Functionality missing
# Database Layer Implementation

This directory contains the database persistence layer for WeeklySignal. The architecture is designed to support both Android (using Room) and Desktop (using SQLite/H2) platforms while maintaining a clean separation between common and platform-specific code.

## Architecture Overview

### Common Layer (commonMain)
- **DatabaseRepository**: Interface defining database operations
- **SignalDatabaseService**: High-level service interface for business operations
- **SignalEntity/TimeSlotEntity**: Database entity models
- **EntityMappers**: Conversion utilities between domain models and entities
- **DatabaseServiceFactory**: Platform-agnostic factory (expect/actual pattern)

### Platform-Specific Implementations
- **Android**: AndroidDatabaseRepository + AndroidSignalDatabaseService
- **Desktop**: DesktopDatabaseRepository + DesktopSignalDatabaseService

## Database Schema

### Signal Table
- id (PRIMARY KEY, TEXT)
- name (TEXT, NOT NULL)
- description (TEXT)
- sound (BOOLEAN)
- vibration (BOOLEAN)

### TimeSlot Table
- id (PRIMARY KEY, TEXT)
- signal_id (FOREIGN KEY, TEXT)
- hour (INTEGER)
- minute (INTEGER)
- day_of_week (INTEGER)

## Usage Example

```kotlin
// Create database service using factory
val databaseServiceFactory = DatabaseServiceFactory()
val databaseService = databaseServiceFactory.createSignalDatabaseService()

// Initialize repository with database service
val repository = SignalRepository(databaseService)

// Initialize ViewModel with repository
val viewModel = WeeklySignalViewModel(repository)

// Use persistent operations in UI
viewModel.saveSignalItem(signalItem) { result ->
    result.onSuccess {
        // Handle success
    }.onFailure { exception ->
        // Handle error
    }
}
```

## Integration Steps

1. **Android**: Implement Room database in AndroidDatabaseRepository
2. **Desktop**: Implement SQLite/H2 database in DesktopDatabaseRepository  
3. **UI Integration**: Pass `onSaveWithPersistence` parameter to SignalRegistrationScreen
4. **Dependency Injection**: Wire up DatabaseServiceFactory in your app initialization

## Testing

- **EntityMappersTest**: Tests domain model ↔ entity conversions
- **MockDatabaseRepository**: Test double for unit testing
- **Integration Tests**: Can be added using actual database implementations

## Next Steps

1. Implement actual Room database for Android
2. Implement actual SQLite/H2 database for Desktop
3. Add database migration support
4. Add data validation and constraint handling
5. Implement database versioning and upgrade strategies
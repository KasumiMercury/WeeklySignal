# Database Implementation Documentation

This document provides a comprehensive overview of the database implementation in the WeeklySignal application, focusing on architectural concepts, data relationships, and access patterns.

## Table of Contents

1. [Database Architecture Overview](#database-architecture-overview)
2. [Schema Design](#schema-design)
3. [Data Access Layers](#data-access-layers)
4. [Platform-Specific Implementations](#platform-specific-implementations)
5. [Database Access Patterns](#database-access-patterns)
6. [Function Dependencies](#function-dependencies)
7. [Transaction Management](#transaction-management)
8. [Performance Considerations](#performance-considerations)

## Database Architecture Overview

### Technology Stack
- **Room Kotlin Multiplatform**: Version 2.7.2 for cross-platform database operations
- **SQLite**: androidx.sqlite 2.5.2 bundled for consistent behavior across platforms
- **KSP (Kotlin Symbol Processing)**: Version 2.2.0-2.0.2 for compile-time code generation
- **Database Storage Locations**:
  - Android: Internal app database directory
  - Desktop: System temporary directory

### Architectural Principles
- **Cross-Platform Consistency**: Single schema definition works across Android and Desktop
- **Type Safety**: Compile-time SQL validation through Room annotations
- **Reactive Data Flow**: StateFlow integration for UI updates
- **Repository Pattern**: Abstraction layer between UI and database operations

## Schema Design

### Entity Relationships
The database consists of three main entities with hierarchical relationships:

#### 1. SignalEntity (Parent)
- **Purpose**: Stores core SignalItem information
- **Key Fields**: Unique identifier, name, description, notification preferences, color
- **Constraints**: Primary key validation, non-null requirements

#### 2. TimeSlotEntity (Child of SignalEntity)
- **Purpose**: Represents individual time/day combinations for each SignalItem
- **Relationship**: Many-to-one with SignalEntity via foreign key
- **Key Fields**: Time components (hour, minute), day of week, signal reference
- **Constraints**: Cascade deletion, time validation, indexed foreign key

#### 3. AlarmStateEntity (Child of TimeSlotEntity)
- **Purpose**: Tracks alarm scheduling state and system integration
- **Relationship**: One-to-one with TimeSlotEntity via foreign key
- **Key Fields**: Scheduling status, system alarm identifiers, timestamps
- **Constraints**: Cascade deletion, indexed references

### Data Integrity Features
- **Foreign Key Constraints**: Automatic relationship enforcement
- **Cascade Deletion**: Automatic cleanup of dependent records
- **Validation Rules**: Entity-level constraints for data consistency
- **Indexing Strategy**: Optimized queries for common access patterns

## Data Access Layers

### Layer Architecture
The database access follows a multi-layered architecture:

#### 1. UI Layer (ViewModels)
- **Responsibility**: Reactive state management and UI coordination
- **Data Flow**: Observes repository state through StateFlow
- **Error Handling**: Result-based error propagation to UI

#### 2. Repository Layer
- **Responsibility**: Business logic and data coordination
- **Features**: Reactive state management, fallback mechanisms, caching
- **Integration**: Bridges UI requirements with database services

#### 3. Service Layer
- **Responsibility**: High-level database operations and Result wrapping
- **Platform Abstraction**: Common interface with platform-specific implementations
- **Transaction Coordination**: Complex multi-table operations

#### 4. Platform-Specific Services
- **Responsibility**: Platform-specific database initialization and context management
- **Implementation**: Expect/actual pattern for multiplatform support
- **Resource Management**: Platform-appropriate database file handling

#### 5. Repository Interface Layer
- **Responsibility**: Direct database access through Room DAOs
- **Type Safety**: Compile-time SQL validation
- **Performance**: Optimized query execution

#### 6. DAO Layer
- **Responsibility**: SQL query definition and execution
- **Features**: Suspend function support, automatic transaction handling
- **Code Generation**: Room compiler generates implementation

## Platform-Specific Implementations

### Android Implementation
- **Database Context**: Android Context for proper database initialization
- **File Location**: Internal app data directory with proper permissions
- **Integration**: Android-specific database builder with Context dependency
- **Resource Management**: Automatic cleanup through Android lifecycle

### Desktop Implementation
- **Database Context**: Java-based file system access
- **File Location**: System temporary directory for development convenience
- **Integration**: Desktop-specific database builder without Context requirement
- **Resource Management**: Manual cleanup and file management

### Common Interface
- **Unified API**: Single SignalDatabaseService interface across platforms
- **Result Pattern**: Consistent error handling through Result wrapper
- **Async Operations**: Suspend functions for non-blocking database access
- **State Management**: Platform-agnostic reactive state updates

## Database Access Patterns

### Application Lifecycle Access Patterns

#### Initialization Phase
- **Trigger**: Application startup and repository initialization
- **Process**: Database service creation, schema validation, initial data loading
- **Fallback**: Sample data loading when database is empty or unavailable
- **State Update**: Repository state initialization with loaded data

#### UI Operation Access Patterns

#### Create Operations
- **Trigger**: User creates new SignalItem through registration screen
- **Process**: Entity validation, database insertion, state update
- **Transaction Scope**: Multi-table insert operations
- **Error Handling**: Rollback on failure, user notification

#### Read Operations
- **Trigger**: UI rendering, data refresh, specific item lookup
- **Process**: Repository state access, database query on refresh
- **Caching Strategy**: In-memory state with periodic database sync
- **Performance**: Batch loading for efficiency

#### Update Operations
- **Trigger**: User edits existing SignalItem through edit screen
- **Process**: Entity validation, database update, state synchronization
- **Transaction Scope**: Delete-and-insert pattern for TimeSlots
- **Consistency**: Atomic updates across related entities

#### Delete Operations
- **Trigger**: User removes SignalItem or system cleanup
- **Process**: Cascade deletion, state cleanup, alarm cancellation
- **Cleanup Scope**: Automatic removal of dependent records
- **Side Effects**: Integration with alarm system for cleanup

### Alarm Integration Access Patterns

#### Alarm State Persistence
- **Trigger**: Alarm scheduling, cancellation, or status changes
- **Process**: AlarmStateEntity management, system alarm coordination
- **Synchronization**: Database state reflects system alarm state
- **Recovery**: Database used for alarm restoration after system restart

## Function Dependencies

### Repository Dependencies
- **SignalRepository**: Central coordinator for all database operations
- **Dependencies**: SignalDatabaseService (optional), CoroutineScope
- **State Management**: Internal StateFlow for reactive updates
- **Error Strategy**: Graceful degradation when database unavailable

### Service Layer Dependencies
- **SignalDatabaseService**: High-level operation interface
- **Platform Services**: AndroidSignalDatabaseService, DesktopSignalDatabaseService
- **Entity Mapping**: Conversion between domain models and database entities
- **Result Wrapping**: Error handling through Result pattern

### Data Mapping Dependencies
- **EntityMappers**: Bidirectional conversion utilities
- **Domain to Entity**: SignalItem/TimeSlot to database entities
- **Entity to Domain**: Database entities to business objects
- **Collection Handling**: Efficient mapping of entity collections

## Transaction Management

### Room Transaction Behavior
- **Automatic Transactions**: Individual DAO operations run in transactions
- **Suspend Function Support**: Non-blocking database operations
- **Error Handling**: Automatic rollback on operation failure
- **Consistency**: ACID properties maintained across operations

### Multi-Operation Transactions
- **Service Layer Coordination**: Complex operations spanning multiple tables
- **Error Recovery**: Consistent state maintenance across operation failures
- **Atomic Updates**: All-or-nothing updates for related entities
- **Performance**: Batched operations where possible

### Concurrency Considerations
- **Repository State**: Thread-safe StateFlow for reactive updates
- **Database Access**: Room handles concurrent access internally
- **UI Updates**: Main thread state updates through coroutine dispatchers
- **Background Operations**: IO dispatcher for database operations

## Performance Considerations

### Query Optimization
- **Index Strategy**: Foreign key indexing for relationship queries
- **Batch Operations**: Efficient loading of related entities
- **Query Planning**: Room compiler optimization for generated queries
- **Result Caching**: Repository-level caching for frequently accessed data

### Memory Management
- **State Management**: Efficient StateFlow implementation for UI updates
- **Entity Conversion**: On-demand conversion between domain and entity models
- **Resource Cleanup**: Proper disposal of database connections and observers
- **Collection Handling**: Efficient processing of entity collections

### Reactive Data Flow
- **StateFlow Integration**: Efficient UI updates through reactive streams
- **Change Propagation**: Automatic UI updates on data changes
- **Subscription Management**: Lifecycle-aware state observation
- **Performance Monitoring**: Efficient state update mechanisms

### Cross-Platform Optimization
- **Shared Logic**: Maximum code reuse between platforms
- **Platform Adaptation**: Optimized implementations for each platform
- **Resource Management**: Platform-appropriate resource handling
- **Testing Strategy**: Consistent behavior validation across platforms

## Migration and Schema Evolution

### Version Management
- **Current Version**: Database version 4 with auto-migration support
- **Schema Export**: Versioned schema files for migration planning
- **Backward Compatibility**: Safe migration paths between versions
- **Testing**: Migration validation across version transitions

### Future Considerations
- **Schema Changes**: Planning for future entity modifications
- **Data Migration**: Strategies for complex data transformations
- **Performance Impact**: Migration performance on large datasets
- **Rollback Strategy**: Recovery mechanisms for failed migrations
# Alarm System Implementation Documentation

This document provides a comprehensive overview of the alarm and notification system implementation in the WeeklySignal application, focusing on architectural concepts, scheduling mechanisms, and cross-platform considerations.

## Table of Contents

1. [Alarm System Architecture](#alarm-system-architecture)
2. [Scheduling Implementation](#scheduling-implementation)
3. [Platform-Specific Implementations](#platform-specific-implementations)
4. [Alarm State Management](#alarm-state-management)
5. [Permission Management](#permission-management)
6. [Notification System](#notification-system)
7. [System Boot Recovery](#system-boot-recovery)
8. [Error Handling Strategy](#error-handling-strategy)
9. [Performance Optimization](#performance-optimization)

## Alarm System Architecture

### Technology Stack
- **Android**: System AlarmManager + NotificationManager + BroadcastReceiver ecosystem
- **Desktop**: SystemTray with limited notification support for testing
- **Common Interface**: SignalAlarmManager abstraction for cross-platform compatibility
- **State Persistence**: Room Database integration for alarm state tracking

### Core Features
- **Weekly Recurring Alarms**: Consistent weekly scheduling across time zones
- **Batch Operations**: SignalItem-level alarm management for efficiency
- **State Tracking**: Persistent alarm state with database synchronization
- **System Recovery**: Boot receiver for alarm restoration after system restart
- **Permission Management**: Android 12+ exact alarm permission handling

## Scheduling Implementation

### Alarm Scheduling Architecture

#### Time Calculation Strategy
- **Target Time Computation**: Calculate next occurrence of weekly recurring alarm
- **Time Zone Handling**: System calendar integration for local time accuracy
- **Week Boundary Logic**: Automatic progression to next week when current time has passed
- **Day of Week Mapping**: Conversion between domain model and system calendar constants

#### Scheduling Flow Process
1. **Permission Verification**: Check system-level alarm permissions before scheduling
2. **Existing Alarm Cleanup**: Cancel any previously scheduled alarm for the same slot
3. **Time Calculation**: Compute next valid alarm time based on current system time
4. **System Integration**: Create PendingIntent and register with system AlarmManager
5. **State Persistence**: Store alarm metadata in database for recovery purposes
6. **Validation**: Confirm successful registration with system services

#### Weekly Recurrence Implementation
- **Single Alarm Strategy**: Use one-time alarms with manual rescheduling for consistency
- **Receiver-Based Rescheduling**: AlarmReceiver handles next week's alarm creation
- **State Synchronization**: Database state updated with each alarm firing
- **Recovery Mechanism**: BootReceiver restores all scheduled alarms on system restart

### Android Alarm Scheduling Details

#### AlarmManager Integration Patterns
- **API Level Adaptation**: Different scheduling methods based on Android version
- **Exact Alarm Requirements**: Android 12+ requires SCHEDULE_EXACT_ALARM permission
- **Doze Mode Compatibility**: Use setExactAndAllowWhileIdle for background execution
- **Battery Optimization**: Efficient alarm scheduling to minimize system impact

#### PendingIntent Management
- **Unique Identifier Generation**: Hash-based alarm ID creation for collision avoidance
- **Intent Flag Configuration**: Immutable flags for Android 12+ compatibility
- **Action Definition**: Custom action strings for alarm identification
- **Serialized Data Transport**: JSON serialization for alarm metadata transfer

#### Alarm Timing Precision
- **Calendar-Based Calculation**: System Calendar API for accurate time computation
- **Current Time Comparison**: Logic to handle alarms in past vs future
- **Week Progression**: Automatic advancement when target time has elapsed
- **Millisecond Precision**: Exact timestamp calculation for system scheduling

### Desktop Scheduling Limitations
- **Test-Only Implementation**: SystemTray notifications without actual scheduling
- **Development Support**: UI testing capability without system alarm dependencies
- **Platform Compatibility**: Java-based notification system integration
- **Resource Management**: Manual cleanup of notification resources

## Platform-Specific Implementations

### Android Implementation Architecture

#### Core Components Integration
- **AlarmManager**: System service for alarm scheduling and management
- **NotificationManager**: Notification display and channel management
- **BroadcastReceiver**: Alarm event reception and processing
- **DatabaseService**: Alarm state persistence and recovery

#### Alarm Lifecycle Management
- **Registration Phase**: Permission check, time calculation, system registration
- **Triggering Phase**: Receiver activation, notification display, sound/vibration
- **Rescheduling Phase**: Next week alarm calculation and registration
- **Cancellation Phase**: System alarm removal and database cleanup

#### System Integration Points
- **Alarm Registration**: Integration with Android system alarm scheduler
- **Notification Display**: Android notification system with custom channels
- **Sound Management**: RingtoneManager integration for alarm audio
- **Vibration Control**: VibratorManager for haptic feedback
- **Permission Handling**: Runtime permission requests and validation

### Desktop Implementation Architecture

#### SystemTray Integration
- **Tray Icon Management**: System tray icon creation and lifecycle
- **Notification Display**: Platform-native notification through SystemTray API
- **Sound Integration**: System beep for audio feedback
- **Resource Cleanup**: Manual resource management for tray components

#### Capability Limitations
- **No Persistent Scheduling**: Desktop implementation focuses on UI testing
- **Immediate Notifications Only**: Test notifications without scheduling capability
- **Development Context**: Suitable for UI development and testing scenarios
- **Cross-Platform Consistency**: Maintains API compatibility for shared code

## Alarm State Management

### Database Integration Strategy
- **AlarmStateEntity**: Persistent representation of alarm scheduling state
- **Synchronization**: Database state reflects actual system alarm status
- **Recovery Data**: Sufficient information stored for complete alarm restoration
- **Cleanup Coordination**: Database updates coordinated with system alarm changes

### State Lifecycle Management
- **Creation**: New alarm state record when scheduling system alarm
- **Updates**: State modifications during rescheduling or configuration changes
- **Deletion**: State removal when alarms are cancelled or SignalItems deleted
- **Validation**: Consistency checks between database and system alarm state

### SignalItem Integration
- **Batch Operations**: Multiple alarm management for SignalItems with multiple TimeSlots
- **Cascading Updates**: Alarm state changes when SignalItem configuration modified
- **Dependency Tracking**: Alarm state linked to specific TimeSlot and SignalItem
- **Cleanup Automation**: Automatic alarm cancellation when parent entities deleted

## Permission Management

### Android Permission Architecture

#### Required Permissions
- **SCHEDULE_EXACT_ALARM**: Android 12+ requirement for precise alarm scheduling
- **POST_NOTIFICATIONS**: Android 13+ requirement for notification display
- **RECEIVE_BOOT_COMPLETED**: System boot event reception for alarm restoration
- **VIBRATE**: Haptic feedback during alarm notifications
- **WAKE_LOCK**: Device wake-up capability for alarm processing

#### Dynamic Permission Handling
- **Runtime Checks**: Permission validation before alarm operations
- **Permission Requests**: Automatic redirection to system settings for permission grants
- **Graceful Degradation**: Alternative behavior when permissions unavailable
- **User Guidance**: Clear messaging about permission requirements and benefits

#### Version-Specific Behavior
- **Android 7-11**: Basic alarm permissions sufficient for operation
- **Android 12+**: Exact alarm permission required for reliable scheduling
- **Android 13+**: Notification permission required for alarm display
- **Future Compatibility**: Proactive permission handling for upcoming Android versions

## Notification System

### Notification Channel Architecture
- **Channel Configuration**: Dedicated alarm notification channel with high importance
- **Audio Settings**: Custom alarm sound selection and channel-level audio configuration
- **Vibration Patterns**: Configurable vibration patterns for alarm notifications
- **Visual Styling**: Material Design notification layout with custom actions

### Notification Content Management
- **Dynamic Content**: SignalItem name and description integration
- **Action Buttons**: Dismiss action for immediate alarm termination
- **Intent Handling**: Deep linking to application main screen
- **Sound Control**: Individual alarm sound management with cleanup

### Multi-Modal Feedback
- **Audio Feedback**: RingtoneManager integration for system alarm sounds
- **Haptic Feedback**: VibratorManager for customizable vibration patterns
- **Visual Feedback**: High-priority notification with appropriate styling
- **User Control**: Per-SignalItem configuration of feedback modalities

## System Boot Recovery

### Boot Receiver Architecture
- **Boot Event Detection**: System boot and quick boot event handling
- **Asynchronous Processing**: Background alarm restoration without blocking boot process
- **Database Query**: Retrieval of all scheduled alarm states from persistent storage
- **Bulk Restoration**: Efficient restoration of multiple alarms in single operation

### Restoration Process Flow
1. **Boot Event Reception**: BroadcastReceiver activation on system startup
2. **Database Access**: Query for all scheduled alarm states
3. **SignalItem Retrieval**: Fetch complete SignalItem data for alarm reconstruction
4. **TimeSlot Matching**: Link alarm states to corresponding TimeSlot configurations
5. **Settings Reconstruction**: Rebuild AlarmSettings from database and SignalItem data
6. **System Rescheduling**: Re-register all alarms with system AlarmManager
7. **Error Handling**: Continue restoration process even if individual alarms fail

### Recovery Robustness
- **Partial Failure Tolerance**: Individual alarm restoration failures don't stop overall process
- **Data Validation**: Verification of alarm state data before restoration attempts
- **Resource Management**: Efficient database access and system integration during boot
- **Logging Strategy**: Error tracking for debugging restoration issues

## Error Handling Strategy

### Exception Classification
- **Permission Errors**: SecurityException handling for insufficient alarm permissions
- **System Errors**: AlarmManager operation failures and system service unavailability
- **Data Errors**: Database access failures and entity validation errors
- **Configuration Errors**: Invalid TimeSlot or AlarmSettings configuration

### Recovery Mechanisms
- **Graceful Degradation**: Continue operation when non-critical components fail
- **User Notification**: Appropriate error messaging without technical details
- **Retry Logic**: Automatic retry for transient system failures
- **Fallback Behavior**: Alternative functionality when primary systems unavailable

### Robustness Patterns
- **Defensive Programming**: Null checks and validation at system boundaries
- **Resource Cleanup**: Proper resource disposal even during error conditions
- **State Consistency**: Maintain database consistency during partial operation failures
- **Logging Strategy**: Comprehensive error logging for debugging and monitoring

## Performance Optimization

### Batch Operation Efficiency
- **SignalItem-Level Operations**: Group alarm operations by parent SignalItem
- **Sequential Processing**: Controlled operation sequencing for system stability
- **Error Isolation**: Individual operation failures don't impact batch completion
- **Resource Sharing**: Efficient database and system service utilization

### Memory Management
- **Sound Resource Tracking**: Active ringtone management with automatic cleanup
- **Notification Lifecycle**: Proper notification resource disposal
- **Database Connection Efficiency**: Optimized database access patterns
- **Background Processing**: Appropriate coroutine scope management

### System Integration Optimization
- **Permission Caching**: Minimize redundant permission checks
- **Database Batching**: Efficient alarm state persistence operations
- **System Service Efficiency**: Optimized AlarmManager and NotificationManager usage
- **Resource Recycling**: Reuse of system resources where appropriate

### Cross-Platform Performance
- **Shared Logic Optimization**: Maximum code reuse between platforms
- **Platform-Specific Optimization**: Tailored performance strategies per platform
- **Testing Efficiency**: Effective testing strategies for both platforms
- **Monitoring Integration**: Performance tracking and optimization feedback loops
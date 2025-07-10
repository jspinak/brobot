# SessionManager Documentation

## Overview

The SessionManager is a core component of the Brobot Runner that manages automation sessions with full lifecycle support, persistence capabilities, and state management. Following a successful refactoring initiative, it now implements a clean service-oriented architecture with clear separation of concerns.

## Architecture

### Component Structure

```
SessionManager (Orchestrator)
├── SessionLifecycleService    - Manages session lifecycle
├── SessionPersistenceService  - Handles file I/O operations
├── SessionStateService        - Captures/restores application state
├── SessionAutosaveService     - Manages automatic periodic saving
└── SessionDiscoveryService    - Provides session search and discovery
```

### Design Principles

1. **Single Responsibility Principle**: Each service has one clear responsibility
2. **Dependency Injection**: All services are Spring-managed components
3. **Thread Safety**: All services use concurrent data structures
4. **Diagnostic Capabilities**: Every service implements diagnostic interfaces
5. **Immutable Context Objects**: Session data passed via immutable objects

## Core Services

### SessionManager (Orchestrator)

The main entry point that coordinates all session operations by delegating to specialized services.

**Key Methods:**
- `startNewSession(String projectName, String configPath, String imagePath)` - Creates a new session
- `endCurrentSession()` - Ends the active session
- `isSessionActive()` - Checks if a session is active
- `getCurrentSession()` - Returns the current session or null
- `saveSession(Session session)` - Saves a session to disk
- `loadSession(String sessionId)` - Loads a session from disk
- `restoreSession(String sessionId)` - Restores and activates a session
- `deleteSession(String sessionId)` - Deletes a session
- `getAllSessionSummaries()` - Lists all available sessions

### SessionLifecycleService

Manages the lifecycle of sessions including creation, state transitions, and termination.

**Responsibilities:**
- Session creation and initialization
- State transition management (ACTIVE, PAUSED, ENDED)
- Tracking active sessions
- Session activation for restored sessions

**Key Features:**
- Thread-safe session tracking with `ConcurrentHashMap`
- Support for multiple concurrent sessions
- State transition validation
- Session state queries

### SessionPersistenceService

Handles all file I/O operations for sessions including saving, loading, and deleting.

**Responsibilities:**
- JSON serialization/deserialization
- File system operations
- Backup creation
- Storage path management

**Configuration:**
```properties
brobot.sessions.storage-path=sessions
```

**Key Features:**
- Automatic directory creation
- Graceful error handling
- Session existence checking
- Backup functionality

### SessionStateService

Captures and restores application state including state transitions and active states.

**Responsibilities:**
- Application state capture
- State restoration
- Snapshot management
- Integration with StateTransitionStore

**Key Features:**
- State snapshots with unique IDs
- Selective snapshot restoration
- Memory usage tracking
- State transition preservation

### SessionAutosaveService

Manages automatic periodic saving of sessions to prevent data loss.

**Responsibilities:**
- Scheduled autosave execution
- Per-session autosave configuration
- Manual autosave triggers
- Graceful shutdown handling

**Configuration Options:**
```java
SessionOptions.builder()
    .autosaveEnabled(true)
    .autosaveInterval(Duration.ofMinutes(5))
    .build()
```

### SessionDiscoveryService

Provides efficient session search and discovery capabilities.

**Responsibilities:**
- Session listing and sorting
- Search by date, project, keyword
- Session summary generation
- Cache management

**Key Features:**
- 5-minute cache for performance
- Multiple search criteria
- Automatic session name generation
- File size tracking

## Usage Examples

### Creating a New Session

```java
@Autowired
private SessionManager sessionManager;

// Start a new session
Session session = sessionManager.startNewSession(
    "My Project",
    "/config/project.json",
    "/images/project"
);

// Session is automatically saved and autosave is enabled
```

### Saving and Loading Sessions

```java
// Save current session state
sessionManager.saveSession(session);

// Load a session by ID
Optional<Session> loadedSession = sessionManager.loadSession(sessionId);
if (loadedSession.isPresent()) {
    // Work with loaded session
}
```

### Restoring a Previous Session

```java
// End current session if active
if (sessionManager.isSessionActive()) {
    sessionManager.endCurrentSession();
}

// Restore a previous session
boolean restored = sessionManager.restoreSession(sessionId);
if (restored) {
    Session current = sessionManager.getCurrentSession();
    // Session is now active with restored state
}
```

### Searching for Sessions

```java
// Get all sessions
List<SessionSummary> allSessions = sessionManager.getAllSessionSummaries();

// Search using discovery service
@Autowired
private SessionDiscoveryService discoveryService;

// Find sessions by date
List<SessionSummary> todaySessions = discoveryService.findSessionsByDate(LocalDate.now());

// Find sessions by project
List<SessionSummary> projectSessions = discoveryService.findSessionsByProject("My Project");

// Search by keyword
List<SessionSummary> results = discoveryService.searchSessions("automation");

// Get recent sessions
List<SessionSummary> recent = discoveryService.getRecentSessions(10);
```

### Managing Autosave

```java
// Create session with custom autosave settings
SessionOptions options = SessionOptions.builder()
    .autosaveEnabled(true)
    .autosaveInterval(Duration.ofMinutes(2))
    .maxSessionHistory(20)
    .build();

SessionContext context = SessionContext.builder()
    .projectName("Custom Project")
    .configPath("/config.json")
    .imagePath("/images")
    .options(options)
    .build();

// Manually trigger autosave
sessionManager.autosaveCurrentSession();

// Check last autosave time
LocalDateTime lastSave = sessionManager.getLastAutosaveTime();
```

## Data Model

### Session

The core data structure containing:
- `id` - Unique session identifier
- `projectName` - Associated project name
- `configPath` - Configuration file path
- `imagePath` - Image resources path
- `startTime` - Session start timestamp
- `endTime` - Session end timestamp (null if active)
- `active` - Current session state
- `events` - List of session events
- `stateTransitions` - Captured state transitions
- `activeStateIds` - Active state identifiers
- `stateData` - Additional state metadata

### SessionContext

Immutable context object for session creation:
```java
SessionContext.builder()
    .sessionId(UUID.randomUUID().toString())
    .sessionName("My Session")
    .projectName("My Project")
    .configPath("/config.json")
    .imagePath("/images")
    .options(SessionOptions.defaultOptions())
    .correlationId("correlation-123")
    .metadata(Map.of("key", "value"))
    .build()
```

### SessionOptions

Configuration options for sessions:
```java
SessionOptions.defaultOptions()       // Default configuration
SessionOptions.quickSession()         // No autosave, minimal history
SessionOptions.longRunningSession()   // Frequent autosave, extended retention
```

## Diagnostic Capabilities

All services implement diagnostic interfaces for monitoring and debugging:

```java
// Enable diagnostic mode for detailed logging
sessionManager.enableDiagnosticMode(true);

// Get diagnostic information
DiagnosticInfo info = sessionManager.getDiagnosticInfo();

// Diagnostic info includes:
// - Active session count
// - Storage statistics
// - Cache status
// - Autosave metrics
// - Error counts
```

## Error Handling

The SessionManager provides comprehensive error handling:

1. **Graceful Degradation**: Operations continue even if individual components fail
2. **Event Logging**: All errors are published to the EventBus
3. **Recovery Options**: Failed autosaves don't crash the application
4. **Validation**: Input validation prevents invalid operations

## Performance Considerations

1. **Caching**: Discovery service caches results for 5 minutes
2. **Concurrent Operations**: Thread-safe for multi-threaded environments
3. **Lazy Loading**: Sessions loaded only when needed
4. **Efficient Storage**: JSON format with optional compression
5. **Resource Cleanup**: Proper shutdown handling

## Migration from Legacy Code

If migrating from the old monolithic SessionManager:

1. **API Compatibility**: Most public methods remain the same
2. **Dependency Updates**: Update Spring configuration for new services
3. **State Migration**: Existing session files are compatible
4. **Testing**: Comprehensive test suite ensures compatibility

## Best Practices

1. **Always End Sessions**: Call `endCurrentSession()` when done
2. **Use Autosave**: Enable for long-running operations
3. **Handle Restoration Failures**: Check return values
4. **Clean Up Old Sessions**: Periodically delete old sessions
5. **Monitor Diagnostics**: Enable diagnostic mode for troubleshooting

## Configuration Reference

### Application Properties

```properties
# Session storage location
brobot.sessions.storage-path=sessions

# Logging configuration
logging.level.io.github.jspinak.brobot.runner.session=DEBUG
```

### Spring Configuration

All services are automatically configured via Spring component scanning. No manual bean configuration required.

## Troubleshooting

### Common Issues

1. **Session Not Found**
   - Check session ID is correct
   - Verify storage path exists
   - Check file permissions

2. **Autosave Not Working**
   - Verify autosave is enabled in SessionOptions
   - Check for save handler exceptions
   - Monitor diagnostic logs

3. **State Restoration Fails**
   - Ensure StateTransitionStore is available
   - Check for state data corruption
   - Verify all dependencies are initialized

### Debug Mode

Enable detailed logging:
```java
sessionManager.enableDiagnosticMode(true);
// Propagates to all services for comprehensive logging
```

## Future Enhancements

1. **Session Templates**: Predefined session configurations
2. **Remote Storage**: Support for cloud storage backends
3. **Session Sharing**: Multi-user session support
4. **Compression**: Automatic compression for large sessions
5. **Migration Tools**: Utilities for session format updates

---

*Last Updated: 2025-07-10*
*Version: 2.0 (Post-Refactoring)*
# Brobot Persistence Module

## Overview

The Brobot Persistence Module provides flexible and pluggable persistence capabilities for the Brobot automation library. It enables recording, storing, and analyzing action executions during automation runs.

## Features

- **Multiple Backends**: File-based, database, and in-memory persistence options
- **Zero Dependencies**: File-based persistence works without any database
- **Spring Integration**: Seamless integration with Spring Boot applications
- **Async Recording**: Non-blocking action recording for better performance
- **Session Management**: Organize recordings into sessions with metadata
- **Import/Export**: Transfer recordings between different persistence backends
- **Compression**: Optional GZIP compression for file storage

## Installation

### Gradle

```gradle
dependencies {
    implementation 'io.github.jspinak:brobot-persistence:1.2.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot-persistence</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Quick Start

### Simple File-Based Recording

```java
// Create a persistence provider
PersistenceProvider persistence = PersistenceProviderFactory.createDefault();

// Start recording session
String sessionId = persistence.startSession("My Automation", "TestApp", null);

// Your automation code here
// Actions can be recorded manually or automatically via events

// Stop recording
persistence.stopSession();

// Export the recorded session
ActionHistory history = persistence.exportSession(sessionId);
System.out.println("Recorded " + history.getSnapshots().size() + " actions");
```

### Spring Boot Integration

```yaml
# application.yml
brobot:
  persistence:
    enabled: true
    type: DATABASE  # or FILE, MEMORY
    database:
      url: jdbc:h2:file:./data/brobot
```

```java
@Component
public class MyAutomation {
    @Autowired
    private PersistenceProvider persistence;
    
    public void runAutomation() {
        persistence.startSession("Daily Task", "MyApp", null);
        // Your automation logic
        persistence.stopSession();
    }
}
```

## Persistence Backends

### File-Based Persistence

No database required. Records are stored as JSON or CSV files.

```java
PersistenceConfiguration config = PersistenceConfiguration.fileDefault();
config.getFile().setBasePath("./recordings");
config.getFile().setFormat(PersistenceConfiguration.FileFormat.JSON);
PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### Database Persistence

Uses JPA/Hibernate for robust storage and querying.

```java
// Requires Spring context with JPA repositories
@Autowired
private PersistenceProvider persistenceProvider;  // Auto-configured
```

### In-Memory Persistence

Perfect for testing and temporary sessions.

```java
PersistenceProvider provider = PersistenceProviderFactory.createInMemory();
```

## Configuration Options

```yaml
brobot:
  persistence:
    type: FILE              # FILE, DATABASE, or MEMORY
    
    file:
      base-path: ./history
      format: JSON          # JSON or CSV
      compress-exports: true
      
    database:
      url: jdbc:h2:file:./data/brobot
      batch-size: 100
      
    memory:
      max-sessions: 10
      max-records-per-session: 1000
      
    performance:
      async-recording: true
      buffer-size: 100
      thread-pool-size: 3
```

## API Usage

### Session Management

```java
// Start a new session
String sessionId = provider.startSession("Session Name", "App Name", "metadata");

// Control recording
provider.pauseRecording();
provider.resumeRecording();
boolean isActive = provider.isRecording();

// Stop session
provider.stopSession();
```

### Recording Actions

```java
// Record individual action
ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    .setDuration(250)
    .setText("Clicked button")
    .build();
provider.recordAction(record, stateObject);

// Batch recording
List<ActionRecord> batch = Arrays.asList(record1, record2, record3);
provider.recordBatch(batch);
```

### Query and Export

```java
// List all sessions
List<String> sessions = provider.getAllSessions();

// Get session metadata
SessionMetadata metadata = provider.getSessionMetadata(sessionId);

// Export session
ActionHistory history = provider.exportSession(sessionId);

// Import session
String newId = provider.importSession(history, "Imported Session");

// Delete session
provider.deleteSession(sessionId);
```

## Architecture

The module follows a three-layer architecture:

1. **Interface Layer** (`PersistenceProvider`): Defines the contract in the library module
2. **Implementation Layer**: Different backend implementations (file, database, memory)
3. **Integration Layer**: Spring Boot auto-configuration and event listeners

```
brobot-persistence/
├── config/           # Configuration models
├── provider/         # Provider implementations
├── database/         # JPA entities and repositories
├── spring/          # Spring Boot integration
└── model/           # Internal data models
```

## Testing

The module includes comprehensive test coverage:

```bash
cd brobot/persistence
./gradlew test
```

Test categories:
- Unit tests for each provider
- Integration tests with Spring
- Performance benchmarks
- Memory leak tests

## Performance

### Async Recording

Recording happens in background threads to minimize impact on automation performance:

```java
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setThreadPoolSize(5);
```

### Buffering

Actions are buffered before being persisted:

```java
config.getPerformance().setBufferSize(100);
config.getPerformance().setFlushIntervalSeconds(60);
```

### Memory Management

Automatic limits prevent memory issues:

```java
config.getMemory().setMaxSessions(10);
config.getMemory().setMaxRecordsPerSession(1000);
```

## Examples

### Recording Test Automation

```java
@Test
void testLoginFlow() {
    String sessionId = persistence.startSession("Login Test", "WebApp", null);
    
    try {
        // Test steps with automatic recording
        loginPage.enterUsername("user");
        loginPage.enterPassword("pass");
        loginPage.clickLogin();
        
        assertTrue(dashboard.isDisplayed());
    } finally {
        persistence.stopSession();
        
        // Analyze results
        ActionHistory history = persistence.exportSession(sessionId);
        double successRate = calculateSuccessRate(history);
        System.out.println("Test success rate: " + successRate + "%");
    }
}
```

### Debugging Failed Automations

```java
public void analyzeFailure(String sessionId) {
    ActionHistory history = persistence.exportSession(sessionId);
    
    // Find failed actions
    history.getSnapshots().stream()
        .filter(record -> !record.isActionSuccess())
        .forEach(failure -> {
            System.out.println("Failed: " + failure.getActionConfig());
            System.out.println("Duration: " + failure.getDuration() + "ms");
        });
}
```

### Migration Between Backends

```java
// Migrate from file to database
PersistenceProvider fileProvider = createFileProvider();
PersistenceProvider dbProvider = createDatabaseProvider();

for (String sessionId : fileProvider.getAllSessions()) {
    ActionHistory history = fileProvider.exportSession(sessionId);
    dbProvider.importSession(history, "Migrated_" + sessionId);
}
```

## Troubleshooting

### Out of Memory

Configure session and record limits:
```java
config.getMemory().setMaxSessions(5);
config.getMemory().setMaxRecordsPerSession(500);
```

### Slow Performance

Enable async recording:
```java
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setBufferSize(500);
```

### Large Files

Enable compression:
```java
config.getFile().setCompressExports(true);
config.getFile().setPrettyPrint(false);
```

## Contributing

Contributions are welcome! Please see the main Brobot repository for contribution guidelines.

## License

This module is part of the Brobot project and follows the same license.

## Support

- Documentation: [Persistence Module Guide](../docs/docs/guides/persistence-user-guide.md)
- Architecture: [Persistence Architecture](../docs/docs/architecture/persistence-module.md)
- Issues: [GitHub Issues](https://github.com/jspinak/brobot/issues)
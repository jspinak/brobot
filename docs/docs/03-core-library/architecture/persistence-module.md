# Persistence Module Architecture

## Overview

The Brobot Persistence Module provides a flexible, pluggable architecture for recording and persisting action execution data during automation runs. This module enables:

- Recording of action executions for debugging and analysis
- Multiple persistence backends (file, database, in-memory)
- Session management and replay capabilities
- Export/import of action histories
- Integration with both Java projects and the JavaFX runner

> **For usage instructions, see the [Persistence User Guide](../user-guides/persistence-user-guide.md).**

## Architecture Design

### Three-Module Architecture

```
brobot-parent/
├── brobot-library/        # Core automation (no persistence)
├── brobot-persistence/    # Optional persistence module
└── brobot-runner/        # JavaFX UI application
```

### Key Design Principles

1. **Separation of Concerns**: The library remains pure automation functionality
2. **Optional Integration**: Projects can choose whether to include persistence
3. **Pluggable Backends**: Different storage mechanisms without code changes
4. **Zero Dependencies**: File-based persistence requires no database
5. **Spring Integration**: Seamless integration with Spring Boot applications

## Module Structure

### Core Components

```
brobot-persistence/
├── config/
│   └── PersistenceConfiguration.java    # Configuration model
├── provider/
│   ├── AbstractPersistenceProvider.java # Base implementation
│   ├── FileBasedPersistenceProvider.java
│   ├── InMemoryPersistenceProvider.java
│   └── DatabasePersistenceProvider.java
├── database/
│   ├── entity/                          # JPA entities
│   └── repository/                      # Spring Data repositories
├── spring/
│   ├── PersistenceAutoConfiguration.java
│   └── PersistenceEventListener.java
└── PersistenceProviderFactory.java      # Factory for creating providers
```

### Interface Design

The [`PersistenceProvider`](../../api/persistence/PersistenceProvider.md) interface in the library module defines the contract:

```java
public interface PersistenceProvider {
    // Session management
    String startSession(String sessionName, String application, String metadata);
    String stopSession();
    void pauseRecording();
    void resumeRecording();
    boolean isRecording();
    
    // Recording
    void recordAction(ActionRecord record, StateObject stateObject); // StateObject: see States guide
    void recordBatch(List<ActionRecord> records);

    // Export/Import
    ActionHistory exportSession(String sessionId); // Returns ActionHistory for replay/analysis
    String importSession(ActionHistory history, String sessionName);
    
    // Query
    List<String> getAllSessions();
    SessionMetadata getSessionMetadata(String sessionId); // SessionMetadata is a nested class
    void deleteSession(String sessionId);

    /**
     * Metadata about a recording session.
     * Defined as PersistenceProvider.SessionMetadata in the actual implementation.
     */
    class SessionMetadata {
        // Session details
    }
}
```

## Persistence Backends

### 1. File-Based Persistence

**Use Case**: Projects that need persistence without database dependencies

**Features**:
- JSON and CSV format support (XML planned but not yet implemented)
- Optional compression (GZIP)
- Organized directory structure
- Automatic file rotation
- No external dependencies

**Storage Structure**:
```
brobot-history/
└── sessions/
    └── 20250807_143022_MySession/
        ├── metadata.json
        ├── records_20250807_143022.json
        └── records_20250807_144512.json.gz
```

### 2. Database Persistence

**Use Case**: Enterprise applications requiring robust querying and analysis

**Features**:
- JPA/Hibernate integration
- Support for any JPA-compatible database
- Rich querying capabilities
- Transaction support
- Relationship management

**Entities**:
- `RecordingSessionEntity`: Session metadata
- `ActionRecordEntity`: Individual action records
- `MatchEntity`: Match results from find operations

### 3. In-Memory Persistence

**Use Case**: Testing and temporary sessions

**Features**:
- Zero configuration
- Fast performance
- Session limits to prevent memory issues
- Optional persistence on shutdown
- Memory statistics

## Configuration

### Configuration Options

```yaml
brobot:
  persistence:
    enabled: true
    type: FILE  # FILE, DATABASE, MEMORY
    
    file:
      base-path: ./brobot-history
      format: JSON  # JSON, CSV
      compress-exports: false
      pretty-print: true
      max-file-size-mb: 100
      
    database:
      url: jdbc:h2:file:./data/brobot
      username: sa
      password: 
      batch-size: 100
      
    memory:
      max-sessions: 10
      max-records-per-session: 1000
      persist-on-shutdown: true
      
    performance:
      async-recording: true
      buffer-size: 100
      flush-interval-seconds: 60
      thread-pool-size: 3
```

### Spring Boot Auto-Configuration

The module provides auto-configuration for Spring Boot applications:

```java
@SpringBootApplication
@Import(PersistenceAutoConfiguration.class)
public class MyApplication {
    // Persistence is automatically configured
}
```

## Usage Patterns

### 1. Java Project with File Persistence

```java
package com.example.automation;

import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.Action;

public class FilePersistenceExample {
    public static void main(String[] args) {
        // Create persistence provider
        PersistenceConfiguration config = PersistenceConfiguration.fileDefault();
        config.getFile().setBasePath("./automation-history");
        PersistenceProvider persistence = PersistenceProviderFactory.create(config);

        // Start recording
        String sessionId = persistence.startSession("TestRun", "MyApp", null);

        try {
            // Define StateImage for automation
            StateImage stateImage = new StateImage.Builder()
                .setName("submit-button")
                .addPattern("submit.png")
                .build();

            // Your automation code (Action would be initialized via dependency injection in real apps)
            // action.click(stateImage);  // Actions are recorded automatically if integrated

            // Stop and export
            persistence.stopSession();
            ActionHistory history = persistence.exportSession(sessionId);

            System.out.println("Session completed with " +
                             history.getTimesSearched() + " actions recorded");
        } catch (Exception e) {
            persistence.stopSession();
            throw e;
        }
    }
}
```

### 2. Spring Boot Application

```java
package com.example.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;

@Component
public class AutomationService {
    @Autowired
    private PersistenceProvider persistence;

    @Autowired
    private Action action;

    // Define StateImages for UI elements
    private final StateImage loginButton = new StateImage.Builder()
        .setName("loginButton")
        .addPattern("login-button.png")
        .build();

    private final StateImage usernameField = new StateImage.Builder()
        .setName("usernameField")
        .addPattern("username-field.png")
        .build();

    private final StateImage submitButton = new StateImage.Builder()
        .setName("submitButton")
        .addPattern("submit-button.png")
        .build();

    public void runAutomation() {
        persistence.startSession("Automated Test", "WebApp", null);

        try {
            // Automation with automatic recording
            ActionResult loginResult = action.click(loginButton);
            if (loginResult.isSuccess()) {
                action.click(usernameField);
                action.type("user");
                action.click(submitButton);
            }
        } finally {
            persistence.stopSession();
        }
    }
}
```

### 3. Runner Application

The runner uses the persistence module internally:

```java
package io.github.jspinak.brobot.runner.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.jspinak.brobot.persistence.PersistenceProvider;

@Service
public class PersistenceAdapterService {
    @Autowired
    private PersistenceProvider persistenceProvider;

    // UI update callback for JavaFX runner (implementation-specific)
    private Runnable uiUpdateCallback;

    public void setUiUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    public void startRecording(String name, String app) {
        persistenceProvider.startSession(name, app, null);
        updateUI();  // Runner-specific UI updates
    }

    public void stopRecording() {
        persistenceProvider.stopSession();
        updateUI();
    }

    public boolean isRecording() {
        return persistenceProvider.isRecording();
    }

    /**
     * Updates runner UI to reflect current recording state.
     * Calls JavaFX UI update callback if configured.
     */
    private void updateUI() {
        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }
}
```

## Performance Considerations

### Asynchronous Recording

- Actions are queued and persisted in background threads
- Configurable buffer sizes and flush intervals
- Automatic fallback to synchronous on queue overflow

### Memory Management

- Configurable limits for in-memory storage
- Automatic session eviction (LRU)
- File rotation for large sessions

### Batch Operations

- Batch inserts for database persistence
- Buffered writes for file persistence
- Configurable batch sizes

## Migration Guide

### For Java Projects

1. **Add Dependency**:
```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot-persistence</artifactId>
    <version>1.1.0</version>
</dependency>
```

2. **Configure Persistence**:
```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

PersistenceConfiguration config = new PersistenceConfiguration();
config.setType(PersistenceConfiguration.PersistenceType.FILE);
PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

3. **Integrate with Automation**:
```java
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import org.springframework.context.event.EventListener;

// Option 1: Manual recording
provider.startSession("Test", "App", null);

// Define StateImage and execute action
StateImage testImage = new StateImage.Builder()
    .setName("test-image")
    .addPattern("test.png")
    .build();

// Execute action and create record
PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
ActionResult result = action.perform(findOptions, testImage.asObjectCollection());

ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(findOptions)
    .setMatchList(result.getMatchList())
    .setActionSuccess(result.isSuccess())
    .build();

// StateImage implements StateObject
StateObject stateObject = testImage;
provider.recordAction(record, stateObject);

// Option 2: Event-based (if using Spring)
@EventListener
public void onActionExecuted(ActionExecutedEvent event) {
    provider.recordAction(event.getRecord(), event.getStateObject());
}
```

### For Runner Application

The runner has been updated to use the persistence module automatically. Configuration is done through `application.yml`:

```yaml
brobot:
  persistence:
    type: DATABASE
    enabled: true
```

## Testing

The module includes comprehensive test coverage. For detailed testing guidance, see:
- [Testing Introduction](../../04-testing/testing-intro.md) - Testing patterns and strategies
- [Integration Testing](../../04-testing/integration-testing.md) - Spring Boot integration testing
- [ActionHistory Integration Testing](../../04-testing/actionhistory-integration-testing.md) - Using ActionHistory in tests

Test coverage includes:
- Unit tests for each provider implementation
- Integration tests with Spring context
- Performance benchmarks
- Memory leak detection

Run tests:
```bash
cd brobot/persistence
./gradlew test
```

## Troubleshooting

### Common Issues

1. **Out of Memory with In-Memory Provider**
   - Solution: Configure session and record limits
   - Enable persist-on-shutdown

2. **File Permission Errors**
   - Solution: Ensure write permissions for base-path
   - Check disk space availability

3. **Database Connection Issues**
   - Solution: Verify JDBC URL and credentials
   - Check database is running

4. **Performance Degradation**
   - Solution: Enable async recording
   - Increase buffer sizes
   - Use batch operations

## Future Enhancements

- Cloud storage backends (S3, Azure Blob)
- Real-time streaming to external systems
- Advanced query API
- Compression algorithms
- Encryption support
- GraphQL API for querying

## Related Documentation

### Core Concepts
- [States and State Management](../../01-getting-started/states.md) - Understanding StateObjects used in recording
- [Installation Guide](../../01-getting-started/installation.md) - Adding Brobot to your project

### Configuration and Setup
- [Persistence User Guide](../user-guides/persistence-user-guide.md) - Comprehensive usage guide
- [Auto-Configuration](../configuration/auto-configuration.md) - Spring Boot integration details
- [Properties Reference](../configuration/properties-reference.md) - Complete configuration options

### Migration
- [Upgrading to Latest](../migration/upgrading-to-latest.md) - Complete migration guide including ActionHistory updates

### Testing and Integration
- [Testing Introduction](../../04-testing/testing-intro.md) - Testing patterns and strategies
- [Integration Testing](../../04-testing/integration-testing.md) - Spring Boot integration testing
- [ActionHistory Integration Testing](../../04-testing/actionhistory-integration-testing.md) - ActionRecord and ActionHistory API
- [Mock Snapshots](../../04-testing/actionhistory-mock-snapshots.md) - Testing with mock data
- [Action Recording](../../04-testing/action-recording.md) - Visual screenshot-based recording (different feature)

### Architecture
- [ActionResult Architecture](./actionresult-architecture.md) - Related action execution architecture
# Persistence Module User Guide

## Overview

The Brobot Persistence Module allows you to record and analyze automation executions. This guide covers installation, configuration, and usage for different scenarios.

## Prerequisites

Before using the Persistence Module, you should be familiar with:
- [Installation](../../01-getting-started/installation.md) - Adding Brobot to your project
- [States](../../01-getting-started/states.md) - Understanding StateObjects and state management
- [ActionConfig](../action-config/01-overview.md) - Action configuration system
- [Testing](../../04-testing/testing-intro.md) - Testing patterns for integration

For architectural details, see the [Persistence Module Architecture](../architecture/persistence-module.md).

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot-persistence</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.jspinak:brobot-persistence:1.1.0'
```

## Quick Start

### Minimal Setup (File-Based)

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.model.action.ActionHistory;

// Create provider with default settings
PersistenceProvider persistence = PersistenceProviderFactory.createDefault();

// Start recording
String sessionId = persistence.startSession("My First Session", "TestApp", null);

// Your automation code here
// ...

// Stop and export
persistence.stopSession();
ActionHistory history = persistence.exportSession(sessionId);
System.out.println("Recorded " + history.getSnapshots().size() + " actions");
```

## Quick Reference

### Common Patterns Cheat Sheet

```java
// Create providers
PersistenceProvider fileProvider = PersistenceProviderFactory.createDefault();
PersistenceProvider memoryProvider = PersistenceProviderFactory.createInMemory();
PersistenceProvider customProvider = PersistenceProviderFactory.create(config);

// Session lifecycle
String sessionId = provider.startSession("name", "app", "metadata");
provider.recordAction(actionRecord, stateObject);
provider.stopSession();

// Query and export
List<String> allSessions = provider.getAllSessions();
ActionHistory history = provider.exportSession(sessionId);
SessionMetadata metadata = provider.getSessionMetadata(sessionId);

// Pause/Resume
provider.pauseRecording();
// ... operations not recorded ...
provider.resumeRecording();
```

For detailed examples, see the sections below.

## Common Use Cases

### Recording a Single Automation Run

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

public class SimpleRecording {
    public static void main(String[] args) {
        // Create provider
        PersistenceProvider persistence = PersistenceProviderFactory.createDefault();

        try {
            // Start session
            String sessionId = persistence.startSession(
                "Login Test",
                "MyApp",
                "Version: 1.0, User: qa_user"
            );

            // Your automation code here
            // Actions are recorded automatically if using AspectJ
            // or manually via persistence.recordAction()

            System.out.println("Session recorded: " + sessionId);
        } finally {
            // Always stop session
            persistence.stopSession();
        }
    }
}
```

### Recording with Try-With-Resources Pattern

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

public class SafeRecording {
    public void automateWithRecording() {
        PersistenceProvider persistence = PersistenceProviderFactory.createDefault();
        String sessionId = null;

        try {
            sessionId = persistence.startSession("Critical Operation", "App", null);

            // Your automation logic
            performCriticalSteps();

        } catch (Exception e) {
            System.err.println("Automation failed: " + e.getMessage());
            // Session is still recorded for debugging
        } finally {
            if (persistence.isRecording()) {
                persistence.stopSession();
                System.out.println("Session saved: " + sessionId);
            }
        }
    }
}
```

### Analyzing Recorded Sessions

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;

public class SessionAnalyzer {
    public void analyzeSessions(PersistenceProvider persistence) {
        for (String sessionId : persistence.getAllSessions()) {
            ActionHistory history = persistence.exportSession(sessionId);

            long totalActions = history.getSnapshots().size();
            long successfulActions = history.getSnapshots().stream()
                .filter(ActionRecord::isActionSuccess)
                .count();

            double successRate = (successfulActions * 100.0) / totalActions;

            System.out.printf("Session %s: %.1f%% success rate (%d/%d actions)%n",
                sessionId, successRate, successfulActions, totalActions);
        }
    }
}
```

## Configuration Examples

### File-Based Persistence

Perfect for standalone applications and testing.

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

PersistenceConfiguration config = new PersistenceConfiguration();
config.setType(PersistenceConfiguration.PersistenceType.FILE);
config.getFile().setBasePath("./my-recordings");
config.getFile().setFormat(PersistenceConfiguration.FileFormat.JSON);
config.getFile().setPrettyPrint(true);

PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### In-Memory Persistence

Ideal for unit tests and temporary sessions.

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

PersistenceConfiguration config = PersistenceConfiguration.memoryDefault();
config.getMemory().setMaxSessions(5);
config.getMemory().setMaxRecordsPerSession(100);

PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### Database Persistence (Spring Boot)

For enterprise applications with Spring Boot. See [Auto-Configuration Guide](../configuration/auto-configuration.md) for more on Spring Boot integration.

**Configuration via YAML**:

```yaml
# application.yml
brobot:
  persistence:
    enabled: true
    type: DATABASE
    database:
      url: jdbc:postgresql://localhost:5432/brobot
      username: brobot_user
      password: ${DB_PASSWORD}
```

**Using in Spring Components**:

```java
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.persistence.PersistenceProvider;

@Component
public class AutomationService {
    @Autowired
    private PersistenceProvider persistence;
    
    public void runAutomation() {
        String sessionId = persistence.startSession(
            "Daily Report Generation",
            "ReportingSystem",
            "Automated daily report at 9 AM"
        );
        
        try {
            // Your automation logic
            generateReport();
        } finally {
            persistence.stopSession();
        }
    }
}
```

## Recording Actions

### Manual Recording

Actions can be recorded using [ActionConfig](../action-config/01-overview.md) classes like `PatternFindOptions`. For more on action configuration, see the [ActionConfig Overview](../action-config/01-overview.md).

```java
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;

// Create an ActionRecord for each action
ActionRecord record = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.85)
        .build())
    .setActionSuccess(true)
    .setDuration(250)
    .setText("Clicked submit button")
    .addMatch(new Match.Builder()
        .setRegion(100, 200, 50, 30)
        .setSimScore(0.92)
        .build())
    .build();

// Record with optional state context
// StateObject provides state information for the action
// See: https://docs.brobot.io/docs/getting-started/states
persistence.recordAction(record, currentStateObject);
```

### Batch Recording

For high-volume recording, batch operations improve performance.

```java
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import java.util.ArrayList;
import java.util.List;

List<ActionRecord> batch = new ArrayList<>();

for (DataRow row : dataRows) {
    ActionRecord record = processRow(row);
    batch.add(record);
    
    // Flush batch periodically
    if (batch.size() >= 100) {
        persistence.recordBatch(batch);
        batch.clear();
    }
}

// Record remaining
if (!batch.isEmpty()) {
    persistence.recordBatch(batch);
}
```

### Event-Based Recording (Spring)

> **⚠️ Note**: Event-based recording requires the Brobot library to publish `ActionExecutedEvent` events. This integration is planned but not yet available in the core library. For now, use [Manual Recording](#manual-recording) or [Batch Recording](#batch-recording) patterns.

```java
@Component
public class ActionRecorder {
    @Autowired
    private PersistenceProvider persistence;

    // Note: ActionExecutedEvent is defined in persistence module
    // but library doesn't publish these events yet
    @EventListener
    public void onActionExecuted(ActionExecutedEvent event) {
        if (persistence.isRecording()) {
            persistence.recordAction(
                event.getRecord(),
                event.getStateObject()
            );
        }
    }
}
```

## Session Management

### Starting Sessions

```java
// Basic session
String sessionId = persistence.startSession("Test Run", "MyApp", null);

// With metadata
String metadata = "Environment: staging, Version: 1.2.3, User: john";
String sessionId = persistence.startSession("Integration Test", "MyApp", metadata);
```

### Controlling Recording

```java
// Pause recording temporarily
persistence.pauseRecording();
performMaintenanceTask();  // Won't be recorded
persistence.resumeRecording();

// Check recording status
if (persistence.isRecording()) {
    System.out.println("Currently recording session: " + 
                      persistence.getCurrentSessionId());
}
```

### Querying Sessions

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProvider.SessionMetadata;
import java.time.Duration;
import java.util.List;

// List all sessions
List<String> sessions = persistence.getAllSessions();

// Get session details
SessionMetadata metadata = persistence.getSessionMetadata(sessionId);
System.out.println("Session: " + metadata.getName());
System.out.println("Duration: " + 
    Duration.between(metadata.getStartTime(), metadata.getEndTime()));
System.out.println("Success Rate: " + metadata.getSuccessRate() + "%");
```

## Import/Export

### Exporting Sessions

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.model.action.ActionHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;

// Export to ActionHistory
ActionHistory history = persistence.exportSession(sessionId);

// Save to file
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.writeValue(new File("session-export.json"), history);
```

### Importing Sessions

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.model.action.ActionHistory;

// From ActionHistory object
ActionHistory history = loadFromFile("session-export.json");
String newSessionId = persistence.importSession(history, "Imported Session");

// From another provider
PersistenceProvider source = createFileProvider();
PersistenceProvider target = createDatabaseProvider();

for (String sessionId : source.getAllSessions()) {
    ActionHistory history = source.exportSession(sessionId);
    target.importSession(history, "Migrated_" + sessionId);
}
```

## Advanced Usage

### Custom File Paths

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import java.time.LocalDate;

// Organize by date
LocalDate today = LocalDate.now();
String basePath = String.format("./recordings/%d/%02d/%02d",
    today.getYear(), today.getMonthValue(), today.getDayOfMonth());

PersistenceConfiguration config = new PersistenceConfiguration();
config.getFile().setBasePath(basePath);
PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### Compression and Optimization

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

PersistenceConfiguration config = new PersistenceConfiguration();
config.getFile().setCompressExports(true);  // GZIP compression
config.getFile().setPrettyPrint(false);      // Smaller files
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setBufferSize(500);  // Larger buffer

PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### Memory Management

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.persistence.memory.InMemoryPersistenceProvider;

// For in-memory provider
PersistenceConfiguration config = new PersistenceConfiguration();
config.setType(PersistenceConfiguration.PersistenceType.MEMORY);
config.getMemory().setMaxSessions(10);
config.getMemory().setMaxRecordsPerSession(1000);
config.getMemory().setPersistOnShutdown(true);
config.getMemory().setShutdownExportPath("./emergency-export");

PersistenceProvider provider = PersistenceProviderFactory.create(config);

// Monitor memory usage
if (provider instanceof InMemoryPersistenceProvider memory) {
    var stats = memory.getMemoryStatistics();
    System.out.println("Memory usage: " + stats);
}
```

### Performance Tuning

```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;

PersistenceConfiguration config = new PersistenceConfiguration();

// Async recording for better performance
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setThreadPoolSize(5);
config.getPerformance().setQueueCapacity(2000);

// Batching for database
config.getDatabase().setBatchSize(200);

// Buffering for files
config.getPerformance().setBufferSize(100);
config.getPerformance().setFlushIntervalSeconds(30);

PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

## Integration Examples

### JUnit Test Integration

For comprehensive testing patterns, see the [Testing Introduction](../../04-testing/testing-intro.md) and [Integration Testing Guide](../../04-testing/integration-testing.md).

```java
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.model.action.ActionHistory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutomationTest {
    private PersistenceProvider persistence;
    private String sessionId;
    
    @BeforeAll
    void setupPersistence() {
        persistence = PersistenceProviderFactory.createInMemory();
    }
    
    @BeforeEach
    void startRecording(TestInfo testInfo) {
        sessionId = persistence.startSession(
            testInfo.getDisplayName(),
            "TestSuite",
            testInfo.getTags().toString()
        );
    }
    
    @AfterEach
    void stopRecording(TestInfo testInfo) {
        persistence.stopSession();
        
        // Export failed tests for analysis
        if (testInfo.getTags().contains("failed")) {
            ActionHistory history = persistence.exportSession(sessionId);
            saveFailedTest(testInfo.getDisplayName(), history);
        }
    }
    
    @Test
    void testLoginFlow() {
        // Test with automatic recording
        login("user", "pass");
        verifyDashboard();
    }
}
```

### CI/CD Integration

For detailed CI/CD setup and best practices, see the [CI/CD Testing Guide](../../04-testing/advanced/ci-cd-testing.md).

```java
public class CIPersistenceConfig {
    public static PersistenceProvider createForCI() {
        String buildNumber = System.getenv("BUILD_NUMBER");
        String branch = System.getenv("GIT_BRANCH");
        
        PersistenceConfiguration config = new PersistenceConfiguration();
        config.setType(PersistenceConfiguration.PersistenceType.FILE);
        config.getFile().setBasePath("./test-results/automation/" + buildNumber);
        
        PersistenceProvider provider = PersistenceProviderFactory.create(config);
        
        // Start session with CI metadata
        provider.startSession(
            "CI Build #" + buildNumber,
            "AutomationSuite",
            "Branch: " + branch + ", Commit: " + System.getenv("GIT_COMMIT")
        );
        
        return provider;
    }
}
```

### Debugging Failed Automations

Persistence enables post-execution analysis of failures.

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import java.util.List;

public class AutomationDebugger {
    private final PersistenceProvider persistence;
    
    public void analyzeFailure(String sessionId) {
        ActionHistory history = persistence.exportSession(sessionId);
        
        // Find failed actions
        List<ActionRecord> failures = history.getSnapshots().stream()
            .filter(record -> !record.isActionSuccess())
            .toList();
        
        System.out.println("Found " + failures.size() + " failed actions:");
        
        for (ActionRecord failure : failures) {
            System.out.println("Failed: " + failure.getActionConfig());
            System.out.println("Duration: " + failure.getDuration() + "ms");
            
            // Analyze patterns
            if (failure.getDuration() > 5000) {
                System.out.println("⚠️ Timeout detected");
            }
            if (failure.getMatchList().isEmpty()) {
                System.out.println("⚠️ No matches found");
            }
        }
        
        // Calculate statistics
        double successRate = history.getSnapshots().stream()
            .filter(ActionRecord::isActionSuccess)
            .count() * 100.0 / history.getSnapshots().size();
        
        System.out.println("Overall success rate: " + successRate + "%");
    }
}
```

## Best Practices

### 1. Session Naming

Use descriptive session names with context:
```java
String sessionName = String.format("%s_%s_%s",
    testSuiteName,
    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    environment
);
```

### 2. Error Handling

Always stop sessions in finally blocks to prevent data loss:

> **💡 Best Practice**: Use `finally` blocks or try-with-resources patterns to guarantee session closure even during exceptions. This prevents incomplete session data and memory leaks.

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.model.action.ActionRecord;

String sessionId = persistence.startSession("Critical Process", "App", null);
try {
    runCriticalProcess();
} catch (Exception e) {
    // Record error for post-execution analysis
    ActionRecord errorRecord = new ActionRecord.Builder()
        .setActionSuccess(false)
        .setText("Error: " + e.getMessage())
        .setDuration(0)  // No duration for error records
        .build();
    persistence.recordAction(errorRecord, null);
    throw e;  // Re-throw to maintain exception propagation
} finally {
    // CRITICAL: Always stop session, even on failure
    persistence.stopSession();
}
```

> **⚠️ Common Pitfall**: Forgetting to stop sessions leads to:
> - Memory leaks (sessions remain in memory)
> - Incomplete data (final session metadata not written)
> - Resource exhaustion (database connections not released)

### 3. Resource Management

Clean up old sessions periodically to prevent storage exhaustion:

> **💡 Tip**: For production systems, implement automated cleanup to manage disk space and database growth. Consider archiving important sessions before deletion.

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProvider.SessionMetadata;
import io.github.jspinak.brobot.model.action.ActionHistory;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;

@Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
public void cleanupOldSessions() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    int deletedCount = 0;
    int archivedCount = 0;

    for (String sessionId : persistence.getAllSessions()) {
        SessionMetadata metadata = persistence.getSessionMetadata(sessionId);
        if (metadata.getEndTime() != null &&
            metadata.getEndTime().isBefore(cutoff)) {

            // Archive critical sessions before deletion
            if (metadata.getSuccessRate() < 80.0) {
                ActionHistory history = persistence.exportSession(sessionId);
                archiveSession(history);  // Save to archive storage
                archivedCount++;
            }

            persistence.deleteSession(sessionId);
            deletedCount++;
        }
    }

    log.info("Cleanup complete: deleted {}, archived {}", deletedCount, archivedCount);
}
```

> **📊 Performance Tip**: For large session counts (>1000), consider batch deletion or pagination to avoid memory issues.

### 4. Performance Monitoring

Monitor recording overhead to ensure minimal impact on automation performance:

> **📊 Performance Guidelines**: Recording should add &lt;5ms overhead per action. Higher values indicate configuration issues or resource constraints.

```java
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.model.action.ActionRecord;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMonitor {
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);
    private final PersistenceProvider persistence;
    private final AtomicLong totalRecordTime = new AtomicLong();
    private final AtomicInteger recordCount = new AtomicInteger();

    public void recordWithMonitoring(ActionRecord record) {
        long start = System.nanoTime();
        persistence.recordAction(record, null);
        long duration = System.nanoTime() - start;

        totalRecordTime.addAndGet(duration);
        recordCount.incrementAndGet();

        // Report every 100 actions
        if (recordCount.get() % 100 == 0) {
            double avgMs = totalRecordTime.get() / recordCount.get() / 1_000_000.0;
            log.info("Avg recording time: {} ms", avgMs);

            // Alert if overhead is too high
            if (avgMs > 5.0) {
                log.warn("Recording overhead exceeds 5ms - consider enabling async mode");
            }
        }
    }
}
```

> **⚙️ Optimization**: If monitoring shows >5ms average, enable async recording:
> ```java
> config.getPerformance().setAsyncRecording(true);
> ```

## Troubleshooting

### Issue: Out of Memory

**Symptoms**: `OutOfMemoryError`, application crashes, slow performance after many sessions

**Root Cause**: In-memory provider retaining too many sessions or large action histories

**Solution**: Configure limits and enable persistence on shutdown
```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

PersistenceConfiguration config = PersistenceConfiguration.memoryDefault();
config.getMemory().setMaxSessions(5);  // Limit concurrent sessions
config.getMemory().setMaxRecordsPerSession(500);  // Cap records per session
config.getMemory().setPersistOnShutdown(true);  // Save before exit
config.getMemory().setShutdownExportPath("./backup");  // Emergency backup
```

**Prevention**: Monitor memory usage with JVM metrics and switch to file/database persistence for long-running applications.

### Issue: Slow Recording Performance

**Symptoms**: Actions take noticeably longer, automation slows down, CPU usage increases

**Root Cause**: Synchronous recording blocking automation threads, inefficient serialization

**Solution**: Enable async recording and increase buffer
```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

PersistenceConfiguration config = new PersistenceConfiguration();
config.getPerformance().setAsyncRecording(true);  // Non-blocking recording
config.getPerformance().setBufferSize(500);  // Buffer more actions
config.getPerformance().setThreadPoolSize(5);  // Parallel processing
config.getPerformance().setQueueCapacity(2000);  // Larger queue
```

**Prevention**: Monitor recording overhead (see [Performance Monitoring](#4-performance-monitoring)) and tune based on workload.

### Issue: Large File Sizes

**Symptoms**: Disk space fills quickly, slow export/import operations

**Root Cause**: JSON pretty printing, uncompressed exports, verbose data

**Solution**: Enable compression and optimize format
```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

PersistenceConfiguration config = new PersistenceConfiguration();
config.getFile().setCompressExports(true);  // GZIP compression (~70% reduction)
config.getFile().setPrettyPrint(false);  // Remove whitespace
config.getFile().setFormat(PersistenceConfiguration.FileFormat.CSV);  // Smaller than JSON
```

**Prevention**: Regularly clean up old sessions, archive to compressed storage, use database for high volumes.

### Issue: Database Connection Pool Exhaustion

**Symptoms**: `SQLException: Cannot get a connection`, slow queries, timeouts

**Root Cause**: Too many concurrent sessions, connection leaks, insufficient pool size

**Solution**: Configure connection pool and batch size
```java
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;

PersistenceConfiguration config = new PersistenceConfiguration();
config.getDatabase().setConnectionPoolSize(10);  // Match concurrent sessions
config.getDatabase().setBatchSize(100);  // Reduce DB round trips
config.getDatabase().setConnectionTimeout(5000);  // Fail fast
```

**Prevention**: Always stop sessions in `finally` blocks, use connection monitoring, tune pool size based on load.

### Issue: Session Not Found After Restart

**Symptoms**: `getAllSessions()` returns empty list, previously recorded data missing

**Root Cause**: Using in-memory provider without shutdown persistence, incorrect file paths

**Solution**: Enable persistence on shutdown or use file/database providers
```java
// For in-memory with persistence
config.getMemory().setPersistOnShutdown(true);
config.getMemory().setLoadOnStartup(true);

// OR switch to file-based (recommended for production)
config.setType(PersistenceConfiguration.PersistenceType.FILE);
config.getFile().setBasePath("./recordings");
```

**Prevention**: Use file or database persistence for production, test recovery procedures.

## Common Pitfalls and Solutions

### ❌ Pitfall 1: Not Stopping Sessions

```java
// WRONG - session never closed
persistence.startSession("Test", "App", null);
runAutomation();
// Oops! Forgot to stop
```

**Impact**: Memory leaks, incomplete data, resource exhaustion

**Solution**: Always use try-finally or try-with-resources
```java
// RIGHT - session always closed
String sessionId = persistence.startSession("Test", "App", null);
try {
    runAutomation();
} finally {
    persistence.stopSession();
}
```

### ❌ Pitfall 2: Recording Without Duration

```java
// WRONG - missing duration
ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    .setText("Clicked button")
    // No duration set!
    .build();
```

**Impact**: Incomplete performance analysis, broken statistics

**Solution**: Always set duration in milliseconds
```java
// RIGHT - includes timing
long start = System.currentTimeMillis();
performAction();
long duration = System.currentTimeMillis() - start;

ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    .setText("Clicked button")
    .setDuration(duration)  // Essential for performance tracking
    .build();
```

### ❌ Pitfall 3: Using In-Memory for Production

```java
// WRONG for production - data lost on restart
PersistenceProvider provider = PersistenceProviderFactory.createInMemory();
```

**Impact**: Data loss on crashes, no historical analysis, limited capacity

**Solution**: Use file or database persistence for production
```java
// RIGHT for production - durable storage
PersistenceConfiguration config = new PersistenceConfiguration();
config.setType(PersistenceConfiguration.PersistenceType.DATABASE);
PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### ❌ Pitfall 4: Ignoring Async Recording Benefits

```java
// WRONG for high-volume - blocks automation
PersistenceConfiguration config = new PersistenceConfiguration();
// Default sync recording
```

**Impact**: 5-10ms overhead per action, slow automation, CPU contention

**Solution**: Enable async for high-volume scenarios
```java
// RIGHT for high-volume - non-blocking
PersistenceConfiguration config = new PersistenceConfiguration();
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setBufferSize(500);
```

### ❌ Pitfall 5: Not Setting Timestamp

```java
// WRONG - no timestamp
ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    // Missing timestamp!
    .build();
```

**Impact**: Cannot track action timing, broken session timelines

**Solution**: Always include timestamp
```java
// RIGHT - includes timestamp
import java.time.LocalDateTime;

ActionRecord record = new ActionRecord.Builder()
    .setActionSuccess(true)
    .setTimeStamp(LocalDateTime.now())  // Essential for timeline
    .setDuration(250)
    .build();
```

### ❌ Pitfall 6: Hardcoding File Paths

```java
// WRONG - hardcoded paths
config.getFile().setBasePath("C:\\Users\\John\\recordings");
```

**Impact**: Breaks on different machines, deployment issues, portability problems

**Solution**: Use relative paths or environment variables
```java
// RIGHT - portable paths
String basePath = System.getenv("BROBOT_RECORDINGS_PATH");
if (basePath == null) {
    basePath = "./recordings";  // Fallback to relative
}
config.getFile().setBasePath(basePath);
```

### ❌ Pitfall 7: Not Handling Import Errors

```java
// WRONG - no error handling
ActionHistory history = loadFromFile("session.json");
persistence.importSession(history, "Imported");
```

**Impact**: Silent failures, corrupted data, application crashes

**Solution**: Validate and handle errors
```java
// RIGHT - robust import
import io.github.jspinak.brobot.model.action.ActionHistory;

try {
    ActionHistory history = loadFromFile("session.json");

    // Validate before import
    if (history.getSnapshots().isEmpty()) {
        log.warn("Empty session, skipping import");
        return;
    }

    String sessionId = persistence.importSession(history, "Imported");
    log.info("Successfully imported session: {}", sessionId);

} catch (IOException e) {
    log.error("Failed to load session file", e);
} catch (Exception e) {
    log.error("Failed to import session", e);
}
```

## Related Guides

### Core Concepts
- [Persistence Module Architecture](../architecture/persistence-module.md) - Technical architecture and design patterns
- [States](../../01-getting-started/states.md) - Understanding StateObjects used in recording
- [ActionConfig Overview](../action-config/01-overview.md) - Action configuration system

### Testing and Integration
- [Testing Introduction](../../04-testing/testing-intro.md) - Testing patterns and strategies
- [Integration Testing](../../04-testing/integration-testing.md) - Using persistence in tests
- [CI/CD Testing](../../04-testing/advanced/ci-cd-testing.md) - CI/CD integration patterns
- [Action Recording](../../04-testing/action-recording.md) - Visual screenshot-based recording (different from persistence)

### Configuration
- [Auto-Configuration](../configuration/auto-configuration.md) - Spring Boot auto-configuration details
- [Properties Reference](../configuration/properties-reference.md) - All Brobot configuration properties

## Support

For issues and questions:
- GitHub Issues: https://github.com/jspinak/brobot/issues
- Documentation: https://github.com/jspinak/brobot/tree/main/docs
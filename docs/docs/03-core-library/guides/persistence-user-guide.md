# Persistence Module User Guide

## Getting Started

The Brobot Persistence Module allows you to record and analyze automation executions. This guide covers installation, configuration, and usage for different scenarios.

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
import io.github.jspinak.brobot.persistence.*;

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

## Configuration Examples

### File-Based Persistence

Perfect for standalone applications and testing.

```java
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
PersistenceConfiguration config = PersistenceConfiguration.memoryDefault();
config.getMemory().setMaxSessions(5);
config.getMemory().setMaxRecordsPerSession(100);

PersistenceProvider provider = PersistenceProviderFactory.create(config);
```

### Database Persistence (Spring Boot)

For enterprise applications with Spring Boot.

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

```java
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

```java
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
persistence.recordAction(record, currentStateObject);
```

### Batch Recording

```java
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

```java
@Component
public class ActionRecorder {
    @Autowired
    private PersistenceProvider persistence;
    
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
// Export to ActionHistory
ActionHistory history = persistence.exportSession(sessionId);

// Save to file
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.writeValue(new File("session-export.json"), history);
```

### Importing Sessions

```java
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
// Organize by date
LocalDate today = LocalDate.now();
String basePath = String.format("./recordings/%d/%02d/%02d",
    today.getYear(), today.getMonthValue(), today.getDayOfMonth());

PersistenceConfiguration config = new PersistenceConfiguration();
config.getFile().setBasePath(basePath);
```

### Compression and Optimization

```java
PersistenceConfiguration config = new PersistenceConfiguration();
config.getFile().setCompressExports(true);  // GZIP compression
config.getFile().setPrettyPrint(false);      // Smaller files
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setBufferSize(500);  // Larger buffer
```

### Memory Management

```java
// For in-memory provider
PersistenceConfiguration config = new PersistenceConfiguration();
config.setType(PersistenceConfiguration.PersistenceType.MEMORY);
config.getMemory().setMaxSessions(10);
config.getMemory().setMaxRecordsPerSession(1000);
config.getMemory().setPersistOnShutdown(true);
config.getMemory().setShutdownExportPath("./emergency-export");

// Monitor memory usage
if (provider instanceof InMemoryPersistenceProvider memory) {
    var stats = memory.getMemoryStatistics();
    System.out.println("Memory usage: " + stats);
}
```

### Performance Tuning

```java
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
```

## Integration Examples

### JUnit Test Integration

```java
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

```java
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
            if (failure.getMatches().isEmpty()) {
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

Always stop sessions in finally blocks:
```java
String sessionId = persistence.startSession("Critical Process", "App", null);
try {
    runCriticalProcess();
} catch (Exception e) {
    // Record error
    ActionRecord errorRecord = new ActionRecord.Builder()
        .setActionSuccess(false)
        .setText("Error: " + e.getMessage())
        .build();
    persistence.recordAction(errorRecord, null);
    throw e;
} finally {
    persistence.stopSession();
}
```

### 3. Resource Management

Clean up old sessions periodically:
```java
@Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
public void cleanupOldSessions() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    
    for (String sessionId : persistence.getAllSessions()) {
        SessionMetadata metadata = persistence.getSessionMetadata(sessionId);
        if (metadata.getEndTime() != null && 
            metadata.getEndTime().isBefore(cutoff)) {
            persistence.deleteSession(sessionId);
            log.info("Deleted old session: {}", sessionId);
        }
    }
}
```

### 4. Performance Monitoring

Monitor recording overhead:
```java
public class PerformanceMonitor {
    private final PersistenceProvider persistence;
    private final AtomicLong totalRecordTime = new AtomicLong();
    private final AtomicInteger recordCount = new AtomicInteger();
    
    public void recordWithMonitoring(ActionRecord record) {
        long start = System.nanoTime();
        persistence.recordAction(record, null);
        long duration = System.nanoTime() - start;
        
        totalRecordTime.addAndGet(duration);
        recordCount.incrementAndGet();
        
        if (recordCount.get() % 100 == 0) {
            double avgMs = totalRecordTime.get() / recordCount.get() / 1_000_000.0;
            log.info("Avg recording time: {} ms", avgMs);
        }
    }
}
```

## Troubleshooting

### Issue: Out of Memory

**Solution**: Configure limits and enable persistence on shutdown
```java
config.getMemory().setMaxSessions(5);
config.getMemory().setMaxRecordsPerSession(500);
config.getMemory().setPersistOnShutdown(true);
```

### Issue: Slow Recording Performance

**Solution**: Enable async recording and increase buffer
```java
config.getPerformance().setAsyncRecording(true);
config.getPerformance().setBufferSize(500);
config.getPerformance().setThreadPoolSize(5);
```

### Issue: Large File Sizes

**Solution**: Enable compression and disable pretty printing
```java
config.getFile().setCompressExports(true);
config.getFile().setPrettyPrint(false);
config.getFile().setFormat(PersistenceConfiguration.FileFormat.CSV);  // Smaller than JSON
```

### Issue: Database Connection Pool Exhaustion

**Solution**: Configure connection pool and batch size
```java
config.getDatabase().setConnectionPoolSize(10);
config.getDatabase().setBatchSize(100);
```

## Support

For issues and questions:
- GitHub Issues: https://github.com/jspinak/brobot/issues
- Documentation: https://github.com/jspinak/brobot/docs
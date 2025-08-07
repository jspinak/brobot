---
title: ActionHistory Integration Testing
sidebar_position: 4
---

# ActionHistory Integration Testing

## Overview

ActionHistory is the core component of Brobot's mock testing framework, enabling realistic simulation of GUI automation without requiring the actual application. It maintains a statistical model of how GUI elements behave, capturing not just whether elements were found but also when, where, and under what conditions.

## Understanding ActionHistory

### What is ActionHistory?

ActionHistory is a data structure that accumulates `ActionRecord` snapshots over time, building a probabilistic model of GUI behavior. This historical data serves two critical purposes:

1. **Mock Execution**: Enables realistic testing without the target application
2. **Pattern Learning**: Provides empirical data for optimizing automation strategies

### Key Components

```java
public class ActionHistory {
    private int timesSearched = 0;      // Total search attempts
    private int timesFound = 0;         // Successful finds
    private List<ActionRecord> snapshots = new ArrayList<>();  // Historical records
}
```

Each `ActionRecord` contains:
- **ActionConfig**: The action configuration (modern API)
- **Match Results**: Screenshot regions where patterns were found
- **Success Status**: Whether the action succeeded
- **Timing Data**: Duration and timestamps
- **Context**: State and environment information

## Modern API Usage

### Creating ActionHistory with ActionConfig

The modern API uses strongly-typed `ActionConfig` classes instead of the deprecated `ActionOptions`:

```java
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;

// Create an ActionHistory
ActionHistory history = new ActionHistory();

// Add a find action record
ActionRecord findRecord = new ActionRecord.Builder()
    .setActionConfig(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.95)
        .build())
    .addMatch(new Match.Builder()
        .setRegion(100, 200, 50, 30)  // x, y, width, height
        .setSimScore(0.96)
        .build())
    .setActionSuccess(true)
    .setDuration(250)  // milliseconds
    .build();

history.addSnapshot(findRecord);
```

### Recording Different Action Types

#### Click Actions

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

ActionRecord clickRecord = new ActionRecord.Builder()
    .setActionConfig(new ClickOptions.Builder()
        .setClickType(ClickOptions.Type.DOUBLE)
        .setNumberOfClicks(2)
        .setPauseBeforeMouseDown(100)
        .setPauseAfterMouseUp(100)
        .build())
    .addMatch(new Match.Builder()
        .setRegion(150, 250, 40, 20)
        .setSimScore(0.92)
        .build())
    .setActionSuccess(true)
    .build();

history.addSnapshot(clickRecord);
```

#### Type Actions with Text

```java
import io.github.jspinak.brobot.action.basic.type.TypeOptions;

ActionRecord typeRecord = new ActionRecord.Builder()
    .setActionConfig(new TypeOptions.Builder()
        .setPauseBeforeBegin(200)
        .setPauseAfterEnd(100)
        .build())
    .setText("Hello World")
    .setActionSuccess(true)
    .build();

history.addSnapshot(typeRecord);
```

#### Vanish Actions

```java
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;

ActionRecord vanishRecord = new ActionRecord.Builder()
    .setActionConfig(new VanishOptions.Builder()
        .setWaitTime(5.0)  // seconds
        .build())
    .setActionSuccess(true)  // Element disappeared
    .setDuration(3500)  // Vanished after 3.5 seconds
    .build();

history.addSnapshot(vanishRecord);
```

## Integration Testing Setup

### 1. Configure Mock Execution

Enable mock mode in your test configuration:

```yaml
# application-test.yml
brobot:
  mock:
    enabled: true
    use-real-screenshots: false
```

**Note**: ActionHistory persistence is handled by the Brobot Runner application, not the library itself. During live automation, the Runner captures and stores ActionRecords in its database. For testing, ActionHistory is maintained in memory or loaded from exported files.

### 2. Initialize State Objects with ActionHistory

```java
import io.github.jspinak.brobot.model.state.stateObject.StateImage;
import io.github.jspinak.brobot.model.action.ActionHistory;

@Component
public class LoginStateInitializer {
    
    public StateImage createLoginButton() {
        StateImage loginButton = new StateImage.Builder()
            .withPattern("login-button.png")
            .build();
        
        // Add historical data for realistic mocking
        ActionHistory history = new ActionHistory();
        
        // Simulate 90% success rate
        for (int i = 0; i < 100; i++) {
            boolean success = i < 90;  // 90% success
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.BEST)
                    .build())
                .setActionSuccess(success)
                .addMatch(success ? createMatch() : null)
                .build();
            
            history.addSnapshot(record);
        }
        
        loginButton.setActionHistory(history);
        return loginButton;
    }
    
    private Match createMatch() {
        return new Match.Builder()
            .setRegion(500, 400, 100, 40)
            .setSimScore(0.85 + Math.random() * 0.1)  // 0.85-0.95
            .build();
    }
}
```

### 3. Write Integration Tests

```java
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.mock.enabled=true",
    "brobot.action-history.source=database"
})
public class LoginWorkflowIntegrationTest {
    
    @Autowired
    private LoginState loginState;
    
    @Autowired
    private ActionHistory actionHistory;
    
    @Test
    public void testLoginWorkflow() {
        // Pre-populate ActionHistory with realistic data
        prepareActionHistory();
        
        // Execute the workflow - will use ActionHistory for mocking
        boolean loginSuccess = loginState.execute();
        
        assertTrue(loginSuccess, "Login should succeed based on ActionHistory");
        
        // Verify action was recorded
        assertEquals(actionHistory.getTimesSearched(), 
                    actionHistory.getTimesFound() + 1);
    }
    
    private void prepareActionHistory() {
        // Add successful login button click
        ActionRecord loginClick = new ActionRecord.Builder()
            .setActionConfig(new ClickOptions.Builder().build())
            .addMatch(new Match.Builder()
                .setRegion(500, 400, 100, 40)
                .setSimScore(0.92)
                .build())
            .setActionSuccess(true)
            .build();
        
        actionHistory.addSnapshot(loginClick);
        
        // Add successful username field interaction
        ActionRecord usernameType = new ActionRecord.Builder()
            .setActionConfig(new TypeOptions.Builder().build())
            .setText("testuser@example.com")
            .setActionSuccess(true)
            .build();
        
        actionHistory.addSnapshot(usernameType);
    }
}
```

## Advanced Testing Patterns

### State-Specific ActionHistory

Different states can have different success patterns:

```java
public class StateSpecificTesting {
    
    @Test
    public void testStateTransitions() {
        Long loginStateId = 1L;
        Long mainStateId = 2L;
        
        // Add state-specific records
        ActionRecord loginRecord = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder().build())
            .setStateId(loginStateId)
            .setActionSuccess(true)
            .addMatch(createLoginMatch())
            .build();
        
        ActionRecord mainRecord = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder().build())
            .setStateId(mainStateId)
            .setActionSuccess(true)
            .addMatch(createMainMatch())
            .build();
        
        actionHistory.addSnapshot(loginRecord);
        actionHistory.addSnapshot(mainRecord);
        
        // Query state-specific snapshots
        Optional<ActionRecord> loginSnapshot = actionHistory.getRandomSnapshot(
            new PatternFindOptions.Builder().build(),
            loginStateId
        );
        
        assertTrue(loginSnapshot.isPresent());
        assertEquals(loginStateId, loginSnapshot.get().getStateId());
    }
}
```

### Simulating Failures

Test error handling with failure scenarios:

```java
public class FailureSimulation {
    
    public ActionHistory createFlakeyButtonHistory() {
        ActionHistory history = new ActionHistory();
        Random random = new Random(42);  // Deterministic for testing
        
        for (int i = 0; i < 20; i++) {
            boolean success = random.nextDouble() < 0.7;  // 70% success
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new ClickOptions.Builder().build())
                .setActionSuccess(success)
                .addMatch(success ? createMatch() : null)
                .setDuration(success ? 200 : 5000)  // Timeout on failure
                .build();
            
            history.addSnapshot(record);
        }
        
        return history;
    }
    
    @Test
    public void testRetryMechanism() {
        StateImage flakeyButton = new StateImage.Builder()
            .withPattern("flakey-button.png")
            .build();
        
        flakeyButton.setActionHistory(createFlakeyButtonHistory());
        
        // Test retry logic
        int attempts = 0;
        boolean success = false;
        
        while (!success && attempts < 3) {
            Optional<ActionRecord> result = flakeyButton.getActionHistory()
                .getRandomSnapshot(new ClickOptions.Builder().build());
            
            success = result.map(ActionRecord::isActionSuccess).orElse(false);
            attempts++;
        }
        
        // Should eventually succeed with retries
        assertTrue(success || attempts == 3, 
            "Should succeed with retries or reach max attempts");
    }
}
```

### Text Extraction Testing

```java
public class TextExtractionTest {
    
    @Test
    public void testOCRResults() {
        ActionHistory ocrHistory = new ActionHistory();
        
        // Add OCR results with varying accuracy
        String[] expectedTexts = {
            "Username:",
            "Password:",
            "Login"
        };
        
        for (String text : expectedTexts) {
            ActionRecord ocrRecord = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setText(text)
                .addMatch(new Match.Builder()
                    .setRegion(100, 100 + ocrHistory.getSnapshots().size() * 50, 200, 30)
                    .setSimScore(0.95)
                    .build())
                .setActionSuccess(true)
                .build();
            
            ocrHistory.addSnapshot(ocrRecord);
        }
        
        // Test random text retrieval
        String randomText = ocrHistory.getRandomText();
        assertTrue(Arrays.asList(expectedTexts).contains(randomText));
        
        // Test all texts are accessible
        Set<String> retrievedTexts = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            retrievedTexts.add(ocrHistory.getRandomText());
        }
        
        assertEquals(expectedTexts.length, retrievedTexts.size(),
            "All texts should be retrievable");
    }
}
```

## Data-Driven Testing

### Loading ActionHistory from Files

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataDrivenTests {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testWithHistoricalData() throws IOException {
        // Load pre-recorded ActionHistory
        Path historyFile = Path.of("src/test/resources/action-histories/login-flow.json");
        String json = Files.readString(historyFile);
        ActionHistory history = objectMapper.readValue(json, ActionHistory.class);
        
        // Use historical data for testing
        StateImage loginButton = new StateImage.Builder()
            .withPattern("login.png")
            .build();
        loginButton.setActionHistory(history);
        
        // Run tests with real historical data
        Optional<ActionRecord> snapshot = history.getRandomSnapshot(
            new ClickOptions.Builder().build()
        );
        
        assertTrue(snapshot.isPresent());
        assertTrue(snapshot.get().isActionSuccess());
    }
    
    public void saveActionHistory(ActionHistory history, Path outputPath) 
            throws IOException {
        String json = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(history);
        Files.writeString(outputPath, json);
    }
}
```

### Using ActionHistory from Brobot Runner

The Brobot Runner application provides persistence for ActionHistory:

```java
// Export ActionHistory from Runner
// 1. Run automation with recording enabled in Runner UI
// 2. Export session as JSON file
// 3. Load in your tests:

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ActionHistoryLoader {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ActionHistory loadFromRunnerExport(String filename) throws IOException {
        Path path = Path.of("test-data/runner-exports", filename);
        return objectMapper.readValue(path.toFile(), ActionHistory.class);
    }
    
    public Map<String, ActionHistory> loadAllExports() throws IOException {
        Map<String, ActionHistory> histories = new HashMap<>();
        Path exportDir = Path.of("test-data/runner-exports");
        
        Files.list(exportDir)
            .filter(p -> p.toString().endsWith(".json"))
            .forEach(path -> {
                try {
                    String name = path.getFileName().toString().replace(".json", "");
                    histories.put(name, loadFromRunnerExport(path.getFileName().toString()));
                } catch (IOException e) {
                    log.error("Failed to load {}", path, e);
                }
            });
        
        return histories;
    }
}
```

**Workflow for Using Runner-Recorded Data:**

1. **Record in Runner**: Enable recording in the Runner UI during live automation
2. **Export Sessions**: Export recorded sessions as JSON files
3. **Import in Tests**: Load exported ActionHistory in your integration tests
4. **Replay Behavior**: Use the recorded data for realistic mock testing

## Performance Testing

### Measuring Action Performance

```java
public class PerformanceTest {
    
    @Test
    public void testActionPerformance() {
        ActionHistory performanceHistory = new ActionHistory();
        
        // Simulate various response times
        for (int i = 0; i < 100; i++) {
            long duration = 100 + (long)(Math.random() * 900);  // 100-1000ms
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setDuration(duration)
                .setActionSuccess(duration < 800)  // Timeout at 800ms
                .build();
            
            performanceHistory.addSnapshot(record);
        }
        
        // Analyze performance
        double avgDuration = performanceHistory.getSnapshots().stream()
            .mapToLong(ActionRecord::getDuration)
            .average()
            .orElse(0);
        
        long maxDuration = performanceHistory.getSnapshots().stream()
            .mapToLong(ActionRecord::getDuration)
            .max()
            .orElse(0);
        
        assertTrue(avgDuration < 600, "Average duration should be under 600ms");
        assertTrue(maxDuration < 1000, "Max duration should be under 1000ms");
    }
}
```

## Best Practices

### 1. Realistic Data Generation

Create ActionHistory that reflects real-world patterns:

```java
public class RealisticDataGenerator {
    
    public ActionHistory generateRealisticHistory() {
        ActionHistory history = new ActionHistory();
        Random random = new Random();
        
        // Morning hours - higher success rate
        for (int hour = 9; hour < 12; hour++) {
            addHourlyData(history, hour, 0.95);  // 95% success
        }
        
        // Afternoon - slightly lower success
        for (int hour = 12; hour < 17; hour++) {
            addHourlyData(history, hour, 0.85);  // 85% success
        }
        
        // Evening - degraded performance
        for (int hour = 17; hour < 20; hour++) {
            addHourlyData(history, hour, 0.70);  // 70% success
        }
        
        return history;
    }
    
    private void addHourlyData(ActionHistory history, int hour, double successRate) {
        for (int i = 0; i < 10; i++) {
            boolean success = Math.random() < successRate;
            
            ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(new PatternFindOptions.Builder().build())
                .setActionSuccess(success)
                .setTimestamp(LocalDateTime.now()
                    .withHour(hour)
                    .withMinute(i * 6))
                .build();
            
            history.addSnapshot(record);
        }
    }
}
```

### 2. Isolation Between Tests

Ensure tests don't interfere with each other:

```java
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class IsolatedActionHistoryTest {
    
    private ActionHistory history;
    
    @BeforeEach
    void setUp() {
        // Fresh ActionHistory for each test
        history = new ActionHistory();
    }
    
    @AfterEach
    void tearDown() {
        // Clear any persistent data
        history = null;
    }
}
```

### 3. Deterministic Testing

Use seeded random for reproducible tests:

```java
public class DeterministicTest {
    
    @Test
    @RepeatedTest(5)
    public void testDeterministicBehavior() {
        // Use fixed seed for reproducibility
        Random random = new Random(12345);
        ActionHistory history = createSeededHistory(random);
        
        // Results should be identical across runs
        List<Boolean> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Optional<ActionRecord> snapshot = history.getRandomSnapshot(
                new PatternFindOptions.Builder().build()
            );
            results.add(snapshot.map(ActionRecord::isActionSuccess).orElse(false));
        }
        
        // Verify deterministic pattern
        assertEquals(Arrays.asList(true, true, false, true, true, 
                                  false, true, true, true, false), 
                    results);
    }
}
```

## Troubleshooting

### Common Issues

1. **Empty ActionHistory**: Ensure snapshots are added before querying
2. **Type Mismatches**: Use consistent ActionConfig types
3. **State Context**: Verify state IDs match when using state-specific queries
4. **Text Snapshots**: Text-only snapshots need matches added automatically

### Debug Logging

Enable detailed logging for troubleshooting:

```yaml
logging:
  level:
    io.github.jspinak.brobot.model.action: DEBUG
    io.github.jspinak.brobot.mock: DEBUG
```

## Migration from Legacy API

If you have existing tests using `ActionOptions`, see the [ActionHistory Migration Guide](/docs/core-library/migration/actionhistory-migration-guide) for detailed migration instructions.

## Next Steps

- Learn about [Enhanced Mocking](/docs/core-library/testing/enhanced-mocking) for advanced scenarios
- Explore [States](/docs/getting-started/states) for complex workflows
- Read about [Core Concepts](/docs/getting-started/core-concepts) for foundational understanding
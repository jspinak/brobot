---
sidebar_position: 2
---

# Integration Testing

Integration testing in Brobot simulates the complete application environment and workflow. Unlike unit testing which focuses on individual components, integration testing validates entire automation sequences using mock execution to ensure robustness and reliability.

## Overview

Integration testing provides:
- **Full application simulation** without GUI interaction
- **Stochastic modeling** of real-world variability
- **State transition validation** across complex workflows
- **Risk assessment** for automation reliability

## Configuration

### Configuration via Properties

Enable integration testing through configuration:

```properties
# Enable mock mode for integration testing
brobot.core.mock=true

# Mock timing configuration
brobot.mock.time-find-first=0.1
brobot.mock.time-find-all=0.2
brobot.mock.time-click=0.05
brobot.mock.time-drag=0.3

# Testing settings
brobot.testing.iteration=1
brobot.testing.send-logs=true

# Dataset collection (optional)
brobot.dataset.build=false
brobot.dataset.path=dataset/
```

Or using YAML:

```yaml
brobot:
  core:
    mock: true
  mock:
    time-find-first: 0.1
    time-find-all: 0.2
    time-click: 0.05
    time-drag: 0.3
  testing:
    iteration: 1
    send-logs: true
```

### Test Configuration

```java
@SpringBootTest
@TestPropertySource(properties = "brobot.core.mock=true")
public class IntegrationTestConfig {
    // Configuration handled automatically by Spring Boot
}
```  

## Mock Execution Architecture

Brobot's mocking system provides comprehensive simulation of GUI automation:

### How Mocking Works

1. **Action Interception**: All GUI actions (click, find, drag) are intercepted at the wrapper level
2. **Realistic Timing**: Mock operations use configurable delays to simulate real execution
3. **Stochastic Results**: Actions return probabilistic outcomes based on historical data
4. **Transparent Operation**: Application code runs identically in mock and live modes

### Mock Timing Configuration

Configure mock timings via properties:

```properties
# Configure realistic timings for different actions
brobot.mock.time-find-first=0.1
brobot.mock.time-find-all=0.3
brobot.mock.time-click=0.05
brobot.mock.time-drag=0.4
brobot.mock.time-move=0.2
```

Or per-test configuration:

```java
@Test
@TestPropertySource(properties = {
    "brobot.mock.time-find-first=0.1",
    "brobot.mock.time-click=0.05"
})
void testWithCustomTimings() {
    // Test uses configured mock timings
}
``` 

![wrappers](/img/wrappers.jpeg)

Mocking can uncover errors in the code in the same way that traditional testing, for
example JUnit testing, does. You don't have to wait 30 minutes to realize that you
forgot to name the new image you saved, and your application can't find it. This
happens instantly.

Mocking also provides insight into how robust your code is. Parts of the code with
narrow paths (little redundancy in making a transition from state A to state B)
may perform poorly if a state has a low probability of appearing. You can introduce
process flow errors into the mocks, including sending your process to an unknown state,
to see how your app will behave.

## State Object Initialization

### Action History Setup

State objects should be initialized with realistic action histories for proper mock behavior. The modern approach uses ActionConfig-based snapshots:

```java
@Component
public class StateInitializer {
    
    public StateImage createLoginButton() {
        return new StateImage.Builder()
            .addPattern("login_button")
            .setName("LoginButton")
            .build();
    }
    
    // Add action history snapshots to patterns
    public void initializeActionHistory(StateImage stateImage) {
        Pattern pattern = stateImage.getPatterns().get(0);
        ActionHistory history = pattern.getActionHistory();
        
        // Add find snapshots with modern ActionConfig
        history.addSnapshot(createFindSnapshot());
        history.addSnapshot(createClickSnapshot());
    }
    
    private ActionRecord createFindSnapshot() {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.95)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(100, 200, 80, 30)
                .setSimScore(0.95)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    private ActionRecord createClickSnapshot() {
        return new ActionRecord.Builder()
            .setActionConfig(new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build())
            .setActionSuccess(true)
            .build();
    }
}
```

### State Structure Builder Integration

When using the State Structure Builder, mock data is automatically generated:

1. **Screenshot Analysis**: Captures current application screens
2. **Image Recognition**: Analyzes UI elements and their properties
3. **History Generation**: Creates realistic action histories from analysis
4. **Code Generation**: Produces Java code with pre-configured mock data

```java
// Generated by State Structure Builder
public class GeneratedStates {
    
    public static StateImage getSubmitButton() {
        StateImage submitButton = new StateImage.Builder()
            .addPattern("submit_button")
            .setName("SubmitButton")
            .build();
            
        // Initialize with auto-generated action histories
        initializeActionHistory(submitButton);
        return submitButton;
    }
    
    private static void initializeActionHistory(StateImage stateImage) {
        Pattern pattern = stateImage.getPatterns().get(0);
        ActionHistory history = pattern.getActionHistory();
        
        // Generated from actual screenshot analysis
        history.addSnapshot(createSnapshot(0.98, 245, 356, 120, 35));
        history.addSnapshot(createSnapshot(0.96, 245, 356, 120, 35));
        history.addSnapshot(createSnapshot(0.89, 245, 356, 120, 35));
    }
    
    private static ActionRecord createSnapshot(double score, int x, int y, int w, int h) {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setSimScore(score)
                .build())
            .setActionSuccess(true)
            .build();
    }
}
```

### API Migration Note

**Important**: The ActionHistory class is currently in a transitional state. While the modern ActionConfig system is the recommended approach, ActionHistory still uses `ActionOptions.Action` internally for backward compatibility. The examples above show the intended modern API pattern that will be fully supported in future versions.

Current ActionHistory methods that still use the legacy API:
- `getRandomSnapshot(ActionOptions.Action action)`
- `getRandomSnapshot(ActionOptions.Action action, Long state)`

For now, when working with ActionHistory directly, you may need to use adapter patterns or continue using ActionOptions.Action until the migration is complete.

```java
private static List<ActionResult> getFindHistorySubmitButton() {
    // Generated from actual screenshot analysis
    return Arrays.asList(
        createActionResult(0.98, 245, 356, 120, 35),
        createActionResult(0.96, 245, 356, 120, 35),
        createActionResult(0.89, 245, 356, 120, 35)
    );
}
```
```

## Logging and Monitoring

### Comprehensive Test Logging

Mock execution provides detailed logging for debugging and analysis:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.testing.send-logs=true",
    "brobot.logging.verbosity=VERBOSE"
})
public class IntegrationTestWithLogging {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestWithLogging.class);
    
    @Autowired
    private Action action;
    
    @Autowired
    private StateImageRepository stateImageRepo;
    
    @Test
    public void testCompleteWorkflow() {
        logger.info("Starting integration test workflow");
        
        // Mock execution provides full logging
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        StateImage loginButton = stateImageRepo.get("login_button");
        ActionResult loginResult = action.perform(findOptions, loginButton);
        logger.info("Login result: {} matches found", loginResult.size());
        
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        action.perform(clickOptions, loginButton);
        
        StateImage dashboardMenu = stateImageRepo.get("dashboard_menu");
        ActionResult navigationResult = action.perform(findOptions, dashboardMenu);
        logger.info("Navigation result: success={}", navigationResult.isSuccess());
        
        // Process data with multiple finds
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        StateImage dataRows = stateImageRepo.get("data_rows");
        ActionResult dataResult = action.perform(findAllOptions, dataRows);
        logger.info("Data processing completed with {} operations", dataResult.size());
    }
}
```

### Log Output Analysis

Mock runs produce detailed output including:
- **Action Timing**: Simulated execution durations
- **Decision Points**: State transition logic
- **Match Results**: Simulated find operation outcomes
- **Error Conditions**: Exception handling and recovery
- **Performance Metrics**: Mock operation statistics

```
[INFO] Starting integration test workflow
[DEBUG] Mock FIND operation: login_button.png -> 1 match (score: 0.95, time: 120ms)
[DEBUG] Mock CLICK operation: (100,200) -> SUCCESS (time: 50ms)
[INFO] Login result: 1 matches found
[DEBUG] State transition: LOGIN -> DASHBOARD (probability: 0.95)
[DEBUG] Mock FIND operation: dashboard_menu.png -> 2 matches (time: 200ms)
[INFO] Navigation result: success=true
```

## Test Assertions and Validation

### Modern Assertion Patterns

```java
public class IntegrationTestAssertions {
    
    @Test
    public void testWorkflowReliability() {
        // Test multiple mock runs for consistency
        List<ActionResult> results = new ArrayList<>();
        
        // Configure action for workflow
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.85)
            .then(new ClickOptions.Builder().build())  // Chain find with click
            .build();
        
        StateImage criticalButton = stateImageRepo.get("critical_button");
        
        for (int i = 0; i < 10; i++) {
            // Each iteration uses configuration properties
            ActionResult result = action.perform(findOptions, criticalButton);
            results.add(result);
        }
        
        // Assert workflow reliability
        long successCount = results.stream()
            .filter(ActionResult::isSuccess)
            .count();
            
        double successRate = (double) successCount / results.size();
        assertTrue(successRate >= 0.8, 
            String.format("Workflow success rate %.2f below threshold 0.8", successRate));
    }
    
    @Test
    public void testStateTransitionProbabilities() {
        // Test state transition reliability
        Map<String, Integer> transitionCounts = new HashMap<>();
        
        State loginState = stateRepository.get("LOGIN");
        StateImage dashboardLink = loginState.getStateImages().stream()
            .filter(img -> img.getName().equals("dashboard_link"))
            .findFirst().orElseThrow();
        
        PatternFindOptions findAndClick = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .then(new ClickOptions.Builder().build())
            .build();
        
        for (int i = 0; i < 100; i++) {
            ActionResult result = action.perform(findAndClick, dashboardLink);
            String outcome = result.isSuccess() ? "SUCCESS" : "FAILURE";
            transitionCounts.merge(outcome, 1, Integer::sum);
        }
        
        // Verify expected probability distribution
        double successRate = (double) transitionCounts.getOrDefault("SUCCESS", 0) / 100;
        assertTrue(successRate >= 0.85, "State transition success rate too low");
        assertTrue(successRate <= 0.98, "State transition success rate unrealistically high");
    }
    
    @Test
    public void testErrorRecovery() {
        // Test recovery mechanisms with retry logic
        StateImage problematicElement = stateImageRepo.get("problematic_element");
        
        // Configure action with retry
        PatternFindOptions findWithRetry = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setMaxIterations(3)  // Retry up to 3 times
            .setPauseBeforeRetry(0.5)
            .build();
        
        ActionResult result = action.perform(findWithRetry, problematicElement);
        
        // In mock mode, the retry behavior is simulated
        // Check that appropriate retries were attempted
        assertTrue(result.getAttempts() <= 3, "Too many retry attempts");
        
        // For successful recovery after retries
        if (result.isSuccess()) {
            assertTrue(result.getAttempts() > 1, "Recovery should have required retries");
        }
    }
}
```

### Performance Assertions

```java
@Test
public void testPerformanceCharacteristics() {
    long startTime = System.currentTimeMillis();
    
    // Complex workflow with multiple actions
    PatternFindOptions complexFind = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .setSimilarity(0.9)
        .then(new DefineRegionOptions.Builder()
            .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
            .build())
        .then(new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.DOUBLE)
            .build())
        .build();
    
    StateImage complexElement = stateImageRepo.get("complex_element");
    ActionResult result = action.perform(complexFind, complexElement);
    
    long duration = System.currentTimeMillis() - startTime;
    
    // Assert mock timing is realistic
    assertTrue(duration >= 500, "Mock execution too fast - unrealistic");
    assertTrue(duration <= 2000, "Mock execution too slow - check configuration");
    
    // Assert result quality
    assertTrue(result.isSuccess());
    assertFalse(result.isEmpty());
}
```

### Custom Test Matchers

```java
public class BrobotMatchers {
    
    public static Matcher<ActionResult> hasMinimumMatches(int minCount) {
        return new TypeSafeMatcher<ActionResult>() {
            @Override
            protected boolean matchesSafely(ActionResult result) {
                return result.size() >= minCount;
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("ActionResult with at least ").appendValue(minCount).appendText(" matches");
            }
        };
    }
    
    public static Matcher<ActionResult> hasSuccessfulExecution() {
        return new TypeSafeMatcher<ActionResult>() {
            @Override
            protected boolean matchesSafely(ActionResult result) {
                return result.isSuccess() && !result.isEmpty();
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("successful ActionResult with matches");
            }
        };
    }
}

// Usage in tests
@Test
public void testWithCustomMatchers() {
    PatternFindOptions findOptions = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build();
    
    StateImage buttons = stateImageRepo.get("buttons");
    ActionResult result = action.perform(findOptions, buttons);
    
    assertThat(result, hasMinimumMatches(2));
    assertThat(result, hasSuccessfulExecution());
}
```

## Best Practices

1. **Modern API Usage**
   - Use ActionConfig subclasses (PatternFindOptions, ClickOptions, etc.) instead of ActionOptions
   - Configure actions through properties files rather than programmatic setup
   - Leverage action chaining with `.then()` for complex workflows

2. **Realistic Mock Data**
   - Use actual screenshot analysis to generate mock histories
   - Include both success and failure scenarios with ActionRecord snapshots
   - Model realistic timing and variability through ActionConfig

3. **Comprehensive Testing**
   - Test complete workflows, not just individual actions
   - Include error conditions and recovery paths
   - Validate state transitions and probabilities

4. **Performance Considerations**
   - Configure realistic mock timings via properties
   - Test for performance regressions
   - Monitor mock execution efficiency

5. **Continuous Integration**
   - Run integration tests in CI/CD pipelines
   - Use deterministic random seeds for reproducible results
   - Archive test logs and reports for analysis




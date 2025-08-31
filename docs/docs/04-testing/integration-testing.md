---
sidebar_position: 2
---

# Integration Testing

Integration testing in Brobot simulates the complete application environment and workflow. Unlike unit testing which focuses on individual components, integration testing validates entire automation sequences using mock execution to ensure robustness and reliability.

## Overview

Integration testing provides:
- **Centralized mock mode management** via `MockModeManager`
- **Full application simulation** without GUI interaction
- **Stochastic modeling** of real-world variability
- **State transition validation** across complex workflows
- **Risk assessment** for automation reliability

## Test Base Class

### BrobotTestBase (Without Spring)

For simple integration tests without Spring context, extend `BrobotTestBase`:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public class WorkflowIntegrationTest extends BrobotTestBase {
    // Mock mode is automatically enabled
    // All mock settings are synchronized via MockModeManager
}
```

### BrobotIntegrationTestBase (With Spring)

For integration tests requiring Spring context and dependency injection:

```java
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

@SpringBootTest(classes = BrobotTestApplication.class)
@ContextConfiguration(initializers = TestConfigurationManager.class)
@Import({TestActionConfig.class, MockBrobotLoggerConfig.class})
public class SpringIntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private BrobotLogger logger;  // Automatically configured via factory pattern
    
    @Autowired
    private Action action;  // Clean dependency injection
}
```

:::tip Clean Architecture for Spring Tests
The test configuration architecture uses factory patterns and proper initialization order to ensure clean dependencies. See [Test Logging Architecture](/docs/core-library/testing/test-logging-architecture) for details.
:::

## Configuration

### Automatic Mock Configuration

When using `BrobotTestBase`, mock mode is automatically configured:

```java
public class IntegrationTest extends BrobotTestBase {
    
    @Test
    public void testWorkflow() {
        // Mock mode is enabled via MockModeManager
        assertTrue(MockModeManager.isMockMode());
        // Your test logic here
    }
}
```

### Configuration via Properties

Additional testing configuration:

```properties
# Mock mode is automatically enabled by BrobotTestBase
# These properties are synchronized by MockModeManager:
brobot.mock.mode=true
brobot.core.mock=true
brobot.framework.mock=true

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
public class IntegrationTestConfig extends BrobotTestBase {
    // Mock mode automatically enabled by BrobotTestBase
    // Additional configuration handled by Spring Boot
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Ensures MockModeManager is configured
        // Add any additional setup
    }
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

## Performance Optimization

### Optimized Test Base Class

For high-performance integration testing, use the `OptimizedIntegrationTestBase`:

```java
import io.github.jspinak.brobot.test.OptimizedIntegrationTestBase;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Share Spring context
@Timeout(value = 5, unit = TimeUnit.MINUTES)     // Default timeout
public class FastIntegrationTest extends OptimizedIntegrationTestBase {
    
    @Test
    public void testWorkflow() {
        // Benefits:
        // - Shared Spring context reduces initialization overhead
        // - Optimized mock timings (0.005-0.015s per operation)
        // - Per-class lifecycle reduces test setup time
    }
}
```

### Gradle Configuration

Configure `library-test/build.gradle` for optimal test execution:

```gradle
test {
    // Increased timeout to prevent premature failures
    timeout = Duration.ofMinutes(10)
    
    // Parallel execution using half of available cores
    maxParallelForks = Math.max(1, Runtime.runtime.availableProcessors().intdiv(2))
    
    // Fork new JVM every 20 tests to prevent memory buildup
    forkEvery = 20
    
    // Optimized memory settings
    maxHeapSize = '4g'
    jvmArgs '-XX:MaxRAMPercentage=75.0', 
            '-XX:+UseG1GC',
            '-Dorg.bytedeco.javacpp.maxphysicalbytes=8G'
}
```

### Gradle Properties

Add `library-test/gradle.properties` for test-specific optimizations:

```properties
# Enable parallel execution
org.gradle.parallel=true
org.gradle.caching=true

# Configure JUnit 5 parallel execution
systemProp.junit.jupiter.execution.parallel.enabled=true
systemProp.junit.jupiter.execution.parallel.mode.default=concurrent
systemProp.junit.jupiter.execution.parallel.config.strategy=dynamic
systemProp.junit.jupiter.execution.parallel.config.dynamic.factor=0.5

# Retry flaky tests
systemProp.gradle.test.retry.maxRetries=2
systemProp.gradle.test.retry.maxFailures=5
```

### Mock Timing Optimization

Configure faster mock timings for test environments:

```java
@TestConfiguration
public class OptimizedTestConfig implements BeforeAllCallback {
    
    @Override
    public void beforeAll(ExtensionContext context) {
        // Ultra-fast mock timings for tests
        FrameworkSettings.mockTimeFindFirst = 0.005;
        FrameworkSettings.mockTimeFindAll = 0.01;
        FrameworkSettings.mockTimeClick = 0.005;
        FrameworkSettings.mockTimeMove = 0.005;
        FrameworkSettings.mockTimeDrag = 0.01;
    }
}
```

### Test Execution Strategies

#### Running Specific Test Categories

```bash
# Run only fast integration tests
./gradlew test --tests "*FastIntegrationTest*"

# Run integration tests with optimized settings
./gradlew integrationTest

# Run tests in parallel with custom fork settings
./gradlew test -PmaxParallelForks=4 -PforkEvery=10
```

#### CI/CD Optimization

For CI/CD pipelines, use environment-specific configurations:

```java
protected double getTimeoutMultiplier() {
    if (isCI()) return 2.0;           // Double timeout in CI
    if (isHeadlessEnvironment()) return 1.5;  // 1.5x in headless
    return 1.0;                       // Normal timeout locally
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

4. **Performance Optimization**
   - Use `OptimizedIntegrationTestBase` for shared Spring context
   - Configure parallel test execution via Gradle properties
   - Optimize mock timings for test environments (0.005-0.015s)
   - Fork JVMs strategically to prevent memory issues
   - Enable test result caching for faster re-runs

5. **Continuous Integration**
   - Run integration tests in CI/CD pipelines
   - Use deterministic random seeds for reproducible results
   - Archive test logs and reports for analysis
   - Configure environment-specific timeouts and retries




---
sidebar_position: 2
title: 'Mock Mode Guide'
---

# Mock Mode Guide

## Understanding Mock Mode in Brobot

Brobot's mock mode provides a powerful testing framework that simulates GUI automation without requiring actual screen interaction. This is essential for:

- **CI/CD pipelines** where GUI access is unavailable
- **Unit testing** automation logic without GUI dependencies
- **Development** when the target application is unavailable
- **Testing state transitions** and automation flow logic

### Mock Mode vs Headless Mode

**Important Distinction**:
- **Mock Mode** (`brobot.mock`): Simulates actions and pattern matching for testing without real screen interaction
- **Headless Mode** (`brobot.headless`): Indicates no display is available (e.g., server environments, CI/CD)

These are independent settings:
- You can run mock mode with a display (development testing)
- You can run mock mode without a display (CI/CD testing)
- Headless environments typically use mock mode, but mock mode doesn't require headless

## Core Concepts

### What Mock Mode Does

When mock mode is enabled (via `brobot.core.mock=true` property or `MockModeManager.setMockMode(true)`):

1. **No screen capture** - Brobot doesn't capture actual screens
2. **No real pattern matching** - Image patterns aren't matched against real screens
3. **Probabilistic simulation** - States and patterns are "found" based on configured probabilities
4. **State-based testing** - Focus on testing state transitions and automation logic

### State Probabilities

State probabilities determine how often a state's objects (images, regions, etc.) are "found" during mock execution:

- **100% probability**: State objects are always found (deterministic testing)
- **0% probability**: State objects are never found (failure testing)
- **1-99% probability**: Stochastic testing for robustness

## Configuration

### Enabling Mock Mode

#### Property-Based Configuration (Recommended)

Brobot uses simplified mock configuration properties:

```properties
# application.properties
# Enable mock mode (simulated actions)
brobot.mock=true

# Probability of action success (0.0 to 1.0, default 1.0)
brobot.mock.action.success.probability=0.95

# Headless configuration (if no display available)
brobot.headless=false  # Set to true for CI/CD environments
brobot.headless.debug=false  # Enable for headless detection debugging
```

**Note**: The `brobot.headless` property must be explicitly set. Auto-detection has been removed due to reliability issues on Windows systems.

#### Programmatic Configuration

```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

import io.github.jspinak.brobot.config.mock.MockModeManager;

// Enable mock mode globally
MockModeManager.setMockMode(true);

// Check if mock mode is enabled
if (brobotProperties.getCore().isMock()) {
    // Mock-specific logic
}

// Log current mock mode state across all components
MockModeManager.logMockModeState();
```

The `MockModeManager` automatically synchronizes mock mode across:
- System properties (`brobot.mock`)
- `ExecutionEnvironment` (for runtime behavior)
- `brobotProperties.getCore().isMock()` (for compatibility)

### Action Success Probability

The `brobot.mock.action.success.probability` property controls how often simulated actions succeed:

- **1.0** (default): All actions always succeed - ideal for deterministic testing
- **0.95**: 95% success rate - simulates realistic conditions
- **0.5**: 50% success rate - stress testing
- **0.0**: All actions always fail - failure path testing

This applies to actions like click, type, and drag. Find operations still require ActionSnapshots for proper match simulation.

### Setting State Probabilities

There are two approaches to configure state probabilities:

#### 1. State-Level Configuration (Recommended)

Configure probabilities directly in state classes:

```java
@State(initial = true)
@Getter
@Slf4j
public class LoginState {
    
    @Autowired(required = false)
    private MockStateManagement mockStateManagement;
    
    // Define mock probability for this state
    private static final int MOCK_PROBABILITY = 100;
    
    private final StateImage loginButton;
    
    public LoginState() {
        loginButton = new StateImage.Builder()
            .addPatterns("login-button")
            .setName("LoginButton")
            .build();
    }
    
    @PostConstruct
    public void configureMockProbability() {
        if (brobotProperties.getCore().isMock() && mockStateManagement != null) {
            mockStateManagement.setStateProbabilities(MOCK_PROBABILITY, "Login");
            log.debug("Configured Login state mock probability to {}%", MOCK_PROBABILITY);
        }
    }
}
```

#### 2. Centralized Configuration

Use `MockStateManagement` to configure multiple states:

```java
@Configuration
@ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
public class MockConfiguration {
    
    @Autowired
    private MockStateManagement mockStateManagement;
    
    @PostConstruct
    public void configureMockStates() {
        // Set initial state probabilities
        mockStateManagement.setStateProbabilities(100, "Login");    // Always found
        mockStateManagement.setStateProbabilities(0, "Dashboard");   // Initially not found
        mockStateManagement.setStateProbabilities(50, "ErrorDialog"); // Sometimes found
    }
}
```

## Testing State Transitions

### Deterministic Flow Testing

For testing automation flow (like claude-automator), use 100% probabilities to ensure reliable transitions:

```java
@State(initial = true)  // Mark as initial state
public class PromptState {
    private static final int MOCK_PROBABILITY = 100;  // Always found
    // ... state definition
}

@State  // Not initial - activated through transition
public class WorkingState {
    private static final int MOCK_PROBABILITY = 100;  // Always found when active
    // ... state definition
}
```

This ensures:
- Transitions always succeed when triggered
- Focus on testing the automation logic, not robustness
- Predictable test outcomes

### Stochastic Testing

For robustness testing, use variable probabilities:

```java
// Simulate unreliable GUI element detection
mockStateManagement.setStateProbabilities(70, "UnstableDialog");

// Test retry logic
for (int i = 0; i < MAX_RETRIES; i++) {
    ActionResult result = action.find(dialogElement);
    if (result.isSuccess()) break;
    Thread.sleep(1000);
}
```

## Dynamic State Simulation

### Simulating State Changes

Adjust probabilities during test execution to simulate state transitions:

```java
@Test
public void testLoginFlow() {
    // Initial state: Login visible, Dashboard not
    mockStateManagement.setStateProbabilities(100, "Login");
    mockStateManagement.setStateProbabilities(0, "Dashboard");
    
    // Perform login action
    stateNavigator.openState("Dashboard");
    
    // Simulate successful login: Dashboard appears, Login disappears
    mockStateManagement.setStateProbabilities(0, "Login");
    mockStateManagement.setStateProbabilities(100, "Dashboard");
    
    // Verify transition
    assertTrue(stateMemory.getActiveStateNames().contains("Dashboard"));
}
```

### Simulating Temporal Behaviors

```java
public void simulateLoadingSequence() {
    // Loading appears
    mockStateManagement.setStateProbabilities(100, "LoadingSpinner");
    
    // Simulate loading time
    Thread.sleep(2000);
    
    // Loading disappears, content appears
    mockStateManagement.setStateProbabilities(0, "LoadingSpinner");
    mockStateManagement.setStateProbabilities(100, "ContentLoaded");
}
```

## Best Practices

### 1. Use 100% Probability for Flow Testing

When testing automation logic (not robustness):

```java
// All states should be reliably findable
private static final int MOCK_PROBABILITY = 100;
```

### 2. Set Initial States Appropriately

```java
@State(initial = true)  // Only the starting state(s)
public class InitialState { }

@State  // Subsequent states reached through transitions
public class SubsequentState { }
```

### 3. Document Mock Behavior

```java
/**
 * Login state - always visible at application start.
 * Mock probability: 100% (deterministic for flow testing)
 */
@State(initial = true)
public class LoginState { }
```

### 4. Separate Mock Configuration

Keep mock-specific configuration separate:

```properties
# application.properties
brobot.core.mock=false  # Production

# application-test.properties
brobot.core.mock=true   # Testing
```

### 5. Clean State Between Tests

```java
@AfterEach
public void cleanup() {
    stateMemory.getActiveStates().clear();
    // Reset probabilities if needed
}
```

## Testing Patterns

### Pattern 1: Simple Flow Test

```java
@Test
public void testBasicFlow() {
    // All states 100% for deterministic testing
    mockStateManagement.setStateProbabilities(100, "Start", "Middle", "End");
    
    // Test the flow
    assertTrue(stateNavigator.openState("Middle"));
    assertTrue(stateNavigator.openState("End"));
}
```

### Pattern 2: Error Recovery Test

```java
@Test
public void testErrorRecovery() {
    // Normal states always found
    mockStateManagement.setStateProbabilities(100, "Normal");
    // Error appears intermittently
    mockStateManagement.setStateProbabilities(30, "Error");
    
    // Test should handle both cases
    ActionResult result = action.find(element);
    if (!result.isSuccess()) {
        // Handle error case
        handleError();
    }
}
```

### Pattern 3: State Verification

```java
@Test
public void verifyStateConfiguration() {
    // Verify initial states
    var initialStates = stateService.getInitialStates();
    assertEquals(1, initialStates.size());
    assertEquals("Login", initialStates.get(0).getName());
    
    // Verify all states registered
    assertTrue(stateService.getAllStates().stream()
        .anyMatch(s -> s.getName().equals("Dashboard")));
}
```

## Debugging Mock Tests

### Enable Verbose Logging

```properties
logging.level.io.github.jspinak.brobot.tools.testing.mock=DEBUG
logging.level.com.yourapp.states=DEBUG
```

### Log State Transitions

```java
@EventListener
public void handleStateTransition(StateTransitionEvent event) {
    log.info("Transition: {} -> {}", 
        event.getFromState(), event.getToState());
}
```

### Verify Mock Configuration

```java
@Test
public void verifyMockSetup() {
    assertTrue(brobotProperties.getCore().isMock(), "Mock mode should be enabled");
    assertNotNull(mockStateManagement, "MockStateManagement should be available");
}
```

## Common Issues and Solutions

### Issue: States Not Being Found

**Solution**: Verify probability is set > 0:

```java
mockStateManagement.setStateProbabilities(100, "StateName");
```

### Issue: Wrong Initial State

**Solution**: Ensure only one state has `initial = true`:

```java
@State(initial = true)  // Only one state should have this
```

### Issue: Transitions Not Working

**Solution**: Check state registration and transition definitions. Use the unified transition format:

```java
@TransitionSet(state = TargetState.class)
@Component
public class TargetStateTransitions {
    
    @FromTransition(from = SourceState.class, priority = 1)
    public boolean fromSource() {
        if (brobotProperties.getCore().isMock()) return true;
        return action.click(sourceState.getButton()).isSuccess();
    }
    
    @IncomingTransition(required = true)
    public boolean verifyArrival() {
        if (brobotProperties.getCore().isMock()) return true;
        return action.find(targetState.getElement()).isSuccess();
    }
}
```

### Issue: Mock Mode Not Activating

**Solution**: Verify configuration:

```properties
brobot.core.mock=true
```

## Enhanced Mock Infrastructure

### Grid Operations Support

Mock mode now provides full support for grid operations without requiring SikuliX:

```java
import io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig;

@Test
public void testGridNavigation() {
    // Configure grid for testing
    MockGridConfig.setDefaultGrid(3, 3); // 3x3 grid
    
    Region screen = new Region(0, 0, 900, 900);
    
    // Test grid navigation
    for (int i = 0; i < 9; i++) {
        Region gridCell = screen.getGridRegion(i);
        assertNotNull(gridCell);
        assertEquals(300, gridCell.w());
        assertEquals(300, gridCell.h());
    }
    
    // Test location to grid mapping
    Location centerOfTopRight = new Location(750, 150);
    Optional<Integer> gridNum = RegionUtils.getGridNumber(screen, centerOfTopRight);
    assertTrue(gridNum.isPresent());
    assertEquals(2, gridNum.get()); // Top-right is index 2 in 3x3 grid
}
```

### Scene and Color Analysis Mocking

The `MockSceneBuilder` provides comprehensive test data for color-based operations:

```java
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;

@Test
public void testColorMatching() {
    // Create mock scene with proper image initialization
    Scene scene = MockSceneBuilder.createMockScene();
    
    // Create scene analysis with color profiles
    SceneAnalysis analysis = MockSceneBuilder.sceneAnalysis()
        .withPixelProfile(0)
        .withPixelProfile(1)
        .build();
    
    // Color operations work in mock mode
    ColorClassifier classifier = new ColorClassifier();
    Mat result = classifier.getImageIndices(analysis, ColorCluster.ColorSchemaName.BGR);
    assertNotNull(result);
}
```

### Test Base Class - BrobotTestBase

All tests should extend `BrobotTestBase` for proper mock configuration:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public class MyAutomationTest extends BrobotTestBase {
    
    @Test
    public void testWithMockMode() {
        // Mock mode is automatically enabled
        // No headless exceptions
        // Fast execution times
        
        ActionResult result = action.find(targetElement);
        assertTrue(result.isSuccess());
    }
}
```

Key benefits of `BrobotTestBase`:
- Automatic mock mode activation
- CI/CD pipeline compatibility
- Fast operation timings (0.01-0.04s)
- No display server required
- Consistent test environment

## Performance Benefits

Mock mode operations are significantly faster:

| Operation | Mock Mode | Real Mode | Speedup |
|-----------|-----------|-----------|---------|
| Find operation | ~0.01s | 0.5-2s | 50-200x |
| Grid calculation | ~0.01s | 0.5s | 50x |
| Color analysis | ~0.02s | 1-2s | 50-100x |
| State transition | ~0.01s | 0.2-1s | 20-100x |

## Common Pitfalls and Solutions

| Issue | Solution |
|-------|----------|
| Tests fail in CI/CD | Extend `BrobotTestBase` |
| Grid operations return empty | Configure `MockGridConfig.setDefaultGrid()` |
| Color tests throw NPE | Use `MockSceneBuilder` for test data |
| SikuliX mocking errors | Use real SikuliX objects or Brobot mocks |
| Inconsistent test results | Ensure mock mode is enabled in setup |

## Summary

Mock mode in Brobot enables:

- **Deterministic testing** with 100% probabilities for flow validation
- **Stochastic testing** with variable probabilities for robustness
- **CI/CD integration** without GUI dependencies
- **Rapid development** without target application availability
- **Grid operations** without SikuliX dependency
- **Color analysis** without real images
- **50-200x faster** test execution

Choose probability settings based on your testing goals:
- **100%** for automation flow testing
- **Variable** for robustness and error handling
- **Dynamic** for simulating complex scenarios

Use the enhanced mock infrastructure for:
- **Grid-based navigation** testing
- **Color matching** validation
- **Headless environment** compatibility
- **Fast CI/CD** pipeline execution
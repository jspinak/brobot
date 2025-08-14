---
sidebar_position: 4
title: 'Mixed-Mode Execution'
---

# Mixed-Mode Execution Guide

## Overview

Mixed-mode execution allows Brobot applications to dynamically switch between mock and live implementations within a single session. This hybrid approach combines the benefits of profile-based architecture with the flexibility of runtime delegation.

## Architecture

### Three-Layer Approach

```
┌─────────────────────────────────────┐
│         Application Layer            │
│    (Uses TextTyper interface)        │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│        Hybrid Layer                  │
│   (HybridTextTyper wrapper)         │
│  ┌─────────────┬─────────────┐      │
│  │Profile-Based│Runtime-Based│      │
│  └─────────────┴─────────────┘      │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│     Implementation Layer             │
│ ┌──────────┐ ┌──────────┐           │
│ │MockTyper │ │LiveTyper │           │
│ └──────────┘ └──────────┘           │
└─────────────────────────────────────┘
```

### Component Structure

1. **Interface**: Defines the contract (e.g., `TextTyper`)
2. **Profile Implementations**: Mock and Live versions with `@Profile` annotations
3. **Hybrid Wrapper**: Bridges profile-based and runtime architectures
4. **Legacy Wrapper**: Original runtime-check implementation for backward compatibility

## Enabling Mixed-Mode

### Configuration

Enable mixed-mode execution in your application properties:

```properties
# application.properties
brobot.hybrid.enabled=true
```

### Programmatic Activation

```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.hybrid.enabled=true"
})
public class MixedModeTest {
    
    @Autowired
    private HybridExecutionConfiguration.HybridComponentConfigurer configurer;
    
    @Test
    public void testDynamicSwitching() {
        // Start in mock mode
        configurer.switchAllToMock();
        performMockOperations();
        
        // Switch to live mode
        configurer.switchAllToLive();
        performLiveOperations();
        
        // Back to mock
        configurer.switchAllToMock();
        verifyMockResults();
    }
}
```

## Use Cases

### 1. Hybrid Testing Scenarios

Test workflows that require both mock and live components:

```java
@Test
public void testLoginWithRealUIAndMockBackend() {
    // Use live mode for UI interaction
    hybridExecutor.switchToLive();
    action.click(loginButton);
    action.type(username);
    
    // Switch to mock for backend calls
    hybridExecutor.switchToMock();
    backend.simulateSuccessfulLogin();
    
    // Back to live for verification
    hybridExecutor.switchToLive();
    assertTrue(action.find(dashboardElement).isSuccess());
}
```

### 2. Performance Testing

Compare mock vs. live execution times:

```java
@Test
public void benchmarkOperations() {
    long mockTime, liveTime;
    
    // Measure mock performance
    configurer.switchAllToMock();
    long start = System.currentTimeMillis();
    performComplexWorkflow();
    mockTime = System.currentTimeMillis() - start;
    
    // Measure live performance
    configurer.switchAllToLive();
    start = System.currentTimeMillis();
    performComplexWorkflow();
    liveTime = System.currentTimeMillis() - start;
    
    log.info("Mock time: {}ms, Live time: {}ms", mockTime, liveTime);
}
```

### 3. Gradual Live Integration

Start with mostly mocked components, gradually enable live ones:

```java
public class GradualIntegrationTest {
    
    @Test
    public void testWithProgressiveLiveComponents() {
        // Phase 1: All mock
        setAllMock();
        assertTrue(basicWorkflow());
        
        // Phase 2: Live clicking only
        setAllMock();
        clickExecutor.switchToLive();
        assertTrue(clickWorkflow());
        
        // Phase 3: Live clicking and typing
        setAllMock();
        clickExecutor.switchToLive();
        typeExecutor.switchToLive();
        assertTrue(interactiveWorkflow());
        
        // Phase 4: Full live
        setAllLive();
        assertTrue(completeWorkflow());
    }
}
```

### 4. Debug Mode

Switch to mock when errors occur:

```java
public class DebugAwareAutomation {
    
    public void executeWithFallback() {
        try {
            // Attempt live execution
            hybridExecutor.switchToLive();
            performAction();
        } catch (AutomationException e) {
            log.warn("Live execution failed, switching to mock", e);
            
            // Fallback to mock mode for debugging
            hybridExecutor.switchToMock();
            performAction(); // This should succeed
            
            // Log the issue for investigation
            debugLogger.logFailure(e);
        }
    }
}
```

## Implementation Example

### Creating a Hybrid Component

Here's how to implement a hybrid component from scratch:

```java
// 1. Define the interface
public interface ClickExecutor {
    boolean click(Location location);
}

// 2. Create mock implementation
@Component
@Profile("test")
public class MockClickExecutor implements ClickExecutor {
    public boolean click(Location location) {
        log.info("Mock click at {}", location);
        pause(FrameworkSettings.mockTimeClick);
        return true;
    }
}

// 3. Create live implementation
@Component
@Profile("!test")
public class LiveClickExecutor implements ClickExecutor {
    @Autowired
    private Mouse mouse;
    
    public boolean click(Location location) {
        mouse.move(location);
        return mouse.click();
    }
}

// 4. Create hybrid wrapper
@Component
@Primary
public class HybridClickExecutor implements ClickExecutor {
    
    @Autowired(required = false)
    private MockClickExecutor mockExecutor;
    
    @Autowired(required = false)
    private LiveClickExecutor liveExecutor;
    
    private boolean useLegacyMode = false;
    
    public boolean click(Location location) {
        // Profile-based selection
        if (!useLegacyMode) {
            if (FrameworkSettings.mock && mockExecutor != null) {
                return mockExecutor.click(location);
            }
            if (!FrameworkSettings.mock && liveExecutor != null) {
                return liveExecutor.click(location);
            }
        }
        
        // Runtime-based fallback
        if (FrameworkSettings.mock) {
            return mockClick(location);
        } else {
            return liveClick(location);
        }
    }
    
    public void enableMixedMode() {
        this.useLegacyMode = true;
    }
}
```

## Configuration Properties

### Hybrid Mode Settings

```properties
# Enable hybrid execution mode
brobot.hybrid.enabled=true

# Allow runtime mode switching (default: false)
brobot.hybrid.allow-runtime-switch=true

# Log mode switches (default: true)
brobot.hybrid.log-switches=true

# Default mode when hybrid is enabled (mock|live)
brobot.hybrid.default-mode=mock
```

### Component-Specific Settings

```properties
# Enable hybrid mode for specific components
brobot.hybrid.text-typer.enabled=true
brobot.hybrid.click-executor.enabled=true
brobot.hybrid.scene-provider.enabled=false

# Component-specific default modes
brobot.hybrid.text-typer.default=mock
brobot.hybrid.click-executor.default=live
```

## Best Practices

### 1. Clear Mode Boundaries

Document when and why mode switches occur:

```java
public void hybridWorkflow() {
    // === MOCK MODE: Setup test data ===
    switchToMock();
    setupTestStates();
    
    // === LIVE MODE: Interact with real UI ===
    switchToLive();
    interactWithUI();
    
    // === MOCK MODE: Verify without side effects ===
    switchToMock();
    verifyResults();
}
```

### 2. Mode Assertion

Verify expected mode before critical operations:

```java
private void assertMockMode() {
    if (!FrameworkSettings.mock) {
        throw new IllegalStateException("Expected mock mode but was live");
    }
}

private void assertLiveMode() {
    if (FrameworkSettings.mock) {
        throw new IllegalStateException("Expected live mode but was mock");
    }
}
```

### 3. Scoped Mode Changes

Use try-finally to ensure mode restoration:

```java
public void temporaryMockExecution(Runnable action) {
    boolean originalMode = FrameworkSettings.mock;
    try {
        FrameworkSettings.mock = true;
        action.run();
    } finally {
        FrameworkSettings.mock = originalMode;
    }
}
```

### 4. Mode-Aware Logging

Include current mode in log messages:

```java
public void logModeAware(String message) {
    String mode = FrameworkSettings.mock ? "MOCK" : "LIVE";
    log.info("[{}] {}", mode, message);
}
```

## Testing Strategies

### Unit Tests with Mode Switching

```java
@Test
public void testModeTransitions() {
    // Test mock → live transition
    executor.switchToMock();
    ActionResult mockResult = executor.execute();
    assertTrue(mockResult.isSuccess());
    
    executor.switchToLive();
    ActionResult liveResult = executor.execute();
    assertTrue(liveResult.isSuccess());
    
    // Verify different behaviors
    assertNotEquals(mockResult.getDuration(), liveResult.getDuration());
}
```

### Integration Tests

```java
@SpringBootTest
@ActiveProfiles("integration")
@EnableHybridMode
public class HybridIntegrationTest {
    
    @Test
    public void testMixedModeWorkflow() {
        // Start with known mock state
        resetToMockBaseline();
        
        // Perform live interactions
        switchToLive();
        performUserInteractions();
        
        // Verify with mock to avoid side effects
        switchToMock();
        verifyExpectedOutcome();
    }
}
```

### Parameterized Tests

```java
@ParameterizedTest
@ValueSource(booleans = {true, false})
public void testInBothModes(boolean mockMode) {
    FrameworkSettings.mock = mockMode;
    
    ActionResult result = executor.execute();
    
    assertTrue(result.isSuccess());
    if (mockMode) {
        assertEquals(0.01, result.getDuration(), 0.001);
    } else {
        assertTrue(result.getDuration() > 0.1);
    }
}
```

## Troubleshooting

### Mode Not Switching

```java
// Check if hybrid mode is enabled
if (!environment.getProperty("brobot.hybrid.enabled", Boolean.class, false)) {
    log.error("Hybrid mode not enabled!");
}

// Verify component supports hybrid
if (!(executor instanceof HybridTextTyper)) {
    log.error("Component doesn't support hybrid execution");
}
```

### Unexpected Behavior After Switch

```java
// Add mode verification
@BeforeEach
public void verifyInitialMode() {
    log.info("Initial mode: {}", FrameworkSettings.mock ? "MOCK" : "LIVE");
    log.info("Hybrid enabled: {}", hybridConfig.isEnabled());
}

@AfterEach
public void logFinalMode() {
    log.info("Final mode: {}", FrameworkSettings.mock ? "MOCK" : "LIVE");
}
```

### Performance Issues

```java
// Monitor mode switch overhead
public class ModeSwitchMonitor {
    
    public void measureSwitchOverhead() {
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            configurer.switchAllToMock();
            configurer.switchAllToLive();
        }
        long elapsed = System.nanoTime() - start;
        log.info("Average switch time: {}ns", elapsed / 2000);
    }
}
```

## Migration Path

### Phase 1: Enable Hybrid Mode
```properties
brobot.hybrid.enabled=true
```

### Phase 2: Identify Switch Points
```java
// Mark where mode switches are needed
// TODO: Switch to mock here
// TODO: Switch to live here
```

### Phase 3: Implement Hybrid Components
Replace runtime checks with hybrid wrappers

### Phase 4: Test Mixed Scenarios
Verify both modes work correctly

### Phase 5: Optimize Switching
Minimize unnecessary mode changes

## Summary

Mixed-mode execution provides:
- **Flexibility**: Switch between mock and live at runtime
- **Gradual Migration**: Move from runtime to profile-based architecture
- **Enhanced Testing**: Combine mock and live components in tests
- **Debugging Support**: Fallback to mock when live fails
- **Performance Analysis**: Compare mock vs. live execution

Use mixed-mode when you need dynamic control over execution behavior, especially during testing, debugging, or gradual system integration.
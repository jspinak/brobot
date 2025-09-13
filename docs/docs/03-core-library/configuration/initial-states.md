# Initial State Configuration Guide

## Overview

Brobot provides automatic initial state management through the `@State(initial = true)` annotation. This guide explains how to configure and use initial states in your Brobot applications.

## Quick Start

### 1. Mark Your Initial State

```java
@State(initial = true)
@Getter
@Slf4j
public class HomeState {
    private StateImage logo = new StateImage.Builder()
        .addPatterns("home-logo")
        .build();
}
```

That's it! The framework will automatically:
- Detect the initial state annotation
- Create startup configuration
- Activate the state when the application starts

## Configuration Properties

All initial state behavior can be configured via `application.properties`:

```properties
# Enable/disable automatic activation (default: true)
brobot.startup.auto-activate=true

# Enable/disable verification (default: true)
brobot.startup.verify=true

# Seconds to wait before verification in real mode (default: 5)
# This allows the GUI to stabilize before searching for states
brobot.startup.initial-delay=5

# Additional startup delay (default: 1)
brobot.startup.delay=1

# Search all states if initial states not found (default: false)
brobot.startup.fallback-search=false

# Only activate the first found state (default: true)
# Set to false to activate all found initial states
brobot.startup.activate-first-only=true
```

## Advanced Features

### Priority-Based Selection

When multiple initial states exist, use priority to influence selection probability:

```java
@State(initial = true, priority = 200)  // Higher priority
public class LoginState { }

@State(initial = true, priority = 100)  // Default priority
public class HomeState { }

@State(initial = true, priority = 50)   // Lower priority
public class DashboardState { }
```

In mock mode, states with higher priority are more likely to be selected.

### Profile-Specific Initial States

Different initial states for different environments:

```java
// Only initial in test profile
@State(initial = true, profiles = {"test"})
public class TestLoginState { }

// Initial in production and staging
@State(initial = true, profiles = {"production", "staging"})
public class MainMenuState { }

// Initial in all profiles (default)
@State(initial = true)
public class DefaultState { }
```

### Test Profile Optimization

The framework automatically optimizes settings for the test profile:

```java
// When running with --spring.profiles.active=test
// These settings are applied automatically:
// - No startup delays (immediate activation)
// - Deterministic behavior (activate first only)
// - No fallback search
```

## How It Works

### In Mock Mode

1. Initial states are registered with their priorities
2. A weighted random selection chooses one state set
3. Selected states are immediately activated
4. No screen verification needed

### In Real Mode

1. Initial states are registered
2. Application waits for `initial-delay` seconds (GUI stabilization)
3. Framework searches for registered initial states on screen
4. Found states are activated in StateMemory
5. If no states found and `fallback-search=true`, searches all states

## Monitoring Initial States

### Logging

The framework provides detailed logging of initial state activation:

```
INFO  Auto-configuring BrobotStartupConfiguration from @State(initial = true) annotations
INFO  Found 1 initial states from annotations: [Home]
INFO  ════════════════════════════════════════════════════════
INFO    AUTO-ACTIVATING INITIAL STATES
INFO  ════════════════════════════════════════════════════════
INFO  Waiting 5 seconds before initial state verification (real mode)
INFO  Searching for initial states: [Home]
INFO  ✅ Successfully activated initial states: [Home]
```

### Programmatic Access

Check initial states programmatically:

```java
@Autowired
private InitialStates initialStates;

@Autowired
private StateMemory stateMemory;

// Get registered initial states
List<String> registered = initialStates.getRegisteredInitialStates();

// Check if any are registered
boolean hasInitial = initialStates.hasRegisteredInitialStates();

// Get currently active states
Set<String> active = stateMemory.getActiveStateNames();
```

## Troubleshooting

### Initial State Not Activating

1. **Check annotation**: Ensure `@State(initial = true)` is present
2. **Check profile**: Verify profile matches if using `profiles` attribute
3. **Check timing**: Increase `initial-delay` if GUI needs more time
4. **Check logs**: Look for "AUTO-ACTIVATING INITIAL STATES" messages
5. **Check property**: Ensure `brobot.startup.auto-activate=true`

### Wrong State Activated

1. **Check priorities**: Higher priority states are selected more often
2. **Check profiles**: Ensure correct profile is active
3. **Use deterministic mode**: Set `activate-first-only=true`

### No States Found (Real Mode)

1. **Increase delay**: Set higher `initial-delay` value
2. **Enable fallback**: Set `fallback-search=true`
3. **Check images**: Verify pattern images exist and are correct
4. **Check similarity**: Adjust pattern matching thresholds

## Best Practices

1. **One Initial State**: Keep it simple with a single initial state when possible
2. **Use Profiles**: Different initial states for test vs production
3. **Set Appropriate Delays**: Allow enough time for GUI to stabilize
4. **Monitor Logs**: Enable INFO logging for troubleshooting
5. **Test Both Modes**: Verify behavior in both mock and real modes

## Example: Complete Application

```java
// HomeState.java
@State(initial = true, priority = 100)
@Getter
@Slf4j
public class HomeState {
    private StateImage logo = new StateImage.Builder()
        .addPatterns("home-logo")
        .setName("HomeLogo")
        .build();
}

// Application.java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.myapp",
    "io.github.jspinak.brobot"
})
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

```properties
# application.properties
brobot.startup.auto-activate=true
brobot.startup.initial-delay=3
brobot.startup.activate-first-only=true
brobot.mock=false
```

With this configuration, the HomeState will automatically be found and activated 3 seconds after application startup.
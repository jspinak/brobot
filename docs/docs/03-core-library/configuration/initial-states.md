# Initial State Configuration Guide

## Overview

> **New to Brobot states?** See the [States Guide](../../01-getting-started/states.md) for fundamental concepts.

Brobot provides automatic initial state management through the [`@State(initial = true)` annotation](../guides/user-guides/annotations.md). This guide explains how to configure and use initial states in your Brobot applications.

For architecture and implementation details, see the [Initial State Handling Architecture](../architecture/initial-state-handling.md) guide.

## Quick Start

### 1. Mark Your Initial State

```java
// File: src/main/java/com/myapp/states/HomeState.java
package com.myapp.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

All initial state behavior can be configured via `application.properties`. For comprehensive property documentation, see the [Configuration Properties Reference](./properties-reference.md).

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

# Only activate the first found state (default: false)
# Set to true to activate only the first matched state
brobot.startup.activate-first-only=false

# Enable mock mode for testing (default: false)
brobot.mock=false
```

## Advanced Features

### Priority-Based Selection

When multiple initial states exist, use priority to influence selection probability **in mock mode only**:

```java
// File: src/main/java/com/myapp/states/LoginState.java
package com.myapp.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

@State(initial = true, priority = 200)  // Higher priority - most likely in mock mode
@Getter
public class LoginState {
    private StateImage loginButton = new StateImage.Builder()
        .addPatterns("login-button")
        .build();
}

// File: src/main/java/com/myapp/states/HomeState.java
@State(initial = true, priority = 100)  // Default priority
@Getter
public class HomeState {
    private StateImage homeLogo = new StateImage.Builder()
        .addPatterns("home-logo")
        .build();
}

// File: src/main/java/com/myapp/states/DashboardState.java
@State(initial = true, priority = 50)   // Lower priority - least likely in mock mode
@Getter
public class DashboardState {
    private StateImage dashboardHeader = new StateImage.Builder()
        .addPatterns("dashboard-header")
        .build();
}
```

:::info Priority Behavior
**Mock Mode:** States with higher priority are more likely to be selected using weighted random selection.

**Real Mode:** Priority is **ignored**. States are activated based on which ones are found on screen, regardless of priority values.
:::

### Profile-Specific Initial States

Different initial states for different environments. For comprehensive profile-based patterns, see the [Profile-Based Testing Guide](../../04-testing/profile-based-testing.md).

```java
// File: src/main/java/com/myapp/states/TestLoginState.java
package com.myapp.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

// Only initial in test profile
@State(initial = true, profiles = {"test"})
@Getter
public class TestLoginState {
    private StateImage mockLoginButton = new StateImage.Builder()
        .addPatterns("test-login-button")
        .build();
}

// File: src/main/java/com/myapp/states/MainMenuState.java
// Initial in production and staging
@State(initial = true, profiles = {"production", "staging"})
@Getter
public class MainMenuState {
    private StateImage mainMenu = new StateImage.Builder()
        .addPatterns("main-menu")
        .build();
}

// File: src/main/java/com/myapp/states/DefaultState.java
// Initial in all profiles (default)
@State(initial = true)
@Getter
public class DefaultState {
    private StateImage defaultLogo = new StateImage.Builder()
        .addPatterns("default-logo")
        .build();
}
```

### Test Profile Optimization

The framework provides optimized settings for the test profile when `spring.profiles.active=test` is set:

```java
// When running with --spring.profiles.active=test
// These settings are applied by TestInitialStateConfiguration:
// - No startup delays (startup-delay=0 for immediate activation)
// - Deterministic behavior (activate-first-only=true)
// - No fallback search (fallback-search=false)
```

:::tip Test Mode Detection
The framework detects test mode through:
- Spring profile: `spring.profiles.active=test`
- System property: `brobot.test.type=unit`
- System property: `brobot.test.mode=true`

These optimizations only apply if no custom `StartupConfiguration` bean is provided.
:::

## How It Works

### In Mock Mode

For comprehensive mock mode documentation, see the [Mock Mode Guide](../../04-testing/mock-mode-guide.md).

1. Initial states are registered with their priorities
2. A weighted random selection chooses one state set (higher priority = more likely)
3. Selected states are immediately activated
4. No screen verification needed

### In Real Mode

1. Initial states are registered
2. Application waits for `initial-delay` seconds (GUI stabilization)
3. Framework searches for registered initial states on screen
4. Found states are activated in StateMemory
5. **Automatic fallback**: If no initial states found, automatically searches all states (regardless of `fallback-search` property)

:::warning Fallback Search Behavior
The `brobot.startup.fallback-search` property controls fallback behavior in the manual verification system (`InitialStateVerifier`). The automatic activation system (`InitialStates`) **always** performs fallback search when no initial states are found on screen.
:::

## Monitoring Initial States

### Logging

The framework provides detailed logging of initial state activation. For comprehensive logging configuration, see the [Logging Configuration Guide](../../07-logging/configuration.md).

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

:::tip Logging Configuration
Enable DEBUG logging for detailed startup information:
```properties
logging.level.io.github.jspinak.brobot.startup=DEBUG
logging.level.io.github.jspinak.brobot.statemanagement=DEBUG
```
:::

### Programmatic Access

Check initial states programmatically:

```java
// File: src/main/java/com/myapp/services/StateMonitor.java
package com.myapp.services;

import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StateMonitor {

    private final InitialStates initialStates;
    private final StateMemory stateMemory;

    public void logInitialStates() {
        // Get registered initial states
        List<String> registered = initialStates.getRegisteredInitialStates();

        // Check if any are registered
        boolean hasInitial = initialStates.hasRegisteredInitialStates();

        // Get currently active states (returns List, not Set)
        List<String> active = stateMemory.getActiveStateNames();

        log.info("Registered initial states: {}", registered);
        log.info("Has initial states: {}", hasInitial);
        log.info("Currently active states: {}", active);
    }
}
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
// File: src/main/java/com/myapp/states/HomeState.java
package com.myapp.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true, priority = 100)
@Getter
@Slf4j
public class HomeState {
    private StateImage logo = new StateImage.Builder()
        .addPatterns("home-logo")
        .setName("HomeLogo")
        .build();
}

// File: src/main/java/com/myapp/Application.java
package com.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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
# File: src/main/resources/application.properties
brobot.startup.auto-activate=true
brobot.startup.initial-delay=3
brobot.startup.activate-first-only=false
brobot.mock=false
```

With this configuration, the HomeState will automatically be found and activated 3 seconds after application startup.

## See Also

### Core Documentation
- [States Guide](../../01-getting-started/states.md) - Introduction to Brobot state management
- [@State Annotation Reference](../guides/user-guides/annotations.md) - Complete annotation documentation
- [Initial State Handling Architecture](../architecture/initial-state-handling.md) - Internal implementation details

### Configuration
- [Configuration Properties Reference](./properties-reference.md) - All available properties
- [Auto-Configuration Guide](./auto-configuration.md) - Spring Boot integration patterns
- [BrobotProperties Usage Guide](./brobot-properties-usage.md) - Using properties in code

### Testing
- [Profile-Based Testing](../../04-testing/profile-based-testing.md) - Testing with Spring profiles
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing without GUI interaction
- [Integration Testing](../../04-testing/integration-testing.md) - Initial state testing patterns

### Logging and Monitoring
- [Logging Configuration](../../07-logging/configuration.md) - Comprehensive logging setup
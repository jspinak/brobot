---
sidebar_position: 2
title: 'Initial State Handling Architecture'
description: 'Internal architecture of automatic initial state detection and activation'
keywords: [initial states, architecture, ApplicationReadyEvent, auto-configuration, startup]
---

# Initial State Handling Architecture

> 🏗️ **Architecture Document** - This explains internal implementation details.
> For usage instructions, see the [Initial State Configuration Guide](../configuration/initial-states.md).

## Quick Links

- **[User Guide: Initial State Configuration](../configuration/initial-states.md)** - How to use initial states
- **[@State Annotation Reference](../user-guides/annotations.md#state-annotation)** - Annotation parameters
- **[Configuration Properties](../configuration/properties-reference.md#startup-configuration)** - Property reference
- **[Auto-Configuration Overview](../configuration/auto-configuration.md)** - Spring Boot integration
- **[States Overview](../../01-getting-started/states.md)** - Introduction to states

## Implementation Status

**✅ Fully Implemented in v1.1.0**

Brobot provides complete automatic initial state handling. States marked with `@State(initial = true)` are automatically detected, configured, and activated at application startup.

## Architecture Overview

### Complete Automatic Flow

The initial state system operates in four phases during application startup:

```
┌─────────────────────────────────────────────────────────────┐
│                   Application Startup                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│ Phase 1: DETECTION                                           │
│ AnnotationProcessor scans @State(initial=true) annotations  │
│ - Checks initial() flag                                      │
│ - Evaluates profiles() against active Spring profiles       │
│ - Extracts priority() for weighted selection                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│ Phase 2: REGISTRATION                                        │
│ InitialStates bean collects detected states                 │
│ - Adds states to potentialActiveStates map                  │
│ - Stores priority values for each state                     │
│ - Profile-aware registration                                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│ Phase 3: CONFIGURATION                                       │
│ InitialStateAutoConfiguration creates StartupConfiguration  │
│ - Reads properties (brobot.startup.*)                       │
│ - Creates configuration bean                                │
│ - Sets verification behavior and delays                     │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│ Phase 4: ACTIVATION (ApplicationReadyEvent)                 │
│ Auto-activation listener triggers                           │
│ - Applies initial-delay (real mode only, not in mock mode)  │
│ - Calls initialStates.findInitialStates()                   │
│ - Updates StateMemory with active states                    │
│ - Logs activation results                                   │
└──────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. @State Annotation

The `@State` annotation supports initial state marking with profile and priority control:

```java
import io.github.jspinak.brobot.annotations.State;

@State(
    initial = true,                        // Mark as initial state
    priority = 100,                        // Optional: weighted selection (default: 100)
    profiles = {"production", "staging"}   // Optional: profile-specific (default: all profiles)
)
public class HomePageState {
    // State implementation
}
```

**Parameters:**
- `initial()` - When true, state is candidate for auto-activation (default: false)
- `priority()` - Higher priority states are preferred (default: 100)
- `profiles()` - Only activated when Spring profile matches (default: empty = all profiles)

**Verification:** `/brobot/library/src/main/java/io/github/jspinak/brobot/annotations/State.java:70,96,107`

### 2. AnnotationProcessor (Detection Phase)

Scans and processes `@State` annotations during Spring context initialization:

```java
// Located at: io.github.jspinak.brobot.annotations.AnnotationProcessor

@Component
public class AnnotationProcessor {

    public void processStates() {
        // Scan all @State beans
        Map<String, Object> stateBeans = StateBeanPostProcessor.getStateBeans();

        for (Map.Entry<String, Object> entry : stateBeans.entrySet()) {
            State stateAnnotation = entry.getValue().getClass()
                .getAnnotation(State.class);

            // Check if marked as initial
            if (stateAnnotation.initial()) {
                // Verify profile matches
                if (isProfileActive(stateAnnotation.profiles())) {
                    int priority = stateAnnotation.priority();
                    String stateName = extractStateName(entry.getKey());

                    // Register with InitialStates bean
                    initialStates.addStateSet(priority, stateName);
                    initialStatePriorities.put(stateName, priority);
                }
            }
        }
    }
}
```

**Verification:** `/brobot/library/src/main/java/io/github/jspinak/brobot/annotations/AnnotationProcessor.java:192-302`

### 3. InitialStates Bean (Registration Phase)

Collects and manages initial state candidates:

```java
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InitialStates {

    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final Map<Set<Long>, Integer> potentialActiveStates;

    /**
     * Returns list of state names marked with @State(initial = true).
     * Used by InitialStateAutoConfiguration to build StartupConfiguration.
     */
    public List<String> getRegisteredInitialStates() {
        return potentialActiveStates.keySet().stream()
            .flatMap(Set::stream)
            .map(allStatesInProjectService::getState)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(io.github.jspinak.brobot.model.state.State::getName)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Checks if any initial states have been registered.
     */
    public boolean hasRegisteredInitialStates() {
        return !potentialActiveStates.isEmpty();
    }

    /**
     * Activates initial states based on priority and configuration.
     * Called automatically by InitialStateAutoConfiguration.
     */
    public void findInitialStates() {
        // Implementation: finds and activates initial states
        // Updates StateMemory with active states
    }
}
```

**Verification:** `/brobot/library/src/main/java/io/github/jspinak/brobot/statemanagement/InitialStates.java:225,335,351`

### 4. InitialStateAutoConfiguration (Configuration & Activation)

Spring Boot auto-configuration that creates startup configuration and triggers activation:

```java
import io.github.jspinak.brobot.startup.state.InitialStateAutoConfiguration;
import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitialStateAutoConfiguration {

    private final InitialStates initialStates;

    /**
     * Creates StartupConfiguration from @State(initial=true) annotations.
     * Only created if user hasn't provided custom configuration.
     */
    @Bean
    @ConditionalOnMissingBean(StartupConfiguration.class)
    public StartupConfiguration brobotStartupConfiguration(
            @Value("${brobot.startup.verify:true}") boolean verify,
            @Value("${brobot.startup.initial-delay:5}") int initialDelay,
            @Value("${brobot.startup.delay:1}") int delay,
            @Value("${brobot.startup.fallback-search:false}") boolean fallbackSearch,
            @Value("${brobot.startup.activate-first-only:true}") boolean activateFirstOnly) {

        log.info("Creating automatic StartupConfiguration from @State annotations");

        StartupConfiguration config = new StartupConfiguration();

        // Set verification based on whether initial states exist
        config.setVerifyInitialStates(
            verify && initialStates.hasRegisteredInitialStates()
        );

        // Set delays
        config.setStartupDelay(delay);
        // Note: initial-delay is handled separately in activation logic

        // Set behavior
        config.setFallbackSearch(fallbackSearch);
        config.setActivateFirstOnly(activateFirstOnly);

        // Add registered initial states
        List<String> registeredStates = initialStates.getRegisteredInitialStates();
        config.getInitialStates().addAll(registeredStates);

        log.info("Registered {} initial states: {}",
            registeredStates.size(), registeredStates);

        return config;
    }

    /**
     * Automatically activates initial states when application is ready.
     * Listens for ApplicationReadyEvent from Spring Boot.
     */
    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnProperty(
        value = "brobot.startup.auto-activate",
        havingValue = "true",
        matchIfMissing = true  // Enabled by default
    )
    public void autoActivateInitialStates(ApplicationReadyEvent event) {
        if (initialStates.hasRegisteredInitialStates()) {
            log.info("Auto-activating initial states...");

            // Note: Initial delay is applied internally by InitialStates
            // based on mock mode vs real mode
            initialStates.findInitialStates();

            log.info("Initial state activation complete");
        } else {
            log.debug("No initial states registered, skipping auto-activation");
        }
    }
}
```

**Verification:** `/brobot/library/src/main/java/io/github/jspinak/brobot/startup/state/InitialStateAutoConfiguration.java:56-181`

### 5. StartupConfiguration

Configuration bean that controls startup behavior:

```java
import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brobot.startup")
public class StartupConfiguration {

    /**
     * Enable verification of initial states after activation.
     */
    private boolean verifyInitialStates = true;

    /**
     * Delay in seconds before verification (allows UI to stabilize).
     */
    private int startupDelay = 1;

    /**
     * Search all states if initial states not found.
     */
    private boolean fallbackSearch = false;

    /**
     * Only activate first found initial state (deterministic behavior).
     */
    private boolean activateFirstOnly = true;

    /**
     * List of initial state names (from @State annotations or manual config).
     */
    private List<String> initialStates = new ArrayList<>();
}
```

**Verification:** `/brobot/library/src/main/java/io/github/jspinak/brobot/startup/orchestration/StartupConfiguration.java:37-52`

## Configuration Properties

### Available Properties

```yaml
brobot:
  startup:
    # Auto-activation control
    auto-activate: true              # Automatically activate initial states (default: true)

    # Verification settings
    verify-initial-states: true      # Verify states after activation (default: true)
    startup-delay: 1                 # Seconds to wait before verification (default: 1)
    initial-delay: 5                 # Seconds to wait before activation in real mode (default: 5)

    # Activation behavior
    fallback-search: false           # Search all states if initial not found (default: false)
    activate-first-only: true        # Only activate first found state (default: true)
```

**Property Details:**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.startup.auto-activate` | boolean | true | Enable automatic activation on ApplicationReadyEvent |
| `brobot.startup.verify-initial-states` | boolean | true | Verify initial states were activated |
| `brobot.startup.startup-delay` | int | 1 | Seconds to wait before verification |
| `brobot.startup.initial-delay` | int | 5 | Seconds to wait before activation (real mode only) |
| `brobot.startup.fallback-search` | boolean | false | Search all states if initial states not found |
| `brobot.startup.activate-first-only` | boolean | true | Activate only first matching state |

**Verification:** `/brobot/library/src/main/resources/brobot-defaults.properties`

### Environment-Specific Configuration

**Development (verbose, slow):**
```yaml
brobot:
  startup:
    auto-activate: true
    verify-initial-states: true
    initial-delay: 2
    startup-delay: 1
```

**Testing (fast, deterministic):**
```yaml
brobot:
  startup:
    auto-activate: true
    verify-initial-states: true
    initial-delay: 0          # No delay in mock mode
    startup-delay: 0          # No delay in tests
    activate-first-only: true # Deterministic behavior
```

**Production (optimized):**
```yaml
brobot:
  startup:
    auto-activate: true
    verify-initial-states: true
    initial-delay: 5          # Allow UI to fully load
    fallback-search: true     # Recover if initial state not found
```

## Profile-Specific Behavior

### Profile-Aware Initial States

```java
import io.github.jspinak.brobot.annotations.State;
import org.springframework.context.annotation.Profile;

// Only activated in production
@State(
    initial = true,
    priority = 100,
    profiles = {"production"}
)
public class ProductionHomeState {
    // Production-specific implementation
}

// Only activated in test/dev
@State(
    initial = true,
    priority = 100,
    profiles = {"test", "dev"}
)
public class TestHomeState {
    // Test-specific implementation with mock data
}
```

### Test-Optimized Configuration

For fast, deterministic test execution:

```java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestConfiguration
@ConditionalOnProperty(value = "spring.profiles.active", havingValue = "test")
@Order(0) // Higher priority than main configuration
public class TestInitialStateConfiguration {

    @Bean
    public StartupConfiguration testStartupConfiguration(InitialStates initialStates) {
        log.info("Configuring test-optimized initial state handling");

        StartupConfiguration config = new StartupConfiguration();
        config.setVerifyInitialStates(true);
        config.getInitialStates().addAll(initialStates.getRegisteredInitialStates());
        config.setStartupDelay(0);           // No delay in tests
        config.setFallbackSearch(false);     // Deterministic behavior
        config.setActivateFirstOnly(true);   // Activate only first state

        return config;
    }
}
```

## Complete Working Example

### Application Structure

```
src/main/java/
├── states/
│   ├── LoginState.java          # @State(initial=true, priority=100)
│   └── MainMenuState.java       # Regular state
├── Application.java              # Spring Boot application
└── resources/
    └── application.yml           # Configuration
```

### LoginState (Initial State)

```java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

@State(
    initial = true,
    priority = 100,
    profiles = {"production", "dev"}
)
public class LoginState {

    // State images are automatically detected
    public StateImage loginButton = new StateImage.Builder()
        .addPattern("login-button")
        .build();

    public StateImage usernameField = new StateImage.Builder()
        .addPattern("username-field")
        .build();
}
```

### Application Configuration

```yaml
# application.yml
brobot:
  startup:
    auto-activate: true              # ✓ Enabled by default
    verify-initial-states: true      # ✓ Verify activation
    initial-delay: 3                 # Wait 3s before activation

spring:
  profiles:
    active: dev                      # Activates LoginState (dev profile)
```

### Application Startup

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        // Initial state automatically activated by framework
        // No manual initialization needed!
    }
}
```

**What Happens:**
1. Spring Boot starts
2. `AnnotationProcessor` scans and finds `LoginState` with `@State(initial=true)`
3. Checks profile matches ("dev" is active, "dev" in profiles list ✓)
4. `InitialStates` registers LoginState with priority 100
5. `InitialStateAutoConfiguration` creates `StartupConfiguration`
6. `ApplicationReadyEvent` fires
7. Auto-activation listener waits 3 seconds (initial-delay)
8. Calls `initialStates.findInitialStates()`
9. LoginState activated in `StateMemory`
10. Application is ready with initial state active ✓

## Execution Timeline

### Real Mode Timeline

```
T+0ms    : Application start
T+100ms  : Spring context initialization
T+150ms  : @State annotations scanned
T+200ms  : InitialStates bean populated
T+250ms  : InitialStateAutoConfiguration bean created
T+300ms  : ApplicationReadyEvent fired
T+300ms  : Auto-activation listener triggered
T+5000ms : Initial-delay complete (default 5s in real mode)
T+5000ms : initialStates.findInitialStates() called
T+5100ms : Initial state found and activated
T+6100ms : Verification delay (startup-delay, default 1s)
T+6100ms : Verification complete
T+6100ms : ✓ Application ready with active initial state
```

### Mock/Test Mode Timeline

```
T+0ms    : Application start
T+100ms  : Spring context initialization
T+150ms  : @State annotations scanned
T+200ms  : InitialStates bean populated
T+250ms  : InitialStateAutoConfiguration bean created
T+300ms  : ApplicationReadyEvent fired
T+300ms  : Auto-activation listener triggered
T+300ms  : Initial-delay skipped (mock mode)
T+300ms  : initialStates.findInitialStates() called
T+310ms  : Initial state activated (mock, instant)
T+310ms  : Verification skipped (test mode)
T+310ms  : ✓ Application ready with active initial state
```

**Speed Difference:** Real mode ~6 seconds vs Mock mode ~0.3 seconds

## Advanced Topics

### Priority-Based Selection

When multiple initial states are registered:

```java
@State(initial = true, priority = 200)  // Higher priority
public class PreferredHomeState { }

@State(initial = true, priority = 100)  // Lower priority
public class FallbackHomeState { }
```

**Behavior:**
- Framework tries `PreferredHomeState` first (priority 200)
- If not found, tries `FallbackHomeState` (priority 100)
- If `activate-first-only: true`, stops after first success

### Manual Override

Disable auto-activation and control manually:

```yaml
brobot:
  startup:
    auto-activate: false  # Disable automatic activation
```

```java
import io.github.jspinak.brobot.statemanagement.InitialStates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ManualInitialStateActivation implements CommandLineRunner {

    @Autowired
    private InitialStates initialStates;

    @Override
    public void run(String... args) throws Exception {
        // Custom initialization logic
        System.out.println("Performing custom initialization...");

        // Manually trigger when ready
        initialStates.findInitialStates();

        System.out.println("Initial states activated manually");
    }
}
```

### Custom StartupConfiguration

Provide custom configuration bean:

```java
import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class CustomStartupConfig {

    @Bean
    public StartupConfiguration customStartupConfiguration() {
        StartupConfiguration config = new StartupConfiguration();

        // Override all settings
        config.setVerifyInitialStates(false);        // Skip verification
        config.setStartupDelay(0);                   // No delay
        config.setActivateFirstOnly(false);          // Try all states

        // Manually specify initial states (ignores @State annotations)
        config.getInitialStates().addAll(Arrays.asList(
            "CustomInitialState",
            "BackupInitialState"
        ));

        return config;
    }
}
```

**Note:** Providing a `StartupConfiguration` bean disables the automatic configuration due to `@ConditionalOnMissingBean`.

## Troubleshooting

### Initial States Not Activating

**Symptoms:** Application starts but no initial state is active.

**Checklist:**

1. **Verify annotation is correct:**
   ```java
   @State(initial = true)  // Correct
   // Not: @State(initial = false)
   ```

2. **Check auto-activate property:**
   ```yaml
   brobot.startup.auto-activate: true  # Must be true (default)
   ```

3. **Verify profile matches:**
   ```java
   @State(initial = true, profiles = {"production"})
   // Only activates when production profile is active
   ```

4. **Check logs for activation:**
   ```
   [INFO] Creating automatic StartupConfiguration from @State annotations
   [INFO] Registered 1 initial states: [LoginState]
   [INFO] Auto-activating initial states...
   [INFO] Initial state activation complete
   ```

5. **Verify state images exist:**
   - Images must be in `images/login-state/` directory
   - Image filenames must match pattern names

### Multiple Initial States

**Problem:** Multiple states marked `initial = true`, behavior unclear.

**Solution:** Use priorities:
```java
@State(initial = true, priority = 200)  // Tried first
@State(initial = true, priority = 100)  // Tried second
```

Or use profiles:
```java
@State(initial = true, profiles = {"production"})
@State(initial = true, profiles = {"test"})
```

### Slow Startup in Real Mode

**Problem:** Application takes 5+ seconds to start in real mode.

**Solution:** Reduce initial-delay for development:
```yaml
# application-dev.yml
brobot:
  startup:
    initial-delay: 1  # Reduce from default 5 seconds
```

**Note:** Mock mode has zero delay by default.

## Benefits of This Implementation

1. **Zero Configuration**: Applications work with just `@State(initial = true)`
2. **Full Control**: Every aspect configurable via properties
3. **Profile Aware**: Different behavior for test/dev/production
4. **Type Safe**: Compile-time checking with annotations
5. **Testable**: Fast, deterministic behavior in test mode
6. **Observable**: Clear logging of initialization process
7. **Extensible**: Can override with custom configuration
8. **Backward Compatible**: Existing manual configuration still works

## Related Documentation

- **[User Guide: Initial State Configuration](../configuration/initial-states.md)** - How to use initial states
- **[@State Annotation Reference](../user-guides/annotations.md#state-annotation)** - Complete annotation documentation
- **[Configuration Properties Reference](../configuration/properties-reference.md#startup-configuration)** - All available properties
- **[Auto-Configuration Guide](../configuration/auto-configuration.md)** - Other auto-configuration features
- **[States Overview](../../01-getting-started/states.md)** - Introduction to state management
- **[Testing Initial States](../../04-testing/integration-testing.md#initial-state-testing)** - Testing strategies

## Summary

The Brobot initial state system provides complete automatic handling from detection through activation:

1. **Detection:** `AnnotationProcessor` scans `@State(initial=true)` annotations
2. **Registration:** `InitialStates` bean collects candidates with priorities
3. **Configuration:** `InitialStateAutoConfiguration` creates `StartupConfiguration`
4. **Activation:** `ApplicationReadyEvent` listener triggers `findInitialStates()`

This architecture enables truly zero-configuration applications while maintaining full flexibility for advanced scenarios through properties and custom configuration beans.

# Spring Configuration and Wiring

## Overview

The configuration class brings everything together, registering states and transitions with the Brobot framework.

## Initial State Verification (v1.1.0+)

Brobot now provides automatic initial state verification through configuration:

### Configuration Properties

```properties
# Enable automatic verification on startup
brobot.startup.verify-initial-states = true

# States to verify (comma-separated)
brobot.startup.initial-states = PROMPT,WORKING

# Search all states if specified ones not found
brobot.startup.fallback-search = false

# Activate only the first found state
brobot.startup.activate-first-only = false

# Delay before verification (seconds)
brobot.startup.startup-delay = 2
```

### How It Works

1. **Automatic Execution**: When enabled, verification runs automatically after Spring context initialization
2. **State Search**: Searches for configured states on the actual screen
3. **State Activation**: Found states are automatically activated in StateMemory
4. **Mock Support**: In mock mode, randomly selects states based on equal probability

### Benefits

- **No Boilerplate**: No need to manually set initial states in code
- **Configuration-Driven**: Change behavior without recompiling
- **Consistent**: Same approach across all Brobot applications
- **Testable**: Works seamlessly with mock mode

## StateRegistrationListener.java (Recommended Approach)

```java
package com.claude.automator.config;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import com.claude.automator.transitions.PromptTransitions;
import com.claude.automator.transitions.WorkingTransitions;
import io.github.jspinak.brobot.config.FrameworkInitializer;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Registers states and transitions after the application is fully ready.
 * This ensures the Brobot framework is initialized before states try to load images.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateRegistrationListener {

    private final StateService stateService;
    private final StateTransitionStore stateTransitionStore;
    private final StateMemory stateMemory;
    private final FrameworkInitializer frameworkInitializer;
    private final WorkingState workingState;
    private final PromptState promptState;
    private final WorkingTransitions workingTransitions;
    private final PromptTransitions promptTransitions;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready - registering states with StateService");
        
        try {
            // Register states with the StateService
            stateService.save(workingState.getState());
            stateService.save(promptState.getState());
            
            // Register state transitions
            stateTransitionStore.add(workingTransitions.getStateTransitions());
            stateTransitionStore.add(promptTransitions.getStateTransitions());
            
            log.info("States and transitions registered successfully");
            
            // Important: Initialize the state structure after ALL states are registered
            // This will properly set up the transitions joint table with all state IDs
            // The first call in FrameworkLifecycleManager only had the Unknown state
            frameworkInitializer.initializeStateStructure();
            
            log.info("State structure initialized with transitions joint table");
            
            // Set initial active state - using Prompt as starting state
            Long promptStateId = stateService.getStateId(PromptState.Name.PROMPT.toString());
            if (promptStateId != null) {
                stateMemory.addActiveState(promptStateId);
                log.info("Set PROMPT as initial active state with ID: {}", promptStateId);
            }
        } catch (Exception e) {
            log.error("Error registering states: ", e);
        }
    }
}
```

## Key Concepts

### 1. Why Use @EventListener Instead of @PostConstruct

The recommended approach uses `@EventListener(ApplicationReadyEvent.class)` instead of `@PostConstruct` for several important reasons:

- **Framework Initialization Timing**: Ensures Brobot framework is fully initialized before states try to load images
- **Dependency Resolution**: All Spring beans are completely wired and ready
- **Error Prevention**: Avoids initialization order issues that can occur with @PostConstruct
- **Better Integration**: Works seamlessly with Spring Boot's lifecycle

### 2. Critical Framework Initialization

```java
// This MUST be called after ALL states are registered
frameworkInitializer.initializeStateStructure();
```

This second call to `initializeStateStructure()` is crucial because:
- The first call during framework startup only knows about the Unknown state
- This call properly sets up the transitions joint table with all registered state IDs
- Without this, state transitions may not work correctly

### 3. Registration Order

1. **States first**: Must exist before transitions reference them
2. **Transitions second**: Need state IDs to function
3. **Framework structure initialization**: Sets up internal mappings
4. **Initial state last**: Requires states to be registered

## Alternative Configuration Patterns

### Bean-Based Configuration

```java
@Configuration
public class BeanConfiguration {
    
    @Bean
    public StateTransitions promptTransitions(PromptState promptState, Action action) {
        return new StateTransitions.Builder(PromptState.Name.PROMPT.toString())
                // ... build transitions
                .build();
    }
}
```

### Conditional Configuration

```java
@Configuration
@ConditionalOnProperty(name = "claude.automator.enabled", havingValue = "true")
public class ConditionalStateConfiguration {
    // Only loads when property is set
}
```

### Profile-Based Configuration

```java
@Configuration
@Profile("claude-automation")
public class ClaudeAutomationConfiguration {
    // Active only in specific profile
}
```

## Error Handling Strategies

### Validation on Startup

```java
@PostConstruct
public void validateConfiguration() {
    // Verify all states are registered
    if (stateService.getStateId(WorkingState.Name.WORKING.toString()) == null) {
        throw new IllegalStateException("Working state not found!");
    }
    
    // Verify transitions exist
    if (stateTransitionStore.getTransitions(WorkingState.Name.WORKING.toString()) == null) {
        throw new IllegalStateException("Working transitions not found!");
    }
}
```

### Graceful Degradation

```java
private void setInitialState() {
    try {
        Long stateId = stateService.getStateId(PromptState.Name.PROMPT.toString());
        if (stateId != null) {
            stateMemory.addActiveState(stateId);
        }
    } catch (Exception e) {
        log.warn("Could not set initial state, continuing with defaults", e);
    }
}
```

## Configuration Best Practices

1. **Use constructor injection** (via @RequiredArgsConstructor)
2. **Log important configuration steps**
3. **Validate critical components**
4. **Handle failures gracefully**
5. **Keep configuration focused** on wiring, not business logic

## Additional Configuration Files

### Custom Properties

Create `src/main/resources/claude-automator.properties`:

```properties
# Claude Automator Settings
claude.automator.monitor.interval=2
claude.automator.monitor.initial-delay=5
claude.automator.icon.timeout=10.0
```

### Loading Custom Properties

```java
@ConfigurationProperties(prefix = "claude.automator")
@Component
@Getter
@Setter
public class ClaudeAutomatorProperties {
    private MonitorProperties monitor = new MonitorProperties();
    private IconProperties icon = new IconProperties();
    
    @Getter
    @Setter
    public static class MonitorProperties {
        private int interval = 2;
        private int initialDelay = 5;
    }
    
    @Getter
    @Setter
    public static class IconProperties {
        private double timeout = 10.0;
    }
}
```

## Next Steps

With everything configured, let's run the application and see it in action!
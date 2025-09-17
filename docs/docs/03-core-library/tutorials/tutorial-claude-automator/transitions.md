# Implementing Transitions with @TransitionSet

## Overview

Transitions define how to navigate between states. With the new @TransitionSet annotation system (Brobot 1.2.0+), all transitions for a state are grouped together in a single class, providing better organization and clearer intent.

## Modern Approach: Unified Transition Classes

### PromptTransitions.java

```java
// Note: BrobotProperties must be injected as a dependency
@Autowired
private BrobotProperties brobotProperties;

package com.claude.automator.transitions;

import org.springframework.stereotype.Component;
import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Prompt state.
 * Contains FromTransitions from other states TO Prompt,
 * and a IncomingTransition to verify arrival at Prompt.
 */
@TransitionSet(state = PromptState.class, description = "Claude Prompt state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {
    
    private final PromptState promptState;
    private final WorkingState workingState;
    private final Action action;
    
    /**
     * Navigate from Working state back to Prompt.
     * This occurs when Claude finishes processing and returns to the prompt.
     */
    @FromTransition(from = WorkingState.class, priority = 1, description = "Return from Working to Prompt")
    public boolean fromWorking() {
        log.info("Navigating from Working to Prompt");
        
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        // Wait for Claude to finish processing and return to prompt
        // This might involve waiting for the working indicator to disappear
        return action.find(promptState.getClaudePrompt()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Prompt state.
     * Checks for the presence of the Claude prompt input area.
     */
    @IncomingTransition(description = "Verify arrival at Prompt state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Prompt state");
        
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of prompt-specific elements
        boolean foundPrompt = action.find(promptState.getClaudePrompt()).isSuccess();
        
        if (foundPrompt) {
            log.info("Successfully confirmed Prompt state is active");
            return true;
        } else {
            log.error("Failed to confirm Prompt state - prompt elements not found");
            return false;
        }
    }
}
```

### WorkingTransitions.java

```java
package com.claude.automator.transitions;

import org.springframework.stereotype.Component;
import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Working state.
 * Contains FromTransitions from other states TO Working,
 * and a IncomingTransition to verify arrival at Working.
 */
@TransitionSet(state = WorkingState.class, description = "Claude Working state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {
    
    private final PromptState promptState;
    private final WorkingState workingState;
    private final Action action;
    
    /**
     * Navigate from Prompt to Working by submitting a command.
     * This transition occurs when the user submits a prompt and Claude begins processing.
     */
    @FromTransition(from = PromptState.class, priority = 1, description = "Submit prompt to start working")
    public boolean fromPrompt() {
        try {
            log.info("Navigating from Prompt to Working");
            
            // In mock mode, just return true for testing
            if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
                log.info("Mock mode: simulating successful navigation");
                return true;
            }

            // Using the fluent API to chain actions: find -> click -> type
            PatternFindOptions findClickType = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause before clicking
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before typing
                            .build())
                    .then(new TypeOptions.Builder()
                            .build())
                    .build();

            // Create target objects for the chained action
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt()) // For find & click
                    .withStrings(promptState.getContinueCommand()) // For type (continue with Enter)
                    .build();

            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);

            if (result.isSuccess()) {
                log.info("Successfully triggered transition from Prompt to Working");
                return true;
            } else {
                log.warn("Failed to execute transition: {}", result.getActionDescription());
                return false;
            }

        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            return false;
        }
    }
    
    /**
     * Verify that we have successfully arrived at the Working state.
     * Checks for the presence of the working indicator.
     */
    @IncomingTransition(description = "Verify arrival at Working state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Working state");
        
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of working-specific elements
        boolean foundWorkingIndicator = action.find(workingState.getWorkingIndicator()).isSuccess();
        
        if (foundWorkingIndicator) {
            log.info("Successfully confirmed Working state is active");
            return true;
        } else {
            log.error("Failed to confirm Working state - working indicator not found");
            return false;
        }
    }
}
```

## Key Features of @TransitionSet

### 1. **Unified Class Structure**
All transitions for a state are in ONE class:
- `@FromTransition` methods define how to get TO this state FROM other states
- `@IncomingTransition` method (only ONE per class) verifies arrival at this state

### 2. **Clear Annotations**

```java
@TransitionSet(state = TargetState.class, description = "Documentation")
```
- **state**: The state these transitions belong to (required)
- **description**: Optional documentation

```java
@FromTransition(from = SourceState.class, priority = 1, description = "Navigation logic")
```
- **from**: The source state (required)
- **priority**: Higher values are preferred when multiple paths exist
- **description**: Optional documentation

```java
@IncomingTransition(description = "Verification logic", required = true)
```
- **required**: Whether verification must succeed (default: false)
- **description**: Optional documentation

### 3. **Mock Mode Support**
Always include mock mode checks for testing:

```java
@FromTransition(from = SourceState.class)
public boolean fromSource() {
    if (io.github.jspinak.brobot.config.core.brobotProperties.getCore().isMock()) {
        log.info("Mock mode: simulating successful navigation");
        return true;
    }
    // Real navigation logic
    return action.click(element).isSuccess();
}
```

## Action Chaining Pattern

The fluent API enables elegant action sequences:

```java
// Chain multiple actions in sequence
PatternFindOptions chainedAction = new PatternFindOptions.Builder()
    .setPauseAfterEnd(0.5)              // Wait after finding
    .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)       // Wait after clicking
            .build())
    .then(new TypeOptions.Builder()
            .build())                     // Type text
    .build();

// Execute all actions in sequence
ActionResult result = action.perform(chainedAction, target);
```

## Comparison: Old vs New

### Old Approach (Pre-1.2.0)
Multiple separate transition classes:
```java
// Separate file for each transition
@Transition(from = PromptState.class, to = WorkingState.class)
public class PromptToWorkingTransition {
    public boolean execute() {
        return action.click(promptState.getButton()).isSuccess();
    }
}

// Another separate file
@Transition(from = WorkingState.class, to = PromptState.class)
public class WorkingToPromptTransition {
    public boolean execute() {
        return action.wait(5).isSuccess();
    }
}
```

### New Approach (1.2.0+)
All transitions for a state in ONE class:
```java
@TransitionSet(state = WorkingState.class)
@Component
public class WorkingTransitions {
    
    @FromTransition(from = PromptState.class, priority = 1)
    public boolean fromPrompt() {
        if (brobotProperties.getCore().isMock()) return true;
        return action.click(promptState.getButton()).isSuccess();
    }
    
    @IncomingTransition(required = true)
    public boolean verifyArrival() {
        if (brobotProperties.getCore().isMock()) return true;
        return action.find(workingState.getIndicator()).isSuccess();
    }
}
```

## File Organization

Organize your transitions alongside states:

```
src/main/java/com/claude/automator/
├── states/
│   ├── PromptState.java
│   └── WorkingState.java
└── transitions/
    ├── PromptTransitions.java    # All transitions for Prompt state
    └── WorkingTransitions.java   # All transitions for Working state
```

## Best Practices

1. **Use Required Annotations**:
   ```java
   @TransitionSet(state = MyState.class)
   @Component                    // For Spring dependency injection
   @RequiredArgsConstructor      // For constructor injection
   @Slf4j                       // For logging
   ```

2. **Descriptive Method Names**: Use `fromStateName()` pattern for clarity

3. **Mock Mode Support**: Always include mock mode checks for testing

4. **Handle Failures Gracefully**:
   ```java
   @FromTransition(from = SourceState.class)
   public boolean fromSource() {
       try {
           // Transition logic
           return action.click("button").isSuccess();
       } catch (Exception e) {
           log.error("Transition failed", e);
           return false;
       }
   }
   ```

5. **Log Appropriately**: Info for success, warn for expected failures, error for exceptions

## Benefits of @TransitionSet Approach

1. **Better Organization**: All transitions for a state in ONE place
2. **Clearer Intent**: FromTransitions vs IncomingTransition makes flow obvious
3. **Less Boilerplate**: No manual StateTransitions builders
4. **Compile-Time Safety**: IDE immediately shows if states don't exist
5. **Easier Testing**: Each transition method can be tested independently
6. **Natural Structure**: File organization mirrors state structure

## Testing Transitions

The new format makes testing straightforward:

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class WorkingTransitionsTest {
    
    @Autowired
    private WorkingTransitions workingTransitions;
    
    @MockBean
    private Action action;
    
    @Test
    public void testFromPromptTransition() {
        // Given
        when(action.perform(any(), any()))
            .thenReturn(new ActionResult.Builder().setSuccess(true).build());
        
        // When
        boolean result = workingTransitions.fromPrompt();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    public void testVerifyArrival() {
        // Given
        when(action.find(any()))
            .thenReturn(new ActionResult.Builder().setSuccess(true).build());
        
        // When
        boolean arrived = workingTransitions.verifyArrival();
        
        // Then
        assertTrue(arrived);
    }
}
```

## Next Steps

With states and transitions defined using @TransitionSet annotations, the entire state machine is automatically configured. The framework handles all registration and wiring - you just focus on your automation logic!
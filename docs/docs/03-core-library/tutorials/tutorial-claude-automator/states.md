# Creating States with Annotations

## Overview

The Claude automator demonstrates the new annotation-based approach for defining states. This significantly simplifies state configuration by eliminating manual registration and boilerplate code.

## Working State

The Working state represents the screen where Claude is actively responding:

### WorkingState.java

```java
package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        log.info("Creating WorkingState");
        
        // Create the claude icon images
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .build();
        
        log.info("WorkingState created successfully");
    }
}
```

### Key Features:

1. **@State Annotation**: Automatically registers the state with Brobot
2. **No Manual Registration**: No need for StateRegistrationListener
3. **Direct Component Access**: The `claudeIcon` is stored as a field with a getter
4. **Multiple Image Patterns**: Uses `addPatterns()` to handle UI variations
5. **Simplified Structure**: No need for State.Builder or StateEnum

## Prompt State

The Prompt state represents the screen where we can enter prompts:

### PromptState.java

```java
package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    
    private final StateImage claudePrompt;
    private final StateString continueCommand;
    
    public PromptState() {
        log.info("Creating PromptState");
        
        // Initialize the claude prompt image
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1",
                        "prompt/claude-prompt-2",
                        "prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .build();
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
        
        log.info("PromptState created successfully");
    }
}
```

### Key Features:

1. **Initial State Marking**: `@State(initial = true)` designates this as the starting state
2. **Automatic Framework Integration**: Spring and Brobot automatically discover and register the state
3. **Mixed State Components**: Combines StateImage (for visual elements) and StateString (for text input)
4. **Clean API**: Direct access to both `claudePrompt` and `continueCommand`
5. **No Boilerplate**: No need for State objects or manual registration

## Comparison: Before and After

### Before (Manual Registration):
```java
// StateRegistrationListener.java - 67 lines of boilerplate
@Component
@RequiredArgsConstructor
@Slf4j
public class StateRegistrationListener {
    private final StateService stateService;
    private final StateTransitionStore stateTransitionStore;
    // ... lots of manual registration code
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        stateService.save(workingState.getState());
        stateService.save(promptState.getState());
        // ... more registration
    }
}
```

### After (With Annotations):
```java
// No registration needed! Just add @State annotation
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    // State definition
}
```

## Benefits of Annotation Approach

1. **Zero Configuration**: States are automatically discovered and registered
2. **Less Code**: Eliminate 60+ lines of registration boilerplate
3. **Clearer Intent**: `@State(initial = true)` clearly marks starting states
4. **Better Separation**: State definition and registration are unified
5. **Type Safety**: Compile-time checking of state relationships

## Best Practices

1. **Always use @Getter** - Lombok generates getters for state components
2. **Always use @Slf4j** - Provides consistent logging
3. **Mark initial states** - Use `@State(initial = true)` for entry points
4. **Keep states focused** - Each state represents one UI screen
5. **Use descriptive names** - Component names should be self-documenting

## Required Annotations

When using the annotation system, include these annotations on your state classes:

```java
@State              // Brobot state registration
@Getter             // Lombok getter generation
@Slf4j              // Lombok logging
public class MyState {
    // State implementation
}
```

## Next Steps

With our states defined using annotations, we'll implement transitions that also use the annotation system for automatic registration and simplified configuration.
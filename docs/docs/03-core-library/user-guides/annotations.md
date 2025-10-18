---
sidebar_position: 20
title: 'Annotations API'
---

# Annotations

Brobot provides a powerful annotation system that simplifies state and transition configuration by using declarative annotations instead of manual registration code.

> 📖 **See Also**: [States Guide](../../../01-getting-started/states.md) for foundational concepts | [Transitions Guide](../../../01-getting-started/transitions.md) for transition fundamentals

## Overview

The annotation system introduces key annotations:
- **`@State`** - Marks a class as a Brobot state
- **`@TransitionSet`** - Marks a class as containing all transitions for a specific state
- **`@IncomingTransition`** - Marks a method that verifies arrival at the state
- **`@OutgoingTransition`** - Marks a method that navigates FROM the state TO another state

These annotations work with Spring's component scanning to automatically discover and register your states and transitions at application startup.

## Quick Start

Here's a minimal complete example showing the annotation system in action:

```java
// Required imports
import io.github.jspinak.brobot.annotations.*;
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Define a state
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    private final StateImage submitButton = new StateImage.Builder()
        .addPattern("submit")
        .build();
}

// Define state for destination
@State
@Getter
@Slf4j
public class WorkingState {
    private final StateImage spinner = new StateImage.Builder()
        .addPattern("loading")
        .build();
}

// Define transitions FROM PromptState
@TransitionSet(state = PromptState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {
    private final Action action;
    private final PromptState promptState;

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(promptState.getSubmitButton()).isSuccess();
    }

    @OutgoingTransition(activate = {WorkingState.class})
    public boolean toWorking() {
        return action.click(promptState.getSubmitButton()).isSuccess();
    }
}
```

**Result**: States are automatically registered, transitions are wired, and your state machine is ready to use!

## Benefits

### Before (Manual Registration)
```java
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
@EventListener(ApplicationReadyEvent.class)
public class StateRegistrationListener {
    private final StateService stateService;
    private final ActionPipeline action;
    private final StateTransitionsJointTable jointTable;
    // ... 67 lines of manual registration code
}
```

### After (With Annotations)
```java
// Just annotate your classes - automatic registration!
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    private final StateImage submitButton = new StateImage.Builder()
        .addPattern("submit")
        .build();
}

@TransitionSet(state = PromptState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {
    private final Action action;
    private final PromptState promptState;

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(promptState.getSubmitButton()).isSuccess();
    }

    @OutgoingTransition(activate = {WorkingState.class})
    public boolean toWorking() {
        return action.click(promptState.getSubmitButton()).isSuccess();
    }
}
```

## @State Annotation

The `@State` annotation marks a class as a Brobot state and includes Spring's `@Component` for automatic discovery.

> 📖 **Learn More**: [States Guide](../../../01-getting-started/states.md) explains state concepts in detail

### Basic Usage

```java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State
@Getter
@Slf4j
public class LoginState {
    private StateImage loginButton = new StateImage.Builder()
        .addPattern("login-button")
        .build();

    private StateImage usernameField = new StateImage.Builder()
        .addPattern("username-field")
        .build();

    private StateImage passwordField = new StateImage.Builder()
        .addPattern("password-field")
        .build();
}
```

### Marking Initial States

Use the `initial` parameter to designate starting states:

```java
import io.github.jspinak.brobot.annotations.State;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true)
@Getter
@Slf4j
public class HomeState {
    // State definition
}
```

### Custom State Names

By default, the state name is derived from the class name (removing "State" suffix if present). You can override this:

```java
import io.github.jspinak.brobot.annotations.State;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(name = "Dashboard", description = "Main application dashboard")
@Getter
@Slf4j
public class DashboardState {
    // State definition
}
```

### Path Cost Configuration

Control pathfinding behavior with the `pathCost` parameter:

```java
import io.github.jspinak.brobot.annotations.State;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

// Expensive state to reach - pathfinding avoids unless necessary
@State(pathCost = 10, description = "Error recovery state")
@Getter
@Slf4j
public class ErrorRecoveryState {
    // State definition
}

// Free state - no cost to be in this state
@State(pathCost = 0, description = "Always-visible header")
@Getter
@Slf4j
public class HeaderState {
    // State definition
}
```

> 📖 **See Also**: [Pathfinding and Path Costs Guide](./pathfinding-and-costs.md) for detailed cost strategies

## Transition Annotations

Brobot uses a cohesive annotation pattern where each state's transitions are grouped in one class.

> 📖 **Learn More**: [Transitions Guide](../../../01-getting-started/transitions.md) explains the transition model

### @TransitionSet

Marks a class as containing all transitions for a specific state:

```java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = LoginState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginTransitions {

    private final Action action;
    private final LoginState loginState;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at Login state");
        return action.find(loginState.getUsernameField()).isSuccess();
    }

    @OutgoingTransition(activate = {DashboardState.class}, pathCost = 0)
    public boolean toDashboard() {
        log.info("Transitioning from Login to Dashboard");
        // Perform login actions
        action.type(loginState.getUsernameField(), "user@example.com");
        action.type(loginState.getPasswordField(), "password");
        return action.click(loginState.getLoginButton()).isSuccess();
    }

    @OutgoingTransition(activate = {ForgotPasswordState.class}, pathCost = 2)
    public boolean toForgotPassword() {
        return action.click(loginState.getForgotPasswordLink()).isSuccess();
    }
}
```

### Multi-State Transitions

Handle complex state changes with multiple activations and exits:

```java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = DashboardState.class)
@RequiredArgsConstructor
@Slf4j
public class DashboardTransitions {

    private final Action action;
    private final DashboardState dashboardState;

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(dashboardState.getMainContent()).isSuccess();
    }

    // Complex transition with multiple state changes
    @OutgoingTransition(
        activate = {ReportsState.class, SidebarState.class, FilterPanelState.class},
        exit = {NotificationPanelState.class},
        pathCost = 1
    )
    public boolean toReports() {
        return action.click(dashboardState.getReportsButton()).isSuccess();
    }

    // Modal overlay - keep dashboard visible
    @OutgoingTransition(
        activate = {SettingsModalState.class},
        staysVisible = true,  // Dashboard remains visible
        pathCost = 2
    )
    public boolean openSettings() {
        return action.click(dashboardState.getSettingsIcon()).isSuccess();
    }
}
```

> 📖 **See Also**: [Multi-State Transitions Guide](./multi-state-transitions-guide.md) for complex activation patterns | [Dynamic Transitions Guide](./dynamic-transitions.md) for hidden states and overlays

### @IncomingTransition

Verifies successful arrival at the state:

```java
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.actions.Action;

@IncomingTransition(description = "Verify settings modal is open")
public boolean verifyArrival() {
    return action.find(settingsState.getCloseButton()).isSuccess() &&
           action.find(settingsState.getSettingsTitle()).isSuccess();
}
```

### @OutgoingTransition Parameters

- **`activate`** (required): Target state class(es) to activate - must be array: `activate = {TargetState.class}`
- **`exit`**: States to deactivate during transition
- **`staysVisible`**: Whether originating state remains visible (default: false)
- **`pathCost`**: Path-finding cost - lower costs preferred (default: 1)
- **`description`**: Documentation for the transition

### Dynamic Transitions with Special States

Use special marker classes for dynamic behavior:

```java
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import io.github.jspinak.brobot.model.state.special.CurrentState;

@TransitionSet(state = ModalDialogState.class)
@RequiredArgsConstructor
@Slf4j
public class ModalDialogTransitions {

    private final Action action;
    private final ModalDialogState modalState;

    // Return to whatever state was hidden by the modal
    @OutgoingTransition(
        activate = {PreviousState.class},  // Dynamic - returns to hidden state
        pathCost = 0
    )
    public boolean close() {
        return action.click(modalState.getCloseButton()).isSuccess();
    }

    // Self-transition - stay in current state
    @OutgoingTransition(
        activate = {CurrentState.class},  // Stays in ModalDialog
        pathCost = 2
    )
    public boolean refresh() {
        return action.click(modalState.getRefreshButton()).isSuccess();
    }
}
```

> 📖 **See Also**: [Dynamic Transitions Guide](./dynamic-transitions.md) for `PreviousState`, `CurrentState`, and `ExpectedState` patterns

### Path Cost Strategy

```java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;

@TransitionSet(state = MainMenuState.class)
@RequiredArgsConstructor
public class MainMenuTransitions {

    private final Action action;
    private final MainMenuState mainMenu;

    // Direct navigation - lowest cost (preferred path)
    @OutgoingTransition(
        activate = {SettingsState.class},
        pathCost = 0  // Preferred path
    )
    public boolean directToSettings() {
        return action.click(mainMenu.getSettingsButton()).isSuccess();
    }

    // Indirect navigation - higher cost (fallback path)
    @OutgoingTransition(
        activate = {SettingsState.class, HelpPanelState.class},
        pathCost = 5  // Less preferred, only if direct fails
    )
    public boolean toSettingsViaHelp() {
        action.click(mainMenu.getHelpButton());
        return action.click(helpPanel.getSettingsLink()).isSuccess();
    }
}
```

> 📖 **See Also**: [Pathfinding and Path Costs Guide](./pathfinding-and-costs.md) for comprehensive cost configuration strategies

## Complete Working Example

Here's a complete example from the Tutorial Basics project showing proper annotation usage:

```java
// State definition - Home.java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true)
@Getter
@Slf4j
public class HomeState {
    private final StateImage searchButton = new StateImage.Builder()
        .addPattern("search-button")
        .build();

    private final StateImage worldButton = new StateImage.Builder()
        .addPattern("world-button")
        .build();
}

// State definition - World.java
@State
@Getter
@Slf4j
public class WorldState {
    private final StateImage worldMap = new StateImage.Builder()
        .addPattern("world-map")
        .build();

    private final StateImage homeButton = new StateImage.Builder()
        .addPattern("home-button")
        .build();
}

// Transitions - HomeTransitions.java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = HomeState.class)
@RequiredArgsConstructor
@Slf4j
public class HomeTransitions {

    private final Action action;
    private final HomeState homeState;

    @IncomingTransition(description = "Verify arrival at Home")
    public boolean verifyArrival() {
        log.info("Verifying arrival at Home state");
        return action.find(homeState.getSearchButton()).isSuccess();
    }

    @OutgoingTransition(
        activate = {WorldState.class},
        pathCost = 1,
        description = "Navigate from Home to World"
    )
    public boolean toWorld() {
        log.info("Navigating from Home to World");
        return action.click(homeState.getWorldButton()).isSuccess();
    }
}
```

> 📖 **Try It Yourself**: [Tutorial Basics](../../02-tutorials/tutorial-basics/index.md) provides hands-on practice with annotations

## How It Works

1. **Component Scanning**: Spring automatically discovers all classes annotated with `@State` and `@TransitionSet`
2. **Annotation Processing**: The `AnnotationProcessor` processes these annotations at application startup
3. **State Registration**: States are registered with the framework and initial states are marked
4. **Transition Wiring**: Transitions are created and connected between the appropriate states
5. **Automatic Configuration**: The state machine is fully configured without manual registration code

## Best Practices

### 1. Always Include Required Lombok Annotations

```java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@State
@Getter  // Required for state objects
@Slf4j   // Recommended for logging
public class MyState {
    // State definition
}

@TransitionSet(state = StateA.class)
@RequiredArgsConstructor  // For dependency injection
@Slf4j                   // For logging
public class MyTransitions {
    // Transition logic
}
```

### 2. Use Descriptive Names

While class names are used by default, consider adding descriptions for clarity:

```java
import io.github.jspinak.brobot.annotations.State;
import lombok.Getter;

@State(
    name = "UserProfile",
    description = "User profile page with account settings and preferences"
)
@Getter
public class UserProfileState {
    // State definition
}
```

### 3. Keep Transitions Focused

Each transition class should handle all transitions for ONE specific state:

```java
// Good: Single state, all its transitions
@TransitionSet(state = CartState.class)
public class CartTransitions {
    @OutgoingTransition(activate = {CheckoutState.class})
    public boolean proceedToCheckout() { }

    @OutgoingTransition(activate = {ProductListState.class})
    public boolean continueShopping() { }
}
```

### 4. Handle Errors Gracefully

```java
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = ProcessingState.class)
@Slf4j
public class ProcessingTransitions {

    @OutgoingTransition(activate = {CompleteState.class}, pathCost = 0)
    public boolean toComplete() {
        try {
            // Processing logic
            return processSuccessfully();
        } catch (Exception e) {
            log.error("Processing failed", e);
            return false; // Transition fails gracefully
        }
    }

    @OutgoingTransition(activate = {ErrorState.class}, pathCost = 5)
    public boolean toError() {
        // Error recovery logic
        return true;
    }
}
```

### 5. Use Path Costs Strategically

```java
// Preferred path - low cost
@OutgoingTransition(activate = {TargetState.class}, pathCost = 0)
public boolean fastPath() { }

// Fallback path - higher cost
@OutgoingTransition(activate = {TargetState.class}, pathCost = 10)
public boolean slowPath() { }
```

> 📖 **See Also**: [Pathfinding and Path Costs Guide](./pathfinding-and-costs.md) for detailed cost strategies

## Migration Guide

To migrate existing code to use annotations:

1. **Remove StateRegistrationListener**: Delete manual registration classes
2. **Add @State annotations**: Mark all state classes
3. **Add @TransitionSet annotations**: Mark transition classes and specify which state they belong to
4. **Add @IncomingTransition**: Mark verification methods
5. **Add @OutgoingTransition**: Mark navigation methods with `activate = {TargetState.class}` (array syntax)
6. **Add initial parameter**: Mark starting states with `@State(initial = true)`
7. **Add Lombok annotations**: Include @Getter and @Slf4j as needed
8. **Test the migration**: Verify all states and transitions are discovered

### Common Migration Issues

**Old pattern (doesn't work)**:
```java
// This annotation doesn't exist
@Transition(from = LoginState.class, to = DashboardState.class)
public class LoginToDashboard { }
```

**New pattern (correct)**:
```java
@TransitionSet(state = LoginState.class)
public class LoginTransitions {
    @OutgoingTransition(activate = {DashboardState.class})
    public boolean toDashboard() { }
}
```

**Important**: The `activate` parameter must be an array, even for single states:
- ✅ Correct: `activate = {TargetState.class}`
- ❌ Wrong: `activate = TargetState.class` (compilation error)

## Troubleshooting

### States Not Being Discovered

Ensure your states are in a package scanned by Spring:
- Check your `@ComponentScan` configuration
- Verify `@State` is properly imported: `io.github.jspinak.brobot.annotations.State`
- Confirm the class is public
- Check application logs for "Discovered state: [StateName]" messages

### Transitions Not Working

- Verify the transition method returns `boolean`
- Check that source and target states exist
- Review logs for registration errors
- Ensure `@TransitionSet` includes valid `state` class
- Confirm `activate` parameter is an array: `activate = {TargetState.class}`
- Verify `Action` is properly injected via constructor

### Initial States Not Set

- At least one state must have `@State(initial = true)`
- Check logs for "Marked [StateName] as initial state" messages
- Verify the state class is being discovered
- Ensure the state is in Spring's component scan path

### Compilation Errors with @OutgoingTransition

```
error: cannot find symbol
    to = TargetState.class
    ^
```

**Cause**: The parameter name is `activate`, not `to`.

**Fix**: Use `activate = {TargetState.class}` (array syntax required).

## Experimental Feature: @CollectData Annotation

> ⚠️ **EXPERIMENTAL**: The `@CollectData` annotation is currently in beta. Some features are incomplete and it must be explicitly enabled via `brobot.aspects.dataset.enabled=true`.

The `@CollectData` annotation enables automatic dataset collection for machine learning applications. When applied to methods, it captures inputs, outputs, and execution context for training ML models.

### Basic Usage

```java
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.aspects.annotations.CollectData;
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.actions.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;

@Component
public class SmartAutomation {

    private final Action action;

    public SmartAutomation(Action action) {
        this.action = action;
    }

    @CollectData(category = "click_accuracy")
    public ActionResult performClick(StateImage target) {
        return action.click(target);
    }
}
```

### Configuration

Enable data collection in `application.properties`:

```properties
# Enable dataset collection (disabled by default)
brobot.aspects.dataset.enabled=true

# Optional: Configure storage location
brobot.aspects.dataset.path=./ml-data
```

### Parameters

- **`category`** (String, default: "general") - Category for organizing collected data
- **`features`** (String[], default: {}) - Specific features to collect (empty = all)
- **`captureScreenshots`** (boolean, default: true) - Capture before/after screenshots (⚠️ partially implemented)
- **`captureIntermediateStates`** (boolean, default: false) - Capture multi-step operations
- **`samplingRate`** (double, default: 1.0) - Collection rate (0.0-1.0, where 1.0 = 100%)
- **`maxSamples`** (int, default: -1) - Maximum samples to collect (-1 = unlimited)
- **`onlySuccess`** (boolean, default: false) - Collect only successful executions
- **`includeTiming`** (boolean, default: true) - Include timing information
- **`anonymize`** (boolean, default: true) - Anonymize sensitive data
- **`format`** (DataFormat, default: JSON) - Storage format
- **`labels`** (String[], default: {}) - Labels for supervised learning
- **`compress`** (boolean, default: true) - Compress collected data

### Advanced Examples

```java
import io.github.jspinak.brobot.aspects.annotations.CollectData;
import io.github.jspinak.brobot.aspects.annotations.DataFormat;

// Collect only 10% of executions with specific features
@CollectData(
    category = "text_recognition",
    features = {"image", "location", "confidence"},
    samplingRate = 0.1,
    format = DataFormat.CSV
)
public String extractText(Region region) {
    // Text extraction logic
}

// Collect data for successful operations only
@CollectData(
    category = "form_submission",
    onlySuccess = true,
    captureIntermediateStates = true,
    labels = {"form_type", "submission_time"}
)
public boolean submitForm(FormData data) {
    // Form submission logic
}

// High-volume data collection with limits
@CollectData(
    category = "mouse_movements",
    maxSamples = 10000,
    captureScreenshots = false,  // Save space
    format = DataFormat.BINARY,   // Efficient storage
    compress = true
)
public void trackMouseMovement(Location from, Location to) {
    // Movement tracking logic
}
```

### Data Formats

- **JSON** - Human-readable, good for debugging
- **CSV** - Tabular data, easy to import into analysis tools
- **BINARY** - Efficient storage for large datasets
- **TFRECORD** - TensorFlow native format
- **PARQUET** - Apache Parquet for big data processing

### Known Limitations

- Screenshot capture is partially implemented
- Region feature extraction may be incomplete
- Performance impact not yet optimized for production use
- No built-in data visualization tools

### Use Cases

1. **Training Click Accuracy Models**: Collect data about successful/failed clicks to improve pattern matching
2. **Text Recognition Improvement**: Gather OCR results with ground truth for model training
3. **Workflow Optimization**: Analyze action sequences to identify bottlenecks
4. **Error Pattern Detection**: Collect failure cases to improve error handling
5. **Performance Tuning**: Gather timing data to optimize action execution

## Testing Your Annotated States

Annotated states and transitions work seamlessly with Brobot's testing framework:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.navigation.StateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

public class StateAnnotationTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Autowired
    private StateManager stateManager;

    @Test
    public void testStateDiscovery() {
        // Verify states are automatically registered
        assertTrue(stateManager.stateExists("Home"));
        assertTrue(stateManager.stateExists("World"));
    }

    @Test
    public void testTransitions() {
        // Set initial state
        stateManager.activateState("Home");

        // Navigate using annotated transitions
        boolean success = stateManager.navigateTo("World");
        assertTrue(success);

        // Verify new state is active
        assertTrue(stateManager.isActive("World"));
    }
}
```

> 📖 **See Also**: [Testing Introduction](../../../04-testing/testing-intro.md) | [Mock Mode Guide](../../../04-testing/mock-mode-guide.md) for testing strategies

## Summary

The Brobot annotation system dramatically simplifies state machine configuration:
- **@State** - Automatic state registration with Spring integration
- **@TransitionSet** - Groups all transitions for a state in one class
- **@IncomingTransition** - Verifies arrival at state
- **@OutgoingTransition** - Handles navigation with `activate` parameter (array required)
- **@CollectData** - (Experimental) Non-invasive ML dataset collection
- Eliminates boilerplate registration code
- Provides clear, declarative configuration
- Integrates seamlessly with Spring
- Supports complex transition scenarios
- Enables better code organization

**Key Takeaway**: Use `activate = {TargetState.class}` (array syntax) in `@OutgoingTransition` annotations.

By using these annotations, you can focus on your automation logic rather than framework setup, making your code more maintainable and easier to understand.

## Related Documentation

### Core Concepts
- **[States Guide](../../../01-getting-started/states.md)** - Understanding states and state management
- **[Transitions Guide](../../../01-getting-started/transitions.md)** - Transition fundamentals and patterns
- **[Core Concepts](../../../01-getting-started/core-concepts.md)** - Brobot architecture overview
- **[Quick Start Guide](../../../01-getting-started/quick-start.md)** - Get started with annotations immediately

### Advanced Features
- **[Pathfinding and Path Costs Guide](./pathfinding-and-costs.md)** - Cost-based pathfinding configuration
- **[Dynamic Transitions Guide](./dynamic-transitions.md)** - PreviousState, CurrentState, hidden states
- **[Multi-State Transitions Guide](./multi-state-transitions-guide.md)** - Complex multi-state activation patterns

### Testing
- **[Testing Introduction](../../../04-testing/testing-intro.md)** - Testing strategy overview
- **[Mock Mode Guide](../../../04-testing/mock-mode-guide.md)** - Headless testing with annotations
- **[Unit Testing Guide](../../../04-testing/unit-testing.md)** - Unit test patterns

### Tutorials
- **[Tutorial Basics](../../02-tutorials/tutorial-basics/index.md)** - Hands-on practice with annotations
- **[Claude Automator Tutorial](../../02-tutorials/tutorial-claude-automator/index.md)** - Real-world annotation usage

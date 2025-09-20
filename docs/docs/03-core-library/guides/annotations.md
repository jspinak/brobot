---
sidebar_position: 20
title: 'Annotations API'
---

# Annotations

Brobot provides a powerful annotation system that simplifies state and transition configuration by using declarative annotations instead of manual registration code.

## Overview

The annotation system introduces key annotations:
- **`@State`** - Marks a class as a Brobot state
- **`@TransitionSet`** - Marks a class as containing all transitions for a specific state
- **`@IncomingTransition`** - Marks a method that verifies arrival at the state
- **`@OutgoingTransition`** - Marks a method that navigates FROM the state TO another state

These annotations work with Spring's component scanning to automatically discover and register your states and transitions at application startup.

## Benefits

### Before (Manual Registration)
```java
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

    @OutgoingTransition(to = WorkingState.class)
    public boolean toWorking() {
        return action.click(promptState.getSubmitButton()).isSuccess();
    }
}
```

## @State Annotation

The `@State` annotation marks a class as a Brobot state and includes Spring's `@Component` for automatic discovery.

### Basic Usage

```java
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
}
```

### Marking Initial States

Use the `initial` parameter to designate starting states:

```java
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
@State(name = "Dashboard", description = "Main application dashboard")
@Getter
@Slf4j
public class DashboardState {
    // State definition
}
```

## Transition Annotations

Brobot uses a cohesive annotation pattern where each state's transitions are grouped in one class.

### @TransitionSet

Marks a class as containing all transitions for a specific state:

```java
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

    @OutgoingTransition(to = DashboardState.class, pathCost = 0)
    public boolean toDashboard() {
        log.info("Transitioning from Login to Dashboard");
        // Perform login actions
        action.type(loginState.getUsernameField(), "user@example.com");
        action.type(loginState.getPasswordField(), "password");
        return action.click(loginState.getLoginButton()).isSuccess();
    }

    @OutgoingTransition(to = ForgotPasswordState.class, pathCost = 2)
    public boolean toForgotPassword() {
        return action.click(loginState.getForgotPasswordLink()).isSuccess();
    }
}
```

### Multi-State Transitions

```java
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
        to = ReportsState.class,
        activate = {SidebarState.class, FilterPanelState.class},
        exit = {NotificationPanelState.class},
        pathCost = 1
    )
    public boolean toReports() {
        return action.click(dashboardState.getReportsButton()).isSuccess();
    }

    // Modal overlay - keep dashboard visible
    @OutgoingTransition(
        to = SettingsModalState.class,
        staysVisible = true,  // Dashboard remains visible
        pathCost = 2
    )
    public boolean openSettings() {
        return action.click(dashboardState.getSettingsIcon()).isSuccess();
    }
}
```

### @IncomingTransition

Verifies successful arrival at the state:

```java
@IncomingTransition(description = "Verify settings modal is open")
public boolean verifyArrival() {
    return action.find(settingsState.getCloseButton()).isSuccess() &&
           action.find(settingsState.getSettingsTitle()).isSuccess();
}
```

### @OutgoingTransition Parameters

- **`to`** (required): Target state class
- **`activate`**: Additional states to activate
- **`exit`**: States to deactivate
- **`staysVisible`**: Whether originating state remains visible (default: false)
- **`pathCost`**: Path-finding cost - lower costs preferred (default: 0)
- **`description`**: Documentation for the transition

### Path Cost Strategy

```java
@TransitionSet(state = MainMenuState.class)
public class MainMenuTransitions {

    // Direct navigation - lowest cost
    @OutgoingTransition(
        to = SettingsState.class,
        pathCost = 0  // Preferred path
    )
    public boolean directToSettings() {
        return action.click(mainMenu.getSettingsButton()).isSuccess();
    }

    // Indirect navigation - higher cost
    @OutgoingTransition(
        to = SettingsState.class,
        activate = {HelpPanelState.class},
        pathCost = 5  // Less preferred, only if direct fails
    )
    public boolean toSettingsViaHelp() {
        action.click(mainMenu.getHelpButton());
        return action.click(helpPanel.getSettingsLink()).isSuccess();
    }
}
```

## Complete Example

Here's a complete example showing how annotations simplify a typical automation flow:

```java
// Initial state
@State(initial = true)
@Getter
@Slf4j
public class LoginPageState {
    private final StateImage logo = new StateImage.Builder()
        .addPattern("app-logo")
        .build();
    
    private final StateImage usernameField = new StateImage.Builder()
        .addPattern("username-field")
        .build();
    
    private final StateImage passwordField = new StateImage.Builder()
        .addPattern("password-field")
        .build();
    
    private final StateImage loginButton = new StateImage.Builder()
        .addPattern("login-button")
        .build();
}

// Main application state
@State
@Getter
@Slf4j
public class DashboardState {
    private final StateImage dashboardHeader = new StateImage.Builder()
        .addPattern("dashboard-header")
        .build();
    
    private final StateImage menuButton = new StateImage.Builder()
        .addPattern("menu-button")
        .build();
}

// Login transition
@Transition(from = LoginPageState.class, to = DashboardState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginTransition {
    
    private final Action action;
    private final LoginPageState loginState;
    
    public boolean execute() {
        log.info("Performing login");
        
        // Click username field
        if (!action.click(loginState.getUsernameField()).isSuccess()) return false;
        
        // Type username
        if (!action.type(new ObjectCollection.Builder()
                .withStrings("user@example.com")
                .build()).isSuccess()) return false;
        
        // Click password field
        if (!action.click(loginState.getPasswordField()).isSuccess()) return false;
        
        // Type password
        if (!action.type(new ObjectCollection.Builder()
                .withStrings("password123")
                .build()).isSuccess()) return false;
        
        // Click login button
        return action.click(loginState.getLoginButton()).isSuccess();
    }
}
```

## How It Works

1. **Component Scanning**: Spring automatically discovers all classes annotated with `@State` and `@Transition`
2. **Annotation Processing**: The `AnnotationProcessor` processes these annotations at application startup
3. **State Registration**: States are registered with the framework and initial states are marked
4. **Transition Wiring**: Transitions are created and connected between the appropriate states
5. **Automatic Configuration**: The state machine is fully configured without manual registration code

## Best Practices

### 1. Always Include Required Lombok Annotations

```java
@State
@Getter  // Required for state objects
@Slf4j   // Recommended for logging
public class MyState {
    // State definition
}

@Transition(from = StateA.class, to = StateB.class)
@RequiredArgsConstructor  // For dependency injection
@Slf4j                   // For logging
public class MyTransition {
    // Transition logic
}
```

### 2. Use Descriptive Names

While class names are used by default, consider adding descriptions for clarity:

```java
@State(
    name = "UserProfile",
    description = "User profile page with account settings and preferences"
)
```

### 3. Keep Transitions Focused

Each transition class should handle one specific navigation path:

```java
// Good: Single responsibility
@Transition(from = CartState.class, to = CheckoutState.class)
public class ProceedToCheckoutTransition { }

// Avoid: Multiple unrelated transitions in one class
```

### 4. Handle Errors Gracefully

```java
@Transition(from = ProcessingState.class, to = {CompleteState.class, ErrorState.class})
public class ProcessingTransition {
    public boolean execute() {
        try {
            // Processing logic
            return processSuccessfully();
        } catch (Exception e) {
            log.error("Processing failed", e);
            return false; // Will transition to ErrorState
        }
    }
}
```

## Migration Guide

To migrate existing code to use annotations:

1. **Remove StateRegistrationListener**: Delete manual registration classes
2. **Add @State annotations**: Mark all state classes
3. **Add @Transition annotations**: Mark transition classes
4. **Add initial parameter**: Mark starting states with `@State(initial = true)`
5. **Add Lombok annotations**: Include @Getter and @Slf4j as needed
6. **Test the migration**: Verify all states and transitions are discovered

## Troubleshooting

### States Not Being Discovered

Ensure your states are in a package scanned by Spring:
- Check your `@ComponentScan` configuration
- Verify `@State` is properly imported
- Confirm the class is public

### Transitions Not Working

- Verify the transition method returns `boolean`
- Check that source and target states exist
- Review logs for registration errors
- Ensure `@Transition` includes valid `from` and `to` classes

### Initial States Not Set

- At least one state must have `@State(initial = true)`
- Check logs for "Marked X as initial state" messages
- Verify the state class is being discovered

## @CollectData Annotation

The `@CollectData` annotation enables automatic dataset collection for machine learning applications. When applied to methods, it captures inputs, outputs, and execution context for training ML models.

### Basic Usage

```java
@Component
public class SmartAutomation {
    
    @CollectData(category = "click_accuracy")
    public ActionResult performClick(StateImage target) {
        return action.click(target);
    }
}
```

### Parameters

- **`category`** (String, default: "general") - Category for organizing collected data
- **`features`** (String[], default: {}) - Specific features to collect (empty = all)
- **`captureScreenshots`** (boolean, default: true) - Capture before/after screenshots
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

### Use Cases

1. **Training Click Accuracy Models**: Collect data about successful/failed clicks to improve pattern matching
2. **Text Recognition Improvement**: Gather OCR results with ground truth for model training
3. **Workflow Optimization**: Analyze action sequences to identify bottlenecks
4. **Error Pattern Detection**: Collect failure cases to improve error handling
5. **Performance Tuning**: Gather timing data to optimize action execution

## Summary

The Brobot annotation system dramatically simplifies state machine configuration and enables advanced features:
- **@State** - Automatic state registration with Spring integration
- **@Transition** - Declarative transition configuration
- **@CollectData** - Non-invasive ML dataset collection
- Eliminates boilerplate registration code
- Provides clear, declarative configuration
- Integrates seamlessly with Spring
- Supports complex transition scenarios
- Enables better code organization

By using these annotations, you can focus on your automation logic rather than framework setup, making your code more maintainable and easier to understand.
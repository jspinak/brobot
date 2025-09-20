---
sidebar_position: 5
title: 'Pathfinding & Multi-State Activation'
---

# Pathfinding & Multi-State Activation

## Introduction

Brobot's pathfinding system is designed to handle the reality of modern GUIs where actions often activate multiple UI elements simultaneously. Unlike traditional page-based navigation where you go from Page A to Page B, real applications often display multiple panels, sidebars, and overlays at once.

**Key Insight**: In Brobot, there is no concept of a "primary target state" in transitions. All states activated by a transition are treated equally for pathfinding purposes.

## How Pathfinding Works

### The State Graph

Brobot builds a directed graph where:
- **Nodes** are States (UI configurations)
- **Edges** are Transitions (actions that change the UI)

### Multi-State Activation

When a transition executes, it can activate multiple states simultaneously:

```java
// This transition activates FOUR states at once
JavaStateTransition loginTransition = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(loginButton))
    .addToActivate("Dashboard")
    .addToActivate("NavigationBar")
    .addToActivate("StatusPanel")
    .addToActivate("NotificationArea")
    .build();
```

### Path Discovery Algorithm

When you call `navigator.openState("TargetState")`, Brobot:

1. **Indexes ALL Activated States**: Each transition is indexed by every state it activates
2. **Explores All Paths**: The pathfinder considers transitions through ANY activated state
3. **Finds Shortest Path**: Returns the path with the lowest total path cost

#### Example: Finding Paths Through Multi-State Transitions

```java
// Given these transitions:
// State A → activates [B, C, D]
// State B → activates [E]
// State C → activates [F]
// State D → activates [G]
// State E → activates [TargetState]

// When calling:
navigator.openState("TargetState");

// Pathfinder considers these paths:
// A → B → E → TargetState ✓ (Found via B)
// A → C → F → ... (Dead end)
// A → D → G → ... (Dead end)

// Result: Path A → B → E → TargetState is used
```

## Important Concepts for Developers

### 1. No Primary Target

**Traditional Approach (NOT how Brobot works):**
```java
// WRONG mental model:
@Transition(from = "Login", to = "Dashboard", alsoActivates = {"Sidebar", "Header"})
// This implies Dashboard is "primary" - IT IS NOT
```

**Brobot's Approach:**
```java
// CORRECT mental model:
// ALL states are equal - any can be used for pathfinding
transition.setActivate(Set.of("Dashboard", "Sidebar", "Header"));
```

### 2. Path Success vs. Complete Activation

**Critical Understanding**: Path success only requires the NEXT NODE in the path to be activated, not all activated states.

```java
// Transition from A activates [B, C, D, E]
// Path is A → B → F

// During traversal:
// 1. Execute transition from A
// 2. Verify B is active ✓ (Path continues)
// 3. Don't need to verify C, D, E for path success
// 4. If C, D, or E fail, path still succeeds (but log warnings)
```

### 3. IncomingTransitions Execute for ALL Activated States

When a transition activates multiple states, each state's `@IncomingTransition` executes:

```java
// Transition activates Dashboard, Sidebar, Header
// Execution order:
// 1. OutgoingTransition executes (leaving source state)
// 2. Dashboard.verifyArrival() executes
// 3. Sidebar.verifyArrival() executes
// 4. Header.verifyArrival() executes
// All must succeed for complete transition success
```

## Practical Implications

### 1. More Paths Available

Your automation has more navigation options:

```java
// Old thinking: "I need a direct transition to StateX"
// New thinking: "Any transition that activates StateX works"

// If Login → [Dashboard, Menu, Profile]
// Then you can reach Menu states via Login, even if
// Login's "main purpose" seems to be Dashboard
```

### 2. Flexible Navigation

States can be reached through unexpected routes:

```java
// UserProfile might be reached via:
// - Direct navigation: Menu → UserProfile
// - Side effect: Dashboard → [Settings, UserProfile]
// - Multi-activation: Login → [Dashboard, UserProfile, Notifications]
```

### 3. Design Considerations

#### When to Use Multi-State Activation

✅ **Good Use Cases:**
- Login opens multiple panels simultaneously
- Tab switches that keep navigation visible
- Modals that overlay existing content
- Dashboards with multiple independent widgets

❌ **Avoid When:**
- States are sequential (load one, then another)
- Activation depends on conditions
- States are mutually exclusive

#### Example: Login Opens Everything

```java
@TransitionSet(state = LoginState.class)
public class LoginTransitions {

    @OutgoingTransition(to = ApplicationState.class)
    public boolean login() {
        // This transition activates multiple states
        return action.click(loginButton).isSuccess();
    }
}

// In ApplicationState definition:
JavaStateTransition loginSuccess = new JavaStateTransition.Builder()
    .setFunction(() -> true) // Login button does all the work
    .addToActivate("Dashboard")
    .addToActivate("NavigationMenu")
    .addToActivate("UserProfile")
    .addToActivate("NotificationPanel")
    .build();
```

### 4. Pathfinding Strategies

#### Finding States with Specific Combinations

If you need multiple specific states active:

```java
// Need both Dashboard AND Settings open
// Option 1: Find path to state that activates both
StateTransition openBoth = findTransitionActivating(
    Set.of("Dashboard", "Settings")
);

// Option 2: Sequential activation
navigator.openState("Dashboard");
navigator.openState("Settings"); // Finds path from Dashboard to Settings
```

#### Understanding Path Choices

The pathfinder chooses paths based on:
1. **Reachability**: Can we get there from current active states?
2. **Path Length**: Fewer transitions preferred
3. **Transition Costs**: Lower path costs preferred
4. **Reliability**: Success history considered

## Best Practices

### 1. Design Transitions Thoughtfully

```java
// Good: Logical grouping of related states
transition.setActivate(Set.of(
    "EmailCompose",    // Main panel
    "EmailToolbar",    // Related toolbar
    "RecipientList"    // Related sidebar
));

// Bad: Unrelated states that happen to appear together
transition.setActivate(Set.of(
    "EmailCompose",
    "StockTicker",     // Unrelated
    "WeatherWidget"    // Unrelated
));
```

### 2. Document Multi-State Transitions

```java
/**
 * Opens the main application workspace.
 * Activates: Dashboard (main content)
 *           NavigationBar (top navigation)
 *           Sidebar (left panel)
 *           StatusBar (bottom status)
 *
 * All four states must be visible for successful workspace initialization.
 */
@OutgoingTransition(to = WorkspaceState.class)
public boolean openWorkspace() {
    return action.click(workspaceButton).isSuccess();
}
```

### 3. Test Path Variations

```java
@Test
public void testAlternativePathsToDashboard() {
    // Dashboard can be reached via Login
    navigator.openState("Login");
    navigator.openState("Dashboard"); // Via login transition
    assertTrue(stateMemory.isActive("Dashboard"));

    // Dashboard can also be reached via Home
    navigator.openState("Home");
    navigator.openState("Dashboard"); // Via home→dashboard
    assertTrue(stateMemory.isActive("Dashboard"));
}
```

### 4. Handle Partial Activation Gracefully

```java
@IncomingTransition
public boolean verifyArrival() {
    boolean primaryVisible = action.find(mainContent).isSuccess();
    boolean secondaryVisible = action.find(sidebar).isSuccess();

    if (primaryVisible && !secondaryVisible) {
        log.warn("Main content visible but sidebar failed to load");
        // Decide: Is this acceptable or should we fail?
        return primaryVisible; // Accept partial success
    }

    return primaryVisible && secondaryVisible;
}
```

## Common Patterns

### 1. Application Initialization

```java
// Login activates entire application structure
JavaStateTransition loginToApp = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(loginButton).isSuccess())
    .addToActivate("Dashboard")
    .addToActivate("Navigation")
    .addToActivate("Sidebar")
    .addToActivate("StatusBar")
    .addToActivate("NotificationArea")
    .build();
```

### 2. Modal Overlays

```java
// Modal keeps background states active
JavaStateTransition openModal = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(settingsButton).isSuccess())
    .addToActivate("SettingsModal")
    // Dashboard stays active (not in exit list)
    .setStaysVisibleAfterTransition(StaysVisible.TRUE)
    .build();
```

### 3. Tab Switching

```java
// Switch tabs while keeping navigation active
JavaStateTransition switchToReports = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(reportsTab).isSuccess())
    .addToActivate("ReportsContent")
    .addToExit("DashboardContent") // Exit old tab
    // Navigation stays active (not in exit list)
    .build();
```

### 4. Contextual Activation

```java
// Different states activated based on context
public boolean openDetails() {
    if (isCustomerView()) {
        // Activates customer-specific panels
        return openCustomerDetails();
    } else {
        // Activates product-specific panels
        return openProductDetails();
    }
}
```

## Debugging Pathfinding

### Enable Detailed Logging

```properties
# application.properties
logging.level.io.github.jspinak.brobot.navigation.path=TRACE
logging.level.io.github.jspinak.brobot.navigation.transition=TRACE
```

### Understanding Path Decisions

When pathfinding seems to take unexpected routes:

1. **Check what states are activated by each transition**
```java
StateTransitions transitions = transitionService.getTransitions(stateId);
transitions.getTransitions().forEach(t ->
    System.out.println("Activates: " + t.getActivate())
);
```

2. **Verify the joint table indexing**
```java
Set<Long> pathsToTarget = jointTable.getIncomingTransitions(targetId);
System.out.println("States that can reach " + targetId + ": " + pathsToTarget);
```

3. **Trace the path finder's decisions**
```java
Paths paths = pathFinder.getPathsToState(activeStates, targetState);
paths.getPaths().forEach(path ->
    System.out.println("Found path: " + path)
);
```

## Summary

Brobot's pathfinding system embraces the complexity of modern GUIs by:

1. **Treating all activated states equally** - No artificial "primary" target
2. **Indexing transitions by ALL activated states** - More paths available
3. **Verifying path progression, not complete activation** - Flexible navigation
4. **Executing IncomingTransitions for all activated states** - Proper verification

This design makes your automation more robust and adaptable to real-world GUI behaviors where multiple elements appear and disappear together.
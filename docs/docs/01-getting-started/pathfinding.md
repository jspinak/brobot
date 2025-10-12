---
sidebar_position: 5
title: 'Pathfinding & Multi-State Activation'
---

# Pathfinding & Multi-State Activation

## Introduction

Brobot's pathfinding system is designed to handle the reality of modern GUIs where actions often activate multiple UI elements simultaneously. Unlike traditional page-based navigation where you go from Page A to Page B, real applications often display multiple panels, sidebars, and overlays at once.

In Brobot 1.1.0, there is no concept of a "primary target state" in transitions. All states activated by a transition are treated equally for pathfinding purposes.

## How Pathfinding Works

### The State Graph

Brobot builds a directed graph where:
- **Nodes** are States (UI configurations)
- **Edges** are Transitions (actions that change the UI)

### Multi-State Activation

When a transition executes, it can activate multiple states simultaneously:

```java
import io.github.jspinak.brobot.annotations.*;
import org.springframework.stereotype.Component;

@Component
@TransitionSet(state = LoginState.class)
public class LoginTransitions {

    @Autowired
    private Action action;

    @Autowired
    private LoginState loginState;

    /**
     * Verify we've arrived at the Login state.
     */
    @IncomingTransition(description = "Verify login screen is visible")
    public boolean verifyLoginScreen() {
        return action.find(loginState.getLoginButton()).isSuccess();
    }

    /**
     * Login transition that activates multiple states.
     * Transitions FROM LoginState TO Dashboard (and related states).
     */
    @OutgoingTransition(
        activate = {
            DashboardState.class,
            NavigationBarState.class,
            StatusPanelState.class,
            NotificationAreaState.class
        }
    )
    public boolean login() {
        return action.click(loginState.getLoginButton()).isSuccess();
    }
}
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

### 1. Path Success vs. Complete Activation

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

### 2. IncomingTransitions Execute for ALL Activated States

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

#### Understanding Path Choices

The pathfinder chooses paths based on:
1. **Reachability**: Can we get there from current active states?
2. **Path Length**: Fewer transitions preferred
3. **Transition Costs**: Lower path costs preferred

## Best Practices

```java
// Good: Logical grouping of related states
@OutgoingTransition(
    activate = {
        EmailComposeState.class,    // Main panel
        EmailToolbarState.class,    // Related toolbar
        RecipientListState.class    // Related sidebar
    }
)
public boolean openEmailCompose() {
    return action.click(composeButton).isSuccess();
}

// Bad: Unrelated states that happen to appear together
@OutgoingTransition(
    activate = {
        EmailComposeState.class,
        StockTickerState.class,     // Unrelated - BAD!
        WeatherWidgetState.class    // Unrelated - BAD!
    }
)
public boolean openEmailWithUnrelatedStuff() {
    return action.click(composeButton).isSuccess();
}
```

## Debugging Pathfinding

```properties
# application.properties
logging.level.io.github.jspinak.brobot.navigation.path=TRACE
logging.level.io.github.jspinak.brobot.navigation.transition=TRACE
```

This will enable TRACE-level logging for:
- Path package: PathFinder algorithm, path calculation, graph traversal
- Transition package: Transition execution, state activation/deactivation

For even more detailed debugging, you could also add:
```
# Enable all navigation logging
logging.level.io.github.jspinak.brobot.navigation=TRACE

# Or be more specific
logging.level.io.github.jspinak.brobot.navigation.path.PathFinder=TRACE
logging.level.io.github.jspinak.brobot.navigation.transition.TransitionExecutor=TRACE
logging.level.io.github.jspinak.brobot.navigation.transition.StateNavigator=TRACE
```

## Summary

Brobot's pathfinding system embraces the complexity of modern GUIs by:

1. **Treating all activated states equally** - No artificial "primary" target
2. **Indexing transitions by ALL activated states** - More paths available
3. **Verifying path progression, not complete activation** - Flexible navigation
4. **Executing IncomingTransitions for all activated states** - Proper verification

This design makes your automation more robust and adaptable to real-world GUI behaviors where multiple elements appear and disappear together.
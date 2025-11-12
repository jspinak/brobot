---
sidebar_position: 4
title: 'Transitions'
---

# Transitions

## Introduction

While states define "where you can be" in a GUI, **transitions** define "how you get there." Every state that is reachable needs an associated transitions class that defines the pathways to and from other states. 

Formally, a transition is a process or a sequence of actions that changes the GUI from one state to another. They form the "edges" of the state graph and are the building blocks used by the framework's pathfinder to navigate the application. 

## The Brobot Implementation: IncomingTransition and OutgoingTransition

Brobot implements transitions using a cohesive pattern where each state's transition class contains:

![Transition Diagram](/img/paper/transitions.png)  
_This diagram is based on Figure 8 from the research paper._

* **IncomingTransition**: This verifies successful arrival at the state, regardless of which state initiated the transition. There is only one IncomingTransition per state, and it contains checks to confirm the state is active.

* **OutgoingTransition**: These handle navigation FROM the current state TO other states. Each OutgoingTransition contains the specific actions needed to navigate to a target state.

## Using @TransitionSet Annotation

Brobot uses a cohesive, method-level annotation approach that groups all transitions for a state in one class. Each transition class contains:
- The IncomingTransition to verify arrival at the state
- All OutgoingTransitions that navigate FROM this state to other states

This pattern maintains high cohesion since outgoing transitions use the current state's images.

### Complete Example

```java
/**
 * TransitionSet for AmountState - handles all transitions for the Amount dialog
 * Transition classes have ONLY methods, no state objects
 */
@TransitionSet(state = AmountState.class)
@RequiredArgsConstructor
@Slf4j
public class AmountTransitions {

    private final AmountState amountState;
    private final Action action;

    @OutgoingTransition(activate = {MainScreenState.class})
    public boolean toMainScreen() {
        return action.type(amountState.getClose()).isSuccess();
    }

    @IncomingTransition
    public boolean verifyArrival() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .withBeforeActionLog("Verifying arrival at Amount dialog...")
            .withSuccessLog("Successfully arrived at Amount dialog")
            .withFailureLog("Failed to verify arrival at Amount dialog")
            .setSearchDuration(5.0)
            .build();

        return action.perform(findOptions, amountState.getEingabe()).isSuccess();
    }
}
```

### Annotation Types

#### @TransitionSet
Marks a class as containing all transitions for a specific state:
- **state**: The state class these transitions belong to (required)
- **name**: Optional state name override (defaults to class name without "State" suffix)
- **description**: Documentation for the transition set

#### @IncomingTransition
Verifies successful arrival at the state:
- **description**: Documentation for the verification

#### @OutgoingTransition
Defines a transition FROM the current state TO other states:
- **activate**: States to activate during this transition (required array of state classes)
- **exit**: States to deactivate during this transition (optional array of state classes)
- **staysVisible**: Whether the originating state remains visible after transition (default: false)
- **pathCost**: Path-finding cost - LOWER costs are preferred when multiple paths exist (default: 1)
- **description**: Documentation for this transition

## World State Example

Here's another complete example showing transitions for a World state with multiple entry and exit points:

```java
@TransitionSet(state = WorldState.class, description = "World map transitions")
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {

    private final WorldState worldState;
    private final Action action;
    
    /**
     * Verify arrival at World state by checking for the state's images.
     */
    @IncomingTransition(description = "Verify arrival at World state")
    public boolean verifyArrival() {
        // Check for world-specific elements
        return action.find(worldState.getWorldMap(), worldState.getIsland()).isSuccess();
    }

    /**
     * Navigate from World to Home by clicking the home button.
     */
    @OutgoingTransition(activate = {HomeState.class}, description = "Navigate from World to Home")
    public boolean toHome() {
        return action.click(worldState.getHomeButton()).isSuccess();
    }

    /**
     * Navigate from World to Island by clicking on the search button.
     */
    @OutgoingTransition(activate = {IslandState.class}, pathCost = 2, description = "Navigate from World to Island")
    public boolean toIsland() {
        return action.click(worldState.getSearchButton()).isSuccess();
    }
}
```

## Advanced OutgoingTransition Examples

### Modal Dialog with Origin State Visible

```java
@TransitionSet(state = SettingsModalState.class)
@RequiredArgsConstructor
@Slf4j
public class SettingsModalTransitions {

    private final SettingsModalState settingsModalState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(settingsModalState.getCloseButton()).isSuccess();
    }

    /**
     * Close the modal and return to previous state.
     * Previous state is reactivated when modal closes.
     */
    @OutgoingTransition(activate = {PreviousState.class})
    public boolean closeToPrevious() {
        return action.click(settingsModalState.getCloseButton()).isSuccess();
    }
}

@TransitionSet(state = DashboardState.class)
public class DashboardTransitions {

    /**
     * Open settings modal while keeping dashboard visible in background.
     */
    @OutgoingTransition(
        activate = {SettingsModalState.class},
        staysVisible = true,  // Dashboard remains visible behind modal
        pathCost = 2
    )
    public boolean openSettingsModal() {
        return action.click(dashboardState.getSettingsButton()).isSuccess();
    }
}
```

### Complex Multi-State Activation

```java
@TransitionSet(state = LoginState.class)
public class LoginTransitions {

    /**
     * Login transition that activates multiple UI components.
     */
    @OutgoingTransition(
        activate = {DashboardState.class, SidebarState.class, HeaderState.class, FooterState.class},
        exit = {SplashScreenState.class},  // Clean up login-related states
        pathCost = 0  // Preferred path
    )
    public boolean login() {
        action.type(loginState.getUsernameField(), "user@example.com");
        action.type(loginState.getPasswordField(), "password");
        return action.click(loginState.getLoginButton()).isSuccess();
    }
}
```

## Key Benefits of This Pattern

1. **High Cohesion**: Each transition class only needs its own state as a dependency, since outgoing transitions use that state's images
2. **Clear Separation**: IncomingTransition verifies arrival, OutgoingTransitions handle navigation FROM the state
3. **Natural Organization**: File structure mirrors state structure (one transitions class per state)
4. **Spring Integration**: Full dependency injection support (@TransitionSet includes @Component)
5. **Type Safety**: Class-based state references prevent typos and enable IDE refactoring
6. **Cleaner Code**: Each transition class is self-contained with its state's navigation logic

## The Formal Model (Under the Hood)

The academic paper provides a formal definition for a transition as a tuple **t = (A, S<sub>t</sub><sup>def</sup>)**.

* **A** is a **process**, which is a sequence of one or more actions `(a¹, a², ..., aⁿ)`. This corresponds to the method body in your @OutgoingTransition methods.
* **S<sub>t</sub><sup>def</sup>** is the **intended state information**. This is handled automatically by the framework based on the @TransitionSet's state parameter and the @OutgoingTransition's activate parameter.

## Multi-State Activation and Pathfinding

Brobot supports transitions that activate multiple states simultaneously. This is useful for scenarios where opening one state brings multiple UI elements or panels into view.

> **📖 For complete pathfinding details, see [Pathfinding & Multi-State Activation](pathfinding.md)**
>
> **📖 Deep Dive**: [Multi-State Transitions Guide](../03-core-library/user-guides/multi-state-transitions-guide.md) for comprehensive multi-state activation examples, verification patterns, and best practices

### Core Concept: No Primary Target State

**Critical Understanding**: In Brobot 1.1.0, transitions don't have a "primary" target. ALL states in the `activate` set are treated equally for pathfinding purposes.

```java
// All four states are equal - any can be used as a path node
transition.setActivate(Set.of("Dashboard", "Sidebar", "Header", "Footer"));
```

### Configuring Multi-State Transitions

While transitions are defined using the `@TransitionSet` and `@OutgoingTransition` annotations, the underlying transition model supports activating and exiting multiple states. This can be configured programmatically through the transition's `activate` and `exit` sets:

```java
// In the transition configuration or builder
transition.setActivate(Set.of(dashboardId, sidebarId, headerId));
transition.setExit(Set.of(loginId));
```

### How Pathfinding Uses Multi-State Transitions

When a transition activates multiple states, **each activated state becomes a potential path node** for future navigation:

```java
// Given: Login transition activates [Dashboard, Sidebar, Menu]
// Then pathfinder can use:
//   Login → Dashboard → TargetState
//   Login → Sidebar → TargetState
//   Login → Menu → TargetState
// Whichever path exists, has not failed, and has the lowest cost will be used
```

### Important Behavior: IncomingTransitions for All Activated States

**When a transition activates multiple states, each activated state's `@IncomingTransition` method is executed to verify successful arrival.** This ensures that all expected UI elements are present before the transition is considered successful.

#### Execution Order:
1. **OutgoingTransition** executes (leaving the source state)
2. **Each state's IncomingTransition** executes in sequence
3. States are only marked as active if their IncomingTransition succeeds

### Example: Login to Dashboard with Multiple Panels

```java
// Transition that activates multiple UI panels
@TransitionSet(state = LoginState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginTransitions {

    private final Action action;
    private final LoginState loginState;

    // This transition is configured to activate multiple states
    @OutgoingTransition(activate = {DashboardState.class, NavigationBarState.class, StatusPanelState.class})
    public boolean login() {
        log.info("Navigating from Login to Dashboard, NavigationBar, and StatusPanel.");
        return action.click(loginState.getLoginButton()).isSuccess();
    }

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying login is visible");
        return action.find(loginState.getLoginButton()).isSuccess();
    }
}

// Each activated state verifies its own presence
@TransitionSet(state = DashboardState.class)
public class DashboardTransitions {
    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying Dashboard is visible");
        return action.find(dashboardState.getMainContent()).isSuccess();
    }
}

@TransitionSet(state = NavigationBarState.class)
public class NavigationBarTransitions {
    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying NavigationBar is visible");
        return action.find(navBarState.getMenuItems()).isSuccess();
    }
}

@TransitionSet(state = StatusPanelState.class)
public class StatusPanelTransitions {
    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying StatusPanel is visible");
        return action.find(statusPanel.getStatusIndicator()).isSuccess();
    }
}
```

### State Management in Transitions

The underlying `StateTransition` interface provides mechanisms for managing state activation and deactivation:

#### Activate Set
States to be activated during the transition:
```java
// In the transition configuration
transition.setActivate(Set.of(gameBoardId, scorePanelId, timerId));
```

#### Exit Set
States to be deactivated (exited) during the transition:
```java
// States that should be deactivated
transition.setExit(Set.of(mainMenuId, settingsId));
```

#### StaysVisible Property
Controls whether the source state remains visible after transition:
```java
// Keep source state visible (e.g., for overlays)
transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);

// Hide source state (default behavior)
transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.FALSE);

// Inherit from StateTransitions container
transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.NONE);
```

### Benefits of Multi-State Activation

1. **Atomic Operations**: All states are verified together, ensuring UI consistency
2. **Better Error Detection**: If any expected element is missing, the transition fails
3. **Cleaner Code**: One transition handles complex UI changes
4. **Accurate Model**: Reflects how modern UIs actually work with multiple components

## Dynamic Transitions for Hidden States

Brobot supports dynamic transitions to handle common UI patterns like menus and pop-ups. When a state opens and covers another, the covered state is registered as "hidden."

You can define transitions that return to the previous state dynamically:

```java
import io.github.jspinak.brobot.model.state.special.*;

@TransitionSet(state = MenuState.class, description = "Menu overlay transitions")
@RequiredArgsConstructor
@Slf4j
public class MenuTransitions {
    
    private final MenuState menuState;
    private final Action action;
    
    /**
     * Close menu and return to whatever state was underneath.
     * This uses the PreviousState special marker for dynamic navigation.
     */
    @OutgoingTransition(
        activate = {PreviousState.class},
        description = "Close menu and return to previous state"
    )
    public boolean toPrevious() {
        // Click close button or press ESC
        return action.click(menuState.getCloseButton()).isSuccess() ||
               action.type(Key.ESC).isSuccess(); 
    }
    
    @IncomingTransition
    public boolean verifyArrival() {
        return action.find(menuState.getMenuHeader()).isSuccess();
    }
}
```

## Path Costs and Pathfinding

Brobot uses a cost-based pathfinding system to automatically select the best path between states. Understanding path costs is essential for predictable navigation.

### Default Path Costs

- **States**: Default pathCost = 1
- **Transitions**: Default pathCost = 1
- **Total Path Cost** = Sum of all state costs + Sum of all transition costs

### Quick Example

```java
@State  // Default state pathCost = 1
public class HomePageState { }

@State(pathCost = 5)  // Expensive state
public class SlowLoadingPageState { }

@TransitionSet(state = HomePage.class)
public class HomePageTransitions {

    @OutgoingTransition(to = SlowLoadingPageState.class)  # default = 1
    public boolean normalRoute() { ... }
}
```

A path from HomePage to SlowLoadingPage would have a cost of 1 (HomePage state cost) + 1 (transition cost from HomePage to SlowLoadingPage) + 5 (SlowLoadingPage state cost) = 7. When multiple paths exist, Brobot automatically selects the path with the **lowest total cost**.

### Learn More

For comprehensive documentation on pathfinding, cost calculation, and advanced patterns, see the [**Pathfinding and Path Costs Guide**](../03-core-library/user-guides/pathfinding-and-costs.md).

## Important Pathfinding Limitation

### One Transition Per State Pair

**Critical**: Brobot's pathfinding algorithm supports **at most one transition** between any pair of states. If you define multiple `@OutgoingTransition` methods that activate the same destination state, **only the first one will be discovered by the pathfinder**.

This limitation exists because the framework uses `getTransitionFunctionByActivatedStateId()` which returns the first matching transition:

```java
// From StateTransitions.java
public Optional<StateTransition> getTransitionFunctionByActivatedStateId(Long to) {
    for (StateTransition transition : transitions) {
        if (transition.getActivate().contains(to))
            return Optional.of(transition);  // Returns FIRST match only!
    }
    return Optional.empty();
}
```

### Example of the Problem

```java
@TransitionSet(state = MainPageState.class)
public class MainPageTransitions {

    // ❌ WRONG - Two transitions to CurrentState
    @OutgoingTransition(activate = {CurrentState.class}, pathCost = 2)
    public boolean refresh() {
        return action.click(mainPageState.getRefreshButton()).isSuccess();
    }

    @OutgoingTransition(activate = {CurrentState.class}, pathCost = 3)
    public boolean nextPage() {
        // This transition will NEVER be used by the pathfinder!
        return action.click(mainPageState.getNextPageButton()).isSuccess();
    }
}
```

**Result**: The pathfinder will only ever discover and use the `refresh()` transition. The `nextPage()` transition is invisible to pathfinding.

### Solution

**Combine Multiple UI Elements in One Transition (Recommended)**
```java
@OutgoingTransition(activate = {PreviousState.class}, pathCost = 0)
public boolean closeDialog() {
    // Use ObjectCollection to accept multiple buttons
    ObjectCollection closeButtons = new ObjectCollection.Builder()
            .withImages(dialogState.getConfirmButton(),
                       dialogState.getCancelButton(),
                       dialogState.getXButton())
            .build();
    return action.click(closeButtons).isSuccess();
}
```

### When This Matters

This limitation is important when:
- Using `CurrentState` for multiple self-transitions
- Defining multiple ways to reach the same state (e.g., keyboard shortcut vs menu)
- Creating fallback transitions to the same destination

**Key Takeaway**: Design your state graph so there's only one transition between any pair of states. If you need multiple ways to trigger the same transition, use an `ObjectCollection` in a single transition method.

## Best Practices

1. **Use descriptive method names** like `toMenu()`, `toHomepage()` for outgoing transitions
2. **Verify critical elements** in IncomingTransition to ensure state is truly active
3. **Set appropriate path costs** - use defaults for normal operations, 0 for free, higher for fallbacks
4. **Keep transitions focused** - each method should do one thing well
5. **Minimize dependencies** - each transition class should only need its own state
6. **One transition per destination** - Ensure at most one `@OutgoingTransition` activates each destination state

## Summary

The @TransitionSet pattern with @OutgoingTransition and @IncomingTransition provides a clean, maintainable way to define state transitions in Brobot. By grouping a state's verification logic (IncomingTransition) with its outgoing navigation logic (OutgoingTransition) in a single class, you achieve:

- **Better cohesion**: Outgoing transitions use the current state's images
- **Fewer dependencies**: Each transition class only needs its own state
- **Clearer organization**: Navigation logic flows naturally from each state
- **Easier maintenance**: All transitions for a state are in one place

This pattern makes your automation code easier to understand, test, and maintain.
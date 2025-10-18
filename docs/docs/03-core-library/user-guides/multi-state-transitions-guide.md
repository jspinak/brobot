---
sidebar_position: 25
title: 'Multi-State Transitions Guide'
---

# Multi-State Transitions Developer Guide

> 📖 **Prerequisites**: Before diving into multi-state patterns, ensure you understand:
> - **[States fundamentals](../../01-getting-started/states.md)** - What states are and how they work
> - **[Transitions basics](../../01-getting-started/transitions.md)** - Using @TransitionSet and @OutgoingTransition
> - **[Pathfinding concepts](../../01-getting-started/pathfinding.md)** - How Brobot navigates between states

## Quick Reference

### Key Concepts

| Concept | Description |
|---------|------------|
| **No Primary Target** | All states in `activate` set are equal |
| **Path Node Eligibility** | ANY activated state can be a path node |
| **Path Success** | Only next path node needs activation |
| **IncomingTransition Execution** | ALL activated states verify arrival |

> 📖 **Learn More**: [Pathfinding & Multi-State Activation](../../01-getting-started/pathfinding.md) for detailed pathfinding behavior

### Code Patterns

#### Defining Multi-State Transitions

```java
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.datatype.state.transition.JavaStateTransition;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private Action action;
private StateImage loginButton;

// Using JavaStateTransition Builder
JavaStateTransition multiActivation = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(loginButton).isSuccess())
    .addToActivate("Dashboard")      // All four states
    .addToActivate("NavigationBar")   // are activated
    .addToActivate("StatusPanel")     // equally - no
    .addToActivate("NotificationArea") // primary target
    .build();
```

#### Using @TransitionSet Pattern

```java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;

@TransitionSet(state = LoginState.class)
@RequiredArgsConstructor
public class LoginTransitions {

    private final Action action;
    private final LoginState loginState;

    @OutgoingTransition(activate = {ApplicationState.class})
    public boolean openApplication() {
        // This click activates multiple states defined in ApplicationState
        return action.click(loginState.getLoginButton()).isSuccess();
    }
}
```

> 📖 **API Reference**: [Annotations Guide](./annotations.md) for complete @TransitionSet and @OutgoingTransition parameters

## Common Scenarios

### 1. Login Opens Entire Application

**Scenario**: Login button opens dashboard, navigation, sidebar, and status bar simultaneously.

```java
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.datatype.state.transition.JavaStateTransition;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private Action action;
private StateImage loginButton;

// Transition definition
JavaStateTransition loginToApp = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(loginButton).isSuccess())
    .addToActivate("Dashboard")
    .addToActivate("Navigation")
    .addToActivate("Sidebar")
    .addToActivate("StatusBar")
    .addToExit("LoginScreen")  // Login screen disappears
    .build();
```

**Pathfinding Impact**:
```java
// After login, these paths become available:
// Login → Dashboard → AnyDashboardChild
// Login → Navigation → AnyNavChild
// Login → Sidebar → AnySidebarChild
// Login → StatusBar → AnyStatusChild

// Example: Reaching Settings via different routes
stateNavigator.openState("Settings");
// Might use: Login → Navigation → Settings
// Or: Login → Sidebar → Settings
// Whichever path exists and is shortest
```

> 📖 **Learn More**: [Pathfinding and Path Costs Guide](./pathfinding-and-costs.md) for path selection strategies

### 2. Tab Switching with Persistent Navigation

**Scenario**: Switching tabs changes content but keeps navigation active.

```java
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.datatype.state.transition.JavaStateTransition;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private Action action;
private StateImage tab2Button;

// Tab1 to Tab2 transition
JavaStateTransition switchToTab2 = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(tab2Button).isSuccess())
    .addToActivate("Tab2Content")
    .addToActivate("Tab2Sidebar")  // Tab2 specific sidebar
    .addToExit("Tab1Content")       // Remove Tab1 content
    .addToExit("Tab1Sidebar")       // Remove Tab1 sidebar
    // Note: Navigation NOT in exit list - stays active
    .build();
```

### 3. Modal Dialog Over Content

**Scenario**: Settings modal appears over dashboard without hiding it.

```java
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.datatype.state.transition.JavaStateTransition;
import io.github.jspinak.brobot.datatype.state.stateObject.stateImage.StaysVisible;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private Action action;
private StateImage settingsIcon;

// Open modal keeping background visible
JavaStateTransition openSettings = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(settingsIcon).isSuccess())
    .addToActivate("SettingsModal")
    .addToActivate("ModalOverlay")
    // Dashboard NOT in exit list - remains visible
    .setStaysVisibleAfterTransition(StaysVisible.TRUE)
    .build();
```

> 📖 **See Also**: [Dynamic Transitions Guide](./dynamic-transitions.md) for overlay patterns and PreviousState usage

### 4. Contextual State Activation

**Scenario**: Different states activate based on user role or context.

```java
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.datatype.state.transition.JavaStateTransition;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;

public class ContextualTransition {

    @Autowired
    private Action action;
    private UserRole userRole;
    private StateImage enterButton;

    public Set<String> determineActivatedStates() {
        Set<String> toActivate = new HashSet<>();

        // Always activate base dashboard
        toActivate.add("Dashboard");

        // Role-specific panels
        if (userRole.isAdmin()) {
            toActivate.add("AdminPanel");
            toActivate.add("SystemMonitor");
        }

        if (userRole.hasNotifications()) {
            toActivate.add("NotificationPanel");
        }

        return toActivate;
    }

    public JavaStateTransition buildTransition() {
        JavaStateTransition.Builder builder = new JavaStateTransition.Builder()
            .setFunction(() -> action.click(enterButton).isSuccess());

        // Add all contextual states
        determineActivatedStates().forEach(builder::addToActivate);

        return builder.build();
    }
}
```

## Pathfinding Examples

### Understanding Path Discovery

```java
// Given this state structure:
// StateA → activates [B, C, D]
// StateB → activates [E]
// StateC → activates [F]
// StateD → activates [G]
// StateE → activates [Target]

// When navigating to Target:
stateNavigator.openState("Target");

// Pathfinder explores:
// From A: Can reach B, C, D
// From B: Can reach E
// From E: Can reach Target ✓
// Path found: A → B → E → Target

// Even though A also activated C and D,
// the path only needs B to continue
```

> 📖 **Deep Dive**: [Pathfinding & Multi-State Activation](../../01-getting-started/pathfinding.md) for complete algorithm details

### Testing Multiple Paths

```java
import io.github.jspinak.brobot.navigation.pathfinding.PathFinder;
import io.github.jspinak.brobot.navigation.pathfinding.Paths;
import io.github.jspinak.brobot.datatype.state.StateMemory;
import io.github.jspinak.brobot.statemanagement.StateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Test
public void testMultiplePathsToTarget() {
    // Setup: Login activates [Dashboard, Menu]
    // Dashboard can reach Settings
    // Menu can also reach Settings

    @Autowired
    private StateMemory stateMemory;
    @Autowired
    private PathFinder pathFinder;
    @Autowired
    private StateService stateService;

    // Get state IDs (PathFinder uses Long IDs, not String names)
    Long loginStateId = stateService.getStateId("Login");
    Long settingsStateId = stateService.getStateId("Settings");

    // Path 1: Via Dashboard
    stateMemory.removeAllStates();
    stateMemory.addActiveState("Login");

    Paths paths = pathFinder.getPathsToState(
        Set.of(loginStateId), settingsStateId
    );

    // Should find both paths:
    // Login → Dashboard → Settings
    // Login → Menu → Settings
    assertEquals(2, paths.getPaths().size());
}
```

> 📖 **Testing Guide**: [Testing Introduction](../../04-testing/testing-intro.md) for comprehensive testing strategies

## Verification Patterns

### Complete Activation Verification

```java
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransitionSet(state = WorkspaceState.class)
@RequiredArgsConstructor
@Slf4j
public class WorkspaceTransitions {

    private final Action action;
    private final WorkspaceState workspaceState;

    @IncomingTransition
    public boolean verifyCompleteActivation() {
        // Check ALL expected components
        boolean dashboardVisible = action.find(workspaceState.getDashboard()).isSuccess();
        boolean navVisible = action.find(workspaceState.getNavigation()).isSuccess();
        boolean sidebarVisible = action.find(workspaceState.getSidebar()).isSuccess();
        boolean statusVisible = action.find(workspaceState.getStatusBar()).isSuccess();

        if (!dashboardVisible || !navVisible || !sidebarVisible || !statusVisible) {
            log.error("Workspace activation incomplete: " +
                "Dashboard={}, Nav={}, Sidebar={}, Status={}",
                dashboardVisible, navVisible, sidebarVisible, statusVisible);
            return false;
        }

        return true;
    }
}
```

### Partial Activation Tolerance

```java
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.actions.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PartialActivationExample {

    private final Action action;
    private final StateImage mainContent;
    private final StateImage sidebar;

    @IncomingTransition
    public boolean verifyWithTolerance() {
        // Required components
        boolean coreVisible = action.find(mainContent).isSuccess();
        if (!coreVisible) {
            log.error("Core content not visible - failing");
            return false;
        }

        // Optional components
        boolean sidebarVisible = action.find(sidebar).isSuccess();
        if (!sidebarVisible) {
            log.warn("Sidebar not visible - continuing anyway");
        }

        return coreVisible; // Accept partial success
    }
}
```

## Best Practices

### DO ✅

1. **Group Related States**
```java
// Good: Logical grouping
.addToActivate("EmailEditor")
.addToActivate("EmailToolbar")
.addToActivate("RecipientPanel")
```

2. **Document Multi-Activation**
```java
/**
 * Opens email composer with all panels.
 * Activates: EmailEditor (main), EmailToolbar (top), RecipientPanel (side)
 */
```

3. **Test Path Variations**
```java
// Test that state can be reached via multiple paths
testPathViaMenu();
testPathViaDashboard();
testPathViaShortcut();
```

4. **Handle Activation Failures**
```java
if (!allStatesActivated()) {
    log.warn("Partial activation - attempting recovery");
    return attemptRecovery();
}
```

### DON'T ❌

1. **Don't Assume Primary Target**
```java
// Wrong thinking:
// "Dashboard is the main state, others are secondary"
// Right thinking:
// "All activated states are equal for pathfinding"
```

2. **Don't Activate Unrelated States**
```java
// Bad: Unrelated states
.addToActivate("EmailComposer")
.addToActivate("StockTicker")  // Unrelated!
.addToActivate("WeatherWidget") // Unrelated!
```

3. **Don't Ignore Partial Activation**
```java
// Bad: Ignoring failures
boolean result = action.click(button).isSuccess();
// Should verify all expected states activated
```

## Debugging

### Enable Transition Logging

```properties
# application.properties
logging.level.io.github.jspinak.brobot.navigation.transition=TRACE
```

### Debug Output

```java
import io.github.jspinak.brobot.datatype.state.transition.StateTransition;
import io.github.jspinak.brobot.navigation.pathfinding.Paths;
import io.github.jspinak.brobot.navigation.pathfinding.PathFinder;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

// Check what states a transition activates
@Autowired
private StateTransitionsJointTable jointTable;
@Autowired
private PathFinder pathFinder;

private Long stateId;
private Set<Long> activeStateIds;
private Long targetStateId;

public void debugTransitions(StateTransition transition) {
    System.out.println("Activates: " + transition.getActivate());
    System.out.println("Exits: " + transition.getExit());

    // Verify joint table indexing
    Set<Long> parents = jointTable.getStatesWithTransitionsTo(stateId);
    System.out.println("Can be reached from: " + parents);

    // Trace path finding
    Paths paths = pathFinder.getPathsToState(activeStateIds, targetStateId);
    paths.getPaths().forEach(System.out::println);
}
```

## Summary Checklist

When implementing multi-state transitions:

- [ ] All activated states have IncomingTransition methods
- [ ] States are logically related (appear together in UI)
- [ ] Documentation lists all activated states
- [ ] Tests verify all states activate correctly
- [ ] Pathfinding tested through different activated states
- [ ] Partial activation handled gracefully
- [ ] Exit states explicitly specified when needed
- [ ] Visibility behavior (staysVisible) set appropriately

## Related Documentation

### Foundational Concepts
- **[States Guide](../../01-getting-started/states.md)** - Understanding states and state management
- **[Transitions Guide](../../01-getting-started/transitions.md)** - Transition fundamentals
- **[Pathfinding & Multi-State Activation](../../01-getting-started/pathfinding.md)** - How pathfinding uses multi-state activation

### Advanced Patterns
- **[Dynamic Transitions Guide](./dynamic-transitions.md)** - Hidden states, overlays, PreviousState patterns
- **[Pathfinding and Path Costs Guide](./pathfinding-and-costs.md)** - Cost-based path selection
- **[Annotations Guide](./annotations.md)** - Complete @TransitionSet and @OutgoingTransition reference

### Testing
- **[Testing Introduction](../../04-testing/testing-intro.md)** - Testing strategy overview
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing multi-state transitions

### Tutorials
- **[Tutorial Basics](../../02-tutorials/tutorial-basics/index.md)** - Hands-on practice with multi-state patterns

---
sidebar_position: 25
title: 'Multi-State Transitions Guide'
---

# Multi-State Transitions Developer Guide

## Quick Reference

### Key Concepts

| Concept | Description |
|---------|------------|
| **No Primary Target** | All states in `activate` set are equal |
| **Path Node Eligibility** | ANY activated state can be a path node |
| **Path Success** | Only next path node needs activation |
| **IncomingTransition Execution** | ALL activated states verify arrival |

### Code Patterns

#### Defining Multi-State Transitions

```java
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
@TransitionSet(state = LoginState.class)
public class LoginTransitions {

    @OutgoingTransition(to = ApplicationState.class)
    public boolean openApplication() {
        // This click activates multiple states defined in ApplicationState
        return action.click(loginState.getLoginButton()).isSuccess();
    }
}
```

## Common Scenarios

### 1. Login Opens Entire Application

**Scenario**: Login button opens dashboard, navigation, sidebar, and status bar simultaneously.

```java
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
navigator.openState("Settings");
// Might use: Login → Navigation → Settings
// Or: Login → Sidebar → Settings
// Whichever path exists and is shortest
```

### 2. Tab Switching with Persistent Navigation

**Scenario**: Switching tabs changes content but keeps navigation active.

```java
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
// Open modal keeping background visible
JavaStateTransition openSettings = new JavaStateTransition.Builder()
    .setFunction(() -> action.click(settingsIcon).isSuccess())
    .addToActivate("SettingsModal")
    .addToActivate("ModalOverlay")
    // Dashboard NOT in exit list - remains visible
    .setStaysVisibleAfterTransition(StaysVisible.TRUE)
    .build();
```

### 4. Contextual State Activation

**Scenario**: Different states activate based on user role or context.

```java
public class ContextualTransition {

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
navigator.openState("Target");

// Pathfinder explores:
// From A: Can reach B, C, D
// From B: Can reach E
// From E: Can reach Target ✓
// Path found: A → B → E → Target

// Even though A also activated C and D,
// the path only needs B to continue
```

### Testing Multiple Paths

```java
@Test
public void testMultiplePathsToTarget() {
    // Setup: Login activates [Dashboard, Menu]
    // Dashboard can reach Settings
    // Menu can also reach Settings

    // Path 1: Via Dashboard
    stateMemory.clear();
    stateMemory.addActiveState("Login");
    Paths paths = pathFinder.getPathsToState(
        Set.of("Login"), "Settings"
    );

    // Should find both paths:
    // Login → Dashboard → Settings
    // Login → Menu → Settings
    assertEquals(2, paths.getPaths().size());
}
```

## Verification Patterns

### Complete Activation Verification

```java
@TransitionSet(state = WorkspaceState.class)
public class WorkspaceTransitions {

    @IncomingTransition
    public boolean verifyCompleteActivation() {
        // Check ALL expected components
        boolean dashboardVisible = action.find(dashboard).isSuccess();
        boolean navVisible = action.find(navigation).isSuccess();
        boolean sidebarVisible = action.find(sidebar).isSuccess();
        boolean statusVisible = action.find(statusBar).isSuccess();

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
// Check what states a transition activates
StateTransition transition = getTransition();
System.out.println("Activates: " + transition.getActivate());
System.out.println("Exits: " + transition.getExit());

// Verify joint table indexing
Set<Long> parents = jointTable.getIncomingTransitions(stateId);
System.out.println("Can be reached from: " + parents);

// Trace path finding
Paths paths = pathFinder.getPathsToState(activeStates, target);
paths.getPaths().forEach(System.out::println);
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
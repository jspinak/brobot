# Special States Example

This example demonstrates the use of special state markers in Brobot:
- **PreviousState** - Dynamic transitions that return to hidden states
- **CurrentState** - Self-transitions that stay in or re-enter the current state

## Project Structure

```
special-states-example/
├── src/main/java/com/example/specialstates/
│   ├── states/
│   │   ├── MainPageState.java       # Main page that can be hidden by modal
│   │   ├── ModalDialogState.java    # Modal that overlays other states
│   │   └── SettingsPageState.java   # Settings page that can also be hidden
│   ├── transitions/
│   │   ├── MainPageTransitions.java     # Includes CurrentState transitions
│   │   ├── ModalDialogTransitions.java  # Uses PreviousState to return
│   │   └── SettingsPageTransitions.java # Can open modal and self-transition
│   ├── runner/
│   │   └── SpecialStatesTestRunner.java # Test scenarios
│   └── SpecialStatesApplication.java    # Main Spring Boot application
└── src/main/resources/
    └── application.properties        # Mock mode configuration

```

## Test Scenarios

### Test 1: PreviousState with MainPage Hidden
1. Navigate to MainPage
2. Open Modal (MainPage becomes hidden)
3. Navigate to MainPage target (triggers PreviousState transition from Modal)
4. Verify return to MainPage

### Test 2: PreviousState with SettingsPage Hidden
1. Navigate to SettingsPage
2. Open Modal (SettingsPage becomes hidden)
3. Navigate to SettingsPage target (triggers PreviousState transition from Modal)
4. Verify return to SettingsPage

### Test 3: CurrentState Self-Transitions
1. Navigate to MainPage
2. Execute refresh (CurrentState transition)
3. Navigate to SettingsPage
4. Execute save settings (CurrentState transition)

## Key Concepts Demonstrated

### Modal State Configuration with canHide
For PreviousState transitions to work, the modal state MUST declare which states it can hide:

```java
@State(
    description = "Modal dialog overlay",
    canHide = {"MainPage", "SettingsPage"}  // CRITICAL: Required for PreviousState
)
@Component
public class ModalDialogState {
    // State implementation
}
```

**Why canHide is Critical:**
- Without `canHide`, the framework doesn't know which states to track as hidden
- PreviousState transitions would have nothing to return to
- The state names in `canHide` must match the state names (without "State" suffix)

### PreviousState
```java
@OutgoingTransition(
    activate = {PreviousState.class},  // Returns to whatever was hidden
    staysVisible = false,              // Modal closes
    pathCost = 0,
    description = "Close modal and return to previous state"
)
public boolean closeModal() {
    // Returns to MainPage OR SettingsPage depending on what was hidden
    return true;
}
```

### CurrentState
```java
@OutgoingTransition(
    activate = {CurrentState.class},  // Stay in current state
    pathCost = 2,
    description = "Refresh page"
)
public boolean refresh() {
    // Executes action but stays on same page
    return true;
}
```

### Hidden State Management

The Brobot framework automatically tracks hidden states when overlays appear:

**How It Works:**
1. When Modal opens over MainPage with `staysVisible = true`:
   - MainPage is automatically moved to "hidden" status
   - Modal becomes the active state
   - The hidden state is tracked in state memory

2. When PreviousState transition is executed from Modal:
   - Framework looks up which state was most recently hidden
   - Resolves PreviousState to that hidden state (e.g., MainPage)
   - Returns to the hidden state and marks it as active again

**Key Requirements:**
- Modal state must declare `canHide = {"MainPage", "SettingsPage"}` in @State annotation
- Transition that opens modal must use `staysVisible = true` parameter
- Transition that closes modal should use `activate = {PreviousState.class}` and `staysVisible = false`

**Example Flow:**
```
MainPage (active) -> openModal() [staysVisible=true]
  -> Modal (active), MainPage (hidden) -> closeModal() [activate={PreviousState.class}]
  -> MainPage (active)
```

## Running the Example

```bash
# From the brobot root directory
./gradlew :examples:03-core-library:guides:dynamic-transitions:special-states-example:bootRun

# Or build and run as JAR
./gradlew :examples:03-core-library:guides:dynamic-transitions:special-states-example:build
java -jar examples/03-core-library/guides/dynamic-transitions/special-states-example/build/libs/special-states-example-*.jar
```

The application will:
1. Start in mock mode (no real GUI required)
2. Execute all test scenarios automatically via CommandLineRunner
3. Log state transitions and results with detailed explanations
4. Demonstrate successful PreviousState and CurrentState usage
5. Print active and hidden states at each step

## Configuration

The `application.properties` file provides configuration for mock mode testing:

```properties
# Enable mock mode for testing without GUI
brobot.mock=true

# Mock timing configuration (fast for testing)
brobot.action.maxWait=0.5
brobot.action.delay=0.01
brobot.action.moveMouseDelay=0.01

# Logging configuration
brobot.logging.global-level=INFO
logging.level.com.example=DEBUG
logging.level.io.github.jspinak.brobot=INFO
```

Key Configuration Features:
- Mock mode for testing without GUI (no SikuliX or display required)
- Fast mock timings for quick test execution
- Structured logging system with categories (actions, transitions, matching, performance)
- Output format options (SIMPLE for human-readable, STRUCTURED/JSON for testing)
- Data enrichment options (similarity scores, timing breakdown, screenshots)

## Expected Output

You should see detailed logs for each test scenario:

### Test 1 Output Example:
```
========================================
TEST 1: PreviousState with MainPage hidden by Modal
========================================
Step 1: Navigating to MainPage...
MainPage opened: true
Current active states: [MainPage]

Step 2: Opening Modal from MainPage...
The modal will automatically hide MainPage because ModalDialog has canHide=["MainPage", "SettingsPage"]
Modal opened: true
Current active states: [ModalDialog]
Hidden states tracked by ModalDialog: [MainPage]

Step 3: Using navigator.openState(MainPage) to test PreviousState transition
Expected: Modal should close using PreviousState transition to return to MainPage
Returned to MainPage via PreviousState: true
Current active states: [MainPage]

✅ TEST 1 PASSED: Successfully returned to MainPage using PreviousState transition
```

Final Summary:
- ✅ TEST 1 PASSED: PreviousState returns to MainPage
- ✅ TEST 2 PASSED: PreviousState returns to SettingsPage
- ✅ TEST 3 PASSED: CurrentState self-transitions work

## Integration with Brobot

This example shows the "Brobot way" of handling:
- Modal dialogs that can appear over any state
- Dynamic returns to hidden states
- Self-transitions for in-page actions
- Proper use of the state management system

### Key Integration Points:

**StateNavigator Usage:**
Instead of directly calling transition methods, the example uses `navigator.openState()` which:
- Properly utilizes the state management system
- Triggers pathfinding to find the optimal route
- Automatically resolves PreviousState and CurrentState markers
- Tracks state visibility changes

**StateMemory and StateVisibilityManager:**
The framework automatically:
- Tracks active states via `StateMemory`
- Manages hidden states via `StateVisibilityManager`
- Updates visibility when states with `canHide` are activated
- Resolves PreviousState references at runtime

**Why Use Special States vs. Direct Transitions:**
```java
// ❌ BAD: Hard-coded transitions for each source state
@OutgoingTransition(activate = {MainPageState.class}, ...)
public boolean closeToMainPage() { ... }

@OutgoingTransition(activate = {SettingsPageState.class}, ...)
public boolean closeToSettings() { ... }

// ✅ GOOD: Single dynamic transition using PreviousState
@OutgoingTransition(activate = {PreviousState.class}, ...)
public boolean closeModal() {
    // Returns to whatever was hidden - no duplication needed
}
```

## Related Documentation

For complete understanding of special states and dynamic transitions:
- **[Dynamic Transitions Guide](/docs/core-library/guides/user-guides/dynamic-transitions)** - Comprehensive reference with advanced patterns
- **[Special States Tutorial](/docs/core-library/tutorials/tutorial-special-states)** - Step-by-step tutorial
- **[Core Concepts - Hidden States](/docs/getting-started/core-concepts#handling-dynamic-overlays-hidden-states)** - Brief overview
- **[Transitions Overview](/docs/getting-started/transitions)** - General transition concepts
- **[Pathfinding](/docs/getting-started/core-concepts#pathfinding)** - How Brobot calculates optimal paths
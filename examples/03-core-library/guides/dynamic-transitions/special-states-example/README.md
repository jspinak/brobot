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

### PreviousState
```java
@OutgoingTransition(
    to = PreviousState.class,  // Returns to whatever was hidden
    staysVisible = false,      // Modal closes
    pathCost = 0
)
public boolean closeModal() {
    // Returns to MainPage OR SettingsPage depending on what was hidden
    return true;
}
```

### CurrentState
```java
@OutgoingTransition(
    to = CurrentState.class,  // Stay in current state
    pathCost = 2,
    description = "Refresh page"
)
public boolean refresh() {
    // Executes action but stays on same page
    return true;
}
```

### Hidden State Management
When Modal opens over MainPage:
1. MainPage is automatically tracked as "hidden"
2. Modal becomes active
3. PreviousState transitions from Modal resolve to MainPage

## Running the Example

```bash
# From the brobot root directory
./gradlew :examples:special-states-example:bootRun
```

The application will:
1. Start in mock mode (no real GUI required)
2. Execute all test scenarios
3. Log state transitions and results
4. Demonstrate successful PreviousState and CurrentState usage

## Configuration

The `application.properties` file enables:
- Mock mode for testing without GUI
- Verbose logging to see state transitions
- Fast mock timings for quick test execution

## Expected Output

You should see:
- ✅ TEST 1 PASSED: PreviousState returns to MainPage
- ✅ TEST 2 PASSED: PreviousState returns to SettingsPage
- ✅ TEST 3 PASSED: CurrentState self-transitions work

## Integration with Brobot

This example shows the "Brobot way" of handling:
- Modal dialogs that can appear over any state
- Dynamic returns to hidden states
- Self-transitions for in-page actions
- Proper use of the state management system

Instead of directly calling transition methods, the example uses `navigator.openState()` which properly utilizes the state management system and special state markers.
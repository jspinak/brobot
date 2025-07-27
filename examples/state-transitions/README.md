# State Transitions Example

This example demonstrates advanced state management and transitions in Brobot with automatic logging at every step.

## Key Features

### 1. State-Based Navigation
- Define application states with unique visual identifiers
- Navigate between states using visual cues
- Automatic state verification with logging

### 2. Transition Logging
Every state transition is automatically logged:
```
[TRANSITION] Navigating from Login to Dashboard...
[ACTION] Looking for Dashboard link...
[ACTION] Found Dashboard link
[ACTION] Clicking Dashboard link...
[ACTION] Successfully clicked Dashboard link
[TRANSITION] Successfully transitioned to Dashboard state in 1250ms
```

### 3. Multi-Path Navigation
The example shows:
- Direct transitions (Login → Dashboard)
- Multi-step transitions (Login → Dashboard → Settings → Profile)
- Conditional transitions based on current state
- Error recovery when transitions fail

### 4. State Verification
Each state has verification logic with automatic logging:
- Check for multiple state indicators
- Verify state stability
- Handle partial state loads

## Example States

The demo application includes these states:

1. **LoginState** - The entry point
   - Username/password fields
   - Login button
   - "Remember me" checkbox

2. **DashboardState** - Main application hub
   - Navigation menu
   - User welcome message
   - Quick action buttons

3. **SettingsState** - Configuration page
   - Settings categories
   - Save/Cancel buttons
   - Configuration options

4. **ProfileState** - User profile
   - Profile information
   - Edit capabilities
   - Avatar display

5. **LogoutState** - Exit confirmation
   - Logout confirmation
   - Session cleanup

## Running the Example

1. Ensure you have screenshots of state indicators in `src/main/resources/images/`
2. Run: `./gradlew bootRun`
3. Watch the console for detailed transition logging

## Project Structure

```
state-transitions/
├── src/main/java/com/example/states/
│   ├── StateTransitionsApplication.java  # Main application
│   ├── states/
│   │   ├── LoginState.java              # Login page state
│   │   ├── DashboardState.java          # Dashboard state
│   │   ├── SettingsState.java           # Settings state
│   │   └── ProfileState.java            # Profile state
│   ├── transitions/
│   │   ├── LoginToDashboard.java        # Custom transition
│   │   └── NavigationHelper.java        # Navigation utilities
│   └── automation/
│       └── StateNavigationDemo.java     # Demo scenarios
└── src/main/resources/
    ├── application.yml                   # Configuration
    └── images/                          # State indicator images
```

## Logging Examples

### Successful Navigation
```
[INFO] Starting navigation: Login → Dashboard → Settings
[TRANSITION] Navigating from Login to Dashboard...
[ACTION] Looking for username field...
[ACTION] Found username field
[ACTION] Typing username...
[ACTION] Successfully typed username
[ACTION] Looking for password field...
[ACTION] Found password field
[ACTION] Typing password...
[ACTION] Successfully typed password
[ACTION] Looking for login button...
[ACTION] Found login button
[ACTION] Clicking login button...
[ACTION] Successfully clicked login button
[TRANSITION] Successfully transitioned to Dashboard state in 2341ms
[TRANSITION] Navigating from Dashboard to Settings...
[ACTION] Looking for Settings menu item...
[ACTION] Found Settings menu item
[ACTION] Clicking Settings menu item...
[ACTION] Successfully clicked Settings menu item
[TRANSITION] Successfully transitioned to Settings state in 1523ms
[INFO] Navigation completed successfully
```

### Failed Transition with Recovery
```
[TRANSITION] Navigating from Dashboard to Profile...
[ACTION] Looking for Profile link...
[ACTION] Profile link not found - menu may be collapsed
[INFO] Attempting alternative navigation path...
[ACTION] Looking for menu toggle...
[ACTION] Found menu toggle
[ACTION] Clicking menu toggle...
[ACTION] Successfully clicked menu toggle
[ACTION] Looking for Profile link...
[ACTION] Found Profile link
[ACTION] Clicking Profile link...
[ACTION] Successfully clicked Profile link
[TRANSITION] Successfully transitioned to Profile state in 3214ms (with recovery)
```

## Customization

1. Add new states by extending the State class
2. Define custom transitions for complex navigation
3. Configure transition timeouts in application.yml
4. Add state verification logic for robustness
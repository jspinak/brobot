# Illustration Features in Brobot v1.1.0

This guide demonstrates the actual IllustrationController API available in v1.1.0.

## Available Illustration Features in v1.1.0

### 1. IllustrationController API
The IllustrationController provides programmatic control over illustrations:

```java
// Check if an action should be illustrated
boolean shouldIllustrate = illustrationController.okToIllustrate(
    actionConfig, objectCollection
);

// Manually create an illustration
boolean illustrated = illustrationController.illustrateWhenAllowed(
    actionResult, searchRegions, actionConfig, objectCollection
);
```

### 2. Configuration via Properties
Base configuration is done through `application.properties`:

```properties
# Enable/disable illustration for different action types
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=true
brobot.illustration.draw-move=true
brobot.illustration.draw-highlight=true

# Screenshot and history settings
brobot.screenshot.save-snapshots=false
brobot.screenshot.save-history=true
brobot.screenshot.path=screenshots/
brobot.screenshot.history-path=history/
brobot.screenshot.filename=screen
brobot.screenshot.history-filename=hist
```

### 3. IllustrationController Features
- **okToIllustrate()** - Check if illustration should happen
- **illustrateWhenAllowed()** - Create illustration with filtering
- **Duplicate Prevention** - Automatically filters repeated actions
- **Action Permissions** - Per-action type control via properties
- **State Tracking** - Tracks last action to prevent duplicates

### 4. What's NOT Available in v1.1.0
- setConfig() method on IllustrationController
- IllustrationConfig builder class
- Adaptive sampling algorithms
- Context-aware illustration filters
- Quality-based filtering
- BatchConfig or PerformanceMetrics
- Dynamic runtime configuration changes

## Working with IllustrationController

### Basic Usage

```java
@Component
@RequiredArgsConstructor
public class MyAutomation {
    private final Action action;
    private final IllustrationController illustrationController;
    
    public void performAction() {
        ObjectCollection target = new ObjectCollection.Builder()
            .withImages(myImage)
            .build();
        
        // Check if illustration would happen
        ClickOptions clickConfig = new ClickOptions.Builder().build();
        if (illustrationController.okToIllustrate(clickConfig, target)) {
            log.info("This action will be illustrated");
        }
        
        // Perform action (illustration happens automatically)
        action.click(target);
    }
}
```

### Manual Illustration Control

```java
// Force illustration
ClickOptions forceIllustrate = new ClickOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.YES)
    .build();

// Prevent illustration
ClickOptions noIllustrate = new ClickOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.NO)
    .build();
```

### Understanding Duplicate Filtering

```java
// First click will be illustrated
action.click(button);  // ✓ Illustrated

// Immediate repeat is filtered
action.click(button);  // ✗ Not illustrated (duplicate)

// Different target is illustrated
action.click(otherButton);  // ✓ Illustrated
```

## Configuration Examples

### Development Mode
```properties
# Full illustrations for debugging
brobot.screenshot.save-history=true
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=true
brobot.illustration.draw-move=true
brobot.illustration.draw-highlight=true
```

### Production Mode
```properties
# Minimal illustrations
brobot.screenshot.save-history=true
brobot.illustration.draw-find=false
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=false
brobot.illustration.draw-move=false
brobot.illustration.draw-highlight=false
```

## Real Example: Login Workflow

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginWorkflowExample {
    
    private final Action action;
    private final StateManager stateManager;
    
    public void executeLogin(String username, String password) {
        log.info("Executing login workflow");
        
        // Navigate to login state
        if (stateManager.goToState("LOGIN")) {
            // These actions will be illustrated based on properties
            ObjectCollection loginButton = new ObjectCollection.Builder()
                .withImages(getLoginButton())
                .build();
                
            if (action.click(loginButton).isSuccess()) {
                // Type username
                ObjectCollection usernameField = new ObjectCollection.Builder()
                    .withImages(getUsernameField())
                    .build();
                    
                action.click(usernameField);
                action.type(new ObjectCollection.Builder()
                    .withStrings(username)
                    .build());
                
                // Type password
                ObjectCollection passwordField = new ObjectCollection.Builder()
                    .withImages(getPasswordField())
                    .build();
                    
                action.click(passwordField);
                action.type(new ObjectCollection.Builder()
                    .withStrings(password)
                    .build());
                
                // Submit
                ObjectCollection submitButton = new ObjectCollection.Builder()
                    .withImages(getSubmitButton())
                    .build();
                    
                action.click(submitButton);
            }
        }
    }
}
```

## Performance Considerations

Since v1.1.0 doesn't have dynamic illustration control:

1. **Development**: Enable all illustrations for debugging
2. **Testing**: Enable only critical illustrations (clicks)
3. **Production**: Disable most illustrations except errors

## Directory Structure

```
project-root/
├── history/              # Illustrated action history
│   └── hist_*.png       # History screenshots
├── screenshots/         # Regular screenshots
│   └── screen_*.png     # Snapshot files
└── src/
    └── main/
        └── resources/
            └── application.properties
```

## Migration Guide

If you need advanced illustration features:

1. **Use screenshot capture**: Manually capture screenshots at critical points
2. **Custom logging**: Add detailed logging for action tracking
3. **External tools**: Use screen recording software for complex workflows
4. **Wait for updates**: Advanced illustration APIs may be added in future versions

## Alternative Approaches

### 1. Manual Screenshot Capture
```java
// Capture screenshot at critical points
@Value("${brobot.screenshot.path}")
private String screenshotPath;

public void captureCustomScreenshot(String name) {
    // Implementation depends on platform
    log.info("Capturing screenshot: {}", name);
}
```

### 2. Action Logging Framework
```java
@Component
@Aspect
public class ActionLogger {
    @Around("@annotation(LogAction)")
    public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Action started: {}", joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();
        log.info("Action completed: {}", result);
        return result;
    }
}
```

### 3. Test-Specific History
```java
@Test
public void testWithHistory() {
    // Enable history for this test
    System.setProperty("brobot.screenshot.save-history", "true");
    
    // Run test
    // ...
    
    // Reset
    System.clearProperty("brobot.screenshot.save-history");
}
```

## Summary

The v1.1.0 illustration system is property-based and static. For dynamic illustration needs:
- Use property files for different environments
- Implement custom logging/screenshot solutions
- Consider external recording tools
- Design tests to work with static illustration settings
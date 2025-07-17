# Running the Claude Automator

## Build and Run

### Using Gradle

```bash
# Clean and build
./gradlew clean build

# Run the application
./gradlew bootRun
```

### Using IDE

1. Open the project in IntelliJ IDEA or Eclipse
2. Run `ClaudeAutomatorApplication.java` as a Spring Boot application
3. Watch the console for log output

## Expected Console Output

```
INFO  c.c.a.ClaudeAutomatorApplication - Starting ClaudeAutomatorApplication
INFO  i.g.j.b.startup.AutoStartupVerifier - === Brobot Auto Startup Verification: Phase 1 (Images) ===
INFO  i.g.j.b.startup.ApplicationStartupVerifier - Discovered 7 images from 2 states
INFO  i.g.j.b.startup.ApplicationStartupVerifier - All 7 required images verified successfully
INFO  c.c.a.config.StateRegistrationListener - Application ready - registering states with StateService
INFO  c.c.a.config.StateRegistrationListener - States and transitions registered successfully
INFO  i.g.j.b.startup.AutoStartupVerifier - === Brobot Auto Startup Verification: Phase 2 (States) ===
INFO  i.g.j.b.startup.ApplicationStartupVerifier - Successfully verified expected states. Active states: [PROMPT]
INFO  c.c.a.automation.ClaudeMonitoringAutomation - Starting Claude monitoring automation
INFO  c.c.a.ClaudeAutomatorApplication - Started ClaudeAutomatorApplication in 3.456 seconds
```

## Testing the Automation

### 1. Initial State

- The auto-verifier detects which state is currently visible
- Open Claude AI in your browser before starting the app
- The automation will automatically set the correct initial state

### 2. Trigger Transition

- The automation will find and click the prompt
- Type "continue" and press Enter
- Both Prompt and Working states become active

### 3. Monitor Working State

- The automation checks every 2 seconds for Claude's icon
- When Claude finishes responding (icon disappears):
  - Working state is removed from active states
  - Working state is reopened to wait for the next response

### 4. Continuous Operation

- The cycle continues indefinitely
- Monitor the logs to see the automation in action

## Debugging Tips

### Enable Debug Logging

Add to `application.properties`:

```properties
logging.level.com.claude.automator=DEBUG
logging.level.io.github.jspinak.brobot=DEBUG
```

### Visual Debugging

Enable Sikuli highlighting:

```properties
brobot.sikuli.highlight=true
brobot.sikuli.highlight.duration=2
```

### Common Issues and Solutions

#### Images Not Found

```
ERROR - Failed to load required image: prompt/claude-prompt-1.png
ERROR -   Suggestion: Check if file exists at: /path/to/images/prompt/claude-prompt-1.png
ERROR -   Suggestion: Verify image format is supported (PNG, JPG, JPEG, GIF, BMP)
```

**Solution**: 
- The auto-verifier provides specific suggestions
- Check the exact path shown in the error
- Verify image format and accessibility
- Add fallback paths in application.yml if needed

#### State Not Activating

```
ERROR - Failed to activate Working state
```

**Solution**:
- Check transition logic
- Verify state registration
- Review logs for transition failures

#### Monitoring Not Working

```
DEBUG - Working state is not active, skipping check
```

**Solution**:
- Ensure Working state is properly activated
- Check StateMemory for active states
- Verify transition completion

## Performance Tuning

### Adjust Monitoring Frequency

```java
// In ClaudeMonitoringAutomation
scheduler.scheduleWithFixedDelay(
    this::checkClaudeIconStatus,
    5,  // Initial delay - increase for slower startup
    1,  // Check interval - decrease for faster response
    TimeUnit.SECONDS
);
```

### Optimize Image Search

```java
// Use search regions to improve performance
PatternFindOptions optimizedFind = new PatternFindOptions.Builder()
    .setSearchRegion(new Region(100, 100, 400, 300))  // Limit search area
    .setSimilarity(0.9)  // Adjust for accuracy vs speed
    .build();
```

## Production Considerations

### 1. Error Recovery

```java
@Component
public class ErrorRecoveryService {
    @EventListener
    public void handleAutomationError(AutomationErrorEvent event) {
        log.error("Automation error: {}", event.getMessage());
        // Implement recovery logic
    }
}
```

### 2. Metrics and Monitoring

```java
@Component
public class AutomationMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordTransitionSuccess(String fromState, String toState) {
        meterRegistry.counter("transitions.success", 
            "from", fromState, 
            "to", toState).increment();
    }
}
```

### 3. Graceful Shutdown

```java
@PreDestroy
public void cleanup() {
    log.info("Shutting down Claude Automator");
    // Save state, cleanup resources
}
```

## Next Steps

Congratulations! You've built a complete Brobot automation using modern patterns. Consider:

1. **Extending the automation** with more states and complex workflows
2. **Adding configuration UI** using Brobot's desktop runner
3. **Creating tests** using Brobot's mock capabilities
4. **Contributing improvements** back to the Brobot library

## Summary

This tutorial demonstrated:

- ✅ Modern state patterns with direct component access
- ✅ JavaStateTransition for flexible navigation
- ✅ Fluent API and action chaining
- ✅ Spring Boot integration
- ✅ Continuous monitoring patterns
- ✅ Enhanced developer experience features

The complete source code serves as a template for building robust, maintainable automations with Brobot v1.1.0.
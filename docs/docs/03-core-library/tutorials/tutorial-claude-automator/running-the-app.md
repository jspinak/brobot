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
INFO  c.c.a.config.StateConfiguration - Registering states and transitions for Claude Automator
INFO  c.c.a.config.StateConfiguration - Registered states: WORKING, PROMPT
INFO  c.c.a.config.StateConfiguration - Registered transitions for Working and Prompt states
INFO  c.c.a.config.StateConfiguration - Set PROMPT as initial active state
INFO  c.c.a.config.StateConfiguration - State registration complete
INFO  c.c.a.automation.ClaudeMonitoringAutomation - Starting Claude monitoring automation
INFO  c.c.a.ClaudeAutomatorApplication - Started ClaudeAutomatorApplication in 2.345 seconds
```

## Testing the Automation

### 1. Initial State

- The application starts with the Prompt state active
- Open Claude AI in your browser
- The automation will look for the prompt interface

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
WARN - Failed to find claude-prompt image
```

**Solution**: 
- Verify images are in the correct location
- Check image quality and cropping
- Adjust similarity threshold if needed

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
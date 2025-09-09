# AspectJ Usage Guide for Brobot

## Overview

This guide explains how to use the AspectJ enhancements in the Brobot framework. AspectJ provides powerful cross-cutting features without modifying existing code, including error recovery, performance monitoring, visual feedback, and more.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Available Aspects](#available-aspects)
3. [Examples](#examples)
4. [Best Practices](#best-practices)
5. [Troubleshooting](#troubleshooting)

## Quick Start

### 1. Enable AspectJ in Your Application

```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 2. Add Configuration Properties

Create or update `application.properties`:

```properties
# Enable core aspects
brobot.aspects.sikuli.enabled=true
brobot.aspects.action-lifecycle.enabled=true
brobot.aspects.performance.enabled=true
brobot.aspects.state-transition.enabled=true

# Enable optional aspects
brobot.aspects.error-recovery.enabled=true
brobot.aspects.dataset.enabled=true
brobot.aspects.multi-monitor.enabled=true
brobot.aspects.visual-feedback.enabled=true
```

### 3. Use Annotations (Optional)

```java
@Recoverable(maxRetries = 3, delay = 1000)
public ActionResult clickLoginButton() {
    return action.perform(clickOptions, loginButton);
}

@Monitored(threshold = 5000, trackMemory = true)
@CollectData(category = "login_automation")
public void performLogin(String username, String password) {
    // Your automation code
}
```

## Available Aspects

### 1. SikuliInterceptionAspect

**Purpose**: Intercepts all Sikuli method calls for error handling and mock mode.

**Benefits**:
- Automatic error translation
- Mock mode without code changes
- Performance metrics for Sikuli operations

**Configuration**:
```properties
brobot.aspects.sikuli.enabled=true
brobot.aspects.sikuli.performance-warning-threshold=5000
```

### 2. ActionLifecycleAspect

**Purpose**: Manages action execution lifecycle (pre/post tasks).

**Benefits**:
- Automatic timing and logging
- Screenshot capture
- Pause point management

**Configuration**:
```properties
brobot.aspects.action-lifecycle.enabled=true
brobot.aspects.action-lifecycle.capture-before-screenshot=false
brobot.aspects.action-lifecycle.capture-after-screenshot=true
brobot.action.pre-pause=0
brobot.action.post-pause=0
```

### 3. PerformanceMonitoringAspect

**Purpose**: Tracks performance metrics for all operations.

**Benefits**:
- Method-level performance tracking
- Automatic slow operation detection
- Performance trend analysis

**Configuration**:
```properties
brobot.aspects.performance.enabled=true
brobot.aspects.performance.alert-threshold=10000
brobot.aspects.performance.report-interval=300
```

**Using @Monitored**:
```java
@Monitored(threshold = 3000, tags = {"critical", "ui"})
public void criticalOperation() {
    // Operation that should complete within 3 seconds
}
```

### 4. StateTransitionAspect

**Purpose**: Tracks and visualizes state machine transitions.

**Benefits**:
- State transition graph building
- Success rate tracking
- DOT file visualization generation

**Configuration**:
```properties
brobot.aspects.state-transition.enabled=true
brobot.aspects.state-transition.generate-visualizations=true
brobot.aspects.state-transition.visualization-dir=./state-visualizations
```

### 5. ErrorRecoveryAspect

**Purpose**: Provides automatic retry logic with sophisticated policies.

**Benefits**:
- Configurable retry strategies
- Circuit breaker pattern
- Fallback methods

**Configuration**:
```properties
brobot.aspects.error-recovery.enabled=true
brobot.aspects.error-recovery.default-retry-count=3
brobot.aspects.error-recovery.default-retry-delay=1000
```

**Using @Recoverable**:
```java
@Recoverable(
    maxRetries = 5,
    delay = 2000,
    backoff = 2.0,
    retryOn = {NetworkException.class},
    fallbackMethod = "loginFallback"
)
public boolean login(String username, String password) {
    // Login logic that might fail
}

public boolean loginFallback(String username, String password) {
    // Alternative login approach
}
```

### 6. DatasetCollectionAspect

**Purpose**: Automatically collects datasets for ML training.

**Benefits**:
- Automatic data capture
- Configurable sampling
- Multiple output formats

**Configuration**:
```properties
brobot.aspects.dataset.enabled=true
brobot.aspects.dataset.output-dir=./ml-datasets
brobot.aspects.dataset.batch-size=100
```

**Using @CollectData**:
```java
@CollectData(
    category = "button_clicks",
    captureScreenshots = true,
    samplingRate = 0.1,
    labels = {"success", "ui_automation"}
)
public ActionResult clickButton(StateObject button) {
    return action.perform(clickOptions, button);
}
```

### 7. MultiMonitorRoutingAspect

**Purpose**: Routes actions to appropriate monitors in multi-monitor setups.

**Benefits**:
- Automatic monitor selection
- Load balancing
- Failover support

**Configuration**:
```properties
brobot.aspects.multi-monitor.enabled=true
brobot.aspects.multi-monitor.default-monitor=0
brobot.aspects.multi-monitor.enable-load-balancing=true
```

### 8. VisualFeedbackAspect

**Purpose**: Provides visual highlighting during automation execution.

**Benefits**:
- Search region highlighting
- Match visualization
- Action flow display

**Configuration**:
```properties
brobot.aspects.visual-feedback.enabled=true
brobot.aspects.visual-feedback.highlight-duration=2
brobot.aspects.visual-feedback.highlight-color=YELLOW
brobot.aspects.visual-feedback.show-action-flow=true
```

## Examples

### Example 1: Resilient Login with Retry

```java
@Component
public class LoginAutomation {
    
    @Autowired
    private Action action;
    
    @Recoverable(maxRetries = 3, delay = 2000, backoff = 1.5)
    @Monitored(threshold = 10000, tags = {"login", "critical"})
    @CollectData(category = "login_success_rate")
    public boolean performLogin(String username, String password) {
        // Find and click username field
        ActionResult usernameResult = action.perform(
            new ClickOptions(), 
            new ObjectCollection.Builder()
                .withStateObjects(usernameField)
                .build()
        );
        
        if (!usernameResult.isSuccess()) {
            throw new RuntimeException("Username field not found");
        }
        
        // Type username
        action.perform(new TypeOptions(username), emptyCollection);
        
        // Similar for password and login button...
        
        return true;
    }
}
```

### Example 2: Performance-Monitored Search

```java
@Component
public class SearchAutomation {
    
    @Monitored(
        name = "ProductSearch",
        threshold = 3000,
        trackMemory = true,
        tags = {"search", "performance-critical"}
    )
    public List<Match> findProducts(String productImage) {
        FindOptions options = new FindOptions.Builder()
            .setSimilarity(0.8)
            .setSearchRegion(productArea)
            .build();
            
        ActionResult result = action.perform(options, 
            new ObjectCollection.Builder()
                .withImages(productImage)
                .build()
        );
        
        return result.getMatchList();
    }
}
```

### Example 3: Multi-Monitor Automation

```java
@Component
public class MultiMonitorDemo {
    
    // This will automatically route to the appropriate monitor
    public void demonstrateMultiMonitor() {
        // Click on left monitor
        ObjectCollection leftButton = new ObjectCollection.Builder()
            .withStateObjects(buttonOnLeftMonitor)
            .build();
        action.perform(clickOptions, leftButton);
        
        // Click on right monitor - aspect handles routing
        ObjectCollection rightButton = new ObjectCollection.Builder()
            .withStateObjects(buttonOnRightMonitor)
            .build();
        action.perform(clickOptions, rightButton);
    }
}
```

## Best Practices

### 1. Selective Aspect Usage

Not all aspects need to be enabled. Choose based on your needs:
- Development: Enable visual feedback and verbose logging
- Testing: Enable performance monitoring and dataset collection
- Production: Enable error recovery and minimal logging

### 2. Performance Considerations

```properties
# Production settings
brobot.aspects.performance.enabled=true
brobot.aspects.performance.report-interval=3600  # Report hourly
brobot.aspects.visual-feedback.enabled=false     # Disable visual feedback
brobot.aspects.dataset.sampling-rate=0.01        # Sample only 1%
```

### 3. Error Recovery Patterns

```java
// Use specific exception types for better control
@Recoverable(
    retryOn = {FindFailed.class, StaleElementException.class},
    skipOn = {IllegalArgumentException.class},
    strategy = RecoveryStrategy.EXPONENTIAL_BACKOFF
)
public void robustAutomation() {
    // Your code
}

// Combine with circuit breaker for critical operations
@Recoverable(maxRetries = 5, timeout = 30000)
@Monitored(threshold = 5000)
public void criticalOperation() {
    // Your code
}
```

### 4. Dataset Collection Strategy

```java
// Collect data only for successful operations
@CollectData(
    category = "successful_clicks",
    onlySuccess = true,
    samplingRate = 0.2,
    maxSamples = 10000
)
public ActionResult performClick(StateObject target) {
    // Your code
}
```

## Troubleshooting

### Aspects Not Working

1. **Check if AspectJ is enabled**:
   ```java
   @EnableAspectJAutoProxy  // Required on main class
   ```

2. **Verify aspect configuration**:
   ```properties
   brobot.aspects.*.enabled=true
   ```

3. **Check logs for aspect initialization**:
   ```
   grep "Aspect initialized" application.log
   ```

### Performance Impact

If you notice performance degradation:

1. **Disable verbose logging**:
   ```properties
   logging.level.io.github.jspinak.brobot.aspects=WARN
   ```

2. **Reduce monitoring scope**:
   ```properties
   brobot.aspects.performance.exclude-patterns=.*toString,.*hashCode
   ```

3. **Use sampling**:
   ```java
   @Monitored(samplingRate = 0.1)  // Monitor only 10% of calls
   ```

### Memory Issues

For memory-intensive operations:

1. **Limit data collection**:
   ```properties
   brobot.aspects.dataset.max-queue-size=100
   brobot.aspects.dataset.batch-size=20
   ```

2. **Disable memory tracking**:
   ```properties
   brobot.aspects.performance.track-memory=false
   ```

### Compile-Time Weaving

For better performance, use compile-time weaving:

1. Add AspectJ Maven plugin:
   ```xml
   <plugin>
       <groupId>org.codehaus.mojo</groupId>
       <artifactId>aspectj-maven-plugin</artifactId>
       <version>1.14.0</version>
       <configuration>
           <complianceLevel>21</complianceLevel>
           <source>21</source>
           <target>21</target>
       </configuration>
       <executions>
           <execution>
               <goals>
                   <goal>compile</goal>
               </goals>
           </execution>
       </executions>
   </plugin>
   ```

2. Build with AspectJ compiler:
   ```bash
   mvn clean compile
   ```

## Conclusion

AspectJ integration in Brobot provides powerful cross-cutting features that enhance automation reliability, observability, and maintainability. By using these aspects appropriately, you can build more robust automation solutions with less code and better insights into system behavior.
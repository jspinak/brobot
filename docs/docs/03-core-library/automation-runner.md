# AutomationRunner - Graceful Failure Handling

## Overview

The `AutomationRunner` is a robust wrapper for automation tasks that provides enterprise-grade error handling, retry logic, and configuration-based behavior control. It ensures your automation applications can handle failures gracefully without crashing.

## Key Benefits

### 1. **Application Resilience**
- **No More Crashes**: Automation failures don't terminate your application
- **Graceful Degradation**: Continue with cleanup or alternative paths after failures
- **Service Continuity**: Perfect for long-running automation services

### 2. **Automatic Retry Logic**
- **Configurable Retries**: Set retry attempts via properties
- **Smart Delays**: Add delays between retry attempts
- **Reduced Flakiness**: Handle transient failures automatically

### 3. **Comprehensive Logging**
- **Structured Logs**: Track attempts, failures, and successes
- **Debug Control**: Toggle stack traces for production vs development
- **Audit Trail**: Complete record of automation execution

### 4. **Configuration-Driven**
- **Environment-Specific**: Different behaviors for dev/test/prod
- **No Code Changes**: Modify behavior through properties
- **Runtime Flexibility**: Change settings without recompilation

## Basic Usage

### Simple Example

```java
@Service
public class MyAutomation {
    
    @Autowired
    private AutomationRunner runner;
    
    @Autowired
    private StateNavigator navigator;
    
    public void runMyAutomation() {
        boolean success = runner.run(() -> {
            // Your automation logic here
            return navigator.openState("TargetState");
        });
        
        if (success) {
            log.info("Automation succeeded!");
        } else {
            log.error("Automation failed but app continues");
            // Handle failure gracefully
        }
    }
}
```

### Spring Boot Application Example

```java
@Slf4j
@SpringBootApplication
public class AutomationApplication {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AutomationApplication.class, args);
        
        AutomationRunner runner = context.getBean(AutomationRunner.class);
        MyAutomationService service = context.getBean(MyAutomationService.class);
        
        // Run with automatic retry and error handling
        boolean success = runner.run(
            service::performAutomation,
            "Main Automation Task"
        );
        
        if (!success) {
            // Application continues running - no System.exit()!
            log.warn("Automation failed but application remains active");
        }
    }
}
```

## Configuration Options

Configure behavior via `application.properties`:

```properties
# Don't exit application on failure (default: false)
brobot.automation.exit-on-failure=false

# Exit code if exit-on-failure is true (default: 1)
brobot.automation.failure-exit-code=1

# Throw exceptions for programmatic handling (default: false)
brobot.automation.throw-on-failure=false

# Log full stack traces (default: true)
brobot.automation.log-stack-traces=true

# Number of retry attempts (default: 0)
brobot.automation.max-retries=3

# Delay between retries in milliseconds (default: 1000)
brobot.automation.retry-delay-ms=2000

# Continue with remaining steps after failure (default: false)
brobot.automation.continue-on-failure=false

# Overall timeout in seconds (default: 0 = no timeout)
brobot.automation.timeout-seconds=300
```

## Advanced Usage

### Custom Task with Context

```java
public class AdvancedAutomation {
    
    @Autowired
    private AutomationRunner runner;
    
    public void runWithContext() {
        // Define a complex task
        AutomationRunner.AutomationTask task = () -> {
            try {
                // Step 1: Initialize
                if (!initialize()) return false;
                
                // Step 2: Navigate
                if (!navigateToTarget()) return false;
                
                // Step 3: Perform actions
                if (!performActions()) return false;
                
                // Step 4: Verify results
                return verifyResults();
                
            } catch (Exception e) {
                log.error("Task failed with exception", e);
                throw e; // Let runner handle it
            }
        };
        
        // Run with custom name for better logging
        boolean success = runner.run(task, "ComplexWorkflow");
    }
}
```

### Handling Different Failure Scenarios

```java
@Service
public class RobustAutomation {
    
    @Autowired
    private AutomationRunner runner;
    
    @Autowired
    private AutomationConfig config;
    
    public void executeWithFallback() {
        // Primary automation
        boolean primarySuccess = runner.run(this::primaryPath, "Primary Path");
        
        if (!primarySuccess && !config.isExitOnFailure()) {
            log.info("Primary path failed, trying fallback");
            
            // Fallback automation
            boolean fallbackSuccess = runner.run(this::fallbackPath, "Fallback Path");
            
            if (!fallbackSuccess) {
                // Both failed - perform cleanup
                performCleanup();
            }
        }
    }
    
    private boolean primaryPath() {
        // Primary automation logic
        return true;
    }
    
    private boolean fallbackPath() {
        // Alternative automation logic
        return true;
    }
    
    private void performCleanup() {
        // Cleanup resources
        log.info("Performing cleanup after automation failure");
    }
}
```

### Scheduled Automation Service

```java
@Service
@Slf4j
public class ScheduledAutomationService {
    
    @Autowired
    private AutomationRunner runner;
    
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void runScheduledAutomation() {
        log.info("Starting scheduled automation");
        
        boolean success = runner.run(() -> {
            // Your periodic automation logic
            return performScheduledTasks();
        }, "Scheduled Task");
        
        if (!success) {
            // Log failure but don't crash
            // Next scheduled run will try again
            log.warn("Scheduled automation failed, will retry on next schedule");
        }
    }
    
    private boolean performScheduledTasks() {
        // Implementation
        return true;
    }
}
```

## Integration with StateNavigator

The `AutomationRunner` works seamlessly with Brobot's `StateNavigator`:

```java
@Service
public class NavigationAutomation {
    
    @Autowired
    private AutomationRunner runner;
    
    @Autowired
    private StateNavigator navigator;
    
    public void navigateWithRetry() {
        // Navigator failures are handled gracefully
        boolean success = runner.run(() -> {
            // Navigate to multiple states
            if (!navigator.openState("LoginPage")) return false;
            if (!navigator.openState("Dashboard")) return false;
            if (!navigator.openState("Settings")) return false;
            return true;
        }, "Multi-State Navigation");
        
        // Application continues even if navigation fails
    }
}
```

## Exception Handling

### Using AutomationException

```java
public class ExceptionHandlingExample {
    
    @Autowired
    private AutomationRunner runner;
    
    public void handleExceptions() {
        try {
            // Configure to throw exceptions
            runner.getConfig().setThrowOnFailure(true);
            
            boolean success = runner.run(() -> {
                // This might throw AutomationException
                return riskyOperation();
            });
            
        } catch (AutomationException e) {
            // Handle structured exception
            log.error("Automation failed in state: {}, operation: {}", 
                     e.getStateName(), e.getOperation());
            
            if (e.isRecoverable()) {
                // Try recovery
                attemptRecovery();
            }
        }
    }
}
```

## Best Practices

### 1. **Always Use AutomationRunner for Main Tasks**
```java
// ❌ Don't do this
public static void main(String[] args) {
    boolean success = runAutomation();
    System.exit(success ? 0 : 1); // App crashes on failure!
}

// ✅ Do this instead
public static void main(String[] args) {
    AutomationRunner runner = context.getBean(AutomationRunner.class);
    boolean success = runner.run(this::runAutomation);
    // App continues running
}
```

### 2. **Configure for Your Environment**
```properties
# Development - verbose logging, no retries
brobot.automation.log-stack-traces=true
brobot.automation.max-retries=0

# Production - less verbose, with retries
brobot.automation.log-stack-traces=false
brobot.automation.max-retries=3
brobot.automation.retry-delay-ms=5000
```

### 3. **Use Descriptive Task Names**
```java
// Helps with debugging and monitoring
runner.run(task, "User Registration Flow");
runner.run(task, "Data Export Process");
runner.run(task, "Nightly Cleanup Job");
```

### 4. **Implement Proper Cleanup**
```java
public boolean automationWithCleanup() {
    try {
        return runner.run(() -> {
            // Automation logic
            return performTask();
        });
    } finally {
        // Always cleanup, even on failure
        cleanup();
    }
}
```

## Monitoring and Observability

The AutomationRunner provides detailed logs for monitoring:

```
INFO  Starting User Login Test (attempt 1 of 3)
WARN  User Login Test failed on attempt 1 of 3
INFO  Waiting 2000ms before retry
INFO  Starting User Login Test (attempt 2 of 3)
INFO  User Login Test completed successfully
```

You can integrate with monitoring systems:

```java
@Component
public class MonitoredAutomation {
    
    @Autowired
    private AutomationRunner runner;
    
    @Autowired
    private MeterRegistry meterRegistry; // Micrometer
    
    public void runWithMetrics() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        boolean success = runner.run(this::automationTask);
        
        sample.stop(Timer.builder("automation.execution")
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
    }
}
```

## Migration Guide

### From Direct Execution to AutomationRunner

**Before:**
```java
public class OldAutomation {
    public static void main(String[] args) {
        try {
            boolean success = performAutomation();
            System.exit(success ? 0 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```

**After:**
```java
public class NewAutomation {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NewAutomation.class);
        AutomationRunner runner = context.getBean(AutomationRunner.class);
        
        runner.run(() -> performAutomation(), "Main Automation");
        // No System.exit() - app continues or stops gracefully
    }
}
```

## Troubleshooting

### Common Issues

1. **Application Still Exits on Failure**
   - Check `brobot.automation.exit-on-failure` is set to `false`
   - Ensure you're using AutomationRunner, not direct execution
   - Remove any `System.exit()` calls from your code

2. **Retries Not Working**
   - Verify `brobot.automation.max-retries` is greater than 0
   - Check that your task returns `false` (not throwing exceptions)
   - Ensure `brobot.automation.retry-delay-ms` is reasonable

3. **Too Verbose Logging**
   - Set `brobot.automation.log-stack-traces=false` for production
   - Adjust Spring Boot logging levels in `application.properties`

## Summary

The `AutomationRunner` transforms brittle automation scripts into robust, production-ready applications. By providing automatic retry logic, graceful failure handling, and configuration-based behavior, it ensures your automation can handle real-world conditions without crashing your application.

Key takeaways:
- **Never call `System.exit()`** in automation code
- **Always wrap main automation tasks** with AutomationRunner
- **Configure behavior** through properties, not code
- **Handle failures gracefully** with fallback strategies
- **Monitor and log** for observability

With AutomationRunner, your automation becomes more reliable, maintainable, and production-ready.
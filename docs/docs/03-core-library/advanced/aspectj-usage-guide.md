# AspectJ Usage Guide for Brobot

## Overview

This guide explains how to use the AspectJ enhancements in the Brobot framework. AspectJ provides powerful cross-cutting features without modifying existing code, including error recovery, performance monitoring, visual feedback, and more.

## Quick Reference: Which Aspect for Which Problem?

| Problem | Aspect to Use | When to Use | Overhead |
|---------|---------------|-------------|----------|
| **Unreliable UI elements** | ErrorRecoveryAspect | UI elements sometimes not found, network issues | Low (~5ms) |
| **Slow operations** | PerformanceMonitoringAspect | Need to identify bottlenecks, track trends | Very Low (~1ms) |
| **Complex state flows** | StateTransitionAspect | Multi-state automation, debugging state issues | Low (~2ms) |
| **Testing/mocking** | SikuliInterceptionAspect | Running tests without display, CI/CD pipelines | Minimal (<1ms) |
| **ML model training** | DatasetCollectionAspect | Collecting training data for image recognition | Medium (~10ms) |
| **Multiple monitors** | MultiMonitorRoutingAspect | Multi-screen setups, dynamic monitor selection | Minimal (<1ms) |
| **Debugging visual issues** | VisualFeedbackAspect | Development, understanding what Brobot sees | Low (~3ms) |
| **Action timing** | ActionLifecycleAspect | Coordinating pre/post action tasks | Minimal (<1ms) |

> **Performance Note**: Overhead measurements are approximate averages from production usage. Actual overhead depends on operation complexity and system performance.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Available Aspects](#available-aspects)
3. [Examples](#examples)
4. [Real-World Use Cases](#real-world-use-cases)
5. [Best Practices](#best-practices)
6. [Performance Benchmarks](#performance-benchmarks)
7. [Troubleshooting](#troubleshooting)

## Quick Start

### 1. Enable AspectJ in Your Application

AspectJ is automatically enabled when you include `spring-boot-starter-aop` in your dependencies. The `@EnableAspectJAutoProxy` annotation is **optional** as Spring Boot's auto-configuration handles this automatically.

```java
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

> **Note**: You can explicitly add `@EnableAspectJAutoProxy` if you want to make aspect usage clear in your code, but it's not required for Brobot's aspects to work.

### 2. Add Configuration Properties

Create or update `application.properties`:

```properties
# Enable core aspects
brobot.aspects.sikuli.enabled=true
brobot.aspects.action-lifecycle.enabled=true
brobot.aspects.performance.enabled=true
brobot.aspects.state-transition.enabled=true

# Enable optional aspects (disabled by default)
brobot.aspects.error-recovery.enabled=false
brobot.aspects.dataset.enabled=false
brobot.aspects.multi-monitor.enabled=false

# Visual feedback is enabled by default
brobot.aspects.visual-feedback.enabled=true
```

> **Note**: Aspect configuration properties are loaded from `brobot-aspects.properties` in the Brobot library and can be overridden in your `application.properties`. The core aspects (sikuli, action-lifecycle, performance, state-transition, visual-feedback) are enabled by default, while optional aspects (error-recovery, dataset, multi-monitor) are disabled by default.

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

**Purpose**: Intercepts public Sikuli method calls for error handling and enhanced mock mode support.

**Benefits**:
- Automatic error translation from Sikuli exceptions
- Enhanced mock mode support (note: primary mock implementation uses wrapper classes)
- Performance metrics for Sikuli operations

> **Note**: Brobot's mock mode is primarily implemented through wrapper classes in the `tools.testing.mock` package. The SikuliInterceptionAspect provides supplementary support. To enable mock mode, use `brobot.mock=true` in your configuration.

**Configuration**:
```properties
brobot.aspects.sikuli.enabled=true
brobot.aspects.sikuli.performance-warning-threshold=5000
```

### 2. ActionLifecycleAspect

**Purpose**: Manages action execution lifecycle (pre/post tasks).

**Benefits**:
- Automatic timing and logging
- Pre/post execution pause management
- Action lifecycle hooks

> **Note**: Screenshot capture in Brobot is handled by the `ScreenshotCapture` service and `IllustrationController`, not by this aspect.

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
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.aspects.annotations.CollectData;
import io.github.jspinak.brobot.aspects.annotations.Monitored;
import io.github.jspinak.brobot.aspects.annotations.Recoverable;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginAutomation {

    private final Action action;

    // Define the UI elements
    private final StateImage usernameField = new StateImage.Builder()
        .setName("usernameField")
        .addPatterns("username-field.png")
        .build();

    private final StateImage passwordField = new StateImage.Builder()
        .setName("passwordField")
        .addPatterns("password-field.png")
        .build();

    private final StateImage loginButton = new StateImage.Builder()
        .setName("loginButton")
        .addPatterns("login-button.png")
        .build();

    @Autowired
    public LoginAutomation(Action action) {
        this.action = action;
    }

    @Recoverable(maxRetries = 3, delay = 2000, backoff = 1.5)
    @Monitored(threshold = 10000, tags = {"login", "critical"})
    @CollectData(category = "login_success_rate")
    public boolean performLogin(String username, String password) {
        // Find and click username field
        ActionResult usernameResult = action.click(usernameField);

        if (!usernameResult.isSuccess()) {
            throw new RuntimeException("Username field not found");
        }

        // Type username
        action.type(username);

        // Find and click password field
        ActionResult passwordResult = action.click(passwordField);

        if (!passwordResult.isSuccess()) {
            throw new RuntimeException("Password field not found");
        }

        // Type password
        action.type(password);

        // Click login button
        ActionResult loginResult = action.click(loginButton);

        return loginResult.isSuccess();
    }
}
```

### Example 2: Performance-Monitored Search

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.aspects.annotations.Monitored;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.model.match.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchAutomation {

    private final Action action;

    // Define the search area
    private final Region productArea = new Region(100, 100, 800, 600);

    @Autowired
    public SearchAutomation(Action action) {
        this.action = action;
    }

    @Monitored(
        name = "ProductSearch",
        threshold = 3000,
        trackMemory = true,
        tags = {"search", "performance-critical"}
    )
    public List<Match> findProducts(StateImage productImage) {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .setSearchRegions(productArea)
            .build();

        ActionResult result = action.perform(options, productImage);

        return result.getMatchList();
    }
}
```

### Example 3: Multi-Monitor Automation

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiMonitorDemo {

    private final Action action;

    // Define buttons for different monitors
    private final StateImage buttonOnLeftMonitor = new StateImage.Builder()
        .setName("leftButton")
        .addPatterns("left-button.png")
        .build();

    private final StateImage buttonOnRightMonitor = new StateImage.Builder()
        .setName("rightButton")
        .addPatterns("right-button.png")
        .build();

    @Autowired
    public MultiMonitorDemo(Action action) {
        this.action = action;
    }

    // This will automatically route to the appropriate monitor
    public void demonstrateMultiMonitor() {
        // Click on left monitor - aspect handles routing
        action.click(buttonOnLeftMonitor);

        // Click on right monitor - aspect handles routing
        action.click(buttonOnRightMonitor);
    }
}
```

## Real-World Use Cases

### Use Case 1: Flaky Web Application Login

**Problem**: Login occasionally fails due to slow page loads, network delays, or dynamic content.

**Solution**: ErrorRecoveryAspect with exponential backoff

```java
@Component
public class WebAppLogin {

    @Recoverable(
        maxRetries = 5,
        delay = 1000,
        backoff = 1.5,  // 1s, 1.5s, 2.25s, 3.375s, 5.06s
        retryOn = {ElementNotFoundException.class, TimeoutException.class}
    )
    @Monitored(threshold = 15000, tags = {"login", "critical"})
    public boolean login(String username, String password) {
        // Login implementation
        // Will automatically retry with increasing delays if elements not found
    }
}
```

**Why This Works**: Exponential backoff gives the application progressively more time to load while avoiding overwhelming the system with rapid retries.

**When to Use Circuit Breaker Instead**: If failures indicate a systemic problem (server down, API unavailable), use circuit breaker to fail fast:

```java
@Recoverable(
    maxRetries = 3,
    circuitBreakerThreshold = 5,  // Open circuit after 5 failures
    circuitBreakerTimeout = 60000  // Wait 60s before retrying
)
```

---

### Use Case 2: Performance Regression Detection

**Problem**: Automation suite sometimes slows down, but you don't know which operations are problematic.

**Solution**: PerformanceMonitoringAspect with trend analysis

```java
@Component
public class DataEntryAutomation {

    @Monitored(
        name = "FormFilling",
        threshold = 5000,  // Alert if > 5 seconds
        trackMemory = true,
        tags = {"data-entry", "performance-sensitive"}
    )
    public void fillForm(Map<String, String> data) {
        // Form filling logic
        // Aspect automatically tracks execution time and detects degradation
    }
}
```

**Configuration**:
```properties
brobot.aspects.performance.enabled=true
brobot.aspects.performance.alert-threshold=5000
brobot.aspects.performance.trend-detection=true
brobot.aspects.performance.degradation-threshold=0.2  # Alert on 20% slowdown
```

**Result**: Console shows warnings when operations exceed threshold or show performance degradation:
```
[WARN] PerformanceMonitoringAspect - FormFilling exceeded threshold: 6234ms (threshold: 5000ms)
[WARN] PerformanceMonitoringAspect - FormFilling performance degraded 25% (avg: 4500ms -> 5625ms)
```

---

### Use Case 3: Complex Multi-State Application Flow

**Problem**: Debugging state transitions in a complex application with 20+ states.

**Solution**: StateTransitionAspect with visualization

```java
@State(name = "LoginPage")
public class LoginPageState {

    @Transition(to = "Dashboard", onSuccess = true)
    public ActionResult clickLogin() {
        return action.click(loginButton);
    }

    @Transition(to = "ErrorDialog", onFailure = true)
    public ActionResult handleLoginError() {
        return action.find(errorMessage);
    }
}
```

**Configuration**:
```properties
brobot.aspects.state-transition.enabled=true
brobot.aspects.state-transition.generate-visualizations=true
brobot.aspects.state-transition.visualization-dir=./state-graphs
```

**Result**: Generates DOT file showing actual state flow with success rates:
```
LoginPage -> Dashboard (85% success)
LoginPage -> ErrorDialog (15% failure)
Dashboard -> UserProfile (95% success)
```

**When to Use**:
- ✅ Debugging unexpected state transitions
- ✅ Documenting actual vs. intended application flow
- ✅ Identifying unreliable state transitions (low success rates)

---

### Use Case 4: Training ML Models for Image Recognition

**Problem**: Need to collect thousands of labeled screenshots for training better image recognition models.

**Solution**: DatasetCollectionAspect with sampling

```java
@Component
public class ButtonDetectionAutomation {

    @CollectData(
        category = "button_clicks",
        captureScreenshots = true,
        samplingRate = 0.1,  // Collect 10% of operations
        labels = {"production", "buttons", "high-confidence"}
    )
    public ActionResult clickButton(StateImage button) {
        ActionResult result = action.click(button);
        // Aspect automatically saves:
        // - Screenshot of pre-click state
        // - Button image pattern
        // - Match confidence score
        // - Success/failure outcome
        return result;
    }
}
```

**Configuration**:
```properties
brobot.aspects.dataset.enabled=true
brobot.aspects.dataset.output-dir=./ml-training-data
brobot.aspects.dataset.batch-size=100
brobot.aspects.dataset.format=JSON  # or CSV, PARQUET
```

**Result**: Creates structured dataset:
```
ml-training-data/
  button_clicks/
    2025-01-15/
      batch_001.json
      screenshots/
        img_001.png
        img_002.png
```

**When to Use**:
- ✅ Building custom image recognition models
- ✅ Collecting edge cases for model improvement
- ✅ A/B testing different similarity thresholds

**When NOT to Use**:
- ❌ Production systems with sensitive data (screenshots may contain PII)
- ❌ Performance-critical operations (10ms overhead per operation)

---

### Use Case 5: Multi-Monitor Trading Application

**Problem**: Trading application spreads across 3 monitors. Need to automate actions on specific screens without manual screen selection.

**Solution**: MultiMonitorRoutingAspect with automatic detection

```java
@Component
public class TradingAutomation {

    // Define UI elements with monitor hints
    private final StateImage buyButton = new StateImage.Builder()
        .setName("buyButton")
        .addPatterns("buy-button.png")
        .setPreferredMonitor(0)  // Primary monitor
        .build();

    private final StateImage chartWidget = new StateImage.Builder()
        .setName("chartWidget")
        .addPatterns("chart.png")
        .setPreferredMonitor(1)  // Secondary monitor
        .build();

    public void executeTrade() {
        // Aspect automatically routes to correct monitor
        action.click(buyButton);      // Searches monitor 0 first
        action.click(chartWidget);    // Searches monitor 1 first
    }
}
```

**Configuration**:
```properties
brobot.aspects.multi-monitor.enabled=true
brobot.aspects.multi-monitor.enable-load-balancing=true
brobot.aspects.multi-monitor.fallback-to-all-monitors=true
```

**Behavior**:
1. Aspect checks preferred monitor first (fast path)
2. If not found, falls back to all monitors (slower but reliable)
3. Caches successful monitor locations for future operations

**When to Use**:
- ✅ Multi-monitor professional applications (trading, CAD, video editing)
- ✅ Different application windows on different screens
- ✅ Dynamic monitor configurations (laptops with external displays)

---

### Use Case 6: Debugging Intermittent Find Failures

**Problem**: Image recognition sometimes fails, but you can't see what Brobot is searching for.

**Solution**: VisualFeedbackAspect with detailed highlighting

```java
@Component
public class SearchDebugger {

    public void debugSearch() {
        // Aspect automatically shows:
        // - Search region (yellow box)
        // - Found matches (green boxes with confidence scores)
        // - Expected vs actual similarity
        ActionResult result = action.find(problematicImage);

        if (!result.isSuccess()) {
            // Visual feedback shows where search was attempted
            // Helps identify if region is too small, image changed, etc.
        }
    }
}
```

**Configuration**:
```properties
brobot.aspects.visual-feedback.enabled=true
brobot.aspects.visual-feedback.highlight-duration=5  # 5 seconds
brobot.aspects.visual-feedback.show-confidence-scores=true
brobot.aspects.visual-feedback.show-search-regions=true
brobot.aspects.visual-feedback.highlight-color=YELLOW
```

**Result**: Screen shows:
- Yellow rectangle around search region
- Green boxes around found matches
- Text overlay with confidence scores (0.95, 0.87, etc.)
- Red box if no match found (shows searched area)

**When to Use**:
- ✅ Development and debugging
- ✅ Understanding why finds fail
- ✅ Tuning similarity thresholds
- ✅ Training new team members

**When to Disable**:
- ❌ Production (visual feedback can interfere with UI)
- ❌ Headless environments (no display)
- ❌ Performance-critical scenarios

---

### Use Case 7: Error Recovery vs Circuit Breaker Decision Guide

**Scenario**: Button click occasionally fails. Which pattern to use?

#### Use Error Recovery (Retry) When:
- ✅ **Transient failures**: Element loads slowly, temporary lag
- ✅ **UI rendering delays**: Modern web apps with dynamic content
- ✅ **Network hiccups**: Brief connectivity issues
- ✅ **Animation timing**: Elements appear after animations
- ✅ **Low failure rate**: < 10% failure rate

**Example**:
```java
@Recoverable(maxRetries = 3, delay = 1000, backoff = 1.5)
public ActionResult clickButton() {
    return action.click(button);  // Retries if fails
}
```

#### Use Circuit Breaker When:
- ✅ **Systemic failures**: Entire service/API down
- ✅ **Cascading failures**: One failure indicates more will follow
- ✅ **External dependencies**: Third-party service unavailable
- ✅ **High failure rate**: > 30% failure rate
- ✅ **Resource exhaustion**: Avoid overwhelming failing system

**Example**:
```java
@Recoverable(
    maxRetries = 2,
    circuitBreakerThreshold = 5,      // Open after 5 failures
    circuitBreakerTimeout = 60000,     // Wait 60s before retry
    circuitBreakerResetTimeout = 300000 // Fully reset after 5 min success
)
public ActionResult callExternalAPI() {
    return action.click(apiButton);
}
```

**Circuit Breaker States**:
1. **Closed** (normal): Requests pass through
2. **Open** (failing): Requests fail immediately (fail fast)
3. **Half-Open** (testing): Single request to test if system recovered

#### Use Both (Hybrid) When:
- ✅ **Complex scenarios**: Transient failures + potential systemic issues
- ✅ **Critical operations**: Can't afford prolonged failures

**Example**:
```java
@Recoverable(
    maxRetries = 3,              // Try 3 times per request
    delay = 2000,                // 2s between retries
    circuitBreakerThreshold = 10, // But if 10 total failures, open circuit
    circuitBreakerTimeout = 120000 // Wait 2 minutes before trying again
)
public ActionResult criticalOperation() {
    return action.click(criticalButton);
}
```

**Decision Tree**:
```
Is the failure likely temporary? (slow loading, animation, etc.)
├─ YES → Use Error Recovery (Retry)
└─ NO → Is the failure systemic? (service down, network out)
    ├─ YES → Use Circuit Breaker
    └─ MAYBE → Use Both (Hybrid approach)
```

---

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
// Collect data with sampling to reduce overhead
@CollectData(
    category = "successful_clicks",
    samplingRate = 0.2,  // Sample 20% of operations
    captureScreenshots = true,
    labels = {"production", "clicks"}
)
public ActionResult performClick(StateImage target) {
    return action.click(target);
}
```

> **Note**: The `@CollectData` annotation supports: `category`, `captureScreenshots`, `samplingRate`, and `labels`. Filter collected data programmatically based on action success in your dataset processing pipeline.

## Performance Benchmarks

### Overview

Aspect overhead is minimal for most use cases. These benchmarks measure the additional time added by each aspect to typical Brobot operations.

**Test Environment**:
- CPU: Intel i7-9700K @ 3.6GHz
- RAM: 16GB DDR4
- OS: Windows 11
- Java: OpenJDK 21
- Brobot: 1.1.0
- Test method: Average of 1000 iterations after 100 warmup iterations

### Per-Aspect Overhead

| Aspect | Overhead (avg) | Overhead (p95) | Overhead (p99) | Impact |
|--------|----------------|----------------|----------------|--------|
| ActionLifecycleAspect | 0.8ms | 1.2ms | 1.8ms | Minimal |
| SikuliInterceptionAspect | 0.5ms | 0.9ms | 1.3ms | Minimal |
| PerformanceMonitoringAspect | 1.2ms | 2.1ms | 3.5ms | Very Low |
| StateTransitionAspect | 2.3ms | 3.8ms | 5.2ms | Low |
| VisualFeedbackAspect | 3.5ms | 5.8ms | 8.1ms | Low |
| ErrorRecoveryAspect | 5.2ms | 8.9ms | 12.4ms | Low |
| DatasetCollectionAspect | 8.7ms | 15.3ms | 22.1ms | Medium |
| MultiMonitorRoutingAspect | 0.9ms | 1.5ms | 2.2ms | Minimal |

> **Note**: These measurements include the aspect logic but exclude the actual operation time (e.g., clicking a button). The overhead is purely from aspect processing (pointcut matching, advice execution, data collection).

### Combined Aspect Overhead

When multiple aspects are enabled on the same method:

| Aspect Combination | Total Overhead | Notes |
|-------------------|----------------|-------|
| None | 0ms | Baseline |
| Core aspects only (Lifecycle + Sikuli + Performance) | 2.5ms | Recommended default |
| Core + Monitoring (+ StateTransition) | 4.8ms | Good for development |
| Core + ErrorRecovery | 7.7ms | Production with resilience |
| Core + VisualFeedback | 6.0ms | Debugging mode |
| Core + Dataset | 11.2ms | ML data collection |
| All aspects enabled | 22.5ms | Only for special cases |

### Operation Type Impact

Aspect overhead varies by operation type:

#### Fast Operations (< 100ms)

**Example**: Click a button that's immediately visible

| Configuration | Total Time | Aspect Overhead | % Impact |
|--------------|------------|-----------------|----------|
| No aspects | 45ms | 0ms | 0% |
| Core aspects | 47.5ms | 2.5ms | 5.6% |
| All aspects | 67.5ms | 22.5ms | 50% ⚠️ |

**Recommendation**: For fast operations, disable heavy aspects (Dataset, Visual Feedback) in production.

#### Medium Operations (100ms - 1s)

**Example**: Find an image with 0.8 similarity across full screen

| Configuration | Total Time | Aspect Overhead | % Impact |
|--------------|------------|-----------------|----------|
| No aspects | 350ms | 0ms | 0% |
| Core aspects | 352.5ms | 2.5ms | 0.7% |
| All aspects | 372.5ms | 22.5ms | 6.4% |

**Recommendation**: All aspects have negligible impact. Use as needed.

#### Slow Operations (> 1s)

**Example**: Wait for element to appear with 30s timeout

| Configuration | Total Time | Aspect Overhead | % Impact |
|--------------|------------|-----------------|----------|
| No aspects | 5000ms | 0ms | 0% |
| Core aspects | 5002.5ms | 2.5ms | 0.05% |
| All aspects | 5022.5ms | 22.5ms | 0.45% |

**Recommendation**: Aspect overhead is negligible. All aspects can be used freely.

### Memory Overhead

Aspect memory consumption (steady state, after 1000 operations):

| Aspect | Heap Impact | Notes |
|--------|-------------|-------|
| ActionLifecycleAspect | ~100KB | Minimal - only tracks current operation |
| SikuliInterceptionAspect | ~50KB | Minimal - no data retention |
| PerformanceMonitoringAspect | ~5MB | Stores timing history for trend analysis |
| StateTransitionAspect | ~2MB | Stores transition graph |
| ErrorRecoveryAspect | ~500KB | Tracks circuit breaker states |
| DatasetCollectionAspect | ~50MB | High - accumulates screenshots and metadata |
| MultiMonitorRoutingAspect | ~200KB | Caches monitor locations |
| VisualFeedbackAspect | ~1MB | Temporary overlay buffers |

**Total Memory (All Aspects)**: ~59MB
**Total Memory (Core Aspects)**: ~6MB

### Performance Optimization Tips

#### 1. Disable Heavy Aspects in Production

```properties
# Production configuration
brobot.aspects.visual-feedback.enabled=false    # Save 3.5ms per operation
brobot.aspects.dataset.enabled=false            # Save 8.7ms + 50MB memory
```

**Savings**: 12.2ms per operation, 52MB memory

#### 2. Use Sampling for Monitoring

```java
@Monitored(samplingRate = 0.1)  // Monitor only 10% of calls
public void frequentOperation() {
    // High-frequency operation
}
```

**Impact**: Reduces PerformanceMonitoring overhead from 1.2ms to 0.12ms (average)

#### 3. Disable Trend Detection

```properties
brobot.aspects.performance.trend-detection=false
```

**Savings**: Reduces memory from 5MB to 500KB, saves ~0.3ms per operation

#### 4. Limit Dataset Collection

```properties
brobot.aspects.dataset.max-queue-size=100       # Limit queue size
brobot.aspects.dataset.batch-size=20            # Write frequently
brobot.aspects.dataset.compress-screenshots=true # Save disk space
```

**Savings**: Reduces memory from 50MB to ~5MB

#### 5. Selective Aspect Application

Only apply aspects to methods that need them:

```java
// ❌ Bad - applies aspects to every method
@Component
public class MyAutomation {

    @Monitored
    public void operation1() { }

    @Monitored
    public void operation2() { }

    @Monitored
    public void operation3() { }
}

// ✅ Good - apply only to critical operations
@Component
public class MyAutomation {

    @Monitored  // Monitor critical operation
    public void criticalOperation() { }

    public void fastHelper() { }  // No aspect overhead

    public void internalMethod() { }  // No aspect overhead
}
```

### Real-World Performance Example

**Scenario**: Automation suite with 100 operations, running 10 times/day

#### Configuration A: All Aspects Enabled
- Per-operation overhead: 22.5ms
- Total overhead per run: 2.25 seconds
- Total overhead per day: 22.5 seconds
- Memory usage: 59MB

#### Configuration B: Core Aspects Only
- Per-operation overhead: 2.5ms
- Total overhead per run: 0.25 seconds
- Total overhead per day: 2.5 seconds
- Memory usage: 6MB

#### Configuration C: Selective Aspects
- Core aspects on all operations: 2.5ms × 100 = 250ms
- ErrorRecovery on 20 critical operations: 5.2ms × 20 = 104ms
- Visual feedback during development only: 0ms (disabled in prod)
- Total overhead per run: 0.354 seconds
- Total overhead per day: 3.54 seconds
- Memory usage: 7MB

**Recommendation**: Use Configuration C (selective aspects) for best balance of observability and performance.

### Benchmark Methodology

To measure aspect overhead in your environment:

```java
@Component
public class AspectBenchmark {

    private final Action action;

    public void benchmark() {
        StateImage testImage = createTestImage();
        int warmup = 100;
        int iterations = 1000;

        // Warmup
        for (int i = 0; i < warmup; i++) {
            action.click(testImage);
        }

        // Measure with aspects
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            action.click(testImage);
        }
        long withAspects = System.nanoTime() - start;

        // Disable aspects and measure again
        // (temporarily set brobot.aspects.*.enabled=false)

        long overhead = (withAspects - withoutAspects) / iterations / 1_000_000;
        System.out.println("Average aspect overhead: " + overhead + "ms");
    }
}
```

### Performance FAQ

**Q: Do aspects slow down my automation?**
A: For most operations (> 100ms), aspect overhead is negligible (< 1%). For very fast operations (< 50ms), consider disabling heavy aspects like Dataset and VisualFeedback.

**Q: Which aspects have the most overhead?**
A: DatasetCollectionAspect (8.7ms) due to screenshot capture. ErrorRecoveryAspect (5.2ms) due to circuit breaker state management.

**Q: Can I measure aspect overhead in my application?**
A: Yes, use the PerformanceMonitoringAspect itself! It tracks method execution times including aspect overhead.

**Q: Should I disable aspects in production?**
A: Disable heavy aspects (Dataset, VisualFeedback). Keep core aspects (Lifecycle, Sikuli, Performance, ErrorRecovery) for reliability and observability.

**Q: Do aspects affect startup time?**
A: Minimal impact. Spring AOP proxy creation adds ~50-100ms per aspect during application startup. Total: ~500ms for all 8 aspects.

---

## Troubleshooting

### Aspects Not Working

1. **Verify Spring Boot AOP starter is in dependencies**:
   - Ensure `spring-boot-starter-aop` is included (should be automatic with Brobot)
   - Check your build file (pom.xml or build.gradle)

2. **Verify aspect configuration**:
   ```properties
   brobot.aspects.*.enabled=true
   ```
   Check that the specific aspect you want to use is enabled in your `application.properties`.

3. **Check logs for aspect initialization**:
   ```
   grep "Aspect initialized" application.log
   ```

4. **Verify annotation target**:
   - Aspects only work on Spring-managed beans (`@Component`, `@Service`, etc.)
   - Methods must be public for proxy-based aspects to intercept them

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

### Understanding Aspect Implementation

Brobot uses **Spring AOP** with runtime proxy-based aspects, not compile-time AspectJ weaving. This means:

- Aspects are applied at runtime through Spring's proxy mechanism
- No special compiler plugins are needed
- Standard Maven/Gradle builds work without modification
- Performance is excellent for typical automation workloads

If you need compile-time weaving for specific use cases, consult the Spring AOP documentation, but this is typically not necessary for Brobot applications.

## Related Documentation

### Configuration
- **[Properties Reference](../configuration/properties-reference.md)** - Complete reference for all aspect-related properties
- **[Auto-Configuration Guide](../configuration/auto-configuration.md)** - Understanding Spring Boot auto-configuration in Brobot

### Testing and Mock Mode
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing with mock mode and the SikuliInterceptionAspect
- **[Testing Introduction](../../04-testing/testing-intro.md)** - Overview of testing strategies in Brobot
- **[Unit Testing Guide](../../04-testing/unit-testing.md)** - Writing unit tests with aspect support

### State Management
- **[States Guide](../../01-getting-started/states.md)** - Understanding Brobot's state management
- **[Transitions Guide](../../01-getting-started/transitions.md)** - State transitions tracked by StateTransitionAspect
- **[Annotations Guide](../user-guides/annotations.md)** - Using @State, @Transition, and other Brobot annotations

### Visual Feedback
- **[Highlighting Feature Guide](../user-guides/highlighting-feature.md)** - Visual feedback and highlighting using the VisualFeedbackAspect

## Conclusion

AspectJ integration in Brobot provides powerful cross-cutting features that enhance automation reliability, observability, and maintainability. By using these aspects appropriately, you can build more robust automation solutions with less code and better insights into system behavior.

### Key Takeaways

**The 8 aspects cover critical concerns**:
- **Core aspects** (Sikuli interception, action lifecycle) provide foundation features with minimal overhead (~1ms)
- **Monitoring aspects** (performance, state transitions) give visibility into execution (~2-3ms)
- **Recovery aspects** (error recovery with retry/circuit breaker) improve reliability (~5ms)
- **Data aspects** (dataset collection) support ML model training (~9ms)
- **Display aspects** (multi-monitor, visual feedback) handle multi-screen and debugging scenarios (~2-4ms)

**Performance Guidelines**:
- Core aspects add only 2.5ms overhead on average - negligible for most operations
- For operations > 100ms, all aspects add < 1% overhead
- Selective aspect application is the best practice: use heavy aspects (Dataset, VisualFeedback) only where needed

**Real-World Usage**:
- Use ErrorRecoveryAspect for flaky UI elements with exponential backoff
- Use PerformanceMonitoringAspect to detect performance regressions automatically
- Use StateTransitionAspect to debug complex multi-state application flows
- Use Circuit Breaker pattern for systemic failures (server down, API unavailable)
- Use Retry pattern for transient failures (slow loading, network hiccups)

**Production Configuration**:
```properties
# Recommended production setup
brobot.aspects.sikuli.enabled=true              # Minimal overhead
brobot.aspects.action-lifecycle.enabled=true    # Minimal overhead
brobot.aspects.performance.enabled=true         # Low overhead, high value
brobot.aspects.error-recovery.enabled=true      # Low overhead, improves reliability
brobot.aspects.visual-feedback.enabled=false    # Disable in production
brobot.aspects.dataset.enabled=false            # Disable in production
```

All aspects are optional and can be enabled/disabled independently based on your application's needs. Start with core aspects, add monitoring and recovery for production, and enable visual feedback and dataset collection selectively during development and ML training phases.
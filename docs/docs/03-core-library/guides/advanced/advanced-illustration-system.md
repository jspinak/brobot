---
sidebar_position: 11
title: 'Illustration System'
description: 'Visual documentation of automation actions'
keywords: [illustration, screenshots, visualization, debugging, testing]
---

# Illustration System

Brobot's illustration system provides visual documentation of automation actions, helping with debugging, testing, and understanding automation behavior.

> **⚠️ Version Notice**: This document describes the illustration system in Brobot v1.1.0. Advanced features (context-aware filtering, adaptive sampling, performance optimization) have infrastructure in place but are not yet integrated into the execution path.

## What Works in v1.1.0

### Property-Based Configuration

Control illustration behavior through `application.properties` or `application.yml`:

```properties
# Master switch - must be true for any illustrations
brobot.screenshot.save-history=true

# Output directory
brobot.screenshot.history-path=history/

# Per-action type control
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=false
brobot.illustration.draw-move=false
brobot.illustration.draw-highlight=true
brobot.illustration.draw-classify=true
brobot.illustration.draw-define=true

# Repetition filtering
brobot.illustration.draw-repeated-actions=false
```

### Per-Action Illustration Control

Override global settings for individual actions using `ActionConfig.Illustrate`:

```java
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IllustrationExample {

    @Autowired
    private Action action;

    public void demonstrateIllustrationControl() {
        // Force illustration for this specific action
        PatternFindOptions forceIllustrate = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)
            .setSimilarity(0.8)
            .build();

        Pattern criticalButton = new Pattern("critical-button.png");
        ObjectCollection buttonCollection = new ObjectCollection.Builder()
            .withPatterns(criticalButton)
            .build();

        action.perform(forceIllustrate, buttonCollection);

        // Prevent illustration for this specific action
        PatternFindOptions noIllustrate = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.NO)
            .setSimilarity(0.8)
            .build();

        action.perform(noIllustrate, buttonCollection);

        // Use global settings (default behavior)
        PatternFindOptions useGlobal = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.USE_GLOBAL)
            .setSimilarity(0.8)
            .build();

        action.perform(useGlobal, buttonCollection);
    }
}
```

**ActionConfig.Illustrate Values**:
- `YES` - Always illustrate this action, regardless of global settings
- `NO` - Never illustrate this action, regardless of global settings
- `USE_GLOBAL` - Follow global property settings (default)

### Basic IllustrationController API

For advanced control, use `IllustrationController` directly:

```java
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.action.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class AdvancedIllustrationControl {

    @Autowired
    private IllustrationController illustrationController;

    public void checkBeforeIllustrating(
            PatternFindOptions findOptions,
            ObjectCollection objectCollection) {

        // Check if illustration should occur based on current settings
        boolean shouldIllustrate =
            illustrationController.okToIllustrate(findOptions, objectCollection);

        if (shouldIllustrate) {
            System.out.println("Illustration will be created for this action");
        }
    }

    public void manuallyTriggerIllustration(
            ActionResult result,
            List<Region> searchRegions,
            PatternFindOptions findOptions,
            ObjectCollection objectCollection) {

        // Manually trigger illustration if conditions are met
        boolean illustrated = illustrationController.illustrateWhenAllowed(
            result,
            searchRegions,
            findOptions,
            objectCollection
        );

        if (illustrated) {
            System.out.println("Illustration was created");
        }
    }
}
```

**IllustrationController Methods** (v1.1.0):
- `okToIllustrate(ActionConfig, ObjectCollection...)` - Check if illustration should occur
- `illustrateWhenAllowed(ActionResult, List<Region>, ActionConfig, ObjectCollection...)` - Create illustration if allowed
- `getLastAction()` - Get the last action type
- `getLastFind()` - Get the last find result
- `getLastCollections()` - Get the last object collections
- `getActionPermissions()` - Get current action permissions map

### Repetition Filtering

Brobot automatically tracks consecutive identical actions and can skip illustrations for repeated operations:

```properties
# Skip illustrations for repeated actions
brobot.illustration.draw-repeated-actions=false
```

When enabled, only the first occurrence of repeated actions will be illustrated until a different action is performed.

## Complete Working Examples

### Example 1: E-Commerce Automation with Selective Illustration

This complete example demonstrates how to use illustration selectively during a login and checkout flow:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckoutAutomation {

    @Autowired
    private Action action;

    public void performCheckout(String username, String password) {
        // Step 1: Find login button (ILLUSTRATED - critical action)
        PatternFindOptions findLoginBtn = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)  // Force illustration
            .setSimilarity(0.85)
            .build();

        StateImage loginButton = new StateImage.Builder()
            .addPattern("login-button")
            .build();

        ActionResult loginResult = action.perform(findLoginBtn, loginButton);
        // Creates: history/20240115_103045_FIND_login-button.png

        if (!loginResult.isSuccess()) {
            System.out.println("Login button not found!");
            return;
        }

        // Step 2: Click login button (NOT ILLUSTRATED - routine action)
        ClickOptions clickLogin = new ClickOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.NO)  // Skip illustration
            .build();

        action.perform(clickLogin, loginButton);
        // No screenshot created

        // Step 3: Type username (NOT ILLUSTRATED - sensitive data)
        TypeOptions typeUsername = new TypeOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.NO)  // Don't capture credentials
            .setText(username)
            .build();

        StateImage usernameField = new StateImage.Builder()
            .addPattern("username-field")
            .build();

        action.perform(typeUsername, usernameField);
        // No screenshot created (security best practice)

        // Step 4: Find checkout button (ILLUSTRATED - verification point)
        PatternFindOptions findCheckout = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)
            .setSimilarity(0.90)
            .build();

        StateImage checkoutButton = new StateImage.Builder()
            .addPattern("checkout-button")
            .build();

        ActionResult checkoutResult = action.perform(findCheckout, checkoutButton);
        // Creates: history/20240115_103052_FIND_checkout-button.png

        System.out.println("Checkout complete. Created 2 illustrations.");
    }
}
```

**Expected Output Structure:**
```
history/
├── 20240115_103045_FIND_login-button.png      (illustrated - critical)
└── 20240115_103052_FIND_checkout-button.png   (illustrated - verification)
```

**Why This Works:**
- Critical UI elements are illustrated for debugging
- Sensitive data (credentials) is never captured
- Routine clicks don't create unnecessary files
- Verification points are documented for test reports

### Example 2: Repetition Filtering in Action

Demonstrates how repetition filtering prevents illustration spam:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScrollingListAutomation {

    @Autowired
    private Action action;

    public void processScrollingList() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.85)
            .build();

        StateImage listItem = new StateImage.Builder()
            .addPattern("list-item")
            .build();

        StateImage loadingSpinner = new StateImage.Builder()
            .addPattern("loading-spinner")
            .build();

        // Configuration: brobot.illustration.draw-repeated-actions=false

        for (int i = 0; i < 50; i++) {
            // First iteration: Creates history/20240115_103100_FIND_list-item.png
            // Iterations 2-50: No screenshots (same action repeated)
            action.find(listItem);

            // Different action: Creates history/20240115_103101_FIND_loading-spinner.png
            action.find(loadingSpinner);

            // Back to list item: Creates NEW screenshot (action changed)
            // Creates: history/20240115_103102_FIND_list-item.png
            action.find(listItem);
        }

        System.out.println("Processed 150 actions, created ~100 illustrations");
        System.out.println("Without filtering: would have created 150 illustrations");
    }
}
```

**Illustration Timeline:**
```
Action 1:  FIND list-item       → Screenshot created (first occurrence)
Action 2:  FIND list-item       → Skipped (repetition)
Action 3:  FIND list-item       → Skipped (repetition)
Action 4:  FIND loading-spinner → Screenshot created (different action)
Action 5:  FIND loading-spinner → Skipped (repetition)
Action 6:  FIND list-item       → Screenshot created (action changed)
```

### Example 3: Conditional Illustration with IllustrationController

Advanced scenario using IllustrationController for runtime decisions:

```java
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ConditionalIllustrationExample {

    @Autowired
    private Action action;

    @Autowired
    private IllustrationController illustrationController;

    public void smartIllustration() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.80)
            .build();

        StateImage errorDialog = new StateImage.Builder()
            .addPattern("error-dialog")
            .build();

        ObjectCollection errorCollection = new ObjectCollection.Builder()
            .withImages(errorDialog)
            .build();

        // Check if illustration will occur
        boolean willIllustrate = illustrationController.okToIllustrate(
            findOptions,
            errorCollection
        );

        if (willIllustrate) {
            System.out.println("→ Searching for error dialog (will be illustrated)...");
        } else {
            System.out.println("→ Searching for error dialog (illustration disabled)...");
        }

        // Perform the action
        ActionResult result = action.perform(findOptions, errorCollection);

        // Manually trigger illustration based on result quality
        if (!result.isSuccess() || result.getBestScore() < 0.90) {
            System.out.println("⚠ Low quality match detected, forcing illustration...");

            List<Region> searchRegions = List.of(Region.getScreenRegion());

            boolean illustrated = illustrationController.illustrateWhenAllowed(
                result,
                searchRegions,
                findOptions,
                errorCollection
            );

            if (illustrated) {
                System.out.println("✓ Low-quality match illustrated for review");
                // Creates: history/20240115_103200_FIND_error-dialog_LOW_QUALITY.png
            }
        }
    }
}
```

**Console Output:**
```
→ Searching for error dialog (will be illustrated)...
⚠ Low quality match detected, forcing illustration...
✓ Low-quality match illustrated for review
```

**Use Case:** Automatically illustrate only problematic matches for manual review while skipping high-confidence matches.

## Common Use Cases

### Development and Debugging

```properties
# Enable all illustrations during development
brobot.screenshot.save-history=true
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-move=true
brobot.illustration.draw-repeated-actions=true
```

**Result:** Every action creates an illustration. Typical session generates 200-500 screenshots.

**When to Use:** Initial development, complex UI debugging, understanding action flow.

### Production Monitoring

```properties
# Only illustrate important actions in production
brobot.screenshot.save-history=true
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-move=false
brobot.illustration.draw-repeated-actions=false
```

**Result:** Only FIND and CLICK actions illustrated, repetitions filtered. Typical session generates 20-50 screenshots.

**When to Use:** Production monitoring, failure investigation, audit trails.

### Test Automation

```properties
# Selective illustration for test verification
brobot.screenshot.save-history=true
brobot.illustration.draw-find=true
brobot.illustration.draw-click=false
brobot.illustration.draw-repeated-actions=false
```

**Result:** Only FIND actions illustrated. Typical test suite generates 10-30 screenshots per test.

**When to Use:** CI/CD pipelines, test reports, regression testing.

## Understanding Illustration Output

### File Naming Convention

Brobot creates illustration files with a standardized naming pattern:

```
{timestamp}_{action_type}_{object_name}.png
```

**Examples:**
```
20240115_103045_FIND_login-button.png
20240115_103052_CLICK_submit-button.png
20240115_103100_DRAG_file-icon.png
```

**Components:**
- **Timestamp**: `YYYYMMDD_HHMMSS` format for chronological sorting
- **Action Type**: FIND, CLICK, DRAG, MOVE, HIGHLIGHT, CLASSIFY, DEFINE
- **Object Name**: Name of the StateImage or pattern being acted upon

### Illustration Content

Each illustration screenshot shows:

1. **Full Screen Capture**: Complete screen state at action time
2. **Highlighted Regions**: Found matches outlined with colored boxes
3. **Match Information**: Similarity scores and coordinates (when available)
4. **Search Regions**: Areas searched for the target (when configured)

**Visual Example:**
```
┌─────────────────────────────────────┐
│  Application Window                 │
│                                     │
│  ┏━━━━━━━━━━━┓  ← Red box          │
│  ┃ Submit    ┃    (Match: 0.95)    │
│  ┗━━━━━━━━━━━┛    at (245, 180)    │
│                                     │
│  [Cancel]                           │
└─────────────────────────────────────┘
```

### Storage Organization Strategies

**Strategy 1: Flat Directory (Default)**
```
history/
├── 20240115_103045_FIND_login-button.png
├── 20240115_103052_CLICK_submit-button.png
├── 20240115_103100_FIND_checkout-button.png
└── 20240115_103105_CLICK_confirm-button.png
```
**Best for:** Short sessions, quick debugging, temporary illustrations

**Strategy 2: Session-Based Organization**
```properties
# Use unique session directories
brobot.screenshot.history-path=history/session-${timestamp}/
```
```
history/
├── session-20240115_103000/
│   ├── 20240115_103045_FIND_login-button.png
│   └── 20240115_103052_CLICK_submit-button.png
└── session-20240115_114000/
    ├── 20240115_114010_FIND_menu-button.png
    └── 20240115_114020_CLICK_settings-button.png
```
**Best for:** Multiple test runs, CI/CD, production monitoring

**Strategy 3: Feature-Based Organization**
```properties
# Organize by feature area
brobot.screenshot.history-path=history/${feature.name}/
```
```
history/
├── login-flow/
│   ├── 20240115_103045_FIND_login-button.png
│   └── 20240115_103052_FIND_username-field.png
└── checkout-flow/
    ├── 20240115_104000_FIND_cart-icon.png
    └── 20240115_104010_FIND_payment-button.png
```
**Best for:** Feature testing, organized debugging, documentation

## Best Practices

### 1. Use Selective Illustration

Don't illustrate everything - focus on important actions:

```java
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

// Force illustration for critical verification points
PatternFindOptions criticalFind = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.YES)  // Always illustrate
    .setSimilarity(0.90)  // High confidence threshold
    .build();

// Disable illustration for frequent housekeeping actions
PatternFindOptions routineFind = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.NO)  // Never illustrate
    .build();

// Security: Never illustrate sensitive data entry
ClickOptions sensitiveClick = new ClickOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.NO)  // Protect credentials
    .build();
```

**Decision Matrix:**

| Action Type | Illustrate? | Reason |
|-------------|-------------|--------|
| Login button find | YES | Critical navigation point |
| Password typing | NO | Security - sensitive data |
| Routine clicks | NO | Reduces noise |
| Error detection | YES | Debugging failures |
| Verification points | YES | Test evidence |
| Loop iterations | NO | Repetition filtering handles this |

### 2. Configure by Environment

Use Spring profiles for environment-specific settings:

**application-dev.properties:**
```properties
# Development: Verbose illustrations
brobot.screenshot.save-history=true
brobot.screenshot.history-path=history/dev/
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-move=true
brobot.illustration.draw-repeated-actions=true
```

**application-test.properties:**
```properties
# Testing: Selective illustrations for CI/CD
brobot.screenshot.save-history=true
brobot.screenshot.history-path=target/test-illustrations/
brobot.illustration.draw-find=true
brobot.illustration.draw-click=false
brobot.illustration.draw-move=false
brobot.illustration.draw-repeated-actions=false
```

**application-prod.properties:**
```properties
# Production: Minimal illustrations for troubleshooting
brobot.screenshot.save-history=true
brobot.screenshot.history-path=/var/log/brobot/illustrations/
brobot.illustration.draw-find=true
brobot.illustration.draw-click=false
brobot.illustration.draw-move=false
brobot.illustration.draw-repeated-actions=false
```

**Activate Profile:**
```bash
# Development
java -jar app.jar --spring.profiles.active=dev

# Testing
./gradlew test -Dspring.profiles.active=test

# Production
java -jar app.jar --spring.profiles.active=prod
```

### 3. Implement Cleanup Strategies

Illustrations can accumulate quickly - implement automatic cleanup:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Component
public class IllustrationCleanup {

    @Value("${brobot.screenshot.history-path}")
    private String historyPath;

    // Run daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldIllustrations() throws IOException {
        Path historyDir = Paths.get(historyPath);
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        try (Stream<Path> files = Files.list(historyDir)) {
            files.filter(Files::isRegularFile)
                 .filter(file -> file.toString().endsWith(".png"))
                 .filter(file -> {
                     try {
                         return Files.getLastModifiedTime(file)
                                    .toInstant()
                                    .isBefore(cutoff);
                     } catch (IOException e) {
                         return false;
                     }
                 })
                 .forEach(file -> {
                     try {
                         Files.delete(file);
                         System.out.println("Deleted old illustration: " + file.getFileName());
                     } catch (IOException e) {
                         System.err.println("Failed to delete: " + file);
                     }
                 });
        }
    }
}
```

**Retention Policies:**
- **Development**: Keep 1-2 days (rapid iteration)
- **Testing**: Keep 7 days (test history review)
- **Production**: Keep 30 days (compliance, auditing)

### 4. Disk Space Monitoring

```properties
# Set reasonable limits
brobot.screenshot.history-path=history/

# Estimate space requirements:
# - Average screenshot size: 200-800 KB
# - 100 actions/hour: 20-80 MB/hour
# - With repetition filtering: 5-20 MB/hour
# - Daily production: 120-480 MB/day
```

**Monitoring Script:**
```java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class IllustrationMonitor {

    public void checkDiskUsage() throws IOException {
        Path historyPath = Paths.get("history/");

        long totalSize = Files.walk(historyPath)
            .filter(Files::isRegularFile)
            .mapToLong(file -> {
                try {
                    return Files.size(file);
                } catch (IOException e) {
                    return 0L;
                }
            })
            .sum();

        long sizeMB = totalSize / (1024 * 1024);
        System.out.println("Illustration storage: " + sizeMB + " MB");

        if (sizeMB > 1000) {
            System.out.println("⚠ Warning: Illustration storage exceeds 1 GB");
        }
    }
}
```

### 5. Use Illustrations in Test Reports

Integrate illustrations into test reports for visual verification:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoginFlowTest {

    @Autowired
    private Action action;

    @Test
    public void testLoginButtonVisible() {
        // Enable illustration for test evidence
        PatternFindOptions findWithEvidence = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)
            .setSimilarity(0.85)
            .build();

        StateImage loginButton = new StateImage.Builder()
            .addPattern("login-button")
            .build();

        ActionResult result = action.perform(findWithEvidence, loginButton);

        // Assertion creates test evidence in: target/test-illustrations/
        assertTrue(result.isSuccess(),
            "Login button should be visible. See illustration for details.");

        // Screenshot automatically attached to test report
    }
}
```

**Test Report Output:**
```
✓ testLoginButtonVisible (0.8s)
  Evidence: target/test-illustrations/20240115_103045_FIND_login-button.png
  Match Quality: 0.92
  Location: (245, 180)
```

## Troubleshooting

### Illustrations Not Being Created

**Check these settings:**

1. Master switch is enabled:
   ```properties
   brobot.screenshot.save-history=true
   ```

2. Action type is enabled:
   ```properties
   brobot.illustration.draw-find=true
   ```

3. Action isn't using `ActionConfig.Illustrate.NO`:
   ```java
   // This will prevent illustration
   .setIllustrate(ActionConfig.Illustrate.NO)
   ```

4. Repetition filtering isn't blocking:
   ```properties
   brobot.illustration.draw-repeated-actions=true  # Allow repeated illustrations
   ```

### Disk Space Issues

If illustrations are consuming too much space:

1. Disable illustrations for high-frequency actions:
   ```properties
   brobot.illustration.draw-move=false
   brobot.illustration.draw-repeated-actions=false
   ```

2. Implement cleanup strategy in your code
3. Use selective per-action control with `ActionConfig.Illustrate.NO`

---

## Advanced Features (Infrastructure Present, Integration Pending)

> **⚠️ Important**: The classes and APIs described below exist in the Brobot codebase but are not currently integrated into the execution path. They represent planned functionality for future releases.

### Context-Aware Configuration (Planned)

The framework includes `IllustrationConfig` and `IllustrationContext` classes for advanced filtering:

```java
import io.github.jspinak.brobot.tools.history.configuration.IllustrationConfig;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationContext;
import io.github.jspinak.brobot.action.ActionInterface.ActionType;
import java.time.Duration;

// Infrastructure exists but not yet wired into execution
IllustrationConfig advancedConfig = IllustrationConfig.builder()
    .globalEnabled(true)
    .actionEnabled(ActionType.FIND, true)
    .actionEnabled(ActionType.CLICK, true)
    .actionEnabled(ActionType.MOVE, false)
    .qualityThreshold(0.75)
    .maxIllustrationsPerMinute(30)
    .contextFilter("failures_only", context ->
        context.getLastActionResult() != null &&
        !context.getLastActionResult().isSuccess())
    .build();
```

**Status**: Classes defined, methods exist, but no integration with IllustrationController yet.

### Performance Optimization (Planned)

`IllustrationPerformanceOptimizer` provides batching and adaptive sampling:

```java
import io.github.jspinak.brobot.tools.history.performance.IllustrationPerformanceOptimizer;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationConfig;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class PerformanceOptimizationExample {

    @Autowired
    private IllustrationPerformanceOptimizer performanceOptimizer;

    public void configureAdvancedOptimization() {
        IllustrationConfig config = IllustrationConfig.builder()
            .adaptiveSampling(true)
            .samplingRate(ActionType.FIND, 1.0)
            .samplingRate(ActionType.MOVE, 0.1)
            .batchConfig(IllustrationConfig.BatchConfig.builder()
                .maxBatchSize(20)
                .flushInterval(Duration.ofSeconds(10))
                .flushOnStateTransition(true)
                .maxMemoryUsageMB(50)
                .build())
            .build();

        // Method exists on optimizer (NOT on IllustrationController)
        performanceOptimizer.setConfig(config);
    }
}
```

**Status**: `IllustrationPerformanceOptimizer` exists as a `@Component`, has `setConfig()` method, but is not used in current execution flow.

### Quality-Based Filtering (Planned)

`QualityMetrics` enables filtering based on match quality:

```java
import io.github.jspinak.brobot.tools.history.configuration.IllustrationConfig.QualityMetrics;

IllustrationConfig qualityConfig = IllustrationConfig.builder()
    .qualityThreshold(0.8)
    .qualityMetrics(QualityMetrics.builder()
        .minSimilarity(0.75)
        .minConfidence(0.6)
        .useRegionSize(true)
        .useExecutionTime(false)
        .customQualityCalculator(context -> {
            // Custom quality calculation logic
            return 0.9;
        })
        .build())
    .build();
```

**Status**: Inner class exists with all methods, but no integration with illustration decision logic.

### Performance Metrics (Planned)

`PerformanceMetrics` provides monitoring:

```java
import io.github.jspinak.brobot.tools.history.performance.PerformanceMetrics;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class MetricsExample {

    @Autowired
    private IllustrationPerformanceOptimizer optimizer;

    public void checkMetrics() {
        PerformanceMetrics.MetricsSnapshot metrics =
            optimizer.getPerformanceMetrics();

        System.out.println("Illustrations per minute: " +
            metrics.getIllustrationsPerMinute());
        System.out.println("Skip rate: " + metrics.getSkipRate());
        System.out.println("High quality rate: " + metrics.getHighQualityRate());
    }
}
```

**Status**: Classes exist, but metrics are not currently collected during execution.

## Configuration Reference (Current in v1.1.0)

### Property Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.screenshot.save-history` | boolean | false | Master switch for all illustrations |
| `brobot.screenshot.history-path` | String | "history/" | Output directory for illustrations |
| `brobot.illustration.draw-find` | boolean | true | Illustrate FIND actions |
| `brobot.illustration.draw-click` | boolean | true | Illustrate CLICK actions |
| `brobot.illustration.draw-drag` | boolean | false | Illustrate DRAG actions |
| `brobot.illustration.draw-move` | boolean | false | Illustrate MOVE actions |
| `brobot.illustration.draw-highlight` | boolean | true | Illustrate HIGHLIGHT actions |
| `brobot.illustration.draw-classify` | boolean | true | Illustrate CLASSIFY actions |
| `brobot.illustration.draw-define` | boolean | true | Illustrate DEFINE actions |
| `brobot.illustration.draw-repeated-actions` | boolean | false | Illustrate repeated identical actions |

### ActionConfig.Illustrate Enum

| Value | Description |
|-------|-------------|
| `YES` | Always illustrate this action |
| `NO` | Never illustrate this action |
| `USE_GLOBAL` | Follow global property settings (default) |

## Related Documentation

- **[BrobotProperties Reference](../../configuration/properties-reference.md)** - Complete properties documentation
- **[ActionConfig Overview](../../action-config/01-overview.md)** - ActionConfig system guide
- **[Screenshot Configuration](../../capture/screenshot-configuration.md)** - Screenshot system details
- **[Example Project](https://github.com/jspinak/brobot/tree/main/examples/03-core-library/guides/advanced-illustration-system)** - Working examples

> **💡 For Working Examples**: See the example project's `README_V1.1.0.md` for accurate, tested code samples using the current v1.1.0 API.

## Summary

The Brobot illustration system in v1.1.0 provides:
- **Property-based configuration** for global and per-action-type control
- **Per-action override** using ActionConfig.Illustrate enum
- **Repetition filtering** to reduce noise from repeated actions
- **Direct API access** via IllustrationController for advanced scenarios

Advanced features like context-aware filtering, adaptive sampling, and performance optimization have infrastructure in place but require additional integration work to become active in the execution flow.

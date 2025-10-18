---
sidebar_position: 4
---

# Action Recording

## Overview

Action recording solves a key challenge in UI automation testing: verifying complex operations without manual result calculation. Instead of precisely calculating expected coordinates for 20+ matches, action recording provides **visual documentation** and **programmatic test data** that can be quickly reviewed or reused in automated tests.

Brobot provides two complementary recording systems:

1. **Illustrated Screenshots** - Visual documentation with annotated screenshots showing what actions did
2. **ActionHistory Recording** - Programmatic test data saved to JSON files for mock testing and performance analysis

For comprehensive debugging with real-time feedback, see the [Image Find Debugging Guide](../03-core-library/tools/image-find-debugging.md). For general testing strategies, see the [Testing Introduction](./testing-intro.md).

## Two Recording Systems

### System 1: Illustrated Screenshots (Visual Debugging)

Illustrated screenshots capture visual proof of automation execution with colored annotations:

- **Search regions** (blue rectangles)
- **Matches found** (pink highlights)
- **Click points** (markers)
- **Drag paths** (arrows)
- **Move trajectories** (lines)
- **Match details** (sidebar with coordinates and similarity scores)

**Use Cases:**
- Visual verification during development
- Debugging failed automation
- Documentation for manual QA review
- Quick sanity checks

**Output:** PNG image files in `history/` directory

### System 2: ActionHistory Recording (Test Data)

ActionHistory records capture structured test data in JSON format:

- Match coordinates and regions
- Similarity scores
- Action configurations
- Success/failure status
- Duration and timestamps
- State context

**Use Cases:**
- Building mock test data for [headless CI/CD testing](./mock-mode-guide.md)
- Performance analysis and benchmarking
- Regression test baselines
- [Integration testing with ActionHistory](./actionhistory-integration-testing.md)

**Output:** JSON files in `src/test/resources/histories/`

## Configuration

### Enabling Illustrated Screenshots

Configure via `application.properties`. For complete property reference, see [Properties Reference](../03-core-library/configuration/properties-reference.md).

```properties
# Enable illustrated screenshot recording
brobot.screenshot.save-history=true

# Configure output directory
brobot.screenshot.history-path=history/

# Configure filename prefixes
brobot.screenshot.history-filename=hist
brobot.screenshot.filename=screen

# Control which actions to illustrate
brobot.illustration.draw-find=true
brobot.illustration.draw-click=true
brobot.illustration.draw-drag=true
brobot.illustration.draw-move=true
brobot.illustration.draw-highlight=true
brobot.illustration.draw-classify=true
```

### Accessing Configuration in Code

```java
package io.github.jspinak.brobot.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;

@Component
public class ActionRecordingConfig {

    @Autowired
    private BrobotProperties brobotProperties;

    public void checkConfiguration() {
        // Access screenshot settings
        String historyPath = brobotProperties.getScreenshot().getHistoryPath();
        String historyFilename = brobotProperties.getScreenshot().getHistoryFilename();
        boolean saveHistory = brobotProperties.getScreenshot().isSaveHistory();

        System.out.println("History path: " + historyPath);
        System.out.println("History filename prefix: " + historyFilename);
        System.out.println("Save history enabled: " + saveHistory);

        // Access illustration settings
        boolean drawFind = brobotProperties.getIllustration().isDrawFind();
        boolean drawClick = brobotProperties.getIllustration().isDrawClick();

        System.out.println("Illustrate FIND actions: " + drawFind);
        System.out.println("Illustrate CLICK actions: " + drawClick);
    }
}
```

### Override Per Action

You can control illustration on a per-action basis:

```java
package io.github.jspinak.brobot.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

@Component
public class SelectiveIllustration {

    @Autowired
    private Action action;

    public void demonstrateOverrides(StateImage loginButton) {
        // Force illustration regardless of global setting
        PatternFindOptions optionsWithIllustration = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)
            .build();

        ActionResult result1 = action.perform(optionsWithIllustration, loginButton.asObjectCollection());
        // Screenshot saved even if brobot.screenshot.save-history=false

        // Explicitly disable illustration
        PatternFindOptions optionsNoIllustration = new PatternFindOptions.Builder()
            .setIllustrate(ActionConfig.Illustrate.NO)
            .build();

        ActionResult result2 = action.perform(optionsNoIllustration, loginButton.asObjectCollection());
        // No screenshot saved even if brobot.screenshot.save-history=true
    }
}
```

## Illustrated Screenshot Output

Illustrated screenshots and original screenshots are both saved to the directory specified by `brobot.screenshot.history-path` (default: `history/`).

**Filename Format:**
- **Illustrated screenshots**: `{historyFilename}{#}-{ACTION}-{objectName}.png`
- **Original screenshots**: `{screenshotFilename}{#}.png`

**Example output from a test session:**
```
history/
├── hist0-FIND-loginButton.png     # Illustrated screenshot
├── screen0.png                     # Original screenshot
├── hist1-CLICK-usernameField.png  # Illustrated screenshot
├── screen1.png                     # Original screenshot
├── hist2-MOVE-submitButton.png    # Illustrated screenshot
└── screen2.png                     # Original screenshot
```

### Visual Examples

**Find Operation with Matches Highlighted:**

![illustrated find](/img/illustrated-find.png)

The above image shows:
- Blue rectangles indicating search regions
- Pink highlights showing matches found
- Sidebar with match details (coordinates, similarity scores)

**Mouse Move Operation with Trajectory:**

![illustrated move](/img/illustrated-move.png)

The above image illustrates:
- Mouse movement path
- Start and end positions
- Action type label

## ActionHistory Recording

For programmatic test data collection, use the `RecordingActionWrapper`. For comprehensive coverage, see [ActionHistory Integration Testing](./actionhistory-integration-testing.md) and [ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md).

### Basic Recording Setup

```java
package io.github.jspinak.brobot.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.actionhistory.RecordingActionWrapper;

import java.util.Map;

/**
 * Basic action recording with RecordingActionWrapper.
 */
public class BasicActionRecordingTest extends BrobotTestBase {

    @Autowired
    private RecordingActionWrapper recordingWrapper;

    @Autowired
    private Action action;

    @Test
    public void testWithRecording() {
        // Enable recording
        recordingWrapper.setRecordingEnabled(true);

        // Create test pattern
        StateImage loginButton = new StateImage.Builder()
            .addPattern("images/login/button-login.png")
            .build();

        // Perform actions - automatically recorded
        ActionResult result = recordingWrapper.find(loginButton);

        // Get recording statistics
        Map<String, Integer> stats = recordingWrapper.getRecordingStatistics();
        System.out.println("Recorded " + stats.get("button-login.png") + " executions");

        // History is automatically saved to Pattern's ActionHistory
        // and can be used for mock testing
    }
}
```

### Session-Based Recording

```java
package io.github.jspinak.brobot.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.patterns.Pattern;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.actionhistory.RecordingActionWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Session-based recording for organized test data collection.
 */
public class SessionRecordingTest extends BrobotTestBase {

    @Autowired
    private RecordingActionWrapper wrapper;

    @Test
    public void recordLoginFlowSession() throws IOException {
        // Start recording session
        wrapper.startSession("login-flow");

        // Create patterns
        StateImage usernameField = new StateImage.Builder()
            .addPattern("images/login/field-username.png")
            .build();

        StateImage passwordField = new StateImage.Builder()
            .addPattern("images/login/field-password.png")
            .build();

        StateImage loginButton = new StateImage.Builder()
            .addPattern("images/login/button-login.png")
            .build();

        // Perform test actions - all recorded to session
        wrapper.find(usernameField);
        wrapper.click(usernameField);
        wrapper.type("testuser");
        wrapper.find(passwordField);
        wrapper.click(passwordField);
        wrapper.type("password123");
        wrapper.click(loginButton);

        // End session and save all histories
        Collection<Pattern> patterns = getAllPatternsFromStateImages(
            List.of(usernameField, passwordField, loginButton)
        );
        wrapper.endSession("login-flow", patterns);

        // History files saved:
        // - src/test/resources/histories/login-flow_field-username.json
        // - src/test/resources/histories/login-flow_field-password.json
        // - src/test/resources/histories/login-flow_button-login.json
    }

    private Collection<Pattern> getAllPatternsFromStateImages(List<StateImage> stateImages) {
        return stateImages.stream()
            .flatMap(si -> si.getPatterns().stream())
            .toList();
    }
}
```

### Loading and Using Recorded History

```java
package io.github.jspinak.brobot.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.patterns.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.actionhistory.ActionHistoryPersistence;

import java.io.IOException;

/**
 * Using recorded history for mock testing.
 */
public class MockTestWithHistoryTest extends BrobotTestBase {

    @Autowired
    private ActionHistoryPersistence persistence;

    @Autowired
    private Action action;

    @Autowired
    private BrobotProperties brobotProperties;

    @Test
    public void mockTestWithRecordedHistory() throws IOException {
        // Load previously recorded history
        ActionHistory history = persistence.loadFromFile(
            "src/test/resources/histories/login-flow_button-login.json"
        );

        // Create pattern and apply history
        StateImage loginButton = new StateImage.Builder()
            .addPattern("images/login/button-login.png")
            .build();

        Pattern buttonPattern = loginButton.getPatterns().get(0);
        buttonPattern.setMatchHistory(history);

        // Enable mock mode
        brobotProperties.getCore().setMock(true);

        // Action will use recorded history for realistic mock behavior
        ActionResult result = action.find(loginButton);

        // Result comes from recorded history
        assertTrue(result.isSuccess());
        assertTrue(result.getMatchList().size() > 0);

        // Can verify specific details from recorded data
        System.out.println("Using recorded match at: " +
            result.getBestMatch().get().getRegion());
    }
}
```

### Exporting History Reports

```java
package io.github.jspinak.brobot.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.patterns.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.actionhistory.ActionHistoryExporter;

import java.io.IOException;
import java.util.Map;

/**
 * Exporting action history to various formats for analysis.
 */
public class HistoryExportTest extends BrobotTestBase {

    @Autowired
    private ActionHistoryExporter exporter;

    @Test
    public void exportHistoryReports() throws IOException {
        // Get history from pattern
        StateImage loginButton = new StateImage.Builder()
            .addPattern("images/login/button-login.png")
            .build();

        Pattern pattern = loginButton.getPatterns().get(0);
        ActionHistory history = pattern.getMatchHistory();

        if (history != null && !history.getSnapshots().isEmpty()) {
            // Export to CSV for spreadsheet analysis
            exporter.exportToCSV(history, "test-reports/login-test-results.csv");

            // Export to HTML with visualizations
            exporter.exportToHTML(history, "test-reports/login-test-report.html");

            // Get summary statistics
            Map<String, Object> summary = exporter.generateSummary(history);
            System.out.println("Total actions: " + summary.get("totalActions"));
            System.out.println("Success rate: " + summary.get("successRate") + "%");
            System.out.println("Average duration: " + summary.get("avgDuration") + "ms");
            System.out.println("Min duration: " + summary.get("minDuration") + "ms");
            System.out.println("Max duration: " + summary.get("maxDuration") + "ms");

            // Action type breakdown
            @SuppressWarnings("unchecked")
            Map<String, Long> actionTypes = (Map<String, Long>) summary.get("actionTypes");
            actionTypes.forEach((type, count) ->
                System.out.println(type + ": " + count + " executions")
            );
        }
    }
}
```

## When to Use Action Recording

### Use Illustrated Screenshots For:

✅ **Quick visual verification** during development
✅ **Debugging failed automation** - see exactly what happened
✅ **Documentation** for manual QA review
✅ **Demonstrating automation** to stakeholders
✅ **Sanity checks** before committing code

### Use ActionHistory Recording For:

✅ **Building mock test data** for headless CI/CD
✅ **Performance benchmarking** and regression detection
✅ **Integration test baselines** for comparison
✅ **Analyzing pattern reliability** across multiple runs
✅ **Creating realistic test fixtures** from real automation

### Don't Use Action Recording For:

❌ **Real-time debugging** - Use [Image Find Debugging](../03-core-library/tools/image-find-debugging.md) instead
❌ **Production automation** - Recording adds overhead
❌ **Long-running operations** - Storage requirements grow quickly
❌ **Continuous monitoring** - Use [logging](../07-logging/usage.md) instead

## Performance Considerations

### Illustrated Screenshots

- **Overhead**: 50-200ms per action (screenshot capture + annotation)
- **Storage**: 100-500 KB per illustrated screenshot
- **Best Practice**: Enable only during development/debugging

### ActionHistory Recording

- **Overhead**: 1-5ms per action (minimal - just data collection)
- **Storage**: 1-10 KB per JSON record
- **Best Practice**: Can run continuously for test data collection

## Troubleshooting

### Illustrated Screenshots Not Saving

**Check configuration:**
```properties
# Must be enabled
brobot.screenshot.save-history=true

# Verify directory exists or is writable
brobot.screenshot.history-path=history/
```

**Check per-action override:**
```java
// Ensure not explicitly disabled
PatternFindOptions options = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.YES)  // Force enable
    .build();
```

### ActionHistory Not Recording

**Verify you're using RecordingActionWrapper:**
```java
// ❌ Wrong - regular Action doesn't record
@Autowired
private Action action;
action.find(pattern);  // Not recorded

// ✅ Correct - RecordingActionWrapper records automatically
@Autowired
private RecordingActionWrapper wrapper;
wrapper.setRecordingEnabled(true);
wrapper.find(pattern);  // Automatically recorded
```

### Storage Growing Too Large

**For illustrated screenshots:**
```properties
# Disable in production
brobot.screenshot.save-history=false

# Or disable specific action types
brobot.illustration.draw-find=false
brobot.illustration.draw-move=false  # Move actions create many screenshots
```

**For ActionHistory JSON files:**
```java
// Use session-based recording with explicit cleanup
wrapper.startSession("test-session");
// ... perform actions ...
wrapper.endSession("test-session", patterns);
// Delete old sessions periodically
```

### Missing Match Details in Illustrated Screenshots

**Ensure illustration is enabled for action type:**
```properties
# Enable specific action illustrations
brobot.illustration.draw-find=true      # For Find actions
brobot.illustration.draw-click=true     # For Click actions
brobot.illustration.draw-drag=true      # For Drag actions
```

## Best Practices

### 1. Use Recording Strategically

**During Development:**
```properties
# Enable illustrated screenshots for visual feedback
brobot.screenshot.save-history=true
```

**During Testing:**
```java
// Use RecordingActionWrapper to build test data
wrapper.startSession("acceptance-test");
// ... perform actions ...
wrapper.endSession("acceptance-test", patterns);
```

**In Production:**
```properties
# Disable recording to avoid overhead
brobot.screenshot.save-history=false
```

### 2. Organize Recorded Data

Use session names to organize recordings:
```java
wrapper.startSession("feature-login");      // history/feature-login_*.json
wrapper.startSession("feature-checkout");   // history/feature-checkout_*.json
wrapper.startSession("regression-suite");   // history/regression-suite_*.json
```

### 3. Clean Up Old Recordings

Implement periodic cleanup to prevent storage bloat:
```java
// Keep only last 7 days of illustrated screenshots
Files.list(Paths.get("history/"))
    .filter(path -> Files.isRegularFile(path))
    .filter(path -> {
        try {
            FileTime modified = Files.getLastModifiedTime(path);
            return modified.toInstant()
                .isBefore(Instant.now().minus(7, ChronoUnit.DAYS));
        } catch (IOException e) {
            return false;
        }
    })
    .forEach(path -> {
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.err.println("Failed to delete: " + path);
        }
    });
```

### 4. Combine with Other Testing Approaches

Action recording works best when combined with other testing strategies:

```java
@Test
public void comprehensiveTest() {
    // 1. Enable recording for this test
    recordingWrapper.setRecordingEnabled(true);

    // 2. Use illustrated screenshots for visual verification
    brobotProperties.getScreenshot().setSaveHistory(true);

    // 3. Perform actions - both systems record simultaneously
    recordingWrapper.find(loginButton);
    recordingWrapper.click(loginButton);

    // 4. Review illustrated screenshots visually
    // 5. Use recorded ActionHistory for mock testing
    // 6. Export performance metrics for analysis
}
```

## Comparison with Image Find Debugging

| Feature | Action Recording | Image Find Debugging |
|---------|-----------------|---------------------|
| **Timing** | Post-execution | Real-time |
| **Output** | PNG screenshots + JSON data | PNG screenshots |
| **Use Case** | Visual verification, test data | Active debugging |
| **Performance** | Minimal overhead | Can pause execution |
| **Best For** | Automated testing | Development debugging |

For real-time debugging with pattern matching details, see the [Image Find Debugging Guide](../03-core-library/tools/image-find-debugging.md).

## Related Documentation

### Testing & Debugging
- **[Testing Introduction](./testing-intro.md)** - Overview of Brobot testing strategies
- **[ActionHistory Integration Testing](./actionhistory-integration-testing.md)** - Using recorded data in tests
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Mock testing with recorded data
- **[Mock Mode Guide](./mock-mode-guide.md)** - Headless testing with mock data
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching issues
- **[Image Find Debugging](../03-core-library/tools/image-find-debugging.md)** - Real-time debugging with screenshots

### Configuration
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete configuration properties
- **[Brobot Properties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Using BrobotProperties in code

### Logging & Monitoring
- **[Logging Usage](../07-logging/usage.md)** - Text-based logging complementary to visual recording
- **[Logging Configuration](../07-logging/configuration.md)** - Configuring log output

## Summary

Brobot's action recording provides two powerful tools for verification and testing:

1. **Illustrated Screenshots** - Visual proof of what automation did, perfect for quick verification and debugging
2. **ActionHistory Recording** - Programmatic test data for mock testing and performance analysis

Both systems work independently or together, providing flexibility for different testing scenarios. Use illustrated screenshots during development for immediate visual feedback, and use ActionHistory recording to build robust test fixtures for CI/CD pipelines.

**Key Takeaways:**
- Configure via `brobot.screenshot.*` and `brobot.illustration.*` properties
- Use `RecordingActionWrapper` for programmatic data collection
- Combine with [mock mode](./mock-mode-guide.md) for headless testing
- Clean up old recordings to manage storage
- Prefer [Image Find Debugging](../03-core-library/tools/image-find-debugging.md) for real-time debugging

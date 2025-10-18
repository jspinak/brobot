# Visual Feedback and Highlighting

## Overview
Brobot provides visual feedback capabilities to help with debugging and monitoring automation execution. The system includes basic highlighting of matches and console action reporting. Visual feedback helps developers understand what the automation is doing and quickly identify issues.

> **Note**: This guide documents the currently implemented highlighting features. Some advanced features (labels, animations, custom colors per StateImage) are planned for future releases.

## Prerequisites

Before using visual feedback features, ensure you understand:
- [States and StateImage](../../../01-getting-started/states.md) - How to define UI elements
- [Actions](../../03-core-library/action-config/01-overview.md) - How to perform operations on elements
- [Testing](../../../04-testing/testing-intro.md) - How to test automation scripts

## Current Implementation Status

### ✅ Working Features
- **Basic highlighting** via `MatchHighlighter` class
- **Console action reporting** with `brobot.console.actions.*` properties
- **Aspect-based visual feedback** via `VisualFeedbackAspect` (limited functionality)
- **Manual highlighting** using `action.highlight()` or `HighlightOptions`

### ⚠️ Planned Features (Not Yet Implemented)
- Custom highlight colors per `StateImage` (field exists but not used)
- Automatic highlighting of all finds
- Confidence score labels
- Flash animations
- Multiple match color gradients
- Advanced performance tuning

## Configuration

### Working Properties

Brobot currently supports two highlighting property namespaces:

#### 1. Aspect-Based Visual Feedback (Recommended)

Configure via `brobot.aspects.visual-feedback.*` properties:

```properties
# Enable/disable the VisualFeedbackAspect
brobot.aspects.visual-feedback.enabled=true

# Duration in seconds
brobot.aspects.visual-feedback.highlight-duration=2

# Highlight color (YELLOW, RED, GREEN, etc.)
brobot.aspects.visual-feedback.highlight-color=YELLOW

# Show action flow arrows
brobot.aspects.visual-feedback.show-action-flow=true

# Show confidence scores
brobot.aspects.visual-feedback.show-confidence-scores=true
```

#### 2. Console Action Reporting (Fully Functional)

Configure detailed console output:

```properties
# Enable console action reporting
brobot.console.actions.enabled=true

# Verbosity: QUIET, NORMAL, VERBOSE
brobot.console.actions.level=VERBOSE

# Show match details (location, score)
brobot.console.actions.show-match-details=true

# Show timing information
brobot.console.actions.show-timing=true

# Use colored output (requires ANSI support)
brobot.console.actions.use-colors=true

# Use unicode icons
brobot.console.actions.use-icons=true

# Report specific action types
brobot.console.actions.report.find=true
brobot.console.actions.report.click=true
brobot.console.actions.report.type=true
brobot.console.actions.report.highlight=false
```

#### 3. Planned Properties (Defined but Not Implemented)

These properties exist in `brobot-visual-feedback.properties` but have no Java configuration classes to bind them:

```properties
# These are PLANNED but not yet functional:
# brobot.highlight.enabled=true
# brobot.highlight.auto-highlight-finds=true
# brobot.highlight.find.color=#00FF00
# brobot.highlight.find.duration=2.0
# ... and 30+ other properties
```

> **Important**: The `brobot.highlight.*` properties are defined in properties files but **do not currently work** because the `VisualFeedbackConfig` class that would bind them has been removed from the codebase.

### Properties Files

The default properties are in:
```
brobot/library/src/main/resources/brobot-visual-feedback.properties
```

For complete property documentation, see [Properties Reference](../../03-core-library/configuration/properties-reference.md).

## Usage

### Method 1: Manual Highlighting with Action API

The most reliable way to highlight elements:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;

public class HighlightExample {
    @Autowired
    private Action action;

    public void highlightButton() {
        // Create StateImage
        StateImage button = new StateImage.Builder()
            .addPattern("button.png")
            .setName("SubmitButton")
            .build();

        // Find the button
        ActionResult result = action.find(button);

        // Highlight it manually
        if (result.isSuccess()) {
            HighlightOptions options = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("yellow")
                .build();

            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(button)
                .build();

            action.perform(options, collection);
        }
    }
}
```

### Method 2: Using MatchHighlighter Directly

For low-level highlighting control:

```java
import io.github.jspinak.brobot.tools.history.draw.MatchHighlighter;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;

public class DirectHighlightExample {
    @Autowired
    private MatchHighlighter matchHighlighter;

    public void highlightMatch(Match match) {
        HighlightOptions options = new HighlightOptions.Builder()
            .setHighlightSeconds(2.0)
            .setHighlightColor("red")
            .build();

        StateObjectMetadata metadata = match.getStateObjectData();
        matchHighlighter.highlight(match, metadata, options);
    }
}
```

### Method 3: Aspect-Based Visual Feedback (Limited)

Enable automatic visual feedback via aspects:

```properties
# In application.properties
brobot.aspects.visual-feedback.enabled=true
brobot.aspects.visual-feedback.highlight-duration=2
brobot.aspects.visual-feedback.highlight-color=YELLOW
```

> **Note**: The `VisualFeedbackAspect` has limited functionality. Much of its highlighting code is commented out awaiting the restoration of removed classes.

## Testing Highlighting

Example test using Brobot's test framework:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test highlighting functionality.
 * For more on testing, see the Testing Guide.
 */
@SpringBootTest
public class HighlightingTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testManualHighlighting() {
        // Create test image
        StateImage testImage = new StateImage.Builder()
            .addPattern("test-button.png")
            .setName("TestButton")
            .build();

        // Find and highlight
        ActionResult findResult = action.find(testImage);
        assertTrue(findResult.isSuccess(), "Should find the test image");

        // Manual highlight
        HighlightOptions options = new HighlightOptions.Builder()
            .setHighlightSeconds(1.0)
            .setHighlightColor("green")
            .build();

        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(testImage)
            .build();

        ActionResult highlightResult = action.perform(options, collection);
        assertTrue(highlightResult.isSuccess(), "Should highlight successfully");
    }
}
```

For more testing patterns, see:
- [Testing Introduction](../../../04-testing/testing-intro.md)
- [Unit Testing Guide](../../../04-testing/unit-testing.md)
- [Mock Mode Guide](../../../04-testing/mock-mode-guide.md)

## How It Works

### Component Architecture

1. **MatchHighlighter** (`io.github.jspinak.brobot.tools.history.draw.MatchHighlighter`)
   - Low-level highlighting implementation
   - Wraps SikuliX highlighting functionality
   - Supports mock mode (logs instead of displaying)
   - Handles timed highlights with custom colors

2. **VisualFeedbackAspect** (`io.github.jspinak.brobot.aspects.display.VisualFeedbackAspect`)
   - AOP-based automatic visual feedback
   - Intercepts find/click/type operations
   - Currently has limited functionality (much code commented out)
   - For more on aspects, see [AspectJ Usage Guide](../../03-core-library/advanced/aspectj-usage-guide.md)

3. **HighlightOptions** (`io.github.jspinak.brobot.action.basic.highlight.HighlightOptions`)
   - Configuration for highlight operations
   - Specifies duration, color, and behavior
   - Used with `action.perform(options, collection)`

4. **ConsoleActionReporter** (part of logging system)
   - Reports actions to console with formatting
   - Supports colors, icons, and detailed information
   - Configured via `brobot.console.actions.*` properties

### Mock Mode Support

Highlighting works in both live and mock modes:

```java
// In mock mode (headless/CI):
// - Highlighting is logged instead of displayed
// - No AWT/graphics exceptions
// - Fast execution for tests

// In live mode (with display):
// - Visual highlights appear on screen
// - Useful for debugging and demonstrations
```

For mock mode details, see [Mock Mode Guide](../../../04-testing/mock-mode-guide.md).

## Environment-Specific Configuration

### Development Profile

Maximum visibility for debugging:

```properties
# application-development.properties
brobot.console.actions.level=VERBOSE
brobot.console.actions.show-match-details=true
brobot.aspects.visual-feedback.enabled=true
```

### CI/CD Profile

Minimal output for automated testing:

```properties
# application-ci.properties
brobot.console.actions.level=QUIET
brobot.aspects.visual-feedback.enabled=false
brobot.core.mock=true
```

### Production Profile

Essential logging only:

```properties
# application-production.properties
brobot.console.actions.enabled=false
brobot.aspects.visual-feedback.enabled=false
```

## Troubleshooting

### Highlighting Not Appearing

**Issue**: No visual highlights appear on screen

**Solutions**:
1. Verify not in mock mode: `brobot.core.mock=false`
2. Verify aspect is enabled: `brobot.aspects.visual-feedback.enabled=true`
3. Check you're using working features (manual highlighting, not auto-highlighting)
4. Verify display is available (not headless environment)

### Properties Not Working

**Issue**: Setting `brobot.highlight.*` properties has no effect

**Solution**: These properties are defined but not yet implemented. Use `brobot.aspects.visual-feedback.*` or `brobot.console.actions.*` instead.

### Mock Mode Issues

**Issue**: Tests fail with AWT/graphics exceptions

**Solution**: Extend `BrobotTestBase` which automatically enables mock mode. See [Testing Guide](../../../04-testing/testing-intro.md).

### Console Output

Enable detailed logging for debugging:

```properties
# For highlighting debugging
logging.level.io.github.jspinak.brobot.tools.history.draw.MatchHighlighter=DEBUG
logging.level.io.github.jspinak.brobot.aspects.display.VisualFeedbackAspect=DEBUG

# For action reporting
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE
```

## Limitations and Future Plans

### Current Limitations

1. **No auto-highlighting** - Must manually call `action.highlight()` or use `HighlightOptions`
2. **No custom colors per StateImage** - `StateImage.setHighlightColor()` exists but isn't used
3. **No confidence labels** - Score display not implemented
4. **No animations** - Flash effects and gradients not available
5. **Limited aspect functionality** - Much of `VisualFeedbackAspect` code is commented out

### Planned Enhancements

Future versions may include:
- Automatic highlighting of all find operations
- Custom highlight colors per StateImage (field already exists)
- Confidence score labels on highlights
- Flash animations and gradient colors
- Screenshot capture of highlighted regions
- Video recording with highlights
- Performance optimization for multiple highlights

### Contributing

If you need advanced highlighting features:
1. Check the existing field: `StateImage.highlightColor` (line 171 in StateImage.java)
2. Implement logic to retrieve and use this color in find operations
3. Consider creating a `VisualFeedbackConfig` class to bind `brobot.highlight.*` properties
4. Submit a pull request with your improvements

## Related Documentation

### Core Concepts
- [States](../../../01-getting-started/states.md) - Define UI elements to highlight
- [Actions](../../03-core-library/action-config/01-overview.md) - Perform operations with highlighting
- [Action Convenience Methods](../../03-core-library/action-config/18-convenience-methods.md) - Includes `action.highlight()`

### Configuration
- [Properties Reference](../../03-core-library/configuration/properties-reference.md) - All configuration options
- [BrobotProperties Usage](../../03-core-library/configuration/brobot-properties-usage.md) - Access properties in code

### Testing and Debugging
- [Testing Introduction](../../../04-testing/testing-intro.md) - Test framework overview
- [Mock Mode Guide](../../../04-testing/mock-mode-guide.md) - Headless testing
- [Image Find Debugging](../../03-core-library/tools/image-find-debugging.md) - Comprehensive debugging system

### Advanced Topics
- [AspectJ Usage Guide](../../03-core-library/advanced/aspectj-usage-guide.md) - How aspects work
- [Search Regions](./search-regions-and-fixed-locations.md) - Define where to look

## Best Practices

1. **Use Manual Highlighting During Development**
   ```java
   // Find, then explicitly highlight for debugging
   ActionResult result = action.find(stateImage);
   if (result.isSuccess()) {
       action.highlight(stateImage); // Convenience method
   }
   ```

2. **Enable Console Reporting for Visibility**
   ```properties
   brobot.console.actions.level=VERBOSE
   brobot.console.actions.show-match-details=true
   ```

3. **Use Mock Mode for Tests**
   ```java
   @SpringBootTest
   public class MyTest extends BrobotTestBase {
       // Mock mode automatically enabled
       // Highlighting logged instead of displayed
   }
   ```

4. **Configure Per Environment**
   - Development: VERBOSE logging, highlighting enabled
   - CI/CD: QUIET logging, mock mode
   - Production: Minimal logging, no highlighting

## Summary

Brobot's current visual feedback system provides:
- ✅ Manual highlighting via `action.highlight()` and `HighlightOptions`
- ✅ Low-level highlighting via `MatchHighlighter`
- ✅ Console action reporting with colors and icons
- ✅ Mock mode support for headless testing
- ✅ Aspect-based interception (limited functionality)

Advanced features like automatic highlighting, custom colors per StateImage, and labels are planned for future releases. For now, use manual highlighting and console reporting for effective debugging and monitoring.
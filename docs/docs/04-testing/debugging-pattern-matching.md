---
sidebar_position: 5
---

# Debugging Pattern Matching

When patterns fail to match during automation, Brobot provides several powerful debugging tools to help identify and resolve issues.

## Image History and Debug Saving

### Important: Images are NOT saved by default

Brobot's image saving features are **disabled by default** to prevent filling up disk space. You must explicitly enable them when debugging.

```properties
# Image saving is DISABLED by default
brobot.screenshot.save-history=false  # Default: false
brobot.debug.image.enabled=false      # Default: false

# Enable only when actively debugging
brobot.screenshot.save-history=true   # Enable action history images
brobot.debug.image.enabled=true       # Enable debug visualizations
```

### What Gets Saved

When enabled, Brobot saves:
- Screenshots with match highlights and search regions
- Action visualization sidebars showing match details
- Classification overlays showing image segmentation
- Motion detection visualizations
- Failed match comparisons

### Configuration

```properties
# Master switches (both default to false)
brobot.screenshot.save-history=false
brobot.debug.image.enabled=false

# Where to save images
brobot.screenshot.history-path=history/
brobot.screenshot.history-filename=hist
brobot.debug.image.output-dir=debug/image-finding

# Control what gets saved (when enabled)
brobot.debug.image.save-screenshots=true
brobot.debug.image.save-patterns=true
brobot.debug.image.save-comparisons=true

# Visual annotations
brobot.debug.image.visual.show-search-regions=true
brobot.debug.image.visual.show-match-scores=true
brobot.debug.image.visual.show-failed-regions=true
brobot.debug.image.visual.highlight-best-match=true
```

### When to Enable

Enable image saving only when:
- Debugging pattern matching failures
- Analyzing automation behavior
- Creating documentation
- Training new patterns

Remember to disable after debugging to avoid disk space issues.

## Programmatic Control in Tests

While most debugging features are configured via properties, `BestMatchCapture` can be controlled programmatically in tests for fine-grained control.

### Enabling in Test Classes

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.find.scene.BestMatchCapture;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootTest
public class PatternMatchingDebugTest extends BrobotTestBase {

    @Autowired
    private BestMatchCapture bestMatchCapture;

    @Autowired
    private Action action;

    @BeforeEach
    void enableDebugCapture() {
        // Enable best match capture for debugging
        bestMatchCapture.setCaptureEnabled(true);
    }

    @Test
    void testPatternMatching() {
        // Assume stateImage is defined elsewhere (e.g., @Autowired State class)
        // When pattern matching fails, best match will be captured
        // and saved to history/best-matches/ directory
        ActionResult result = action.find(stateImage);

        // Check captured files if match failed
        if (!result.isSuccess()) {
            // Images saved as: history/best-matches/YYYYMMDD-HHmmss_pattern_simXXX_*.png
        }
    }

    @AfterEach
    void disableDebugCapture() {
        // Clean up: disable after each test
        bestMatchCapture.setCaptureEnabled(false);
    }
}
```

### Selective Debugging

Enable capture for specific test methods only:

```java
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;

@Test
void debugProblematicPattern() {
    // Assume problematicPattern is defined elsewhere
    // Enable only for this test
    bestMatchCapture.setCaptureEnabled(true);

    try {
        ActionResult result = action.find(problematicPattern);
        // Capture will be saved if match fails

        // Analyze results...
        if (!result.isSuccess()) {
            // Check history/best-matches/ for captured images
        }
    } finally {
        // Always disable in finally block
        bestMatchCapture.setCaptureEnabled(false);
    }
}
```

### Test Profile Configuration

For `ImageDebugConfig` features, use test profiles:

```properties
# src/test/resources/application-debug.properties
brobot.debug.image.enabled=true
brobot.debug.image.save-screenshots=true
brobot.debug.image.save-patterns=true
brobot.debug.image.save-comparisons=true
```

Then activate in test:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootTest
@ActiveProfiles("debug")
public class VisualDebugTest extends BrobotTestBase {

    @Autowired
    private Action action;

    // ImageFindDebugger will be automatically enabled
    // Debug images will be saved to debug/image-finding/

    @Test
    void testWithVisualDebugging() {
        // Assume stateImage is defined elsewhere
        // All pattern matching will save debug visualizations
        ActionResult result = action.find(stateImage);
    }
}
```

**Design Note**: `BestMatchCapture` provides programmatic control because it's designed for runtime debugging in tests. `ImageDebugConfig` is configuration-only by design, meant to be enabled via Spring profiles or properties files.

## Best Match Capture

The Best Match Capture feature helps debug pattern matching failures by capturing and saving the region that best matches your pattern, even when it doesn't meet the similarity threshold.

### How It Works

When enabled, the Best Match Capture system:
1. Detects when pattern searches fail or find low-similarity matches
2. Performs a search at very low threshold (0.1) to find any potential match
3. Captures the best matching region from the screen
4. Saves both the matched region and original pattern for comparison
5. Names files with timestamp and similarity score for easy tracking

### Configuration

Add these properties to your `application.properties`:

```properties
# Enable best match capture (default: false)
brobot.debug.capture-best-match=true

# Only capture when match is below this threshold (default: 0.95)
brobot.debug.capture-threshold=0.95

# Directory to save captured images (default: history/best-matches)
brobot.debug.capture-directory=history/best-matches

# Also save the pattern image for comparison (default: true)
brobot.debug.save-pattern-image=true
```

### Output Format

Captured images are saved with descriptive filenames:
- **Match image**: `YYYYMMDD-HHmmss_PatternName_sim###_match.png`
- **Pattern image**: `YYYYMMDD-HHmmss_PatternName_sim###_pattern.png`

Example:
```
history/best-matches/
├── 20250810-143022_claude-prompt-1_sim045_match.png
├── 20250810-143022_claude-prompt-1_sim045_pattern.png
├── 20250810-143025_submit-button_sim062_match.png
└── 20250810-143025_submit-button_sim062_pattern.png
```

### Logging Output

Best Match Capture uses SLF4J DEBUG-level logging. To see capture messages, enable DEBUG logging:

```properties
# Enable DEBUG logging for best match capture
logging.level.io.github.jspinak.brobot.action.internal.find.scene.BestMatchCapture=DEBUG
```

When a best match is captured, you'll see log messages like:
```
DEBUG BestMatchCapture - Best match captured - Pattern: claude-prompt-1, Location: (100, 200), Size: 50x30, Similarity: 0.453
```

**Note**: File save operations happen silently. Check the `history/best-matches/` directory for captured images.

### Use Cases

This feature is particularly useful for:

1. **Understanding Match Failures**: Compare what the pattern looks like vs what was actually found
2. **Similarity Threshold Tuning**: See the actual similarity scores to adjust thresholds
3. **UI Changes Detection**: Identify when the UI has changed slightly (fonts, colors, scaling)
4. **Resolution Issues**: Detect when patterns fail due to different screen resolutions
5. **Alpha Channel Issues**: Identify transparency-related matching problems

### Analyzing Captured Images

When analyzing captured images:

1. **Compare visually**: Open both pattern and match images side-by-side
2. **Check for differences**:
   - Color variations (especially with dark/light themes)
   - Font rendering differences
   - Scaling or resolution mismatches
   - Anti-aliasing artifacts
   - Transparency/alpha channel issues

3. **Adjust patterns based on findings**:
   - Update pattern images if UI has changed
   - Adjust similarity thresholds if minor variations are acceptable
   - Consider using multiple pattern variations
   - Remove alpha channels if causing issues

## Progressive Similarity Testing

Brobot can perform progressive similarity testing when patterns fail, testing at decreasing thresholds (0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3) to find the minimum similarity at which a pattern would match.

### Configuration

Enable diagnostic logging to see similarity analysis:

```properties
# Enable diagnostic logging for similarity testing
logging.level.io.github.jspinak.brobot.tools.logging.DiagnosticLogger=DEBUG
```

**Note**: Progressive similarity testing uses Brobot's structured logging system (BrobotLogger) rather than console output. The testing helps identify what similarity threshold would be needed for pattern matching to succeed.

## Debug Image Saving

When patterns with "prompt" in their name fail to match, Brobot automatically saves debug images to help diagnose the issue:

```
debug_images/
├── pattern_claude-prompt-1.png
├── pattern_claude-prompt-2.png
└── scene_current.png
```

## Image Analysis

Brobot provides automatic image content analysis to detect common issues like black screens or invalid captures. This analysis is performed internally and logged via Brobot's structured logging system.

### Configuration

Enable diagnostic logging to see image analysis:

```properties
# Enable diagnostic logging for image analysis
logging.level.io.github.jspinak.brobot.tools.logging.DiagnosticLogger=DEBUG
logging.level.io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher=DEBUG
```

Image analysis examines:
- Image dimensions and type (RGB vs ARGB)
- Pixel content distribution (% black, % white)
- Average RGB values
- Potential capture failures (mostly black screens)

**Note**: Analysis results are logged to Brobot's structured logging system for diagnostic purposes.

## Logging Configuration

Control the amount of debug information with logging settings:

```properties
# Brobot verbosity level (QUIET, NORMAL, VERBOSE)
brobot.logging.verbosity=VERBOSE
brobot.console.actions.level=VERBOSE

# Enable specific debug logging for debugging features
logging.level.io.github.jspinak.brobot.action.internal.find.scene.BestMatchCapture=DEBUG
logging.level.io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher=DEBUG
logging.level.io.github.jspinak.brobot.tools.logging.DiagnosticLogger=DEBUG

# Enable all find operation logging
logging.level.io.github.jspinak.brobot.action.internal.find=DEBUG
```

**Note**: Brobot uses SLF4J for logging. The default implementation is Logback, configured via `logback.xml` or `logback-spring.xml`.

## Troubleshooting Common Issues

### Pattern Not Found at Expected Similarity

**Symptoms**: Pattern matches at 0.5 but threshold is 0.7

**Solutions**:
1. Enable best match capture to see what's being matched
2. Check for alpha channel issues (transparent backgrounds)
3. Verify color space consistency (RGB vs ARGB)
4. Consider UI theme differences (dark vs light mode)

### Black or Invalid Screen Captures

**Symptoms**: Scene images are all black or invalid

**Solutions**:
1. Check display permissions and settings
2. Verify not running in true headless mode
3. For WSL2, ensure X11 forwarding is configured
4. Check `DISPLAY` environment variable

### Patterns Match in Wrong Location

**Symptoms**: Patterns found but in unexpected screen areas

**Solutions**:
1. Define search regions to limit search area
2. Check for duplicate UI elements
3. Increase similarity threshold
4. Use fixed regions for consistent UI elements

## Best Practices

1. **Always enable during development**: Keep best match capture enabled while developing patterns
2. **Review captured images regularly**: Check the history folder to understand matching behavior
3. **Clean up periodically**: Delete old captures to save disk space
4. **Use meaningful pattern names**: Makes captured files easier to identify
5. **Adjust thresholds based on captures**: Use actual similarity scores to set appropriate thresholds
6. **Use programmatic control in tests**: Enable `BestMatchCapture` selectively with `setCaptureEnabled()`
7. **Configure logging appropriately**: Enable DEBUG logging only when needed for specific debugging tasks

## Related Documentation

### Testing & Debugging
- **[Testing Introduction](testing-intro.md)** - Overview of Brobot testing capabilities
- **[Unit Testing Guide](unit-testing.md)** - Unit testing with static screenshots
- **[Integration Testing Guide](integration-testing.md)** - Full workflow simulation with mock execution
- **[Mock Mode Guide](mock-mode-guide.md)** - Testing without GUI using mock mode
- **[Profile-Based Testing](profile-based-testing.md)** - Using Spring profiles for test configuration
- **[Test Utilities](test-utilities.md)** - BrobotTestBase and test data factories
- **[Mat Testing Utilities](mat-testing-utilities.md)** - Safe OpenCV Mat operations for tests
- **[Testing Strategy](testing-strategy.md)** - Comprehensive testing strategy

### Configuration
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all Brobot configuration properties
- **[Auto-Configuration Guide](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration setup
- **[Action Config Factory](../03-core-library/guides/configuration/action-config-factory.md)** - Factory patterns for creating action configurations

### Pattern Matching
- **[Capture Quick Reference](../03-core-library/capture/capture-quick-reference.md)** - Screen capture provider setup
- **[DPI Resolution Guide](../03-core-library/capture/dpi-resolution-guide.md)** - Handling DPI scaling and resolution issues
- **[Modular Capture System](../03-core-library/capture/modular-capture-system.md)** - Complete capture provider guide
- **[Pattern Creation Tools](../03-core-library/tools/pattern-creation-tools.md)** - Best tools for creating patterns
- **[Pattern Capture Tool Guide](../03-core-library/tools/pattern-capture-tool-guide.md)** - Using Brobot's pattern capture tool
- **[Capture Methods Comparison](../03-core-library/tools/capture-methods-comparison.md)** - Performance comparison of capture providers

### Core Concepts
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Modern ActionConfig API for pattern matching
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical pattern matching examples
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference including PatternFindOptions
- **[Search Regions and Fixed Locations](../03-core-library/guides/user-guides/search-regions-and-fixed-locations.md)** - Optimizing pattern searches with regions
- **[Declarative Region Definition](../03-core-library/guides/user-guides/declarative-region-definition.md)** - Defining search regions relative to other objects
- **[Screen Adaptive Regions](../03-core-library/guides/user-guides/screen-adaptive-regions.md)** - Resolution-independent region definitions

### Getting Started
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Getting started with Brobot pattern matching
- **[Installation Guide](../01-getting-started/installation.md)** - Platform setup including dependencies
- **[States Guide](../01-getting-started/states.md)** - Understanding StateImage and pattern organization
- **[Core Concepts](../01-getting-started/core-concepts.md)** - Fundamental Brobot concepts

### Other Testing
- **[Mock Stochasticity](mock-stochasticity.md)** - Probabilistic behavior in mock mode
- **[Mock Mode Migration](mock-mode-migration.md)** - Migrating to modern mock mode architecture
- **[Fail-Safe Image Loading](fail-safe-image-loading.md)** - Robust image loading strategies
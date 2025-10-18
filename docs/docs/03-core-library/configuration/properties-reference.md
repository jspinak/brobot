# Brobot Configuration Properties Reference

This document provides a comprehensive reference for all Brobot configuration properties that can be set in `application.properties` or `application.yml`.

> **Looking for usage examples?** See the [BrobotProperties Usage Guide](brobot-properties-usage.md) for how to use these properties in your code.

## Table of Contents
- [Image Find Debugging](#image-find-debugging)
- [Headless Detection](#headless-detection)
- [Mock Mode](#mock-mode)
- [Automation Runner](#automation-runner)
- [Core Settings](#core-settings)
- [Mouse Configuration](#mouse-configuration)
- [Logging](#logging)
- [Visual Feedback and Highlighting](#visual-feedback-and-highlighting)
- [DPI Configuration](#dpi-configuration)
- [Screen Capture](#screen-capture)
- [Monitor Configuration](#monitor-configuration)
- [GUI Access](#gui-access)
- [Startup Configuration](#startup-configuration)
- [Profile-Based Configuration](#profile-based-configuration)
- [Environment Variables](#environment-variables)
- [Best Practices](#best-practices)
- [Related Documentation](#related-documentation)

---

## Image Find Debugging

Properties for the comprehensive image finding debug system. See [Image Find Debugging Guide](../tools/image-find-debugging.md) for detailed usage.

### Master Controls

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.enabled` | boolean | `false` | Master switch for image debugging |
| `brobot.debug.image.level` | enum | `BASIC` | Debug level: OFF, BASIC, DETAILED, VISUAL, FULL |
| `brobot.debug.image.output-dir` | string | `debug/image-finding` | Output directory for debug files |

### File Saving

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.save-screenshots` | boolean | `true` | Save screenshots of each find operation |
| `brobot.debug.image.save-patterns` | boolean | `true` | Save pattern images for reference |
| `brobot.debug.image.save-comparisons` | boolean | `true` | Save visual comparison grids |

### Visual Debugging

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.visual.enabled` | boolean | `true` | Enable visual debugging features |
| `brobot.debug.image.visual.show-search-regions` | boolean | `true` | Highlight search regions on screenshots |
| `brobot.debug.image.visual.show-match-scores` | boolean | `true` | Display similarity scores on matches |
| `brobot.debug.image.visual.show-failed-regions` | boolean | `true` | Mark regions where patterns were not found |
| `brobot.debug.image.visual.highlight-best-match` | boolean | `true` | Highlight the best scoring match |
| `brobot.debug.image.visual.create-heatmap` | boolean | `false` | Generate similarity heatmaps (experimental) |
| `brobot.debug.image.visual.create-comparison-grid` | boolean | `true` | Create pattern vs match comparison grids |

### Console Output

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.console.use-colors` | boolean | `true` | Enable ANSI colors in console output |
| `brobot.debug.image.console.show-box` | boolean | `true` | Show decorative boxes around headers |
| `brobot.debug.image.console.show-timestamp` | boolean | `true` | Include timestamps in console output |
| `brobot.debug.image.console.show-stack-trace` | boolean | `false` | Show full stack traces for errors |
| `brobot.debug.image.console.compact-mode` | boolean | `false` | Use compact output format |

### Logging Details

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.log.similarity-scores` | boolean | `true` | Log similarity scores for all matches |
| `brobot.debug.image.log.search-time` | boolean | `true` | Log search duration |
| `brobot.debug.image.log.pattern-details` | boolean | `true` | Log pattern dimensions and properties |
| `brobot.debug.image.log.dpi-info` | boolean | `true` | Log DPI and scaling information |
| `brobot.debug.image.log.search-path` | boolean | `false` | Log the complete search path |
| `brobot.debug.image.log.memory-usage` | boolean | `false` | Log memory usage statistics |

### Real-time Monitoring (Future Feature)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.debug.image.realtime.enabled` | boolean | `false` | Enable real-time web dashboard |
| `brobot.debug.image.realtime.port` | int | `8888` | Port for web dashboard |
| `brobot.debug.image.realtime.auto-open` | boolean | `false` | Auto-open dashboard in browser |

---

## Headless Detection

Properties for configuring headless mode detection. Brobot no longer auto-detects headless environments due to reliability issues. See the [Headless Configuration Guide](./headless-configuration.md) for detailed usage.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.core.headless` | boolean | `false` | Explicitly set headless mode (GUI unavailable) |

:::info Property Path
The headless property is accessed via `brobot.core.headless` in `BrobotProperties`, but `HeadlessDetector` also reads `brobot.headless` via `@Value` injection. Both paths reference the same configuration value. Use `brobot.headless` for simplicity in `application.properties`.
:::

**Important Notes:**
- Headless mode must be explicitly configured via `brobot.headless` property
- Auto-detection has been removed due to false positives on Windows systems (September 2025)
- Defaults to `false` (assumes GUI is available)
- For CI/CD environments, set `brobot.headless=true`
- This is separate from mock mode - headless means no display, mock means simulated actions

---

## Mock Mode

Properties for running Brobot in mock mode for testing without actual screen interaction. See the [Mock Mode Guide](../../04-testing/mock-mode-guide.md) for comprehensive usage.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.mock` | boolean | `false` | Enable mock mode (simulated actions) |
| `brobot.mock.action.successProbability` | double | `1.0` | Probability of action success (0.0-1.0) |
| `brobot.screenshot.path` | string | `screenshots/` | Path to mock screenshots |

**Mock vs Headless:**
- **Mock Mode**: Simulates actions for testing, can run with or without display
- **Headless Mode**: No display available, often used in CI/CD environments
- Both can be used together for CI/CD testing

---

## Automation Runner

Properties for the AutomationRunner failure handling system. See [Automation Runner Guide](../automation-runner.md).

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.automation.exit-on-failure` | boolean | `false` | Exit application on automation failure |
| `brobot.automation.max-retries` | int | `3` | Maximum retry attempts for failed operations |
| `brobot.automation.retry-delay-ms` | long | `1000` | Delay between retries in milliseconds |
| `brobot.automation.log-stack-traces` | boolean | `true` | Log full stack traces for debugging |

---

## Core Settings

Basic Brobot operation settings.

### Find Operations

:::info Similarity Configuration
Find similarity is typically configured per-pattern via `StateImage.Builder().setSimilarity()` rather than globally. The following properties are used for testing and DPI configuration:
:::

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.find.similarity.min` | double | `0.60` | Minimum similarity threshold (test profile) |
| `brobot.find.similarity.default` | double | `0.70` | Default similarity threshold (test profile) |
| `brobot.action.similarity` | double | `0.70` | DPI configuration similarity threshold |

### Action Timing

:::info Per-Action Timing Configuration
Action timing is configured per-action via `ActionConfig.Builder()` methods (`setPauseBeforeBegin()`, `setPauseAfterEnd()`), not globally via properties. The `brobot.action.pause-after` property exists in `brobot-test-defaults.properties` for test-specific timing but is not bound to `BrobotProperties`.
:::

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.action.pause-after` | int | `0` | Global pause after action in milliseconds (test profile only) |

**Recommended Approach**: Use per-action timing configuration:
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setPauseBeforeBegin(0.2)  // Pause before action (seconds)
    .setPauseAfterEnd(0.5)     // Pause after action (seconds)
    .build();
```

---

## Mouse Configuration

Mouse action timing and behavior configuration.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.mouse.move-delay` | float | `0.5` | Delay in seconds for mouse movement |
| `brobot.mouse.pause-before-down` | double | `0.0` | Pause before mouse down action (seconds) |
| `brobot.mouse.pause-after-down` | double | `0.0` | Pause after mouse down action (seconds) |
| `brobot.mouse.pause-before-up` | double | `0.0` | Pause before mouse up action (seconds) |
| `brobot.mouse.pause-after-up` | double | `0.0` | Pause after mouse up action (seconds) |
| `brobot.mouse.x-move-after-down` | int | `0` | X offset after mouse down (pixels) |
| `brobot.mouse.y-move-after-down` | int | `0` | Y offset after mouse down (pixels) |

:::tip Mouse Timing Configuration
These properties control global mouse behavior. For per-action timing, use `ActionConfig.Builder()` methods (`setPauseBeforeBegin()`, `setPauseAfterEnd()`). In test environments, these values are typically set to `0` for faster execution (see `brobot-test-defaults.properties`).
:::

**Common Use Cases:**
- **`move-delay`**: Smooths mouse movement for visual observation (set to `0` in tests)
- **`pause-before-down/up`**: Ensures UI elements are ready before/after mouse button actions
- **`x-move-after-down/y-move-after-down`**: Simulates drag gestures by moving mouse slightly after button press

---

## Logging

Brobot logging configuration. For comprehensive logging configuration, see the [Logging Configuration Guide](../../07-logging/configuration.md).

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.logging.verbosity` | enum | `NORMAL` | Verbosity level: QUIET, NORMAL, VERBOSE |
| `brobot.console.actions.enabled` | boolean | `true` | Enable action logging to console |
| `brobot.console.actions.level` | enum | `NORMAL` | Console action logging level |

### Spring Logging Levels

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logging.level.io.github.jspinak.brobot` | string | `INFO` | Brobot library log level |
| `logging.level.io.github.jspinak.brobot.debug` | string | `INFO` | Debug components log level |
| `logging.level.io.github.jspinak.brobot.action.basic.find` | string | `INFO` | Find operations log level |
| `logging.level.io.github.jspinak.brobot.capture` | string | `INFO` | Screen capture log level |

---

## Visual Feedback and Highlighting

Properties for visual highlighting of found patterns and UI elements. See the [Highlighting Feature Guide](../user-guides/highlighting-feature.md) for detailed usage.

### Aspect-Based Visual Feedback (Working)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.aspects.visual-feedback.enabled` | boolean | `false` | Enable VisualFeedbackAspect for automatic feedback |
| `brobot.aspects.visual-feedback.highlight-duration` | int | `2` | Highlight duration in seconds |
| `brobot.aspects.visual-feedback.highlight-color` | string | `YELLOW` | Highlight color (YELLOW, RED, GREEN, etc.) |
| `brobot.aspects.visual-feedback.show-action-flow` | boolean | `true` | Show action flow arrows between operations |
| `brobot.aspects.visual-feedback.show-confidence-scores` | boolean | `true` | Show confidence scores on matches |

### Console Action Reporting (Working)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.console.actions.enabled` | boolean | `true` | Enable console action reporting |
| `brobot.console.actions.level` | enum | `NORMAL` | Verbosity: QUIET, NORMAL, VERBOSE |
| `brobot.console.actions.show-match-details` | boolean | `true` | Show match location and scores (VERBOSE mode) |
| `brobot.console.actions.show-timing` | boolean | `true` | Show action timing information |
| `brobot.console.actions.use-colors` | boolean | `true` | Use ANSI colored output |
| `brobot.console.actions.use-icons` | boolean | `true` | Use unicode icons in output |
| `brobot.console.actions.report.find` | boolean | `true` | Report find operations |
| `brobot.console.actions.report.click` | boolean | `true` | Report click operations |
| `brobot.console.actions.report.type` | boolean | `true` | Report type operations |
| `brobot.console.actions.report.drag` | boolean | `true` | Report drag operations |
| `brobot.console.actions.report.highlight` | boolean | `false` | Report highlight operations |
| `brobot.console.actions.report-transitions` | boolean | `true` | Report state transitions |

### Planned Highlighting Features (Not Yet Implemented)

> **Note**: The following properties are defined in `brobot-visual-feedback.properties` but do not currently work because the configuration class to bind them has been removed. See the [Highlighting Feature Guide](../user-guides/highlighting-feature.md) for details.

| Property | Type | Default | Status |
|----------|------|---------|--------|
| `brobot.highlight.enabled` | boolean | `true` | ⚠️ Planned |
| `brobot.highlight.auto-highlight-finds` | boolean | `true` | ⚠️ Planned |
| `brobot.highlight.auto-highlight-search-regions` | boolean | `false` | ⚠️ Planned |
| `brobot.highlight.find.color` | string | `#00FF00` | ⚠️ Planned |
| `brobot.highlight.find.duration` | double | `2.0` | ⚠️ Planned |
| `brobot.highlight.find.border-width` | int | `3` | ⚠️ Planned |
| `brobot.highlight.find.flash` | boolean | `false` | ⚠️ Planned |
| `brobot.highlight.find.flash-count` | int | `2` | ⚠️ Planned |
| `brobot.highlight.find.flash-interval` | int | `300` | ⚠️ Planned |
| `brobot.highlight.search-region.color` | string | `#0000FF` | ⚠️ Planned |
| `brobot.highlight.error.enabled` | boolean | `false` | ⚠️ Planned |
| `brobot.highlight.click.enabled` | boolean | `true` | ⚠️ Planned |
| ... and 20+ other properties | - | - | ⚠️ Planned |

For the complete list of planned properties and implementation status, see [Highlighting Feature Guide](../user-guides/highlighting-feature.md#current-implementation-status).

---

## DPI Configuration

DPI (Dots Per Inch) configuration for optimal pattern matching across different screen resolutions and scaling factors. See the [DPI Resolution Guide](../capture/dpi-resolution-guide.md) for comprehensive guidance.

### DPI Control

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.dpi.disable` | boolean | `true` | Disable DPI awareness to force physical resolution capture (recommended) |
| `brobot.dpi.resize-factor` | double | `1.0` | Resize factor for pattern matching (1.0 = no scaling, recommended) |
| `brobot.dpi.pattern-source` | enum | `WINDOWS_TOOL` | Pattern source hint: SIKULI_IDE, WINDOWS_TOOL, FFMPEG_TOOL |

:::info Optimal Configuration for Pattern Matching
The default configuration (`dpi.disable=true`, `resize-factor=1.0`) ensures 100% similarity when matching patterns created with:
- Windows Snipping Tool
- SikuliX IDE
- FFmpeg capture tool

This forces physical resolution capture (e.g., 1920x1080) without scaling, matching the resolution of pattern images.
:::

**Pattern Source Hints:**
- **`WINDOWS_TOOL`**: Patterns created with Windows Snipping Tool (recommended)
- **`SIKULI_IDE`**: Patterns created with SikuliX IDE
- **`FFMPEG_TOOL`**: Patterns created with FFmpeg capture

**When to Change:**
- Set `dpi.disable=false` for legacy applications expecting logical resolution
- Adjust `resize-factor` if patterns are created at different scale (e.g., `0.5` for half-size patterns)

---

## Screen Capture

Configuration for screen capture strategies. See the [DPI Resolution Guide](../capture/dpi-resolution-guide.md) and [Modular Capture System](../capture/modular-capture-system.md) for detailed information.

### Main Capture Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.capture.provider` | enum | `JAVACV_FFMPEG` | Capture provider: JAVACV_FFMPEG, ROBOT, FFMPEG, SIKULIX, AUTO |
| `brobot.capture.prefer-physical` | boolean | `true` | Prefer physical resolution captures (recommended for pattern matching) |
| `brobot.capture.fallback-enabled` | boolean | `true` | Enable fallback to other providers if preferred fails |
| `brobot.capture.fallback-chain` | string | `JAVACV_FFMPEG,ROBOT,SIKULIX` | Fallback priority chain |
| `brobot.capture.enable-logging` | boolean | `false` | Enable capture operation logging for debugging |
| `brobot.capture.auto-retry` | boolean | `true` | Auto-retry failed captures |
| `brobot.capture.retry-count` | int | `3` | Number of retry attempts for failed captures |

### Robot Capture Settings

Configuration for Java Robot API capture (default provider with no external dependencies).

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.capture.robot.scale-to-physical` | boolean | `true` | Scale captures to physical resolution (compensates for DPI scaling) |
| `brobot.capture.robot.expected-physical-width` | int | `1920` | Expected physical screen width for scaling detection |
| `brobot.capture.robot.expected-physical-height` | int | `1080` | Expected physical screen height for scaling detection |

### FFmpeg Settings

Configuration for external FFmpeg capture (requires FFmpeg installation).

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.capture.ffmpeg.path` | string | `ffmpeg` | FFmpeg executable path (defaults to 'ffmpeg' in PATH) |
| `brobot.capture.ffmpeg.timeout` | int | `5` | FFmpeg capture timeout in seconds |
| `brobot.capture.ffmpeg.format` | string | `png` | FFmpeg output format (png recommended for quality) |
| `brobot.capture.ffmpeg.log-level` | enum | `error` | FFmpeg log level: error, warning, info, verbose, debug |

:::tip Capture Provider Recommendations
**For best pattern matching:**
1. **`JAVACV_FFMPEG`** (default) - JavaCV bundled FFmpeg, 100% match with Windows/SikuliX patterns
2. **`ROBOT`** - Java Robot API with physical resolution, 95%+ match, no external dependencies
3. **`FFMPEG`** - External FFmpeg (requires installation), 100% match
4. **`SIKULIX`** - Legacy SikuliX capture, ~77% match due to resolution scaling issues

The fallback chain ensures automatic failover if JavaCV is unavailable.
:::

---

## Monitor Configuration

Configuration for multi-monitor automation support.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.monitor.default-screen-index` | int | `-1` | Monitor to use (0=primary, 1=secondary, etc., -1=auto-detect) |
| `brobot.monitor.multi-monitor-enabled` | boolean | `false` | Enable multi-monitor support |
| `brobot.monitor.search-all-monitors` | boolean | `false` | Search across all monitors when finding elements |
| `brobot.monitor.log-monitor-info` | boolean | `true` | Log monitor information for each operation |

:::info Multi-Monitor Support
When `multi-monitor-enabled=true`, Brobot can:
- Target specific monitors for automation operations
- Search for patterns across all connected monitors
- Handle different resolutions per monitor

Use `default-screen-index` to specify which monitor to use by default. Set `search-all-monitors=true` to automatically search all monitors for patterns.
:::

**Monitor Index Values:**
- **`-1`**: Auto-detect (use primary monitor)
- **`0`**: Primary monitor
- **`1`**: Secondary monitor
- **`2`**: Tertiary monitor
- And so on...

---

## GUI Access

Settings for handling GUI availability.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.gui-access.continue-on-error` | boolean | `false` | Continue if GUI is not available |
| `brobot.gui-access.check-on-startup` | boolean | `true` | Check GUI availability at startup |

---

## Startup Configuration

Initial state and startup behavior. See the [Initial States Configuration Guide](./initial-states.md) for comprehensive usage.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.startup.verify-initial-states` | boolean | `false` | Verify initial state activation |
| `brobot.startup.fallback-search` | boolean | `false` | Search all states if initial states not found |
| `brobot.startup.activate-first-only` | boolean | `false` | Only activate first found state |
| `brobot.startup.startup-delay` | int | `0` | Startup delay in seconds |

:::warning Auto-Activation Property
The `brobot.startup.auto-activate` property is used in `InitialStateAutoConfiguration` with `matchIfMissing=true`, meaning auto-activation is **enabled by default** even when not explicitly set. This property is accessed via `@Value` annotation, not bound to `StartupConfiguration`.
:::

---

## Profile-Based Configuration

Spring Boot profiles allow you to organize configurations for different environments. Profiles can be combined for powerful configuration combinations. For comprehensive profile-based testing patterns, see the [Profile-Based Testing Guide](../../04-testing/profile-based-testing.md).

### Profile Strategy

**Standard Pattern**: Use a modular profile strategy to minimize duplication:

1. **Base Configuration** (`application.properties`) - Common settings for all profiles
2. **Feature Profiles** - Each profile adds specific functionality
3. **Profile Composition** - Combine multiple profiles for complex configurations

### Available Profiles

#### Debug Profile
Create `application-debug.properties` for image finding debugging:

```properties
# Image Find Debugging
brobot.debug.image.enabled=true
brobot.debug.image.level=DETAILED
brobot.debug.image.save-screenshots=true
brobot.debug.image.save-patterns=true
brobot.debug.image.save-comparisons=true
brobot.debug.image.output-dir=debug/image-finding

# Visual Debugging
brobot.debug.image.visual.enabled=true
brobot.debug.image.visual.show-search-regions=true
brobot.debug.image.visual.show-match-scores=true
brobot.debug.image.visual.highlight-best-match=true
brobot.debug.image.visual.create-comparison-grid=true

# Console Output (with ANSI colors for Windows via Jansi)
brobot.debug.image.console.use-colors=true
brobot.debug.image.console.show-box=true
brobot.debug.image.console.show-timestamp=true

# Enhanced Logging
brobot.logging.verbosity=VERBOSE
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE

# Slow down for observation
brobot.action.pause-after=500
brobot.action.pause-before=200

# Spring Boot Debug Logging
logging.level.io.github.jspinak.brobot.debug=DEBUG
logging.level.io.github.jspinak.brobot.action.basic.find=DEBUG
```

#### Mock Profile
Create `application-mock.properties` for testing without screen interaction:

```properties
# Enable Mock Mode (simulated actions)
brobot.mock=true
brobot.mock.action.success.probability=0.8

# Headless Configuration (if running in CI/CD without display)
brobot.headless=true

# GUI Access Settings for Mock Mode
brobot.gui-access.continue-on-error=true
brobot.gui-access.check-on-startup=false

# Automation Failure Handling
brobot.automation.exit-on-failure=false
brobot.automation.max-retries=2
brobot.automation.retry-delay-ms=500
```

### Profile Combinations

Profiles can be combined using comma separation:

| Command | Description | Use Case |
|---------|-------------|----------|
| `./gradlew bootRun` | Default (no profile) | Live automation on Windows/Linux with GUI |
| `./gradlew bootRun --args='--spring.profiles.active=debug'` | Debug profile | Live automation with comprehensive debugging |
| `./gradlew bootRun --args='--spring.profiles.active=mock'` | Mock profile | Testing without GUI/display |
| `./gradlew bootRun --args='--spring.profiles.active=mock,debug'` | Mock + Debug | Mock testing with debug output |

### Example Usage Scenarios

#### Windows Live Automation with Debugging
```bash
# For debugging image finding issues on Windows
./gradlew bootRun --args='--spring.profiles.active=debug'
```
This enables:
- Colorful console output (via Jansi library)
- Visual annotations on screenshots
- Debug file saving to `debug/image-finding/`
- Detailed similarity scoring logs

#### CI/CD Pipeline Testing
```bash
# For automated testing in headless environments
./gradlew test --args='--spring.profiles.active=mock'
```
This enables:
- Mock mode (no GUI required)
- Predictable test results
- Fast execution

#### Development with Full Debugging
```bash
# For development with both mock and debug features
./gradlew bootRun --args='--spring.profiles.active=mock,debug'
```
This enables:
- Mock mode for consistent testing
- Full debug output for troubleshooting
- No GUI dependencies

---

## Environment Variables

Properties can also be set via environment variables by converting to uppercase and replacing dots with underscores:

```bash
export BROBOT_DEBUG_IMAGE_ENABLED=true
export BROBOT_DEBUG_IMAGE_LEVEL=DETAILED
export BROBOT_FIND_SIMILARITY=0.7
```

---

## Best Practices

1. **Development**: Use debug profile with DETAILED level
2. **Testing**: Use mock mode with BASIC debugging
3. **Production**: Minimal logging, debugging OFF
4. **CI/CD**: Enable debugging on failure only
5. **Performance**: Higher debug levels may impact performance (typical debug overhead)

---

## Related Documentation

### Configuration & Setup
- [BrobotProperties Usage Guide](./brobot-properties-usage.md) - How to use properties in code
- [Auto-Configuration Guide](./auto-configuration.md) - Spring Boot auto-configuration
- [Headless Configuration Guide](./headless-configuration.md) - Configuring headless detection
- [Initial States Configuration](./initial-states.md) - Startup state configuration

### Testing & Debugging
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing without GUI
- [Profile-Based Testing](../../04-testing/profile-based-testing.md) - Using profiles for testing
- [Image Find Debugging Guide](../tools/image-find-debugging.md) - Comprehensive debugging system

### Features & Advanced
- [Automation Runner Guide](../automation-runner.md) - Failure handling and retry logic
- [DPI Resolution Guide](../capture/dpi-resolution-guide.md) - DPI and resolution configuration
- [Modular Capture System](../capture/modular-capture-system.md) - Screen capture providers and configuration
- [Highlighting Feature Guide](../user-guides/highlighting-feature.md) - Visual feedback configuration
- [Logging Configuration](../../07-logging/configuration.md) - Detailed logging setup
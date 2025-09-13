# Brobot Configuration Properties Reference

This document provides a comprehensive reference for all Brobot configuration properties that can be set in `application.properties` or `application.yml`.

## Table of Contents
- [Image Find Debugging](#image-find-debugging)
- [Mock Mode](#mock-mode)
- [Automation Runner](#automation-runner)
- [Core Settings](#core-settings)
- [Logging](#logging)
- [Screen Capture](#screen-capture)
- [GUI Access](#gui-access)
- [Startup Configuration](#startup-configuration)

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

## Mock Mode

Properties for running Brobot in mock mode for testing without GUI.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.mock` | boolean | `false` | Enable mock mode |
| `brobot.mock.action.success.probability` | double | `1.0` | Probability of action success (0.0-1.0) |
| `brobot.screenshot.path` | string | `images` | Path to mock screenshots |

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

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.settings.find.similarity` | double | `0.8` | Default similarity threshold (0.0-1.0) |
| `brobot.find.similarity` | double | `0.8` | Alternative property name |

### Wait Operations

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.settings.wait.timeout` | int | `10` | Default wait timeout in seconds |

### Action Timing

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.action.pause-before` | int | `0` | Pause before action in milliseconds |
| `brobot.action.pause-after` | int | `0` | Pause after action in milliseconds |

---

## Logging

Brobot logging configuration.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.logging.verbosity` | enum | `NORMAL` | Verbosity level: QUIET, NORMAL, VERBOSE |
| `brobot.console.actions.enabled` | boolean | `false` | Enable action logging to console |
| `brobot.console.actions.level` | enum | `NORMAL` | Console action logging level |

### Spring Logging Levels

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logging.level.io.github.jspinak.brobot` | string | `INFO` | Brobot library log level |
| `logging.level.io.github.jspinak.brobot.debug` | string | `INFO` | Debug components log level |
| `logging.level.io.github.jspinak.brobot.action.basic.find` | string | `INFO` | Find operations log level |
| `logging.level.io.github.jspinak.brobot.capture` | string | `INFO` | Screen capture log level |

---

## Screen Capture

Configuration for screen capture strategies.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.capture.strategy` | enum | `ADAPTIVE` | Capture strategy: NATIVE, PHYSICAL, ADAPTIVE, EXTERNAL |
| `brobot.capture.force-physical` | boolean | `false` | Force physical resolution capture |

---

## GUI Access

Settings for handling GUI availability.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.gui-access.continue-on-error` | boolean | `false` | Continue if GUI is not available |
| `brobot.gui-access.check-on-startup` | boolean | `true` | Check GUI availability at startup |

---

## Startup Configuration

Initial state and startup behavior.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `brobot.startup.auto-activate` | boolean | `false` | Auto-activate initial states |
| `brobot.startup.verify` | boolean | `false` | Verify startup conditions |
| `brobot.startup.verify-initial-states` | boolean | `false` | Verify initial state activation |

---

## Profile-Based Configuration

Spring Boot profiles allow you to organize configurations for different environments. Profiles can be combined for powerful configuration combinations.

### Profile Strategy

Brobot uses a modular profile approach to minimize duplication:

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
Create `application-mock.properties` for headless testing:

```properties
# Enable Mock Mode
brobot.mock=true
brobot.mock.action.success.probability=0.8

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
5. **Performance**: Higher debug levels impact performance (~5-30%)

---

## Related Documentation

- [Image Find Debugging Guide](../tools/image-find-debugging.md) - Comprehensive debugging system
- [Automation Runner Guide](../automation-runner.md) - Failure handling and retry logic
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing without GUI
- [DPI Resolution Guide](../capture/dpi-resolution-guide.md) - Screen capture configuration
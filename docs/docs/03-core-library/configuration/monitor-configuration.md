---
sidebar_position: 22
title: 'Multi-Monitor Configuration'
description: 'Configure Brobot for multi-monitor automation environments'
keywords: [monitor, multi-monitor, screen, display, configuration]
---

# Multi-Monitor Configuration Guide

Brobot supports multi-monitor environments, allowing you to specify which monitor to use for automation operations. This is particularly useful when you want to run your IDE on one monitor while performing GUI automation on another.

## Prerequisites

Before configuring multi-monitor support, ensure you have:

- **Multiple Physical Monitors**: Connected and recognized by your operating system
- **Spring Boot Knowledge**: Understanding of Spring Boot configuration and profiles
- **Brobot Basics**: Familiarity with [BrobotProperties](../../configuration/brobot-properties-usage.md) configuration system
- **Java Environment**: Running in a graphical environment (not headless) with display access

> **💡 Quick Check**: Run `GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length` in Java to verify monitor count.

## Overview

By default, Brobot uses the primary monitor for all operations. With multi-monitor support enabled, you can:

- Specify a default monitor for all operations
- Assign specific operations to specific monitors
- Enable logging to track which monitor is being used
- Search across all monitors or limit to specific ones

> **💡 Common Use Case**: Run your IDE or debugger on one monitor while automation executes on another, preventing interference and improving workflow efficiency.

## Configuration

### Basic Configuration

Add the following to your `application.yml` or create a profile-specific configuration file:

```yaml
brobot:
  monitor:
    # Enable multi-monitor support
    multi-monitor-enabled: true
    
    # Default monitor to use (0 = primary, 1 = secondary, etc.)
    default-screen-index: 1
    
    # Enable logging of monitor information
    log-monitor-info: true
```

### Advanced Configuration

For more control, you can assign specific operations to specific monitors:

```yaml
brobot:
  monitor:
    multi-monitor-enabled: true
    default-screen-index: 1
    log-monitor-info: true

    # Search across all monitors when finding elements
    search-all-monitors: false

    # Assign specific operations to specific monitors
    operation-monitor-map:
      find: 1            # Standard Brobot find operation
      click: 1           # Standard Brobot click operation
      type: 1            # Standard Brobot type operation
      drag: 1            # Standard Brobot drag operation
      screenshot: 1      # Custom operation name
```

### Operation Name Conventions

The `operation-monitor-map` accepts arbitrary string keys as operation names. These are **not** limited to `ActionInterface.Type` enum values. You can use:

1. **Lowercase ActionInterface.Type names**: `find`, `click`, `drag`, `type`, `move`, `vanish`
2. **Custom operation names**: `custom-operation`, `screenshot`, `my-special-action`

Your application code calls `monitorManager.getScreen("operation-name")` to retrieve the appropriate monitor for that operation.

**Example in action implementation:**
```java
import io.github.jspinak.brobot.monitor.MonitorManager;
import org.sikuli.script.Screen;

@Autowired
private MonitorManager monitorManager;

public void performFind() {
    Screen screen = monitorManager.getScreen("find");  // Uses monitor from operation-monitor-map
    // ... perform find operation on that screen
}
```

> **💡 Best Practice**: Common operation names like "find", "click", etc. typically align with Brobot's ActionInterface.Type enum values (lowercase), but you're free to define custom names for your specific needs.

## Using Configuration Profiles

Create a profile-specific configuration file `application-multimonitor.yml`:

```yaml
brobot:
  monitor:
    multi-monitor-enabled: true
    default-screen-index: 1
    log-monitor-info: true
```

Then activate it using one of these methods:

1. **Command line argument:**
   ```bash
   java -jar your-app.jar --spring.profiles.active=multimonitor
   ```

2. **Environment variable:**
   ```bash
   export SPRING_PROFILES_ACTIVE=multimonitor
   ```

3. **In application.yml:**
   ```yaml
   spring:
     profiles:
       active: multimonitor
   ```

## Monitor Indexing

Monitors are indexed starting from 0:

- Monitor 0: Primary monitor
- Monitor 1: First secondary monitor
- Monitor 2: Second secondary monitor
- etc.

> **⚠️ Important**: Monitor indices are assigned by your operating system and may change when monitors are connected/disconnected. Always verify monitor detection at startup using `log-monitor-info: true`.

### Example 3-Monitor Setup

```
[Monitor 0 - Left]   [Monitor 1 - Middle]   [Monitor 2 - Right]
   IDE/Development       GUI Automation          Other Apps
```

## Logging

When `log-monitor-info` is enabled, you'll see detailed information about monitor detection and usage:

### Startup Logs
```
INFO  MonitorManager - Found 3 screen device(s) - display mode confirmed
INFO  MonitorManager - Monitor 0: 1920x1080, Monitor 1: 1920x1080, Monitor 2: 1920x1080
```

### Operation Logs
```
INFO  BufferedImageUtilities - Using monitor 1 for find operation
INFO  MonitorManager - Assigned operation 'click' to monitor 1
```

## Programmatic Access

You can also interact with monitors programmatically:

```java
import io.github.jspinak.brobot.monitor.MonitorManager;
import io.github.jspinak.brobot.monitor.MonitorManager.MonitorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sikuli.script.Screen;
import java.awt.Point;
import java.util.List;

@Component
public class MonitorExample {

    @Autowired
    private MonitorManager monitorManager;

    public void demonstrateMonitorAccess() {
        // Get information about all monitors
        List<MonitorInfo> monitors = monitorManager.getAllMonitorInfo();
        for (MonitorInfo info : monitors) {
            System.out.printf("Monitor %d: %dx%d at (%d, %d)%n",
                info.getIndex(), info.getWidth(), info.getHeight(),
                info.getX(), info.getY());
        }

        // Set a specific monitor for an operation
        monitorManager.setOperationMonitor("custom-operation", 2);

        // Get the monitor containing a specific point
        int monitorIndex = monitorManager.getMonitorAtPoint(new Point(2000, 500));
        System.out.println("Point is on monitor: " + monitorIndex);

        // Get Screen object for specific monitor
        Screen screen = monitorManager.getScreen(1);
        // Use screen for automation operations
    }
}
```

## Use Cases

### Development Setup

Run your IDE on the left monitor while automation happens on the middle monitor:

```yaml
brobot:
  monitor:
    default-screen-index: 1  # Middle monitor
    operation-monitor-map:
      find: 1
      click: 1
```

### Testing Across Monitors

Search for elements across all monitors:

```yaml
brobot:
  monitor:
    search-all-monitors: true
    log-monitor-info: true
```

### Monitor-Specific Operations

Different operations on different monitors:

```yaml
brobot:
  monitor:
    operation-monitor-map:
      find: 0        # Search on primary
      click: 1       # Click on secondary
      screenshot: 2  # Capture from third monitor
```

## Troubleshooting

### Common Issues

1. **Monitor not detected:**
   - Ensure the monitor is connected and recognized by the OS
   - Check startup logs for monitor detection information
   - Verify display settings in your operating system

   > **💡 Tip**: Run `monitorManager.getMonitorCount()` programmatically to verify detection.

2. **Operations happening on wrong monitor:**
   - Verify the monitor index in configuration
   - Enable `log-monitor-info` to see which monitor is being used
   - Check that operation names in `operation-monitor-map` match your code

   > **⚠️ Common Mistake**: Using "FIND" (uppercase) instead of "find" (lowercase) in operation-monitor-map.

3. **Configuration not taking effect:**
   - Ensure the configuration profile is active
   - Check that `multi-monitor-enabled` is set to `true`
   - Verify no typos in property names (kebab-case required)

   > **💡 Tip**: Enable debug logging with `logging.level.io.github.jspinak.brobot.monitor=DEBUG` to see configuration loading.

### Debugging

Enable debug logging for more detailed information:

```yaml
logging:
  level:
    io.github.jspinak.brobot.monitor: DEBUG
```

### Headless Environment Handling

> **⚠️ Headless Mode**: Multi-monitor configuration requires a graphical environment. In headless mode or CI/CD environments, Brobot automatically falls back to a default virtual monitor. Multi-monitor configuration will be ignored.

**Headless Detection**:
```java
if (GraphicsEnvironment.isHeadless()) {
    // Multi-monitor features unavailable
    // Brobot uses default monitor/screen
}
```

### Monitor Hot-Plugging

> **⚠️ Monitor Changes**: If monitors are connected or disconnected after application startup, monitor indices may change. Brobot caches monitor configuration at startup and does not automatically detect runtime changes.

**Best Practice**: Restart your application after connecting/disconnecting monitors to ensure correct monitor detection.

## API Reference

### Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `multi-monitor-enabled` | boolean | false | Enable multi-monitor support |
| `default-screen-index` | int | -1 | Default monitor index (-1 uses primary) |
| `search-all-monitors` | boolean | false | Search across all monitors (not fully implemented) |
| `log-monitor-info` | boolean | true | Log monitor information |
| `operation-monitor-map` | Map&lt;String, Integer&gt; | empty | Map operations to specific monitors |

> **⚠️ Index Validation**: If `default-screen-index` specifies an invalid monitor index (e.g., monitor 3 when only 2 monitors exist), Brobot will fall back to the primary monitor (index 0) and log a warning.

### MonitorManager Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getScreen(int monitorIndex)` | `Screen` | Get Screen for specific monitor |
| `getScreen(String operationName)` | `Screen` | Get Screen based on operation mapping |
| `getAllScreens()` | `List<Screen>` | Get all available screens |
| `getAllMonitorInfo()` | `List<MonitorInfo>` | Get information about all monitors |
| `getMonitorInfo(int index)` | `MonitorInfo` | Get information about a specific monitor |
| `getMonitorAtPoint(Point point)` | `int` | Get monitor index containing a point |
| `setOperationMonitor(String operation, int monitor)` | `void` | Assign operation to monitor |
| `getMonitorCount()` | `int` | Get total number of monitors |
| `getPrimaryMonitorIndex()` | `int` | Get index of primary monitor |
| `isValidMonitorIndex(int index)` | `boolean` | Check if monitor index is valid |

## Advanced Features

### MultiMonitorRoutingAspect

For advanced scenarios, Brobot provides an AspectJ-based routing aspect that can intercept and route operations to specific monitors:

```yaml
brobot:
  aspects:
    multi-monitor:
      enabled: true  # Enable aspect-based routing (opt-in)
  monitor:
    multi-monitor-enabled: true
    operation-monitor-map:
      find: 1
      click: 2
```

This aspect is **disabled by default** and requires explicit enablement. For details, see the [AspectJ Usage Guide](../../advanced/aspectj-usage-guide.md).

### Search All Monitors Status

The `search-all-monitors` property exists and infrastructure is in place to search across multiple monitors. However, integration with the FindPipeline is currently in development. The property can be configured but may not affect find operations in the current version.

> **⚠️ Implementation Status**: While you can set `search-all-monitors: true`, this feature is not fully integrated. For reliable multi-monitor find operations, use explicit monitor selection via `operation-monitor-map` or programmatic `getScreen()` calls.

**Current Workaround**:
```java
// Search across specific monitors manually
for (int i = 0; i < monitorManager.getMonitorCount(); i++) {
    Screen screen = monitorManager.getScreen(i);
    // Perform find operation on this screen
}
```

## Related Documentation

### Configuration
- **[Properties Reference](../../configuration/properties-reference.md)** - Complete Brobot properties reference
- **[BrobotProperties Usage](../../configuration/brobot-properties-usage.md)** - Using properties in code
- **[Auto-Configuration](../../configuration/auto-configuration.md)** - Spring Boot auto-configuration

### Screen and Region
- **[Screen-Adaptive Regions](../user-guides/screen-adaptive-regions.md)** - Region positioning across screens
- **[DPI Resolution Guide](../../capture/dpi-resolution-guide.md)** - Handling DPI scaling in multi-monitor setups

### Testing
- **[Profile-Based Testing](../../../04-testing/profile-based-testing.md)** - Using Spring profiles in tests
- **[Testing Configuration](../../../04-testing/testing-intro.md)** - Configuration for testing environments

### Advanced
- **[AspectJ Usage Guide](../../advanced/aspectj-usage-guide.md)** - MultiMonitorRoutingAspect details
- **[Modular Capture System](../../capture/modular-capture-system.md)** - Screen capture with multiple monitors

### Examples
- **[Multi-Monitor Config Example](./examples/multimonitor-config.yml)** - Complete configuration file example

## Summary

Multi-monitor support in Brobot enables:
- **Automatic monitor detection** and management
- **Operation-specific monitor assignment** via configuration
- **Programmatic monitor control** through MonitorManager API
- **Flexible configuration** using Spring Boot properties and profiles
- **Cross-platform support** for Windows, Mac, and Linux

The feature is production-ready and used in automation scenarios where operations need to target specific displays.
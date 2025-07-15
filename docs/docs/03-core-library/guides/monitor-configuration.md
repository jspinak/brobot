# Multi-Monitor Configuration Guide

Brobot supports multi-monitor environments, allowing you to specify which monitor to use for automation operations. This is particularly useful when you want to run your IDE on one monitor while performing GUI automation on another.

## Overview

By default, Brobot uses the primary monitor for all operations. With multi-monitor support enabled, you can:

- Specify a default monitor for all operations
- Assign specific operations to specific monitors
- Enable logging to track which monitor is being used
- Search across all monitors or limit to specific ones

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
      find: 1
      find-multiple: 1
      click: 1
      type: 1
      drag: 1
      screenshot: 1
```

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

### Example 3-Monitor Setup

```
[Monitor 0 - Left]   [Monitor 1 - Middle]   [Monitor 2 - Right]
   IDE/Development       GUI Automation          Other Apps
```

## Logging

When `log-monitor-info` is enabled, you'll see detailed information about monitor detection and usage:

### Startup Logs
```
INFO  MonitorManager - Detected 3 monitor(s)
INFO  MonitorManager - Monitor 0: :0.0 - Bounds: x=0, y=0, width=1920, height=1080
INFO  MonitorManager - Monitor 1: :0.1 - Bounds: x=1920, y=0, width=1920, height=1080
INFO  MonitorManager - Monitor 2: :0.2 - Bounds: x=3840, y=0, width=1920, height=1080
```

### Operation Logs
```
INFO  BufferedImageUtilities - Using monitor 1 for find operation
INFO  MonitorManager - Operation 'click' assigned to monitor 1
```

## Programmatic Access

You can also interact with monitors programmatically:

```java
@Autowired
private MonitorManager monitorManager;

// Get information about all monitors
List<MonitorInfo> monitors = monitorManager.getAllMonitorInfo();

// Set a specific monitor for an operation
monitorManager.setOperationMonitor("custom-operation", 2);

// Get the monitor containing a specific point
int monitorIndex = monitorManager.getMonitorAtPoint(new Point(2000, 500));
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

2. **Operations happening on wrong monitor:**
   - Verify the monitor index in configuration
   - Enable `log-monitor-info` to see which monitor is being used

3. **Configuration not taking effect:**
   - Ensure the configuration profile is active
   - Check that `multi-monitor-enabled` is set to `true`

### Debugging

Enable debug logging for more detailed information:

```yaml
logging:
  level:
    io.github.jspinak.brobot.monitor: DEBUG
```

## API Reference

### Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `multi-monitor-enabled` | boolean | false | Enable multi-monitor support |
| `default-screen-index` | int | -1 | Default monitor index (-1 uses primary) |
| `search-all-monitors` | boolean | false | Search across all monitors |
| `log-monitor-info` | boolean | true | Log monitor information |
| `operation-monitor-map` | Map&lt;String, Integer&gt; | empty | Map operations to specific monitors |

### MonitorManager Methods

- `getScreen(int monitorIndex)`: Get Screen for specific monitor
- `getScreen(String operationName)`: Get Screen based on operation mapping
- `getAllScreens()`: Get all available screens
- `getMonitorInfo(int index)`: Get information about a specific monitor
- `setOperationMonitor(String operation, int monitor)`: Assign operation to monitor
---
sidebar_position: 7
---

# Action Logging with Console Output and Visual Feedback

The enhanced action logging system provides real-time console output and visual highlighting to improve the development and debugging experience. This guide covers the console reporting and visual feedback features added to Brobot's action logging.

## Overview

When developing automation scripts, it's crucial to understand what Brobot is doing at each step. The enhanced action logging provides:

- **Real-time console output** showing action execution with visual indicators
- **Visual highlighting** on screen to show what's being found and clicked
- **GUI access detection** to quickly identify environment issues
- **Performance warnings** for slow operations
- **Configurable verbosity** for different environments

## Console Action Reporting

### Features

The console reporter provides formatted output with:
- Visual icons for different action types (🔍 for FIND, 👆 for CLICK, etc.)
- Success/failure indicators (✓/✗)
- Timing information for each action
- Match details including location and confidence scores
- Performance warnings for slow operations

### Example Output

```
🔍 FIND: login-button → ✓ FOUND (234ms)
   └─ Location: (450,320) Score: 98.5%

👆 CLICK: login-button (156ms) ✓

⌨️ TYPE: "user@example.com" ✓

✗ FIND submit-button (2003ms)
   └─ Search regions: 3 areas checked
   └─ Similar matches: button-disabled (85.2%)

⚠️ Performance Warning: FIND took 2003ms (threshold: 1000ms)

🔄 STATE: LoginPage → Dashboard [425ms] [SUCCESS]
```

### Configuration

Configure console output through properties:

```yaml
brobot:
  console:
    actions:
      # Enable/disable console reporting
      enabled: true
      
      # Verbosity level
      level: NORMAL  # Options: QUIET, NORMAL, VERBOSE
      
      # Show match details in VERBOSE mode
      show-match-details: true
      
      # Display timing information
      show-timing: true
      
      # Use colored output (requires ANSI support)
      use-colors: true
      
      # Use unicode icons
      use-icons: true
      
      # Filter which actions to report
      report:
        find: true
        click: true
        type: true
        drag: true
        highlight: false
      
      # Report state transitions
      report-transitions: true
      
      # Performance thresholds (milliseconds)
      performance-warn-threshold: 1000
      performance-error-threshold: 5000
```

### Verbosity Levels

- **QUIET**: Minimal output - only errors and failures
- **NORMAL**: Standard output - actions with success/failure
- **VERBOSE**: Detailed output - includes match details, timing, and metadata

## Visual Highlighting

Visual feedback helps you see exactly what Brobot is doing on screen.

### Highlight Types

1. **Find Highlights** (Green by default)
   - Shows successful pattern matches
   - Displays match confidence score (optional)
   - Can flash to draw attention

2. **Search Region Highlights** (Blue by default)
   - Shows areas being searched
   - Displays region dimensions (optional)
   - Helps understand search scope

3. **Click Highlights** (Yellow by default)
   - Shows click locations
   - Optional ripple effect
   - Helps verify click accuracy

4. **Error Highlights** (Red by default)
   - Shows where searches failed
   - Optional cross mark overlay
   - Helps identify problem areas

### Configuration

```yaml
brobot:
  highlight:
    # Global enable/disable
    enabled: true
    
    # Automatically highlight successful finds
    auto-highlight-finds: true
    
    # Automatically highlight search regions
    auto-highlight-search-regions: false
    
    # Find highlighting configuration
    find:
      color: "#00FF00"  # Green
      duration: 2.0      # seconds
      border-width: 3    # pixels
      flash: false       # Enable flashing
      flash-count: 2     # Number of flashes
      flash-interval: 300 # milliseconds
    
    # Search region highlighting
    search-region:
      color: "#0000FF"   # Blue
      duration: 1.0
      border-width: 2
      opacity: 0.3       # For filled regions
      filled: false      # Fill region or just border
      show-dimensions: false
    
    # Error highlighting
    error:
      enabled: false
      color: "#FF0000"   # Red
      duration: 3.0
      show-cross-mark: true
    
    # Click highlighting
    click:
      enabled: true
      color: "#FFFF00"   # Yellow
      duration: 0.5
      radius: 20         # pixels
      ripple-effect: true
```

## GUI Access Detection

The GUI access monitor automatically detects and reports environment issues that could prevent automation from working.

### Common Issues Detected

- Headless environment (no display)
- Missing DISPLAY variable on Linux
- X11 server not accessible
- Wayland compatibility issues
- Remote desktop limitations
- Low screen resolution

### Example Output

```
❌ GUI Problem: No DISPLAY environment variable set
💡 Possible solutions:
   • Set DISPLAY=:0 for local display
   • For SSH: use -X or -Y flag for X11 forwarding
   • For Docker: pass --env DISPLAY=$DISPLAY
   • For WSL: install and configure X server (VcXsrv, Xming)

❌ GUI Problem: Running in Remote Desktop session
💡 Possible solutions:
   • Some screen capture features may be limited
   • Consider running directly on the machine
   • Use alternative remote access tools if needed
```

### Configuration

```yaml
brobot:
  gui-access:
    # Report problems to console
    report-problems: true
    
    # Show detailed error information
    verbose-errors: true
    
    # Suggest solutions for problems
    suggest-solutions: true
    
    # Check on application startup
    check-on-startup: true
    
    # Continue despite GUI problems
    continue-on-error: false
    
    # Minimum screen resolution
    min-screen-width: 800
    min-screen-height: 600
```

## Usage Examples

### Basic Usage with Default Settings

```java
@Autowired
private ActionLogger actionLogger;

// Actions are automatically logged to console with default settings
ActionResult result = action.perform(findOptions, targetImage);
actionLogger.logAction("FIND", targetImage, result);
```

### Using Enhanced Action Logger

```java
@Autowired
private EnhancedActionLogger logger;

// Log with visual feedback
logger.logActionWithVisuals(
    "FIND", 
    target, 
    result,
    VisualFeedbackOptions.defaults()
);

// Check GUI access at startup
if (!logger.checkAndLogGuiAccess()) {
    // Handle GUI access problems
}
```

### Custom Visual Feedback

```java
// Create custom visual options
VisualFeedbackOptions options = VisualFeedbackOptions.builder()
    .highlightFinds(true)
    .highlightSearchRegions(true)
    .findHighlightColor(Color.YELLOW)
    .findHighlightDuration(5.0)
    .flashHighlight(true)
    .flashCount(3)
    .showMatchScore(true)
    .highlightLabel("Target Found!")
    .build();

logger.logActionWithVisuals("CLICK", button, result, options);
```

### Debugging Complex Searches

```java
// Log search start with regions
logger.logSearchStart(targetImage, searchRegion1, searchRegion2);

// Perform search
ActionResult result = finder.find(targetImage);

// Log search completion with timing
logger.logSearchComplete(targetImage, result, timer.stop());
```

## Profile Configurations

### Development Profile

Maximum visibility for debugging:

```yaml
# application-dev.yml
brobot:
  console:
    actions:
      enabled: true
      level: VERBOSE
      show-match-details: true
  highlight:
    enabled: true
    auto-highlight-finds: true
    auto-highlight-search-regions: true
    error:
      enabled: true
```

### CI/CD Profile

Minimal output for automated tests:

```yaml
# application-ci.yml
brobot:
  console:
    actions:
      enabled: true
      level: QUIET
  highlight:
    enabled: false
  gui-access:
    continue-on-error: true
```

### Production Profile

Disabled for production use:

```yaml
# application-prod.yml
brobot:
  console:
    actions:
      enabled: false
  highlight:
    enabled: false
  gui-access:
    report-problems: true
    suggest-solutions: false
```

## Visual Feedback Presets

The library provides convenient presets for common scenarios:

```java
// Maximum debugging visibility
VisualFeedbackOptions.debug()

// No visual feedback (production)
VisualFeedbackOptions.none()

// Only highlight successful finds
VisualFeedbackOptions.findsOnly()

// Default settings from configuration
VisualFeedbackOptions.defaults()
```

## Performance Considerations

1. **Console Output**: Minimal overhead (~1ms per log)
2. **Visual Highlighting**: Asynchronous, non-blocking
3. **GUI Checks**: Only performed on startup by default
4. **Conditional Loading**: Components only created when enabled

## Troubleshooting

### No Console Output

1. Check if console actions are enabled:
   ```yaml
   brobot.console.actions.enabled: true
   ```

2. Verify the verbosity level:
   ```yaml
   brobot.console.actions.level: NORMAL
   ```

3. Check if specific action types are filtered:
   ```yaml
   brobot.console.actions.report.find: true
   ```

### Visual Highlighting Not Working

1. Ensure highlighting is enabled globally:
   ```yaml
   brobot.highlight.enabled: true
   ```

2. Check if running in mock mode (highlights are logged but not shown)

3. Verify GUI access:
   ```java
   logger.checkAndLogGuiAccess();
   ```

### Icons Not Displaying

Some terminals don't support unicode. Disable icons:
```yaml
brobot.console.actions.use-icons: false
```

## Integration with Desktop Runner

The desktop runner automatically integrates console output through the `ConsoleActionEventListener`. No additional configuration is needed - console output appears in both the terminal and the runner's log viewer.

## Best Practices

1. **Use profiles** to configure different environments appropriately
2. **Enable verbose logging** during development, disable in production
3. **Check GUI access** early to catch environment issues
4. **Use visual feedback** sparingly in automated tests to avoid delays
5. **Configure performance thresholds** based on your application's needs
6. **Filter action types** to reduce noise in logs

## API Reference

- `EnhancedActionLogger` - Extended logging interface
- `VisualFeedbackOptions` - Visual feedback configuration
- `ConsoleActionConfig` - Console output settings
- `GuiAccessMonitor` - GUI environment checking

## Next Steps

- Try the visual-debug profile: `--spring.profiles.active=visual-debug`
- Customize colors and durations for your needs
- Create custom visual feedback options for specific scenarios
- Configure performance thresholds based on your SLAs
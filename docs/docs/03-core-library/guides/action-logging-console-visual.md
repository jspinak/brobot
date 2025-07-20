---
sidebar_position: 7
---

# Enhanced Action Logging with Console Output and Visual Feedback

## Overview

The Brobot framework includes enhanced action logging that provides real-time console output and visual highlighting during automation execution. This makes debugging and development significantly easier by showing exactly what Brobot is doing at each step.

## Key Features

### 1. Console Action Reporting
- Real-time feedback with visual indicators (🔍, ✓, ✗, ⚠️)
- Configurable verbosity levels (QUIET, NORMAL, VERBOSE)
- Performance warnings for slow operations
- Detailed match information including location and confidence scores

### 2. Visual Highlighting
- **Green borders** for successful pattern matches
- **Blue borders** for search regions
- **Yellow ripple effects** for clicks
- **Red indicators** for errors
- All colors and durations are configurable

### 3. GUI Access Detection
- Automatic detection of environment issues
- Platform-specific solutions (Linux/X11, Windows, macOS)
- Clear, actionable advice for fixing problems

## Quick Start

### Basic Usage

The enhanced logging is automatically enabled. Just run your Brobot application and you'll see:

```
🔍 FIND: login-button → ✓ FOUND (234ms)
   └─ Location: (450,320) Score: 98.5%

✓ CLICK login-button (156ms)

✗ FIND submit-button (2003ms)
   └─ Search regions: 3 areas checked

⚠️ Performance Warning: FIND took 2003ms (threshold: 1000ms)
```

### Configuration

Add to your `application.yml`:

```yaml
brobot:
  # Console output settings
  console:
    actions:
      enabled: true
      level: NORMAL  # QUIET, NORMAL, VERBOSE
      show-match-details: true
      use-icons: true
  
  # Visual highlighting
  highlight:
    enabled: true
    auto-highlight-finds: true
    find:
      color: "#00FF00"
      duration: 2.0
```

### Using Visual Debug Profile

For maximum visibility during development:

```bash
java -jar your-app.jar --spring.profiles.active=visual-debug
```

## Common Use Cases

### 1. Debugging Failed Finds

With VERBOSE logging, you'll see exactly where Brobot searched:

```
✗ FIND submit-button (2003ms)
   └─ Search regions: 3 areas checked
   └─ Region 1: (0,0 800x600)
   └─ Region 2: (800,0 800x600)
   └─ Similar matches: button-disabled (85.2%)
```

### 2. Performance Optimization

Identify slow operations immediately:

```
⚠️ Performance Warning: FIND took 2003ms (threshold: 1000ms)
```

### 3. GUI Environment Issues

Get immediate feedback about environment problems:

```
❌ GUI Problem: No DISPLAY environment variable set
💡 Possible solutions:
   • Set DISPLAY=:0 for local display
   • For SSH: use -X or -Y flag for X11 forwarding
   • For Docker: pass --env DISPLAY=$DISPLAY
   • For WSL: install and configure X server (VcXsrv, Xming)
```

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

## Advanced Features

### Custom Visual Feedback

```java
@Autowired
private EnhancedActionLogger logger;

// Create custom visual options
VisualFeedbackOptions options = VisualFeedbackOptions.builder()
    .highlightFinds(true)
    .findHighlightColor(Color.YELLOW)
    .findHighlightDuration(5.0)
    .flashHighlight(true)
    .showMatchScore(true)
    .build();

logger.logActionWithVisuals("FIND", target, result, options);
```

### Profile-Based Configuration

#### Development Profile
```yaml
# Maximum visibility
brobot:
  console.actions.level: VERBOSE
  highlight.auto-highlight-finds: true
  highlight.auto-highlight-search-regions: true
```

#### CI/CD Profile
```yaml
# Minimal output
brobot:
  console.actions.level: QUIET
  highlight.enabled: false
  gui-access.continue-on-error: true
```

#### Production Profile
```yaml
# Disabled
brobot:
  console.actions.enabled: false
  highlight.enabled: false
```

## Troubleshooting

### No Console Output
1. Check if enabled: `brobot.console.actions.enabled: true`
2. Check verbosity: `brobot.console.actions.level: NORMAL`
3. Check action filters: `brobot.console.actions.report.find: true`

### Icons Not Showing
Some terminals don't support unicode. Disable icons:
```yaml
brobot.console.actions.use-icons: false
```

### Visual Highlighting Not Working
1. Check if enabled: `brobot.highlight.enabled: true`
2. Verify GUI access: Run the GUI access check
3. Check if in mock mode (highlights are logged but not shown visually)

## Example Application

See the complete example in `/examples/src/main/java/io/github/jspinak/brobot/examples/logging/EnhancedLoggingDemo.java`

Run it with:
```bash
cd examples
./gradlew bootRun
```

Or with visual debug profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=visual-debug'
```

## Performance Impact

- Console output: ~1ms overhead per action
- Visual highlighting: Asynchronous, non-blocking
- GUI access check: Only on startup (configurable)
- Overall impact: < 2% in typical usage

## Migration from Old Logging

The new system is backward compatible. Existing code continues to work, but you can enhance it:

```java
// Old way
actionLogger.logAction("CLICK", button, result);

// Enhanced way with visual feedback
enhancedLogger.logActionWithVisuals("CLICK", button, result, 
    VisualFeedbackOptions.debug());
```

## Configuration Reference

See `/library/src/main/resources/brobot-visual-feedback.properties` for all available settings with documentation.

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

1. Enable enhanced logging in your application
2. Try the visual-debug profile during development
3. Customize colors and durations for your needs
4. Use GUI access detection to catch environment issues early
5. Monitor performance with automatic warnings
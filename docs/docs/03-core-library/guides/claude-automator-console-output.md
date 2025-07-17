# Claude Automator Expected Console Output

## Overview
This document outlines the expected console output for the Claude Automator application on both Windows and Mac platforms. The application uses Spring Boot with custom automation monitoring for Claude AI interface.

## Log Symbols and Platform Differences

The Brobot logging system uses different symbols based on the platform and terminal capabilities:

### Symbol Sets

| Symbol Type | Unicode (Mac/Linux/Windows Terminal) | ASCII Fallback (Legacy Windows) |
|------------|-------------------------------------|----------------------------------|
| Success | ✓ (Check mark) | [OK] |
| Failure | ✗ (X mark) | [FAIL] |
| Action | ▶ (Play symbol) | > |
| Transition | → (Arrow) | -> |
| Warning | ⚠ (Warning triangle) | [WARN] |
| Error | ⚠ (Warning triangle) | [ERROR] |
| Info | ℹ (Info symbol) | [INFO] |
| Debug | • (Bullet point) | * |

### Platform Detection

The system automatically detects the terminal capabilities:

1. **Windows Terminal Detection**:
   - Checks for `WT_SESSION` environment variable
   - Windows Terminal supports full Unicode and ANSI colors

2. **ANSI Color Support**:
   - Mac/Linux: Always supported
   - Windows: Checks for Windows Terminal, ConEmu, or ANSICON
   - Can be forced with `-Dbrobot.console.ansi=true`

3. **Unicode Support**:
   - Follows ANSI color support detection
   - When ANSI is supported, Unicode symbols are used
   - Otherwise, ASCII fallbacks are used

### Color Coding

When ANSI colors are supported:
- **Green**: Success operations, passed tests (✓)
- **Red**: Errors, failures (✗)
- **Blue**: Actions (▶)
- **Purple**: State transitions (→)
- **Yellow**: Warnings (⚠)
- **Cyan**: Informational messages (ℹ)
- **Dim**: Timestamps and debug info

## Common Log Patterns

### Application Startup Sequence
1. **Initial Configuration**
   - Display capability check
   - Headless mode configuration
   - Spring Boot banner
   
2. **Monitor Detection**
   - Lists all available monitors with dimensions
   - Identifies primary monitor

3. **State Initialization**
   - WorkingState creation with image loading
   - PromptState creation with image loading
   - Post-initialization configuration

4. **Framework Setup**
   - Brobot framework initialization
   - SikuliX configuration
   - Image path validation

5. **Annotation Processing**
   - State bean discovery
   - State registration
   - Transition mapping

6. **Active State Verification**
   - Screen scanning for Claude UI elements
   - State detection results

7. **Monitoring Loop**
   - Continuous monitoring messages
   - State change detection

## Windows Expected Output

```
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Starting Claude Automator Application...
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Display capability check: SUCCESS - Screen access is available
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Windows environment detected - Setting java.awt.headless=false
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Headless mode configuration: os.name=windows 10, WSL=false, DISPLAY=null, profile=null, canUseDisplay=true, java.awt.headless=false

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

HH:MM:SS INFO  Starting ClaudeAutomatorApplication using Java 21 with PID XXXXX (C:\path\to\claude-automator\build\classes\java\main started by USERNAME in C:\path\to\claude-automator)
HH:MM:SS INFO  No active profile set, falling back to 1 default profile: "default"
HH:MM:SS INFO  Detected N monitor(s)
HH:MM:SS INFO  Monitor 0: \Display0 - Bounds: x=XXXX, y=XXX, width=XXXX, height=XXXX
[Additional monitors listed...]
HH:MM:SS INFO  Primary monitor detected using default screen device: Monitor X

[State Creation]
HH:MM:SS INFO  Creating WorkingState
HH:MM:SS INFO  Extracting images from JAR to: C:\Users\USERNAME\AppData\Local\Temp\brobot-images-XXXXXXXXXXXXX
HH:MM:SS INFO  Extracted N images from JAR
HH:MM:SS INFO  Image 'working/claude-icon-1' loaded successfully
[Additional image loading messages...]
HH:MM:SS INFO  WorkingState created successfully

[Framework Initialization]
HH:MM:SS INFO  Initializing FrameworkSettings from BrobotProperties
HH:MM:SS INFO  ExecutionEnvironment initialized: ExecutionEnvironment[mockMode=false, hasDisplay=true, canCaptureScreen=true, useRealFiles=true]
HH:MM:SS INFO  SikuliX internal logging disabled - all logs will come through Brobot

[State Verification - Windows Terminal]
HH:MM:SS INFO  Starting active state verification
HH:MM:SS INFO  ▶ Searching for Working state: [SUCCESS/FAILED] ✓/✗
HH:MM:SS INFO  ▶ Searching for Prompt state: [SUCCESS/FAILED] ✓/✗

[State Verification - Legacy Windows Console]
HH:MM:SS INFO  Starting active state verification
HH:MM:SS INFO  > Searching for Working state: [SUCCESS/FAILED] [OK]/[FAIL]
HH:MM:SS INFO  > Searching for Prompt state: [SUCCESS/FAILED] [OK]/[FAIL]

[Monitoring]
HH:MM:SS INFO  Claude Automator is running. Press Ctrl+C to stop.
HH:MM:SS INFO  Application ready and monitoring started
[Continuous monitoring messages every few seconds]
```

## Mac Expected Output

```
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Starting Claude Automator Application...
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Display capability check: SUCCESS - Screen access is available
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- macOS environment detected - Configuring for Mac display
HH:MM:SS.mmm [main] INFO com.claude.automator.ClaudeAutomatorApplication -- Headless mode configuration: os.name=mac os x, WSL=false, DISPLAY=:0, profile=null, canUseDisplay=true, java.awt.headless=false

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

HH:MM:SS INFO  Starting ClaudeAutomatorApplication using Java 21 with PID XXXXX (/path/to/claude-automator/build/classes/java/main started by USERNAME in /path/to/claude-automator)
HH:MM:SS INFO  No active profile set, falling back to 1 default profile: "default"
HH:MM:SS INFO  Detected N monitor(s)
HH:MM:SS INFO  Monitor 0: Built-in Retina Display - Bounds: x=0, y=0, width=XXXX, height=XXXX
[Additional monitors if connected...]
HH:MM:SS INFO  Primary monitor detected using default screen device: Monitor 0

[State Creation - Mac specific paths]
HH:MM:SS INFO  Creating WorkingState
HH:MM:SS INFO  Extracting images from JAR to: /var/folders/xx/xxxxxxxxxx/T/brobot-images-XXXXXXXXXXXXX
HH:MM:SS INFO  Extracted N images from JAR
HH:MM:SS INFO  Image 'working/claude-icon-1' loaded successfully
[Additional image loading messages...]

[Framework Initialization]
HH:MM:SS INFO  Initializing FrameworkSettings from BrobotProperties
HH:MM:SS INFO  ExecutionEnvironment initialized: ExecutionEnvironment[mockMode=false, hasDisplay=true, canCaptureScreen=true, useRealFiles=true]
HH:MM:SS INFO  SikuliX internal logging disabled - all logs will come through Brobot

[macOS Specific Messages]
HH:MM:SS INFO  Accessibility permissions verified for screen capture
HH:MM:SS INFO  Retina display scaling factor: 2.0

[State Verification - Always Unicode on Mac]
HH:MM:SS INFO  Starting active state verification
HH:MM:SS INFO  ▶ Searching for Working state: [SUCCESS/FAILED] ✓/✗
HH:MM:SS INFO  ▶ Searching for Prompt state: [SUCCESS/FAILED] ✓/✗

[Monitoring]
HH:MM:SS INFO  Claude Automator is running. Press Ctrl+C to stop.
HH:MM:SS INFO  Application ready and monitoring started
[Continuous monitoring messages every few seconds]
```

## Key Differences Between Platforms

### Windows
- Temp directory: `C:\Users\USERNAME\AppData\Local\Temp\`
- Display names: `\Display0`, `\Display1`, etc.
- Path separators: Backslashes (`\`)
- No accessibility permission messages
- Symbol support varies by terminal:
  - Windows Terminal: Full Unicode symbols
  - Legacy Console: ASCII fallbacks

### Mac
- Temp directory: `/var/folders/xx/xxxxxxxxxx/T/`
- Display names: `Built-in Retina Display`, `External Display`, etc.
- Path separators: Forward slashes (`/`)
- Accessibility permission verification
- Retina display scaling messages
- Always supports Unicode symbols and ANSI colors
- May show additional security/privacy related logs

## Error Scenarios

### Image Loading Failures
```
HH:MM:SS ERROR Failed to load image: working/claude-icon-1. Creating placeholder.
HH:MM:SS INFO  Image 'working/claude-icon-1' loaded from null in Xms
```

### State Not Found (with symbols)
```
# Windows Terminal / Mac:
HH:MM:SS INFO  ▶ Find_COMPLETE [FAILED] ✗
HH:MM:SS WARN  ⚠ Claude prompt not found - cannot proceed with monitoring
HH:MM:SS INFO  ℹ Claude prompt not found

# Legacy Windows:
HH:MM:SS INFO  > Find_COMPLETE [FAILED] [FAIL]
HH:MM:SS WARN  [WARN] Claude prompt not found - cannot proceed with monitoring
HH:MM:SS INFO  [INFO] Claude prompt not found
```

### Permission Issues (Mac only)
```
HH:MM:SS ERROR ⚠ Screen capture permission denied. Please grant accessibility permissions in System Preferences > Security & Privacy > Privacy > Accessibility
```

## Success Indicators
1. All images load successfully (no ERROR messages for image loading)
2. At least one state is found and becomes active
3. Monitoring loop starts without warnings
4. No permission or accessibility errors
5. Appropriate symbols display based on terminal capabilities

## Forcing Symbol Display

To force specific symbol display:
- Enable ANSI/Unicode: `-Dbrobot.console.ansi=true`
- Disable ANSI/Unicode: `-Dbrobot.console.ansi=false`
- Use Windows Terminal for best Windows experience
- Use iTerm2 or Terminal.app for Mac
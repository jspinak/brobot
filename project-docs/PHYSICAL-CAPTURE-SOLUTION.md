# Physical Resolution Capture Solution for Brobot

## Problem Statement
Java 21 introduced DPI awareness, causing Robot.createScreenCapture() to return logical resolution (e.g., 1536x864 with 125% Windows scaling) instead of physical resolution (1920x1080). This breaks pattern matching with images captured at physical resolution.

## Solution Overview
Implemented a provider-based capture system with Robot as the default provider that automatically scales captures to physical resolution when DPI scaling is detected.

## Architecture

### 1. Provider Interface (`CaptureProvider.java`)
- Defines standard interface for capture providers
- Supports screen and region capture
- Reports resolution type (PHYSICAL, LOGICAL, UNKNOWN)

### 2. Capture Providers

#### RobotCaptureProvider (Default)
- Uses Java's Robot API for capture
- Detects DPI scaling automatically
- Scales captures from logical to physical resolution
- Configuration:
  ```properties
  brobot.capture.robot.scale-to-physical=true
  brobot.capture.robot.expected-physical-width=1920
  brobot.capture.robot.expected-physical-height=1080
  ```

#### FFmpegCaptureProvider (Alternative)
- Uses FFmpeg for true physical resolution capture
- Platform-specific commands (gdigrab, avfoundation, x11grab)
- No scaling needed - captures directly at physical resolution
- Configuration:
  ```properties
  brobot.capture.ffmpeg.path=ffmpeg
  brobot.capture.ffmpeg.timeout=5
  brobot.capture.ffmpeg.format=png
  ```

#### SikuliXCaptureProvider (Compatibility)
- Wrapper around existing SikuliX Screen class
- Maintains backward compatibility
- Reports UNKNOWN resolution type

### 3. Capture Service (`BrobotCaptureService.java`)
- Central service managing capture providers
- Auto-selects best provider based on configuration
- Provider priority: Robot → FFmpeg → SikuliX
- Configuration:
  ```properties
  brobot.capture.provider=AUTO
  brobot.capture.prefer-physical=true
  brobot.capture.fallback-enabled=true
  ```

## DPI Scaling Detection

The RobotCaptureProvider detects scaling through multiple methods:

1. **Graphics Transform Check**: Checks `GraphicsConfiguration.getDefaultTransform().getScaleX()`
2. **Resolution Comparison**: Compares captured resolution with expected physical resolution
3. **Common Scaling Scenarios**:
   - 1536x864 → 1920x1080 (125% scaling)
   - 1280x720 → 1920x1080 (150% scaling)
   - 960x540 → 1920x1080 (200% scaling)

## Usage

### Basic Usage
```java
@Autowired
private BrobotCaptureService captureService;

// Capture full screen at physical resolution
BufferedImage screen = captureService.captureScreen();

// Capture region at physical resolution
Rectangle region = new Rectangle(100, 100, 400, 300);
BufferedImage regionCapture = captureService.captureRegion(region);
```

### Provider Switching
```java
// Switch to FFmpeg if available
captureService.setProvider("FFMPEG");

// Switch back to Robot
captureService.setProvider("ROBOT");

// Get provider info
String info = captureService.getProvidersInfo();
```

## Configuration

Add to `application.properties`:

```properties
# Capture provider selection (AUTO, ROBOT, FFMPEG, SIKULIX)
brobot.capture.provider=AUTO

# Prefer physical resolution captures
brobot.capture.prefer-physical=true

# Enable fallback to other providers
brobot.capture.fallback-enabled=true

# Robot provider settings
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080

# FFmpeg provider settings (if using)
brobot.capture.ffmpeg.path=ffmpeg
brobot.capture.ffmpeg.timeout=5
```

## Benefits

1. **Automatic Physical Resolution**: Robot provider automatically scales to physical resolution
2. **No External Dependencies**: Robot is built into Java, no FFmpeg installation required
3. **Fast Performance**: In-memory scaling is faster than external tools
4. **Cross-Platform**: Works on Windows, Mac, and Linux
5. **Backward Compatible**: Existing code continues to work
6. **Flexible**: Can switch providers based on needs

## Testing

The implementation includes comprehensive tests:
- `RobotCaptureProviderTest`: Tests Robot provider functionality
- `BrobotCaptureServiceIntegrationTest`: Tests service integration

Run tests:
```bash
./gradlew test --tests "*Robot*" --no-daemon
./gradlew test --tests "*BrobotCaptureService*" --no-daemon
```

## Migration Guide

No code changes required for existing Brobot applications. The new capture system:
1. Automatically uses Robot as default provider
2. Scales captures to physical resolution transparently
3. Falls back to SikuliX if Robot unavailable
4. Maintains full API compatibility

## Troubleshooting

### Issue: Captures still at logical resolution
**Solution**: Verify configuration:
```properties
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
```

### Issue: Scaling not detected
**Solution**: Set expected resolution to match your monitor:
```properties
brobot.capture.robot.expected-physical-width=2560
brobot.capture.robot.expected-physical-height=1440
```

### Issue: Want true physical capture without scaling
**Solution**: Use FFmpeg provider:
```properties
brobot.capture.provider=FFMPEG
```

## Implementation Files

- `/library/src/main/java/io/github/jspinak/brobot/capture/provider/CaptureProvider.java`
- `/library/src/main/java/io/github/jspinak/brobot/capture/provider/RobotCaptureProvider.java`
- `/library/src/main/java/io/github/jspinak/brobot/capture/provider/FFmpegCaptureProvider.java`
- `/library/src/main/java/io/github/jspinak/brobot/capture/provider/SikuliXCaptureProvider.java`
- `/library/src/main/java/io/github/jspinak/brobot/capture/BrobotCaptureService.java`
- `/library/src/main/resources/brobot-defaults.properties`

## Summary

The Robot-based capture solution provides automatic physical resolution capture for Brobot, solving the DPI scaling issues introduced in Java 21. It requires no external dependencies, works across platforms, and maintains full backward compatibility while ensuring pattern matching works correctly with images captured at physical resolution.
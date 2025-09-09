# Modular Screen Capture System Guide

## Overview
Brobot's screen capture system is fully modular, allowing you to switch between different capture providers (SikuliX, Robot, FFmpeg) with just a single property change. No code modifications needed!

## Quick Start

### Default Configuration (No Setup Required)

Brobot defaults to SikuliX with automatic DPI recognition:

```properties
# Already configured in brobot-defaults.properties:
brobot.capture.provider=SIKULIX
brobot.dpi.resize-factor=auto
```

**No configuration needed!** These defaults provide maximum compatibility.

### Switching Providers via Properties

To use a different provider, set `brobot.capture.provider` in your `application.properties`:

```properties
# Use Robot with DPI scaling compensation
brobot.capture.provider=ROBOT

# Use FFmpeg for true physical capture
brobot.capture.provider=FFMPEG

# Return to SikuliX (default)
brobot.capture.provider=SIKULIX

# Let the system choose automatically
brobot.capture.provider=AUTO
```

That's it! Your entire application will now use the specified capture provider.

### Command Line Override

You can also override the provider at runtime:

```bash
# Run with Robot provider
java -Dbrobot.capture.provider=ROBOT -jar myapp.jar

# Run with FFmpeg provider
java -Dbrobot.capture.provider=FFMPEG -jar myapp.jar
```

## Available Providers

### 1. SikuliX Provider (Default)
- **Maximum compatibility** - Works with existing patterns
- **Automatic DPI handling** - Uses `resize-factor=auto`
- **No configuration needed** - Already set as default
- **Proven reliability** - Well-tested with Brobot

```properties
brobot.capture.provider=SIKULIX
brobot.dpi.resize-factor=auto
```

### 2. Robot Provider
- **No external dependencies** - Built into Java
- **DPI scaling compensation** - Automatically scales to physical resolution
- **Fast performance** - In-memory operations
- **Cross-platform** - Works on Windows, Mac, Linux

```properties
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

### 3. FFmpeg Provider
- **True physical capture** - No scaling needed
- **Professional quality** - Industry-standard capture
- **Requires FFmpeg installation**

```properties
brobot.capture.provider=FFMPEG
brobot.capture.ffmpeg.path=ffmpeg
brobot.capture.ffmpeg.timeout=5
brobot.capture.ffmpeg.format=png
```


## Configuration Examples

### Example 1: Development Environment
Use default SikuliX with auto DPI:

```properties
# application-dev.properties
# No configuration needed - uses defaults:
# brobot.capture.provider=SIKULIX
# brobot.dpi.resize-factor=auto
brobot.capture.enable-logging=true
```

### Example 2: Production Environment
Use FFmpeg for maximum accuracy:

```properties
# application-prod.properties
brobot.capture.provider=FFMPEG
brobot.capture.enable-logging=false
brobot.capture.retry-count=5
```

### Example 3: CI/CD Pipeline
Use AUTO for flexibility:

```properties
# application-ci.properties
brobot.capture.provider=AUTO
brobot.capture.prefer-physical=true
brobot.capture.fallback-enabled=true
```

## Runtime Provider Switching

You can also switch providers programmatically:

```java
@Autowired
private CaptureConfiguration captureConfig;

// Switch to different providers at runtime
captureConfig.useRobot();     // Use Robot with scaling
captureConfig.useFFmpeg();    // Use FFmpeg (if available)
captureConfig.useSikuliX();   // Use SikuliX
captureConfig.useAuto();      // Automatic selection

// Apply preset configurations
captureConfig.setCaptureMode(CaptureMode.ROBOT_PHYSICAL);
captureConfig.setCaptureMode(CaptureMode.FFMPEG);

// Check current configuration
String provider = captureConfig.getCurrentProvider();
boolean isPhysical = captureConfig.isCapturingPhysicalResolution();
```

## Using the Unified Capture Service

All capture operations use the same interface regardless of provider:

```java
@Autowired
private UnifiedCaptureService captureService;

// These methods work with ANY provider
BufferedImage screen = captureService.captureScreen();
BufferedImage region = captureService.captureRegion(new Rectangle(100, 100, 400, 300));

// Provider is determined by configuration
// No code changes needed when switching providers!
```

## Complete Configuration Reference

```properties
# ============================================================================
# Main Capture Settings
# ============================================================================
# Provider selection: AUTO, ROBOT, FFMPEG, SIKULIX
brobot.capture.provider=ROBOT

# Prefer physical resolution captures (recommended for pattern matching)
brobot.capture.prefer-physical=true

# Enable fallback if preferred provider fails
brobot.capture.fallback-enabled=true

# Enable debug logging
brobot.capture.enable-logging=false

# Retry failed captures automatically
brobot.capture.auto-retry=true
brobot.capture.retry-count=3

# ============================================================================
# Robot Provider Settings
# ============================================================================
# Scale captures to physical resolution
brobot.capture.robot.scale-to-physical=true

# Expected physical screen resolution
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080

# ============================================================================
# FFmpeg Provider Settings
# ============================================================================
# Path to FFmpeg executable
brobot.capture.ffmpeg.path=ffmpeg

# Capture timeout in seconds
brobot.capture.ffmpeg.timeout=5

# Output format
brobot.capture.ffmpeg.format=png

# Log level: error, warning, info, verbose, debug
brobot.capture.ffmpeg.log-level=error

# Platform-specific input devices (auto-detected if not set)
brobot.capture.ffmpeg.windows-input=gdigrab
brobot.capture.ffmpeg.mac-input=avfoundation
brobot.capture.ffmpeg.linux-input=x11grab
```

## Provider Selection Logic

When `brobot.capture.provider=AUTO`, the system selects providers in this order:

1. **Robot** - Always available, preferred for physical resolution
2. **FFmpeg** - If installed and available
3. **SikuliX** - Fallback option

## Migration Guide

### From Hardcoded SikuliX
Before:
```java
Screen screen = new Screen();
BufferedImage img = screen.capture().getImage();
```

After:
```java
@Autowired
private UnifiedCaptureService captureService;

BufferedImage img = captureService.captureScreen();
```

### From Direct Robot Usage
Before:
```java
Robot robot = new Robot();
BufferedImage img = robot.createScreenCapture(bounds);
```

After:
```java
@Autowired
private UnifiedCaptureService captureService;

BufferedImage img = captureService.captureRegion(bounds);
```

## Troubleshooting

### Provider Not Available
```
Error: Provider not available: FFMPEG
```
**Solution**: Install FFmpeg or switch to ROBOT provider

### Wrong Resolution Captured
```
Captured: 1536x864 (expected 1920x1080)
```
**Solution**: Enable Robot scaling:
```properties
brobot.capture.robot.scale-to-physical=true
```

### Capture Fails Intermittently
**Solution**: Enable retry logic:
```properties
brobot.capture.auto-retry=true
brobot.capture.retry-count=5
```

## Best Practices

1. **Use Properties for Configuration**
   - Configure via `application.properties` rather than code
   - Use Spring profiles for different environments

2. **Choose the Right Provider**
   - Development: ROBOT (no dependencies)
   - Production: FFMPEG (if accuracy critical)
   - Legacy: SIKULIX (for existing scripts)

3. **Enable Fallback**
   ```properties
   brobot.capture.fallback-enabled=true
   ```

4. **Monitor Provider Status**
   ```java
   captureConfig.printConfigurationReport();
   ```

5. **Validate Configuration**
   ```java
   if (!captureConfig.validateConfiguration()) {
       // Handle invalid configuration
   }
   ```

## Examples

### Simple Property Switch
```properties
# Just change this one line to switch providers!
brobot.capture.provider=FFMPEG
```

### Environment-Specific Configuration
```properties
# application-local.properties
brobot.capture.provider=ROBOT

# application-test.properties
brobot.capture.provider=AUTO

# application-prod.properties
brobot.capture.provider=FFMPEG
```

### Dynamic Provider Selection
```java
// Check environment and select appropriate provider
if (isHighAccuracyNeeded()) {
    captureConfig.useFFmpeg();
} else {
    captureConfig.useRobot();
}
```

## Summary

The modular capture system makes switching between screen capture tools as simple as changing a single property. No code changes, no recompilation, just update `brobot.capture.provider` and restart your application.

**Key Benefits:**
- **Zero code changes** when switching providers
- **Property-based configuration** for easy deployment
- **Automatic fallback** for robustness
- **Unified interface** for all capture operations
- **Runtime switching** when needed

**Remember:** The capture provider is now just a configuration choice, not a code dependency!
---
sidebar_position: 1
title: Modular Capture System
description: Complete guide to Brobot's modular screen capture system
keywords: [screen capture, robot, ffmpeg, sikulix, dpi, physical resolution]
---

# Modular Screen Capture System

## Overview

Brobot's screen capture system is designed to be completely modular, allowing you to switch between different capture providers (Robot, FFmpeg, SikuliX) with just a single configuration property. This architecture provides maximum flexibility while maintaining a consistent API across all providers.

## Key Features

- **Property-Based Configuration**: Switch providers via `application.properties`
- **Zero Code Changes**: Change capture tools without modifying code
- **Automatic Fallback**: System selects best available provider
- **DPI Scaling Support**: Automatic compensation for Windows scaling
- **Unified Interface**: Same API regardless of provider

## Quick Start

### Basic Configuration

Add to your `application.properties`:

```properties
# Choose your capture provider
brobot.capture.provider=ROBOT
```

That's it! Your entire application will now use the Robot capture provider.

### Available Providers

| Provider | Property Value | Dependencies | Best For |
|----------|---------------|--------------|----------|
| Robot | `ROBOT` | None (built-in Java) | General use, no setup required |
| FFmpeg | `FFMPEG` | JavaCV (included in Brobot) | Maximum accuracy, professional capture |
| SikuliX | `SIKULIX` | SikuliX library | Legacy compatibility |
| Auto | `AUTO` | None | Automatic selection |

## Provider Details

### Robot Provider (Recommended Default)

The Robot provider uses Java's built-in `java.awt.Robot` class with intelligent DPI scaling compensation.

**Advantages:**
- No external dependencies
- Fast in-memory operations
- Automatic DPI scaling to physical resolution
- Cross-platform support

**Configuration:**
```properties
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

**DPI Scaling Detection:**
The Robot provider automatically detects Windows DPI scaling and compensates:
- 125% scaling: 1536x864 → 1920x1080
- 150% scaling: 1280x720 → 1920x1080
- 200% scaling: 960x540 → 1920x1080

### FFmpeg Provider

FFmpeg provides true physical resolution capture without any scaling, using the JavaCV library that's already included in Brobot.

**Advantages:**
- True physical resolution capture
- Professional-grade quality
- Platform-specific optimizations
- No scaling artifacts
- **No external installation required** (uses bundled JavaCV)

**Configuration:**
```properties
brobot.capture.provider=FFMPEG
brobot.capture.ffmpeg.timeout=5
brobot.capture.ffmpeg.format=png
brobot.capture.ffmpeg.log-level=error
```

**Platform-Specific Capture Methods (via JavaCV):**
- Windows: Uses `gdigrab`
- macOS: Uses `avfoundation`
- Linux: Uses `x11grab`

### SikuliX Provider

Maintains backward compatibility with existing SikuliX-based scripts.

**Configuration:**
```properties
brobot.capture.provider=SIKULIX
```

**Note:** Resolution behavior varies by Java version:
- Java 8: Physical resolution
- Java 21+: Logical resolution (DPI-aware)

## Usage Examples

### Basic Screen Capture

```java
@Autowired
private UnifiedCaptureService captureService;

// Capture full screen
BufferedImage screen = captureService.captureScreen();

// Capture specific screen (multi-monitor)
BufferedImage screen1 = captureService.captureScreen(1);

// Capture region
Rectangle region = new Rectangle(100, 100, 400, 300);
BufferedImage regionCapture = captureService.captureRegion(region);
```

### Runtime Provider Switching

```java
@Autowired
private CaptureConfiguration captureConfig;

// Switch providers at runtime
captureConfig.useRobot();     // Use Robot with scaling
captureConfig.useFFmpeg();    // Use FFmpeg (if available)
captureConfig.useSikuliX();   // Use SikuliX
captureConfig.useAuto();      // Automatic selection

// Check current configuration
String provider = captureConfig.getCurrentProvider();
boolean isPhysical = captureConfig.isCapturingPhysicalResolution();
```

### Configuration Validation

```java
// Validate configuration
if (captureConfig.validateConfiguration()) {
    System.out.println("Capture configuration is valid");
}

// Get detailed configuration report
captureConfig.printConfigurationReport();

// Get all properties
Map<String, String> props = captureConfig.getAllCaptureProperties();
```

## Configuration Reference

### Complete Properties List

```properties
# ==================================================
# Main Capture Settings
# ==================================================
# Provider selection: AUTO, ROBOT, FFMPEG, SIKULIX
brobot.capture.provider=ROBOT

# Prefer physical resolution captures
brobot.capture.prefer-physical=true

# Enable fallback to other providers
brobot.capture.fallback-enabled=true

# Enable debug logging
brobot.capture.enable-logging=false

# Retry configuration
brobot.capture.auto-retry=true
brobot.capture.retry-count=3

# ==================================================
# Robot Provider Settings
# ==================================================
# Scale to physical resolution
brobot.capture.robot.scale-to-physical=true

# Expected physical resolution
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080

# ==================================================
# FFmpeg Provider Settings (uses bundled JavaCV)
# ==================================================
# Capture timeout (seconds)
brobot.capture.ffmpeg.timeout=5

# Output format
brobot.capture.ffmpeg.format=png

# Log level
brobot.capture.ffmpeg.log-level=error
```

### Environment-Specific Profiles

```properties
# application-dev.properties
brobot.capture.provider=ROBOT
brobot.capture.enable-logging=true

# application-test.properties  
brobot.capture.provider=AUTO
brobot.capture.fallback-enabled=true

# application-prod.properties
brobot.capture.provider=FFMPEG
brobot.capture.retry-count=5
```

## Advanced Topics

### Custom Provider Implementation

You can create custom capture providers by implementing the `CaptureProvider` interface:

```java
@Component
public class CustomCaptureProvider implements CaptureProvider {
    
    @Override
    public BufferedImage captureScreen() throws IOException {
        // Your implementation
    }
    
    @Override
    public boolean isAvailable() {
        // Check if your provider can work
    }
    
    @Override
    public String getName() {
        return "Custom";
    }
    
    @Override
    public ResolutionType getResolutionType() {
        return ResolutionType.PHYSICAL;
    }
}
```

Register as a Spring bean and use via properties:
```properties
brobot.capture.provider=CUSTOM
```

### Provider Selection Logic

When `AUTO` is configured, the selection order is:

1. **Configured Provider** - If explicitly set and available
2. **Robot** - Always available, preferred for physical resolution
3. **FFmpeg** - If installed and available
4. **SikuliX** - Fallback option

### Handling DPI Scaling

The system automatically handles DPI scaling in different ways:

- **Robot Provider**: Detects and compensates via image scaling
- **FFmpeg Provider**: Captures at true physical resolution
- **SikuliX Provider**: Behavior varies by Java version

### Performance Considerations

| Provider | Speed | Memory Usage | Quality |
|----------|-------|--------------|---------|
| Robot | Fast | Low | Good (with scaling) |
| FFmpeg | Medium | Medium | Excellent |
| SikuliX | Medium | Medium | Variable |

## Troubleshooting

### Common Issues

**Provider Not Available**
```
Error: Provider not available: FFMPEG
```
**Solution:** Install FFmpeg or switch to ROBOT provider

**Wrong Resolution Captured**
```
Captured: 1536x864 (expected 1920x1080)
```
**Solution:** Enable Robot scaling:
```properties
brobot.capture.robot.scale-to-physical=true
```

**Capture Fails Intermittently**
**Solution:** Enable retry logic:
```properties
brobot.capture.auto-retry=true
brobot.capture.retry-count=5
```

### Debugging

Enable logging to diagnose issues:
```properties
brobot.capture.enable-logging=true
```

Check provider status programmatically:
```java
System.out.println(captureService.getProvidersInfo());
```

## Migration Guide

### From Direct SikuliX Usage

**Before:**
```java
Screen screen = new Screen();
BufferedImage img = screen.capture().getImage();
```

**After:**
```java
@Autowired
private UnifiedCaptureService captureService;

BufferedImage img = captureService.captureScreen();
```

### From Direct Robot Usage

**Before:**
```java
Robot robot = new Robot();
BufferedImage img = robot.createScreenCapture(bounds);
```

**After:**
```java
@Autowired
private UnifiedCaptureService captureService;

BufferedImage img = captureService.captureRegion(bounds);
```

## Best Practices

1. **Use Properties for Configuration**
   - Configure via properties files, not code
   - Use Spring profiles for different environments

2. **Enable Fallback for Production**
   ```properties
   brobot.capture.fallback-enabled=true
   ```

3. **Validate Configuration on Startup**
   ```java
   @PostConstruct
   public void validateCapture() {
       captureConfig.validateConfiguration();
   }
   ```

4. **Monitor Provider Status**
   - Log provider selection
   - Monitor capture failures
   - Track performance metrics

5. **Choose Appropriate Provider**
   - Development: `ROBOT` (no setup required)
   - CI/CD: `AUTO` (flexible)
   - Production: `FFMPEG` (if accuracy critical)

## API Reference

### UnifiedCaptureService

Primary service for all capture operations:

```java
public interface UnifiedCaptureService {
    BufferedImage captureScreen() throws IOException;
    BufferedImage captureScreen(int screenId) throws IOException;
    BufferedImage captureRegion(Rectangle region) throws IOException;
    BufferedImage captureRegion(int screenId, Rectangle region) throws IOException;
    void setProvider(String providerName);
    CaptureProvider getActiveProvider();
    String getProvidersInfo();
}
```

### CaptureConfiguration

Configuration and management helper:

```java
public interface CaptureConfiguration {
    void useRobot();
    void useFFmpeg();
    void useSikuliX();
    void useAuto();
    void setCaptureMode(CaptureMode mode);
    String getCurrentProvider();
    boolean isCapturingPhysicalResolution();
    boolean validateConfiguration();
    Map<String, String> getAllCaptureProperties();
}
```

## Summary

The modular capture system provides:

- **Complete Flexibility**: Switch providers with a single property
- **Zero Code Impact**: No code changes when switching
- **Production Ready**: Automatic fallback and retry logic
- **DPI Aware**: Handles Windows scaling correctly
- **Extensible**: Support for custom providers

Simply set `brobot.capture.provider` in your properties file and let the system handle the rest!
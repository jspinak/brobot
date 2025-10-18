---
sidebar_position: 1
title: Modular Capture System
description: Complete guide to Brobot's modular screen capture system
keywords: [screen capture, robot, ffmpeg, sikulix, dpi, physical resolution]
---

# Modular Screen Capture System

## Overview

Brobot's screen capture system is designed to be completely modular, allowing you to switch between different capture providers (JAVACV_FFMPEG, Robot, FFmpeg, SikuliX) with just a single configuration property. This architecture provides maximum flexibility while maintaining a consistent API across all providers.

**Default Configuration**: Brobot uses **JAVACV_FFMPEG** as the default provider for 100% accurate physical resolution capture. No configuration needed for optimal pattern matching!

> For comprehensive information on DPI scaling and resolution strategies, see the **[DPI and Resolution Guide](./dpi-resolution-guide.md)**.

## Key Features

- **Property-Based Configuration**: Switch providers via `application.properties`
- **Zero Code Changes**: Change capture tools without modifying code
- **Automatic Fallback**: System selects best available provider
- **DPI Scaling Support**: Physical resolution capture with DPI disable strategy
- **Unified Interface**: Same API regardless of provider
- **100% Match Accuracy**: Default configuration provides pixel-perfect pattern matching

## Quick Start

### Basic Configuration

The default configuration uses JAVACV_FFMPEG for optimal physical resolution capture:

```properties
# Default settings in brobot-defaults.properties:
brobot.capture.provider=JAVACV_FFMPEG  # JavaCV FFmpeg for 100% pattern match accuracy
brobot.dpi.disable=true                # Disable DPI awareness for physical resolution
brobot.dpi.resize-factor=1.0           # No pattern scaling (1:1 pixel matching)
```

**No configuration needed!** These defaults provide:
- Physical resolution capture (1920×1080 on Full HD displays)
- 100% pattern match accuracy (pixel-perfect matching)
- No scaling artifacts or interpolation blur
- Works with patterns from any source (SikuliX IDE, Snipping Tool, etc.)

### Available Providers

| Provider | Property Value | Dependencies | Best For |
|----------|---------------|--------------|----------|
| JAVACV_FFMPEG | `JAVACV_FFMPEG` | JavaCV (bundled) | **Default** - 100% accurate physical capture |
| Robot | `ROBOT` | None (built-in Java) | Fast, dynamic DPI scaling |
| FFmpeg | `FFMPEG` | External FFmpeg binary | Physical capture with external FFmpeg |
| SikuliX | `SIKULIX` | SikuliX library (included) | Legacy compatibility (Java version dependent) |
| Auto | `AUTO` | None | Automatic selection |

## Provider Details

### JAVACV_FFMPEG Provider (Default) ✅

JAVACV_FFMPEG is the default and recommended provider for maximum pattern matching accuracy.

**Advantages:**
- **100% pattern match accuracy** - Pixel-perfect matching with no scaling artifacts
- **Physical resolution capture** - Always captures at true hardware resolution (e.g., 1920×1080)
- **No external dependencies** - JavaCV libraries bundled with Brobot
- **Universal pattern compatibility** - Works with patterns from any source
- **Fast performance** - Native capture with no runtime scaling overhead
- **Cross-platform** - Windows, macOS, Linux support

**Configuration:**
```properties
brobot.capture.provider=JAVACV_FFMPEG  # Default - no configuration needed
brobot.dpi.disable=true                 # Physical resolution capture
brobot.dpi.resize-factor=1.0            # No pattern scaling
```

**How It Works:**
- Uses bundled JavaCV/FFmpeg libraries for native screen capture
- Disables Java's DPI awareness to capture at physical resolution
- No pattern scaling needed (1:1 pixel matching)
- Platform-specific optimizations (gdigrab on Windows, avfoundation on macOS, x11grab on Linux)

**When to Use:**
- ✅ **Recommended for all new projects** - Best accuracy and performance
- ✅ When you need 100% pattern match reliability
- ✅ When using patterns from multiple sources
- ✅ Production environments where accuracy is critical

### SikuliX Provider

SikuliX is a legacy provider maintained for backward compatibility.

**Advantages:**
- Compatibility with older Brobot projects
- Works with existing SikuliX workflows
- No additional configuration for basic usage

**Disadvantages:**
- ⚠️ **Java version dependent resolution behavior**
- ⚠️ Lower pattern match accuracy (~77%) compared to physical capture
- ⚠️ May require pattern scaling with `resize-factor=auto`

**Configuration:**
```properties
brobot.capture.provider=SIKULIX
brobot.dpi.disable=false          # Enable DPI awareness
brobot.dpi.resize-factor=auto     # Automatic pattern scaling
```

**Resolution Behavior:**
- Java 8: Captures at physical resolution
- Java 21+: Captures at logical resolution (requires resize-factor=auto for scaling)

**When to Use:**
- Migrating from older Brobot versions
- Existing projects using SikuliX patterns with auto-scaling
- Testing backward compatibility

### Robot Provider

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

### FFmpeg Provider (External)

The FFmpeg provider uses an **external FFmpeg installation** for screen capture. This is different from JAVACV_FFMPEG, which uses bundled libraries.

**Advantages:**
- True physical resolution capture
- Professional-grade quality
- Platform-specific optimizations
- No scaling artifacts
- Can use latest FFmpeg features

**Disadvantages:**
- ❌ **Requires external FFmpeg installation** (not bundled)
- ⚠️ Platform-specific setup required
- ⚠️ Version compatibility considerations

**Configuration:**
```properties
brobot.capture.provider=FFMPEG  # Requires FFmpeg installed on system
brobot.capture.ffmpeg.timeout=5
brobot.capture.ffmpeg.format=png
brobot.capture.ffmpeg.log-level=error
```

**Platform-Specific Capture Methods:**
- Windows: Uses `gdigrab` (requires FFmpeg.exe in PATH)
- macOS: Uses `avfoundation` (requires FFmpeg via Homebrew)
- Linux: Uses `x11grab` (requires FFmpeg package)

**When to Use:**
- You already have FFmpeg installed for other purposes
- You need specific FFmpeg features not in bundled JavaCV
- **Otherwise, use JAVACV_FFMPEG instead** (no installation needed)


## Usage Examples

### Required Imports

```java
// Core Java imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

// Brobot imports
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import io.github.jspinak.brobot.capture.CaptureConfiguration;
import io.github.jspinak.brobot.capture.provider.CaptureProvider;
```

### Basic Screen Capture

```java
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class ScreenCaptureService {

    @Autowired
    private UnifiedCaptureService captureService;

    public BufferedImage captureFullScreen() throws IOException {
        // Capture full screen
        return captureService.captureScreen();
    }

    public BufferedImage captureMonitor(int screenId) throws IOException {
        // Capture specific screen (multi-monitor)
        return captureService.captureScreen(screenId);
    }

    public BufferedImage captureArea() throws IOException {
        // Capture region
        Rectangle region = new Rectangle(100, 100, 400, 300);
        return captureService.captureRegion(region);
    }
}
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
# Provider selection: JAVACV_FFMPEG, AUTO, ROBOT, FFMPEG, SIKULIX
# Default is JAVACV_FFMPEG for 100% accurate physical resolution capture
brobot.capture.provider=JAVACV_FFMPEG

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
brobot.capture.provider=JAVACV_FFMPEG  # Or use default
brobot.capture.enable-logging=true

# application-test.properties
brobot.capture.provider=AUTO  # Flexible for CI/CD
brobot.capture.fallback-enabled=true

# application-prod.properties
brobot.capture.provider=JAVACV_FFMPEG  # 100% accuracy
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

1. **JAVACV_FFMPEG** - Preferred (100% accuracy, bundled)
2. **Robot** - Fallback (always available, dynamic DPI)
3. **FFmpeg** - If external FFmpeg is installed
4. **SikuliX** - Legacy fallback option

**Recommendation**: Use explicit `brobot.capture.provider=JAVACV_FFMPEG` instead of `AUTO` for predictable behavior.

### Handling DPI Scaling

The system automatically handles DPI scaling in different ways:

- **JAVACV_FFMPEG Provider (Default)**: Captures at true physical resolution with DPI awareness disabled
- **Robot Provider**: Detects and compensates via dynamic image scaling
- **FFmpeg Provider**: Captures at true physical resolution (requires external FFmpeg)
- **SikuliX Provider**: Behavior varies by Java version (8: physical, 21: logical)

> **For detailed DPI handling strategies and troubleshooting**, see the **[DPI and Resolution Guide](./dpi-resolution-guide.md)**.

### Performance Considerations

| Provider | Speed | Memory Usage | Quality | Accuracy |
|----------|-------|--------------|---------|----------|
| JAVACV_FFMPEG | Fast | Low-Medium | Excellent | 100% |
| Robot | Fast | Low | Excellent (with scaling) | 95%+ |
| FFmpeg | Medium | Medium | Excellent | 100% |
| SikuliX | Medium | Medium | Variable | 77% |

**Recommendation**: JAVACV_FFMPEG provides the best balance of speed, quality, and accuracy.

## Troubleshooting

### Common Issues

**Provider Not Available**
```
Error: Provider not available: FFMPEG
```
**Solution:**
- If using external `FFMPEG`: Install FFmpeg binary on your system
- **Recommended**: Switch to `JAVACV_FFMPEG` (no installation needed)
- Alternative: Switch to `ROBOT` provider (always available)

**Wrong Resolution Captured**
```
Captured: 1536x864 (expected 1920x1080)
```
**Diagnosis**: This indicates logical resolution capture (DPI scaling issue).

**Solution 1 (Recommended)**: Use default JAVACV_FFMPEG provider:
```properties
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
```

**Solution 2**: Enable Robot physical scaling:
```properties
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

> See the **[DPI and Resolution Guide](./dpi-resolution-guide.md)** for comprehensive troubleshooting.

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
   - **Recommended (Default)**: `JAVACV_FFMPEG` (100% accuracy, no setup)
   - Development: `JAVACV_FFMPEG` or `ROBOT` (no setup required)
   - CI/CD: `AUTO` (flexible) or `JAVACV_FFMPEG` (reliable)
   - Production: `JAVACV_FFMPEG` (accuracy + performance) or `ROBOT` (fallback)

## API Reference

### UnifiedCaptureService

Primary service for all capture operations:

```java
@Service
@Primary
public class UnifiedCaptureService {
    // Capture methods
    public BufferedImage captureScreen() throws IOException;
    public BufferedImage captureScreen(int screenId) throws IOException;
    public BufferedImage captureRegion(Rectangle region) throws IOException;
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException;

    // Provider management
    public void setProvider(String providerName);
    public CaptureProvider getActiveProvider();
    public String getActiveProviderName();
    public String getProvidersInfo();
    public boolean isPhysicalResolution();
}
```

### CaptureConfiguration

Configuration and management helper:

```java
@Component
public class CaptureConfiguration {
    // Provider switching
    public void useRobot();
    public void useFFmpeg();
    public void useSikuliX();
    public void useAuto();
    public void setCaptureMode(CaptureMode mode);

    // Configuration inspection
    public String getCurrentProvider();
    public boolean isCapturingPhysicalResolution();
    public boolean validateConfiguration();
    public Map<String, String> getAllCaptureProperties();
    public void printConfigurationReport();
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

---

## Related Documentation

### Core Capture Documentation
- **[Capture Quick Reference](./capture-quick-reference.md)** - Quick provider switching guide with decision tree and common scenarios
- **[DPI and Resolution Guide](./dpi-resolution-guide.md)** - Comprehensive DPI scaling and resolution handling strategies
- **[Capture Methods Comparison](../tools/capture-methods-comparison.md)** - Detailed performance benchmarks comparing all providers

### Configuration
- **[Configuration Properties Reference](../configuration/properties-reference.md)** - Complete reference for all Brobot configuration properties
- **[Auto-Configuration](../configuration/auto-configuration.md)** - Spring Boot auto-configuration and integration details
- **[Headless Configuration](../configuration/headless-configuration.md)** - Configuring Brobot for CI/CD and headless environments

### Testing & CI/CD
- **[Testing Introduction](../../04-testing/testing-intro.md)** - Overview of Brobot testing strategies and patterns
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing without screen interaction using mock mode
- **[Integration Testing](../../04-testing/integration-testing.md)** - Spring Boot integration testing with Brobot
- **[CI/CD Testing Guide](../../04-testing/advanced/ci-cd-testing.md)** - Testing Brobot applications in CI/CD pipelines

### Getting Started
- **[Introduction](../../01-getting-started/introduction.md)** - Brobot overview, why use Brobot, and core concepts
- **[Installation](../../01-getting-started/installation.md)** - Adding Brobot dependencies to your project
- **[Quick Start](../../01-getting-started/quick-start.md)** - Get started with Brobot quickly
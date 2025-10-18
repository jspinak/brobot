---
sidebar_position: 2
title: Capture Quick Reference
description: Quick reference for screen capture configuration
keywords: [capture, configuration, robot, ffmpeg, javacv]
---

# Screen Capture Quick Reference

## Switch Capture Provider

### Method 1: Properties File ✅ (Recommended)

```properties
# application.properties
# Default is already JAVACV_FFMPEG, but you can change it:
brobot.capture.provider=JAVACV_FFMPEG  # Default (or SIKULIX, ROBOT, FFMPEG, AUTO)

# DPI manual scaling (default is 1.0, or use 'auto' for auto-detection):
brobot.dpi.resize-factor=1.0
```

### Method 2: Command Line

```bash
java -Dbrobot.capture.provider=FFMPEG -jar myapp.jar
```

### Method 3: Runtime Code

```java
@Autowired
private CaptureConfiguration config;

config.useRobot();    // Switch to Robot
config.useFFmpeg();   // Switch to FFmpeg (JavaCV)
config.useSikuliX();  // Switch to SikuliX
```

## Provider Comparison

| Provider | Dependencies | Resolution | Performance | Setup | Default |
|----------|-------------|------------|-------------|-------|---------|
| **JAVACV_FFMPEG** | JavaCV (included) | Physical | Good | None | ✅ Yes |
| **SikuliX** | SikuliX (included) | Java 21: Logical / Java 8: Physical | Good | None | No |
| **Robot** | None | Configurable (Physical or Logical) | Fast | Configure scaling | No |
| **FFMPEG** | External FFmpeg | Physical | Good | Install FFmpeg | No |

*Robot captures at logical resolution by default, physical when `scale-to-physical=true`
**SikuliX resolution depends on Java version (21: logical, 8: physical)**
***JAVACV_FFMPEG uses bundled JavaCV library for native capture***

## Essential Properties

```properties
# Choose provider (JAVACV_FFMPEG is default, or SIKULIX, ROBOT, FFMPEG, AUTO)
brobot.capture.provider=JAVACV_FFMPEG

# DPI scaling (default is 1.0 - no scaling, or use 'auto' for auto-detection)
brobot.dpi.resize-factor=1.0

# Robot: Enable physical resolution scaling
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080

# FFmpeg: Configure capture (uses JavaCV)
brobot.capture.ffmpeg.timeout=5
brobot.capture.ffmpeg.format=png

# General: Enable retry and fallback
brobot.capture.auto-retry=true
brobot.capture.retry-count=3
brobot.capture.fallback-enabled=true
```

## Usage Examples

> **Prerequisites:** All examples assume a Spring Boot application with Brobot dependencies. Add these imports to your class:
> ```java
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.stereotype.Component;
> import io.github.jspinak.brobot.capture.*;
> import java.awt.Rectangle;
> import java.awt.image.BufferedImage;
> import java.io.IOException;
> ```
>
> For complete runnable examples with full class context, see the [Modular Capture System Guide](./modular-capture-system.md).

### Basic Capture

```java
@Autowired
private UnifiedCaptureService capture;

// Same code works with ANY provider!
BufferedImage screen = capture.captureScreen();
BufferedImage region = capture.captureRegion(new Rectangle(100, 100, 400, 300));
```

### Check Configuration

```java
@Autowired
private CaptureConfiguration config;

// Current provider
String provider = config.getCurrentProvider();

// Physical resolution?
boolean physical = config.isCapturingPhysicalResolution();

// Print full report
config.printConfigurationReport();
```

## Common Scenarios

### Scenario 1: Development Machine

```properties
# Use default JAVACV_FFMPEG for reliable physical resolution capture
# (No configuration needed - these are defaults)
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.resize-factor=1.0
```

### Scenario 2: CI/CD Pipeline

```properties
# Auto-select best available
brobot.capture.provider=AUTO
brobot.capture.fallback-enabled=true
```

### Scenario 3: Production Server

```properties
# Use JAVACV_FFMPEG for reliability (no external dependencies)
brobot.capture.provider=JAVACV_FFMPEG
brobot.capture.retry-count=5
```

### Scenario 4: Windows with DPI Scaling

```properties
# Robot with scaling compensation
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Wrong resolution (1536x864) | Enable Robot scaling: `brobot.capture.robot.scale-to-physical=true` |
| Provider not available | Switch to ROBOT: `brobot.capture.provider=ROBOT` |
| Intermittent failures | Enable retry: `brobot.capture.auto-retry=true` |
| Need to debug | Enable logging: `brobot.capture.enable-logging=true` |

## Decision Tree

```
Which provider should I use?

┌─ Need reliable physical resolution capture?
│  └─ Yes → JAVACV_FFMPEG (default, bundled JavaCV)
│  └─ No ↓
│
├─ Need fastest performance?
│  └─ Yes → Robot (with manual DPI configuration)
│  └─ No ↓
│
├─ Using Java 8 and need backward compatibility?
│  └─ Yes → SikuliX (physical on Java 8)
│  └─ No ↓
│
├─ Have external FFmpeg installed?
│  └─ Yes → FFMPEG (external FFmpeg binary)
│  └─ No ↓
│
└─ Unsure? → Stay with JAVACV_FFMPEG (default)
```

## Key Points

✅ **No code changes needed** when switching providers
✅ **JAVACV_FFMPEG bundled** (JavaCV included in Brobot)
✅ **Physical resolution by default** for accurate pattern matching
✅ **Properties control everything**
✅ **Automatic fallback** for robustness

## Default Setup

No configuration needed! Brobot defaults to:

```properties
# These are already set in brobot-defaults.properties:
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.resize-factor=1.0
```

Your application automatically uses JAVACV_FFMPEG with physical resolution capture for optimal pattern matching accuracy.

---

## Related Documentation

### Core Capture Documentation
- **[DPI and Resolution Guide](./dpi-resolution-guide.md)** - Comprehensive guide to handling DPI scaling and resolution issues
- **[Capture Methods Comparison](../tools/capture-methods-comparison.md)** - Detailed performance study comparing all capture providers
- **[Modular Capture System Guide](./modular-capture-system.md)** - Complete examples with class context and imports

### Configuration
- **[Configuration Properties Reference](../configuration/properties-reference.md)** - Complete reference for all Brobot configuration properties
- **[Configuration Note](../guides/finding-objects/configuration-note.md)** - Configuration best practices

### Testing & Deployment
- **[CI/CD Testing Guide](../testing/ci-cd-testing.md)** - Testing Brobot applications in CI/CD pipelines
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing without GUI using mock mode

### Technical Deep Dives
- **[SikuliX Physical Capture Analysis](../../../sikuli-physical-capture-analysis.md)** - Analysis of SikuliX's capture behavior and Java version differences

### Troubleshooting
- **[Action Config Troubleshooting](../action-config/troubleshooting-chains.md)** - Debugging action chains and configuration issues
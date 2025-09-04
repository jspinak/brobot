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
# Default is already SIKULIX, but you can change it:
brobot.capture.provider=SIKULIX  # Default (or ROBOT, FFMPEG, AUTO)

# DPI auto-detection is enabled by default:
brobot.dpi.resize-factor=auto
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
| **SikuliX** | SikuliX (included) | Auto-handled | Good | None | ✅ Yes |
| **Robot** | None | Physical* | Fast | None | No |
| **FFmpeg** | JavaCV (included) | Physical | Good | None | No |

*Robot scales logical to physical when DPI scaling detected  
**SikuliX uses auto resize-factor for DPI handling**

## Essential Properties

```properties
# Choose provider (SIKULIX is default, or ROBOT, FFMPEG, AUTO)
brobot.capture.provider=SIKULIX

# DPI auto-detection (enabled by default)
brobot.dpi.resize-factor=auto

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
# Use default SikuliX with auto DPI
# (No configuration needed - these are defaults)
brobot.capture.provider=SIKULIX
brobot.dpi.resize-factor=auto
```

### Scenario 2: CI/CD Pipeline

```properties
# Auto-select best available
brobot.capture.provider=AUTO
brobot.capture.fallback-enabled=true
```

### Scenario 3: Production Server

```properties
# Use FFmpeg for accuracy
brobot.capture.provider=FFMPEG
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

┌─ Want maximum compatibility?
│  └─ Yes → SikuliX (default, with auto DPI)
│  └─ No ↓
│
├─ Need true physical capture?
│  └─ Yes → FFmpeg (JavaCV)
│  └─ No ↓
│
├─ Want manual DPI control?
│  └─ Yes → Robot (with scaling settings)
│  └─ No ↓
│
├─ Need fastest performance?
│  └─ Yes → Robot
│  └─ No ↓
│
└─ Unsure? → Stay with SikuliX (default)
```

## Key Points

✅ **No code changes needed** when switching providers  
✅ **FFmpeg uses JavaCV** (already included in Brobot)  
✅ **Robot handles DPI scaling** automatically  
✅ **Properties control everything**  
✅ **Automatic fallback** for robustness  

## Default Setup

No configuration needed! Brobot defaults to:

```properties
# These are already set in brobot-defaults.properties:
brobot.capture.provider=SIKULIX
brobot.dpi.resize-factor=auto
```

Your application automatically uses SikuliX with automatic DPI recognition.
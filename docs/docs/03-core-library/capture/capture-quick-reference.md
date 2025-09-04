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
brobot.capture.provider=ROBOT    # or FFMPEG, SIKULIX, AUTO
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

| Provider | Dependencies | Resolution | Performance | Setup |
|----------|-------------|------------|-------------|-------|
| **Robot** | None | Physical* | Fast | None |
| **FFmpeg** | JavaCV (included) | Physical | Good | None |
| **SikuliX** | SikuliX | Varies | Good | None |

*Robot scales logical to physical when DPI scaling detected

## Essential Properties

```properties
# Choose provider (ROBOT, FFMPEG, SIKULIX, AUTO)
brobot.capture.provider=ROBOT

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
# Use Robot - no dependencies needed
brobot.capture.provider=ROBOT
brobot.capture.enable-logging=true
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

┌─ Need maximum accuracy?
│  └─ Yes → FFmpeg (JavaCV)
│  └─ No ↓
│
├─ Have DPI scaling issues?
│  └─ Yes → Robot (with scaling enabled)
│  └─ No ↓
│
├─ Need fastest performance?
│  └─ Yes → Robot
│  └─ No ↓
│
├─ Using legacy SikuliX code?
│  └─ Yes → SikuliX
│  └─ No ↓
│
└─ Unsure? → AUTO (let system choose)
```

## Key Points

✅ **No code changes needed** when switching providers  
✅ **FFmpeg uses JavaCV** (already included in Brobot)  
✅ **Robot handles DPI scaling** automatically  
✅ **Properties control everything**  
✅ **Automatic fallback** for robustness  

## One-Line Setup

Just add to `application.properties`:

```properties
brobot.capture.provider=ROBOT
```

That's it! Your entire application now uses the selected capture provider.
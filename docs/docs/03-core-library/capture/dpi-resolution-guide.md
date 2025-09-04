---
sidebar_position: 3
title: DPI and Resolution Guide
description: Understanding and handling DPI scaling in screen capture
keywords: [dpi, scaling, resolution, physical, logical, windows]
---

# DPI Scaling and Resolution Guide

## The Problem

Java 21 introduced DPI awareness, which causes screen captures to return **logical resolution** instead of **physical resolution** when Windows display scaling is enabled.

### Example
- **Physical Monitor**: 1920×1080 pixels
- **Windows Scaling**: 125%
- **Java 21 Captures**: 1536×864 pixels (logical resolution)
- **Pattern Images**: 1920×1080 pixels (physical resolution)
- **Result**: Pattern matching fails! ❌

## Understanding Resolution Types

### Physical Resolution
The actual number of pixels on your monitor.
- Example: 1920×1080, 2560×1440, 3840×2160
- What pattern images are usually created at
- What Java 8 captures

### Logical Resolution
The scaled resolution that applications see.
- Formula: Physical Resolution ÷ Scaling Factor
- Example: 1920×1080 ÷ 1.25 = 1536×864
- What Java 21+ captures by default

### Common Scaling Scenarios

| Windows Scaling | Physical | Logical | Scale Factor |
|-----------------|----------|---------|--------------|
| 100% | 1920×1080 | 1920×1080 | 1.0 |
| 125% | 1920×1080 | 1536×864 | 1.25 |
| 150% | 1920×1080 | 1280×720 | 1.5 |
| 175% | 1920×1080 | 1097×617 | 1.75 |
| 200% | 1920×1080 | 960×540 | 2.0 |

## Solution: Brobot's Approach

Brobot provides multiple solutions to handle DPI scaling:

### 1. Robot Provider with Scaling (Default)

The Robot provider automatically detects DPI scaling and compensates by scaling captured images to physical resolution.

```properties
# Automatically scales to physical resolution
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

**How it works:**
1. Captures at logical resolution (e.g., 1536×864)
2. Detects scaling factor (e.g., 1.25)
3. Scales image to physical resolution (1920×1080)
4. Pattern matching works! ✅

### 2. FFmpeg Provider (JavaCV)

FFmpeg captures at true physical resolution without any scaling needed.

```properties
# True physical resolution capture
brobot.capture.provider=FFMPEG
```

**Advantages:**
- No scaling artifacts
- Always physical resolution
- Uses bundled JavaCV (no external installation)

### 3. Disable DPI Awareness (JVM Flag)

You can disable Java's DPI awareness entirely:

```bash
java -Dsun.java2d.dpiaware=false -jar myapp.jar
```

**Note:** This must be set as a JVM argument, not in code.

## Configuration by Use Case

### High-DPI Windows Laptop

```properties
# Robot with automatic scaling
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

### 4K Monitor with Scaling

```properties
# Adjust expected resolution
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=3840
brobot.capture.robot.expected-physical-height=2160
```

### Multi-Monitor Setup

```properties
# FFmpeg handles multi-monitor well
brobot.capture.provider=FFMPEG
```

### CI/CD Environment

```properties
# Auto-detect and adapt
brobot.capture.provider=AUTO
brobot.capture.prefer-physical=true
```

## Detecting Your Setup

### Check Current Resolution

```java
@Autowired
private CaptureConfiguration config;

// Print detailed configuration
config.printConfigurationReport();
```

### Manual Detection

```java
// Get screen dimensions
Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
System.out.println("Logical: " + screenSize.width + "×" + screenSize.height);

// Check for scaling
GraphicsConfiguration gc = GraphicsEnvironment
    .getLocalGraphicsEnvironment()
    .getDefaultScreenDevice()
    .getDefaultConfiguration();
    
double scale = gc.getDefaultTransform().getScaleX();
System.out.println("Scale Factor: " + scale);

// Calculate physical resolution
int physicalWidth = (int)(screenSize.width * scale);
int physicalHeight = (int)(screenSize.height * scale);
System.out.println("Physical: " + physicalWidth + "×" + physicalHeight);
```

## Troubleshooting

### Symptom: Pattern matching fails

**Diagnosis:**
```java
BufferedImage capture = captureService.captureScreen();
System.out.println("Captured: " + capture.getWidth() + "×" + capture.getHeight());
// If this shows 1536×864 instead of 1920×1080, you have scaling issues
```

**Solution:**
```properties
brobot.capture.robot.scale-to-physical=true
```

### Symptom: Captures are blurry after scaling

**Solution:** Use FFmpeg for true physical capture:
```properties
brobot.capture.provider=FFMPEG
```

### Symptom: Wrong expected resolution

**Solution:** Update to match your monitor:
```properties
brobot.capture.robot.expected-physical-width=2560
brobot.capture.robot.expected-physical-height=1440
```

## Best Practices

### 1. Always Test Your Resolution

```java
@PostConstruct
public void verifyResolution() {
    BufferedImage test = captureService.captureScreen();
    System.out.println("Capture resolution: " + 
        test.getWidth() + "×" + test.getHeight());
    
    if (test.getWidth() != 1920) {
        System.out.println("Warning: Not capturing at expected resolution!");
    }
}
```

### 2. Create Patterns at Consistent Resolution

- Always create pattern images at physical resolution
- Or always create them at logical resolution
- Be consistent across your pattern library

### 3. Document Your Setup

```properties
# This application expects patterns at 1920×1080 physical resolution
# Ensure capture is configured to match:
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
```

### 4. Handle Multiple Resolutions

```java
// Detect and adapt to different resolutions
int screenWidth = captureService.captureScreen().getWidth();

if (screenWidth == 1920) {
    // Full HD setup
} else if (screenWidth == 2560) {
    // 2K setup
} else if (screenWidth == 3840) {
    // 4K setup
}
```

## Platform-Specific Notes

### Windows
- Most likely to have DPI scaling enabled
- Check: Settings → Display → Scale and layout
- Common scales: 125%, 150%, 175%

### macOS
- Retina displays use 2× scaling
- JavaCV FFmpeg handles this well
- Consider using FFmpeg provider

### Linux
- Less common to have scaling issues
- Check with: `xrandr --current`
- Robot provider usually sufficient

## Summary

**The Problem:** Java 21 captures at logical resolution with DPI scaling

**The Solution:** Brobot automatically handles it via:
1. Robot provider with scaling compensation
2. FFmpeg provider with true physical capture
3. Configurable resolution expectations

**Quick Fix:**
```properties
# Just add this to application.properties:
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
```

Your pattern matching will work correctly regardless of DPI scaling! ✅
---
sidebar_position: 3
title: DPI and Resolution Guide
description: Understanding and handling DPI scaling in screen capture
keywords: [dpi, scaling, resolution, physical, logical, windows, javacv_ffmpeg]
---

# DPI Scaling and Resolution Guide

## Table of Contents
- [The Problem](#the-problem)
- [Understanding Resolution Types](#understanding-resolution-types)
- [Brobot's Two-Strategy Approach](#brobots-two-strategy-approach)
- [Strategy 1: Disable DPI (Current Default)](#strategy-1-disable-dpi-awareness-current-default-)
- [Strategy 2: Enable DPI with Auto-Detection](#strategy-2-enable-dpi-with-auto-detection-alternative)
- [Provider Comparison](#provider-comparison)
- [Configuration by Use Case](#configuration-by-use-case)
- [Code Examples](#code-examples)
- [Troubleshooting](#troubleshooting)
- [Migration Guide](#migration-guide)
- [Platform-Specific Notes](#platform-specific-notes)
- [Related Documentation](#related-documentation)

## The Problem

Java 21 introduced DPI awareness, which causes screen captures to return **logical resolution** instead of **physical resolution** when Windows display scaling is enabled.

### Example Scenario
- **Physical Monitor**: 1920×1080 pixels
- **Windows Scaling**: 125%
- **Java 21 Captures**: 1536×864 pixels (logical resolution)
- **Pattern Images**: 1920×1080 pixels (created in SikuliX IDE at physical resolution)
- **Result**: Pattern matching fails! ❌ (15-20% size mismatch)

This mismatch prevents pattern matching from working, as your patterns are at physical resolution but captures are at logical resolution.

## Understanding Resolution Types

### Physical Resolution
The **actual number of pixels** on your monitor hardware.
- **Examples**: 1920×1080 (Full HD), 2560×1440 (2K), 3840×2160 (4K)
- **What it means**: Real pixel count of your display
- **Java 8 behavior**: Always captures at physical resolution
- **SikuliX IDE**: Creates patterns at physical resolution (on Java 8)

### Logical Resolution
The **scaled resolution** that DPI-aware applications see.
- **Formula**: Physical Resolution ÷ Scaling Factor
- **Example**: 1920×1080 ÷ 1.25 = 1536×864
- **What it means**: Virtual resolution adjusted for readability
- **Java 21 behavior**: Captures at logical resolution by default

### Common Scaling Scenarios

| Windows Scaling | Physical Resolution | Logical Resolution | Scale Factor |
|-----------------|--------------------|--------------------|--------------|
| 100% (no scaling) | 1920×1080 | 1920×1080 | 1.0 |
| 125% | 1920×1080 | 1536×864 | 1.25 |
| 150% | 1920×1080 | 1280×720 | 1.5 |
| 175% | 1920×1080 | 1097×617 | 1.75 |
| 200% | 1920×1080 | 960×540 | 2.0 |

**Why it matters**: If your patterns are 1920×1080 but captures are 1536×864, pattern matching will fail due to size mismatch.

## Brobot's Two-Strategy Approach

Brobot provides **two distinct strategies** for handling DPI scaling, each with specific use cases:

### Strategy Comparison

| Aspect | Strategy 1: Disable DPI (Default) | Strategy 2: Enable DPI + Auto-Detect |
|--------|-----------------------------------|-------------------------------------|
| **DPI Awareness** | Disabled (`brobot.dpi.disable=true`) | Enabled (`brobot.dpi.disable=false`) |
| **Capture Resolution** | Physical (1920×1080) | Logical (1536×864 with 125% scaling) |
| **Pattern Scaling** | None needed (`resize-factor=1.0`) | Automatic (`resize-factor=auto`) |
| **Provider** | JAVACV_FFMPEG (default) | SIKULIX |
| **Pattern Match Accuracy** | 100% (exact pixel match) | ~95% (with scaling artifacts) |
| **Pattern Source** | SikuliX IDE, Windows Snipping Tool, any tool | SikuliX IDE |
| **Setup Complexity** | Simple (just use defaults) | Simple (change 3 properties) |
| **When to Use** | **Recommended for most users** | When you need adaptive scaling |

### Why Strategy 1 is Now Default

**Brobot changed from Strategy 2 to Strategy 1 as the default in 2024** because:
1. **100% Pattern Match**: No scaling artifacts, exact pixel-perfect matching
2. **Universal Compatibility**: Works with patterns from any source (SikuliX IDE, Snipping Tool, screenshots)
3. **Simpler Mental Model**: Physical captures match physical patterns, no conversion needed
4. **Faster Performance**: No runtime scaling calculations

**Strategy 2 is still available** for users who prefer automatic DPI adaptation.

---

## Strategy 1: Disable DPI Awareness (Current Default) ✅

### Configuration

```properties
# application.properties
# These are the current defaults - no configuration needed!
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

### How It Works

1. **System properties set early**: Brobot disables Java's DPI awareness during application startup
   - Sets `-Dsun.java2d.dpiaware=false` programmatically
   - Must happen before GUI initialization
2. **Java captures at physical resolution**: All screen captures return 1920×1080 (actual pixels)
3. **No pattern scaling**: Patterns used as-is (`resize-factor=1.0`)
4. **Perfect match**: Physical captures match physical patterns exactly

### Advantages

✅ **100% Pattern Match Accuracy** - Exact pixel-for-pixel matching
✅ **Works with Any Pattern Source** - SikuliX IDE, Snipping Tool, screenshots, etc.
✅ **No Scaling Artifacts** - No interpolation blur or edge distortion
✅ **Simple Configuration** - Just use the defaults
✅ **Better Performance** - No runtime scaling overhead

### Use Cases

- ✅ Creating new Brobot projects (recommended default)
- ✅ Using patterns from Windows Snipping Tool or other tools
- ✅ Need 100% pattern match accuracy
- ✅ Working with high-resolution monitors
- ✅ Production environments where reliability is critical

### Example

```properties
# No configuration needed - these are already the defaults!
# But you can explicitly set them for clarity:

brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

---

## Strategy 2: Enable DPI with Auto-Detection (Alternative)

### Configuration

```properties
# application.properties
# Enable this strategy by changing these properties:
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

### How It Works

1. **DPI awareness enabled**: Java captures at logical resolution (e.g., 1536×864 with 125% scaling)
2. **Auto-detection**: Brobot detects the scaling factor using `GraphicsConfiguration.getDefaultTransform()`
3. **Pattern scaling**: Patterns automatically scaled by inverse factor (0.8x for 125% scaling)
4. **Match**: Scaled patterns match logical captures

### Advantages

✅ **Automatic Adaptation** - Works across different DPI settings without reconfiguration
✅ **Dynamic Scaling** - Adapts when user changes Windows scaling settings
✅ **No System Property Changes** - Doesn't modify JVM flags

### Disadvantages

⚠️ **~95% Accuracy** - Scaling introduces interpolation artifacts
⚠️ **Pattern Source Limited** - Works best with SikuliX IDE patterns only
⚠️ **Performance Overhead** - Runtime pattern scaling calculations

### Use Cases

- ✅ Need to support multiple DPI settings dynamically
- ✅ Users frequently change Windows scaling
- ✅ Using only SikuliX IDE-created patterns
- ✅ Migrating from older Brobot versions that used this approach

### Example

```properties
# Enable DPI awareness and auto-detection
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

---

## Provider Comparison

### Physical Resolution Providers

#### 1. JAVACV_FFMPEG (Default with Strategy 1) ✅

```properties
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true  # Required for physical capture
```

**How it works**: Uses bundled JavaCV/FFmpeg to capture at physical resolution directly.

**Advantages**:
- ✅ Always captures at physical resolution (1920×1080)
- ✅ No external dependencies (JavaCV bundled)
- ✅ Works perfectly with Strategy 1
- ✅ Default provider for good reason

**When to use**: **This is the recommended default.** Use it unless you have a specific need for another provider.

#### 2. Robot Provider with Physical Scaling

```properties
brobot.capture.provider=ROBOT
brobot.capture.robot.scale-to-physical=true
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
brobot.dpi.disable=false  # Can work with DPI enabled
```

**How it works**:
1. Captures at logical resolution (e.g., 1536×864)
2. Detects DPI scaling factor dynamically
3. Scales captured image up to physical resolution (1920×1080)

**Advantages**:
- ✅ Dynamic DPI detection
- ✅ Can adapt to different monitors
- ✅ No external dependencies

**Disadvantages**:
- ⚠️ Scaling introduces minor artifacts
- ⚠️ Requires configuring expected physical resolution

**When to use**: When you need dynamic DPI adaptation or can't use JAVACV_FFMPEG.

#### 3. External FFmpeg Provider

```properties
brobot.capture.provider=FFMPEG  # External FFmpeg binary
```

**How it works**: Uses external FFmpeg installation (must be installed separately).

**Advantages**:
- ✅ Physical resolution capture
- ✅ Can use latest FFmpeg features

**Disadvantages**:
- ❌ Requires separate FFmpeg installation
- ❌ Platform-specific setup

**When to use**: When you have FFmpeg installed and need its specific features.

### Logical Resolution Providers

#### 4. SikuliX Provider (For Strategy 2)

```properties
brobot.capture.provider=SIKULIX
brobot.dpi.disable=false  # Must be false for Strategy 2
brobot.dpi.resize-factor=auto
```

**How it works**: Uses SikuliX native capture (Java version dependent).

**Resolution behavior**:
- Java 8: Captures at physical resolution (1920×1080)
- Java 21: Captures at logical resolution (1536×864 with scaling)

**When to use**: When using Strategy 2 (Enable DPI + Auto-Detection).

---

## Configuration by Use Case

### Development Machine (Recommended)

```properties
# Use defaults - optimal for most development
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

### High-DPI Laptop (125-200% Windows Scaling)

```properties
# Strategy 1 works perfectly with any scaling
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

### 4K Monitor with Scaling

```properties
# Same approach - DPI disable handles all resolutions
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

### Multi-Monitor Setup (Different DPI Settings)

```properties
# JAVACV_FFMPEG handles multi-monitor well
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
```

### CI/CD Environment

```properties
# Auto-detect best provider, prefer physical resolution
brobot.capture.provider=AUTO
brobot.capture.prefer-physical=true
brobot.dpi.disable=true
```

### Need Dynamic DPI Adaptation

```properties
# Use Strategy 2 for runtime DPI changes
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

---

## Code Examples

### Required Imports

```java
// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

// Brobot
import io.github.jspinak.brobot.capture.CaptureConfiguration;
import io.github.jspinak.brobot.capture.UnifiedCaptureService;

// Java AWT
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

// Java IO
import java.io.IOException;
```

### Check Current Configuration

```java
import io.github.jspinak.brobot.capture.CaptureConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResolutionChecker {

    @Autowired
    private CaptureConfiguration captureConfig;

    public void printConfiguration() {
        // Print comprehensive configuration report
        captureConfig.printConfigurationReport();

        // Check specific settings
        System.out.println("Current provider: " + captureConfig.getCurrentProvider());
        System.out.println("Capturing physical resolution: " +
                         captureConfig.isCapturingPhysicalResolution());
    }
}
```

### Manual DPI Detection

```java
import java.awt.*;

public class DPIDetector {

    public static void detectCurrentDPI() {
        // Get logical screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Logical Resolution: " +
                         screenSize.width + "×" + screenSize.height);

        // Get graphics configuration
        GraphicsConfiguration gc = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();

        // Get DPI scaling factor
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        System.out.println("Scale Factor: " + scaleX + " (x) " + scaleY + " (y)");

        // Calculate physical resolution
        int physicalWidth = (int)(screenSize.width * scaleX);
        int physicalHeight = (int)(screenSize.height * scaleY);
        System.out.println("Physical Resolution: " +
                         physicalWidth + "×" + physicalHeight);

        // Determine Windows scaling percentage
        int scalingPercent = (int)(scaleX * 100);
        System.out.println("Windows Scaling: " + scalingPercent + "%");
    }
}
```

### Verify Capture Resolution at Startup

```java
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class CaptureVerifier {

    @Autowired
    private UnifiedCaptureService captureService;

    @PostConstruct
    public void verifyResolution() {
        try {
            BufferedImage testCapture = captureService.captureScreen();
            int width = testCapture.getWidth();
            int height = testCapture.getHeight();

            System.out.println("Capture Resolution: " + width + "×" + height);

            // Verify against expected physical resolution
            if (width != 1920 || height != 1080) {
                System.out.println("⚠️  Warning: Not capturing at expected 1920×1080!");
                System.out.println("Check your DPI and capture provider settings.");
            } else {
                System.out.println("✅ Capturing at physical resolution correctly.");
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to verify resolution: " + e.getMessage());
        }
    }
}
```

### Troubleshooting Capture Issues

```java
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class CaptureTroubleshooter {

    @Autowired
    private UnifiedCaptureService captureService;

    public void diagnoseResolution() {
        try {
            BufferedImage capture = captureService.captureScreen();
            int width = capture.getWidth();
            int height = capture.getHeight();

            System.out.println("Captured: " + width + "×" + height);

            // Diagnose common issues
            if (width == 1536 && height == 864) {
                System.out.println("❌ Problem: Capturing at logical resolution");
                System.out.println("Solution: Set brobot.dpi.disable=true");
            } else if (width == 1920 && height == 1080) {
                System.out.println("✅ Capturing at physical resolution correctly");
            } else {
                System.out.println("⚠️  Unexpected resolution");
                System.out.println("Expected: 1920×1080 or 1536×864");
                System.out.println("Check your monitor's native resolution");
            }
        } catch (IOException e) {
            System.err.println("Capture failed: " + e.getMessage());
        }
    }
}
```

### Handle Multiple Resolutions

```java
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class MultiResolutionHandler {

    @Autowired
    private UnifiedCaptureService captureService;

    public void detectAndAdapt() {
        try {
            BufferedImage capture = captureService.captureScreen();
            int screenWidth = capture.getWidth();
            int screenHeight = capture.getHeight();

            System.out.println("Detected: " + screenWidth + "×" + screenHeight);

            if (screenWidth == 1920 && screenHeight == 1080) {
                System.out.println("Full HD (1080p) setup");
                // Load Full HD patterns
            } else if (screenWidth == 2560 && screenHeight == 1440) {
                System.out.println("2K (1440p) setup");
                // Load 2K patterns
            } else if (screenWidth == 3840 && screenHeight == 2160) {
                System.out.println("4K (2160p) setup");
                // Load 4K patterns
            } else {
                System.out.println("Custom resolution: " + screenWidth + "×" + screenHeight);
                // Handle custom resolution
            }
        } catch (IOException e) {
            System.err.println("Failed to detect resolution: " + e.getMessage());
        }
    }
}
```

---

## Troubleshooting

### Problem: Pattern matching fails after upgrading to Java 21

**Symptoms**:
- Patterns worked in Java 8/11 but fail in Java 21
- Console shows resolution mismatch (1536×864 vs 1920×1080)

**Diagnosis**:
```java
BufferedImage capture = captureService.captureScreen();
System.out.println("Captured: " + capture.getWidth() + "×" + capture.getHeight());
// If this shows 1536×864 but your patterns are 1920×1080, you have DPI issues
```

**Solution**: Use Strategy 1 (default configuration)
```properties
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

---

### Problem: Captures are blurry after scaling

**Symptoms**:
- Captured images look pixelated or blurry
- Pattern match accuracy is reduced (<95%)

**Cause**: You're using Strategy 2 with pattern scaling, which introduces artifacts.

**Solution**: Switch to Strategy 1 for pixel-perfect captures
```properties
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

---

### Problem: Wrong resolution captured

**Symptoms**:
- Captures are 1280×720 instead of expected 1920×1080
- Multiple monitors cause unexpected resolutions

**Diagnosis**:
```java
captureConfig.printConfigurationReport();
// Check: Are you capturing the correct monitor?
// Check: What's the configured expected physical resolution?
```

**Solution 1**: Verify monitor's native resolution
- Windows: Settings → Display → Advanced scaling settings
- Look for "native resolution" or "recommended resolution"

**Solution 2**: Update expected resolution for Robot provider
```properties
brobot.capture.robot.expected-physical-width=1920
brobot.capture.robot.expected-physical-height=1080
```

**Solution 3**: Use JAVACV_FFMPEG which auto-detects resolution
```properties
brobot.capture.provider=JAVACV_FFMPEG
```

---

### Problem: DPI disable doesn't work

**Symptoms**:
- Set `brobot.dpi.disable=true` but still capturing logical resolution
- Still getting 1536×864 instead of 1920×1080

**Cause**: DPI awareness must be disabled BEFORE GUI initialization.

**Solution**: Ensure Brobot initializes early
```java
// This should happen automatically via:
// io.github.jspinak.brobot.config.dpi.DPIApplicationContextInitializer

// If it doesn't work, check your Spring Boot configuration
// Ensure Brobot auto-configuration is enabled
```

**Workaround**: Set JVM property manually
```bash
java -Dsun.java2d.dpiaware=false -jar your-application.jar
```

---

### Problem: Need to support both strategies

**Scenario**: Some users need Strategy 1, others need Strategy 2

**Solution**: Use profiles
```properties
# application.properties (default - Strategy 1)
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

```properties
# application-adaptive.properties (Strategy 2)
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

Users can activate with:
```bash
java -Dspring.profiles.active=adaptive -jar your-application.jar
```

---

## Migration Guide

### Migrating from Old Brobot Versions (Pre-2024)

If you're upgrading from an older Brobot version that used Strategy 2 by default:

#### What Changed

**Old Default (Pre-2024)**:
```properties
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

**New Default (2024+)**:
```properties
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

#### Why Changed

1. **100% vs ~95% accuracy**: Physical capture provides perfect pixel matching
2. **Pattern source flexibility**: Works with any tool (Snipping Tool, SikuliX IDE, etc.)
3. **No scaling artifacts**: Cleaner, crisper captures
4. **Simpler mental model**: Physical resolution = physical patterns

#### Migration Options

**Option 1: Adopt New Defaults (Recommended)**

1. Remove any old DPI configuration from your `application.properties`
2. Let Brobot use new defaults
3. Test pattern matching - should work automatically

If you need to verify:
```properties
# Explicitly set new defaults (optional - these are defaults anyway)
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
brobot.capture.provider=JAVACV_FFMPEG
```

**Option 2: Keep Old Strategy**

If you prefer the old auto-detection approach:
```properties
# Explicitly enable Strategy 2
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

**Your patterns and code don't need changes** - just configuration.

#### Pattern Considerations

**If your patterns were created with Strategy 2** (DPI enabled + auto):
- They may be at logical resolution (1536×864)
- Option A: Re-capture at physical resolution (recommended)
- Option B: Keep using Strategy 2 configuration

**If your patterns were created in SikuliX IDE**:
- They're already at physical resolution
- New defaults will work perfectly

---

## Best Practices

### 1. Always Verify Resolution at Startup

Add a verification component to your application:

```java
@Component
public class StartupVerifier {
    @Autowired
    private UnifiedCaptureService captureService;

    @PostConstruct
    public void verify() {
        try {
            BufferedImage test = captureService.captureScreen();
            System.out.println("✅ Capture resolution: " +
                             test.getWidth() + "×" + test.getHeight());
        } catch (IOException e) {
            System.err.println("❌ Startup verification failed");
        }
    }
}
```

### 2. Document Your Pattern Source

In your project's README:
```markdown
## Pattern Resolution

This project uses patterns captured at **1920×1080 physical resolution**.

Patterns were created using:
- SikuliX IDE on Java 8 (physical resolution)
- Windows Snipping Tool at 100% zoom
- Direct screenshots with no scaling

Brobot is configured for physical resolution capture (Strategy 1).
```

### 3. Create Patterns Consistently

**Best practice**: Always create patterns at physical resolution
- Use SikuliX IDE on Java 8, OR
- Use Windows Snipping Tool with 100% display scaling, OR
- Use any tool but ensure Windows scaling is 100%

**Why**: Consistency ensures all patterns match the same resolution

### 4. Test on Multiple Environments

```java
@Test
public void testResolutionConsistency() throws IOException {
    BufferedImage capture = captureService.captureScreen();

    // Verify expected resolution
    assertEquals(1920, capture.getWidth(),
                "Width should be 1920 (physical resolution)");
    assertEquals(1080, capture.getHeight(),
                "Height should be 1080 (physical resolution)");
}
```

### 5. Handle Multi-Monitor Setups

```properties
# For multi-monitor, JAVACV_FFMPEG handles it best
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
```

### 6. Use Configuration Properties, Not Code

❌ **Avoid**:
```java
// Don't set DPI properties in code
System.setProperty("sun.java2d.dpiaware", "false");
```

✅ **Prefer**:
```properties
# Use Brobot properties - they handle timing correctly
brobot.dpi.disable=true
```

---

## Platform-Specific Notes

### Windows

**DPI Scaling Settings**:
- Location: Settings → System → Display → Scale and layout
- Common values: 100%, 125%, 150%, 175%, 200%
- Recommendation: Use 100% for pattern creation, any value for runtime

**With Strategy 1** (default):
- ✅ Works with any scaling (100%-200%)
- ✅ Always captures at physical resolution
- ✅ No manual configuration needed

**With Strategy 2** (adaptive):
- ✅ Automatically adapts to scaling changes
- ⚠️ Pattern accuracy ~95% due to scaling

### macOS

**Retina Displays**:
- Use 2× scaling by default
- Physical resolution often different from advertised resolution
- Example: "2560×1600 Retina" may actually be 5120×3200 scaled to 2560×1600

**Recommendation**:
```properties
# JAVACV_FFMPEG handles Retina displays well
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
```

**Check your actual resolution**:
```bash
# macOS command
system_profiler SPDisplaysDataType | grep Resolution
```

### Linux

**DPI Scaling**:
- Less common than Windows/macOS
- Usually 100% (1.0) scaling
- Some desktop environments (GNOME, KDE) support scaling

**Check DPI settings**:
```bash
# X11
xrandr --current | grep " connected"

# Wayland
echo $GDK_SCALE
echo $GDK_DPI_SCALE
```

**Recommendation**:
```properties
# Robot or JAVACV_FFMPEG both work well on Linux
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
```

---

## Summary

### The Problem
Java 21 introduced DPI awareness, causing mismatches between pattern resolution and capture resolution on high-DPI displays.

### The Solution
Brobot provides two strategies:

**Strategy 1 (Default)**: Disable DPI Awareness
- Captures at physical resolution
- 100% pattern match accuracy
- Works with patterns from any source
- **Recommended for most users**

**Strategy 2 (Alternative)**: Enable DPI with Auto-Detection
- Captures at logical resolution
- Automatically scales patterns
- ~95% pattern match accuracy
- Useful for dynamic DPI environments

### Quick Start

**For new projects** - Just use the defaults:
```properties
# No configuration needed - these are already defaults:
# brobot.dpi.disable=true
# brobot.dpi.resize-factor=1.0
# brobot.capture.provider=JAVACV_FFMPEG
```

**For adaptive DPI** - Enable Strategy 2:
```properties
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
brobot.capture.provider=SIKULIX
```

### Your Pattern Matching Will Work! ✅

With correct configuration, Brobot handles DPI scaling transparently, ensuring reliable pattern matching regardless of display scaling settings.

---

## Related Documentation

### Core Capture Documentation
- **[Capture Quick Reference](./capture-quick-reference.md)** - Quick configuration guide for capture providers
- **[Capture Methods Comparison](../tools/capture-methods-comparison.md)** - Detailed performance analysis of all providers
- **[Modular Capture System Guide](./modular-capture-system.md)** - Complete examples with class context

### Configuration
- **[Configuration Properties Reference](../configuration/properties-reference.md)** - Complete reference for all Brobot properties
- **[Auto-Configuration](../configuration/auto-configuration.md)** - Spring Boot integration details

### Testing
- **[Testing Introduction](../../04-testing/testing-intro.md)** - Testing patterns and strategies
- **[Integration Testing](../../04-testing/integration-testing.md)** - Spring Boot integration testing
- **[CI/CD Testing Guide](../testing/ci-cd-testing.md)** - Testing in CI/CD pipelines

### Getting Started
- **[Introduction](../../01-getting-started/introduction.md)** - Why Brobot and core concepts
- **[Installation](../../01-getting-started/installation.md)** - Adding Brobot to your project

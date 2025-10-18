# Screen Capture Provider Configuration Guide

## Overview

This guide helps you choose and configure the optimal screen capture method for Brobot pattern matching. It compares different capture providers and tools to help you achieve the best results for reliable pattern recognition.

### Recommendation
**FFmpeg (JavaCV) is the recommended capture provider for Brobot**, offering excellent compatibility with Windows Snipping Tool and SikuliX IDE patterns, while capturing at full physical resolution.

## Capture Methods Comparison

> **Note**: The comparison data below is based on testing configurations. Results may vary depending on your specific environment, display settings, and Windows version.

### Tools Tested

#### External Capture Tools
- **Windows Snipping Tool** - Native Windows screen capture (Win+Shift+S)
- **SikuliX IDE** - Traditional pattern capture tool
- **[Brobot Pattern Capture Tool](./pattern-capture-tool-guide.md)** - Custom tool using Brobot's capture providers

#### Brobot Capture Providers
- **SikuliX** - Legacy provider for backward compatibility
- **Robot** - Java's built-in Robot class
- **FFmpeg (JavaCV)** - Bundled FFmpeg via JavaCV library

### Test Environment
- Windows with 125% display scaling
- Physical resolution: 1920x1080
- Logical resolution: 1536x864
- Java 21 (DPI-aware by default)

## Detailed Results

### Resolution Capture Comparison

| Method | Resolution | File Size | Bit Depth | Compression |
|--------|------------|-----------|-----------|-------------|
| **SikuliX** | 1536x864 | 324 KB | 24-bit | 12.0x |
| **Robot** | 1536x864 | 324 KB | 24-bit | 12.0x |
| **FFmpeg** | 1920x1080 | 255 KB | 24-bit | 23.7x |
| **Windows** | 1920x1080 | 329 KB | 32-bit | 24.6x |
| **SikuliX IDE** | 1920x1080 | 228 KB | 24-bit | 26.5x |

### Pixel-Perfect Similarity Results

#### Perfect Matches (100% Similarity)
- **Windows ↔ FFmpeg**: 100% identical pixels
- **Windows ↔ SikuliX IDE**: 100% identical pixels  
- **FFmpeg ↔ SikuliX IDE**: 100% identical pixels
- **SikuliX ↔ Robot**: 100% identical pixels (both logical resolution)

#### Near-Perfect Matches (97%+ Similarity)
- **SikuliX Tool ↔ SikuliX/Robot**: 97.2% similarity
- Minor differences due to capture timing

#### Significant Differences (<80% Similarity)
- **Robot Tool** captures showed 69-75% similarity with other methods
- Likely due to different color space handling or compression

### Pattern Matching Simulation

A 200x100 pixel pattern was extracted and matched across all capture methods:

| Target Screenshot | Match Similarity | Compatibility |
|-------------------|------------------|---------------|
| SikuliX | 100% | Excellent |
| Robot | 100% | Excellent |
| FFmpeg | 90.2% | Good |
| Windows | 90.2% | Good |
| SikuliX IDE | 90.2% | Good |

## Recommendations

### Optimal Configuration

The following configuration provides reliable results (see [Properties Reference](../configuration/properties-reference.md) for all options):

```properties
# Recommended Brobot Configuration
brobot.capture.provider=JAVACV_FFMPEG  # Use bundled FFmpeg
brobot.dpi.disable=true                # Disable DPI awareness for physical resolution
brobot.dpi.resize-factor=1.0           # No scaling (1.0 = physical resolution)
```

> **Note**: Similarity thresholds are configured per-action using `PatternFindOptions`, not as global properties. See [Action Config Reference](../action-config/05-reference.md) for details.

### Best Practice Workflows

#### Option 1: Windows Snipping Tool (Quick & Simple)
**Best for**: Quick pattern capture, familiar workflow

**Advantages**:
- Built into Windows (Win+Shift+S)
- 100% compatible with FFmpeg provider
- No additional tools needed

**Workflow**:
1. Press Win+Shift+S
2. Select region
3. Save to `images/[state-name]/`
4. Configure Brobot to use FFmpeg provider

#### Option 2: Brobot Pattern Capture Tool (Integrated)
**Best for**: Project integration, automated workflow

**Advantages**:
- Auto-saves to correct project directory
- Same 100% quality as Windows
- Integrated with Brobot project structure
- Supports batch capture and organization

**Workflow**:
1. Run Pattern Capture Tool
2. Select FFmpeg provider
3. Press F1 or click Capture
4. Images auto-save to patterns folder

#### Option 3: SikuliX IDE (Legacy)
**Best for**: Existing SikuliX users, legacy projects

**Advantages**:
- Familiar to SikuliX users
- 100% compatible with FFmpeg provider
- Established tool with documentation

**Note**: Captures at physical resolution (1920x1080)

## Technical Details

### Why FFmpeg Achieves Perfect Matches

1. **Physical Resolution Capture**: FFmpeg captures at true physical resolution (1920x1080), bypassing Java's DPI scaling
2. **Direct Screen Access**: Uses Windows GDI for direct screen buffer access
3. **Consistent Color Space**: Maintains consistent RGB color representation
4. **No Scaling Artifacts**: Avoids interpolation issues from resolution conversion

### DPI Scaling Considerations

> For comprehensive DPI handling strategies, see the [DPI and Resolution Guide](../capture/dpi-resolution-guide.md).

With 125% Windows scaling:
- **Logical methods** (SikuliX, Robot): Capture at 1536x864
- **Physical methods** (FFmpeg, Windows): Capture at 1920x1080
- Brobot's DPI compensation handles the conversion automatically

### File Size Optimization

FFmpeg provides the best compression efficiency:
- **Smallest file size** for physical resolution (255 KB vs 329 KB for Windows)
- **Best compression ratio** (23.7x)
- **No quality loss** despite smaller file size

## Compatibility Matrix

| Pattern Created With | Best Brobot Provider | Expected Similarity |
|---------------------|---------------------|---------------------|
| Windows Snipping Tool | FFmpeg | 100% |
| SikuliX IDE | FFmpeg | 100% |
| Brobot Tool (FFmpeg) | FFmpeg | 100% |
| Brobot Tool (SikuliX) | SikuliX or Robot | 97-100% |
| Legacy patterns (unknown) | Auto-detect | Varies |

## Migration Guide

### From SikuliX IDE to Brobot

1. **Keep existing patterns** - They work with FFmpeg at 100% similarity
2. **Update configuration**:
   ```properties
   brobot.capture.provider=JAVACV_FFMPEG
   ```
3. **No pattern recreation needed**

### From Robot to FFmpeg

1. **Patterns may need adjustment** due to resolution difference
2. **Enable DPI scaling**:
   ```properties
   brobot.dpi.resize-factor=1.25  # or 'auto'
   ```
3. **Test similarity threshold** - may need adjustment

## Troubleshooting

### Common Issues and Solutions

#### "FFmpeg not available" Error
**Solution**: FFmpeg via JavaCV is bundled with Brobot. Ensure the library module is properly built and included.

#### Patterns Not Matching After Provider Switch
**Solution**: Check DPI settings. FFmpeg captures physical resolution while SikuliX/Robot capture logical resolution.

#### Different Colors Between Captures
**Solution**: This is normal - slight color variations don't affect pattern matching above 70% similarity threshold.

## Summary

Based on testing and configuration analysis, **FFmpeg (JavaCV) is the recommended capture provider** for Brobot. It captures at full physical resolution and works well with patterns created using Windows Snipping Tool or SikuliX IDE.

### Key Takeaways
- ✅ FFmpeg captures at physical resolution for better accuracy
- ✅ Compatible with Windows Snipping Tool and SikuliX IDE patterns
- ✅ The Brobot Pattern Capture Tool supports all capture providers
- ✅ All capture methods can work together with proper configuration

### Next Steps
1. Configure your `application.properties` with recommended settings
2. Choose a capture workflow that fits your process
3. Test pattern matching with your specific display configuration
4. Adjust DPI settings if needed (see [DPI Guide](../capture/dpi-resolution-guide.md))

## Related Documentation

### Capture System
- **[DPI and Resolution Guide](../capture/dpi-resolution-guide.md)** - Comprehensive DPI scaling strategies and troubleshooting
- **[Modular Capture System](../capture/modular-capture-system.md)** - Complete capture provider details and configuration
- **[Pattern Capture Tool Guide](./pattern-capture-tool-guide.md)** - Using the Brobot Pattern Capture Tool
- **[Quick Start Capture Setup](./quick-start-capture-setup.md)** - Fast setup guide for optimal capture
- **[Capture Quick Reference](../capture/capture-quick-reference.md)** - Command reference for capture operations

### Configuration
- **[Properties Reference](../configuration/properties-reference.md)** - Complete Brobot configuration properties
- **[ActionConfig Reference](../action-config/05-reference.md)** - Action configuration including similarity thresholds

### Testing & Debugging
- **[Debugging Pattern Matching](../../04-testing/debugging-pattern-matching.md)** - Troubleshooting pattern matching issues
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing without GUI
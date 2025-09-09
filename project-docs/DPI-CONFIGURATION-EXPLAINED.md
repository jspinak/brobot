# DPI Configuration Explained

## Overview

Brobot now has automatic DPI detection and compensation enabled by default. This ensures pattern matching works correctly regardless of Windows display scaling settings.

## Default Configuration

```properties
# These are the new defaults in brobot-defaults.properties:
brobot.dpi.disable=false        # Keep DPI awareness enabled
brobot.dpi.resize-factor=auto    # Auto-detect and compensate for scaling
brobot.capture.provider=SIKULIX  # Use SikuliX for capture
```

## How It Works

### Step 1: DPI Awareness Enabled
When `brobot.dpi.disable=false`:
- Java 21 remains DPI-aware (its default behavior)
- Screen captures happen at **logical resolution**
- Example: With 125% Windows scaling, captures are 1536×864 instead of 1920×1080

### Step 2: Auto-Detection
When `brobot.dpi.resize-factor=auto`:
- DPIAutoDetector examines the graphics transform
- Detects Windows scaling percentage (100%, 125%, 150%, etc.)
- Calculates pattern resize factor

### Step 3: Pattern Scaling
The detected scaling is applied to patterns:
- 125% Windows scaling → 0.8x pattern resize
- 150% Windows scaling → 0.667x pattern resize
- 100% scaling → 1.0x (no resize)

### Step 4: Successful Matching
- Patterns (created at physical resolution) are scaled down
- They now match the logical resolution captures
- Pattern matching works! ✅

## Example Scenario

**Windows Settings:**
- Display: 1920×1080 physical resolution
- Scaling: 125%

**What Happens:**
1. Java captures at 1536×864 (logical resolution)
2. DPIAutoDetector detects 125% scaling
3. Sets pattern resize factor to 0.8 (1/1.25)
4. Your 100×100 pattern is scaled to 80×80
5. Pattern matching succeeds!

## Configuration Options

### Option 1: Automatic (Default - Recommended)
```properties
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
```
- Automatically detects and compensates for DPI scaling
- Works with any Windows scaling setting
- No manual configuration needed

### Option 2: Manual Scaling
```properties
brobot.dpi.disable=false
brobot.dpi.resize-factor=0.8  # For 125% Windows scaling
```
- Manually specify the resize factor
- Useful if auto-detection doesn't work

### Option 3: Force Physical Resolution (Legacy)
```properties
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
```
- Disables DPI awareness entirely
- Forces captures at physical resolution
- May cause issues with modern applications

## Troubleshooting

### Problem: "DPI awareness DISABLED" message
This means `brobot.dpi.disable=true` is set somewhere.

**Solution:** Ensure these settings:
```properties
brobot.dpi.disable=false
brobot.dpi.resize-factor=auto
```

### Problem: Pattern matching fails
Check the console output for DPI detection results:
```
[Brobot DPI Detection]
  Logical resolution: 1536x864
  Transform scale: 1.25x1.25
  Physical resolution: 1920x1080
  Windows DPI scaling: 125%
  Pattern scale factor: 0.8
```

### Problem: Auto-detection not working
The transform scale shows as 1.0 even with Windows scaling.

**Solution:** Try manual configuration:
```properties
# For 125% scaling
brobot.dpi.resize-factor=0.8

# For 150% scaling
brobot.dpi.resize-factor=0.667
```

## Common Scaling Factors

| Windows Scaling | Pattern Resize Factor | Property Value |
|----------------|----------------------|----------------|
| 100% | 1.0 | `1.0` or `auto` |
| 125% | 0.8 | `0.8` or `auto` |
| 150% | 0.667 | `0.667` or `auto` |
| 175% | 0.571 | `0.571` or `auto` |
| 200% | 0.5 | `0.5` or `auto` |

## Key Points

✅ **DPI awareness must be enabled** for auto-detection to work  
✅ **Auto resize-factor** handles scaling automatically  
✅ **SikuliX provider** works best with this configuration  
✅ **No manual configuration needed** for most users  

## Migration from Old Configuration

If you previously had:
```properties
brobot.dpi.disable=true  # Old default
```

Change to:
```properties
brobot.dpi.disable=false  # New default for auto-detection
```

The system will now automatically detect and compensate for DPI scaling!
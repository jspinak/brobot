# Pattern Creation Tools Guide

## Overview
This guide helps you choose the best tool for creating pattern images that work with Brobot's pattern matching system. The choice of pattern creation tool directly impacts the accuracy of your UI automation.

## Recommended Tools

### 🏆 Optimal Tools (100% Similarity)
These tools provide perfect pattern matching when used with Brobot's default configuration:

#### 1. **Windows Snipping Tool** 
- **Resolution**: 1920x1080 (physical)
- **Similarity Score**: 100% with Brobot's JAVACV_FFMPEG capture
- **Platform**: Windows only
- **How to use**:
  1. Press `Win + Shift + S` to open Snipping Tool
  2. Select the area you want to capture
  3. Save as PNG in your project's `images/` directory

#### 2. **SikuliX IDE**
- **Resolution**: 1920x1080 (physical)
- **Similarity Score**: 100% with Brobot's JAVACV_FFMPEG capture
- **Platform**: Cross-platform (Windows, Mac, Linux)
- **How to use**:
  1. Open SikuliX IDE
  2. Click the camera icon or use the capture shortcut
  3. Select the pattern area
  4. Export/save as PNG to your project's `images/` directory

#### 3. **Brobot FFmpeg Tool** *(Coming Soon)*
- **Resolution**: 1920x1080 (physical)
- **Similarity Score**: 100% with Brobot's JAVACV_FFMPEG capture
- **Platform**: Cross-platform
- **How to use**: 
  - Standalone tool that saves patterns directly to configured directory
  - Captures at physical resolution using FFmpeg
  - Perfect compatibility with Brobot

### ✅ Good Alternative (95% Similarity)
#### **Custom SikuliX-based Tool**
- **Resolution**: 1536x864 (logical)
- **Similarity Score**: 95% with Brobot's SikuliX/Robot capture
- **Platform**: Cross-platform
- **Note**: Requires changing Brobot configuration (see below)

### ⚠️ Not Recommended (< 70% Similarity)
- Screenshot tools that apply compression or filters
- Browser-based screenshot extensions
- Tools that capture at non-standard resolutions
- Robot-based custom tools (typically 59-69% similarity)

## Brobot Configuration

### Default Configuration (Optimal)
The default Brobot configuration is optimized for patterns created with Windows Snipping Tool, SikuliX IDE, or Brobot FFmpeg Tool:

```properties
# Already configured in brobot-defaults.properties
brobot.capture.provider=JAVACV_FFMPEG
brobot.dpi.disable=true
brobot.dpi.resize-factor=1.0
```

This configuration:
- Captures at physical resolution (1920x1080)
- Disables DPI awareness to avoid scaling issues
- Uses no pattern scaling for 1:1 pixel matching
- Provides 100% similarity with recommended tools

### Alternative Configuration (For Logical Resolution Tools)
If you're using tools that capture at logical resolution (1536x864 with 125% DPI scaling):

```properties
# application.properties
brobot.capture.provider=SIKULIX
brobot.dpi.disable=false
brobot.dpi.resize-factor=1.0
```

## Pattern Creation Workflow

### Step 1: Choose Your Tool
1. **For best results**: Use Windows Snipping Tool or SikuliX IDE
2. **For automation**: Wait for Brobot FFmpeg Tool release
3. **If you have existing patterns**: Check their resolution and adjust configuration

### Step 2: Capture Patterns
1. Ensure your display is at the target resolution
2. Capture UI elements with your chosen tool
3. Save as PNG files (avoid JPEG to prevent compression artifacts)
4. Use descriptive names: `button-submit.png`, `field-username.png`

### Step 3: Organize Patterns
```
your-project/
├── images/
│   ├── login-page/
│   │   ├── button-login.png
│   │   ├── field-username.png
│   │   └── field-password.png
│   └── main-menu/
│       ├── menu-file.png
│       └── menu-edit.png
```

### Step 4: Verify Pattern Matching
```java
// Test your patterns
@Autowired
private Action action;

StateImage pattern = new StateImage.Builder()
    .withImage("button-login.png")
    .build();

ActionResult result = action.find(pattern);
System.out.println("Similarity: " + result.getMaxSimilarity());
// Should be > 0.94 for optimal tools
```

## Troubleshooting

### Low Similarity Scores
If you're getting similarity scores below 90%:

1. **Check Resolution Mismatch**
   - Pattern: 1920x1080, Capture: 1536x864 → ~77% similarity
   - Solution: Use matching capture provider or enable scaling

2. **Check DPI Settings**
   ```bash
   # In your application.properties
   brobot.dpi.disable=true  # For physical resolution
   ```

3. **Verify Tool Output**
   - Some tools apply compression or filters
   - Always save as PNG, not JPEG
   - Check file size - unusually small files may be compressed

### Pattern Not Found
1. Lower similarity threshold if needed:
   ```properties
   brobot.action.similarity=0.85  # Default is 0.95
   ```

2. Check if pattern is unique enough:
   - Avoid capturing too small areas
   - Include distinctive features
   - Avoid areas with changing content

## Resolution Compatibility Table

| Pattern Tool | Resolution | Best Brobot Provider | Expected Similarity |
|-------------|------------|---------------------|---------------------|
| Windows Snipping Tool | 1920x1080 | JAVACV_FFMPEG | 100% |
| SikuliX IDE | 1920x1080 | JAVACV_FFMPEG | 100% |
| Brobot FFmpeg Tool | 1920x1080 | JAVACV_FFMPEG | 100% |
| Custom SikuliX Tool | 1536x864 | SIKULIX | 95% |
| Custom Robot Tool | 1536x864 | ROBOT | 69% |

## Best Practices

### DO:
- ✅ Use PNG format for patterns
- ✅ Capture at the same resolution you'll run automation
- ✅ Include unique visual elements in patterns
- ✅ Test patterns immediately after creation
- ✅ Organize patterns by screen/state

### DON'T:
- ❌ Use JPEG format (compression artifacts)
- ❌ Capture patterns with transparency
- ❌ Include dynamic content (timestamps, counters)
- ❌ Make patterns too small (< 20x20 pixels)
- ❌ Mix patterns from different tools without testing

## Platform-Specific Notes

### Windows
- Windows Snipping Tool is built-in and optimal
- Captures at physical resolution regardless of DPI scaling
- Perfect compatibility with default Brobot configuration

### macOS
- Use SikuliX IDE for best results
- Built-in screenshot tool may not capture at correct resolution
- Verify resolution before creating many patterns

### Linux
- SikuliX IDE recommended
- Native screenshot tools vary by distribution
- Test compatibility before creating pattern library

### WSL (Windows Subsystem for Linux)
⚠️ **CRITICAL LIMITATION**: WSL cannot capture the Windows desktop
- **Pattern matching will NOT work in WSL** - captures return black/empty images
- **Development only** - Use WSL for coding, not for running pattern matching
- **Solutions**:
  1. Run Brobot on Windows directly (use PowerShell or Command Prompt)
  2. Use mock mode for testing in WSL (`brobot.core.mock=true`)
  3. Create patterns on Windows, test on Windows

## Migration Guide

### From Existing Patterns
If you have patterns created with other tools:

1. **Identify pattern resolution**:
   ```bash
   # Check image properties
   file pattern.png
   # Or use image viewer to check dimensions
   ```

2. **Adjust Brobot configuration**:
   - 1920x1080 patterns → Use default configuration
   - 1536x864 patterns → Switch to SIKULIX provider
   - Other resolutions → May need custom scaling

3. **Test and verify**:
   ```java
   // Run similarity test
   PatternFindOptions options = new PatternFindOptions.Builder()
       .setSimilarity(0.7)  // Start low for testing
       .build();
   ```

## Frequently Asked Questions

### Q: Why do different tools have different similarity scores?
**A**: Tools capture screens differently - some use logical resolution (DPI-aware), others use physical resolution. Rendering engines also differ slightly between tools.

### Q: Can I mix patterns from different tools?
**A**: Not recommended. Stick to one tool for consistency. If you must mix, group patterns by tool and adjust configuration per state.

### Q: What if my screen resolution is different from 1920x1080?
**A**: Create patterns at your target resolution. Brobot will capture at the same resolution. The key is consistency between pattern creation and execution.

### Q: How can I verify which resolution my patterns are?
**A**: Check the image properties or use Brobot's debug mode to log pattern dimensions:
```properties
brobot.screenshot.save-snapshots=true
brobot.console.actions.enabled=true
```

## Summary

For optimal pattern matching:
1. **Use Windows Snipping Tool or SikuliX IDE** - 100% similarity guaranteed
2. **Keep default Brobot configuration** - Already optimized for these tools
3. **Save patterns as PNG** - Avoid compression artifacts
4. **Organize patterns by state** - Easier maintenance

The default Brobot configuration is already optimized for the best pattern creation tools. Simply create your patterns with Windows Snipping Tool or SikuliX IDE, and they'll work perfectly with Brobot's pattern matching system.
# Pattern Creation Tools Guide

## Overview
This guide helps you choose the best tool for creating pattern images that work with Brobot's [pattern matching system](../../04-testing/debugging-pattern-matching.md). The choice of pattern creation tool directly impacts the accuracy of your UI automation.

## Recommended Tools

### 🏆 Optimal Tools for Pattern Creation
These tools provide the best pattern matching during runtime automation:

#### 1. **Windows Snipping Tool** (RECOMMENDED)
- **Resolution**: 1920x1080 (physical)
- **Runtime Match Rate**: **95-100%** with Brobot's [JAVACV_FFMPEG capture provider](../capture/modular-capture-system.md)
- **Platform**: Windows only
- **Why it's best**: Produces the cleanest, artifact-free patterns
- **How to use**:
  1. Press `Win + Shift + S` to open Snipping Tool
  2. Select the area you want to capture
  3. Save as PNG in your project's `images/` directory

For detailed provider comparison, see the [Capture Methods Comparison](./capture-methods-comparison.md).

#### 2. **macOS Screenshot Tool**
- **Platform**: macOS
- **Runtime Match Rate**: 95-100%
- **How to use**: Press `Cmd + Shift + 4`

#### 3. **Linux Screenshot Tools**
- **Platform**: Linux
- **Runtime Match Rate**: 95-100%
- **Tools**: GNOME Screenshot or Spectacle

### ✅ Alternative Tools (Lower Match Rates)
#### **SikuliX IDE**
- **Resolution**: Variable (depends on DPI settings)
- **Runtime Match Rate**: 70-80% (can be used for testing similarity)
- **Platform**: Cross-platform
- **Use case**: Testing similarity thresholds, not for pattern creation

#### **Brobot FFmpeg Tool**
- **Resolution**: 1920x1080 (physical)
- **Runtime Match Rate**: 70-80% when used for patterns
- **Platform**: Cross-platform
- **Note**: Better for testing than pattern creation

### ⚠️ Not Recommended (< 70% Similarity)
- Screenshot tools that apply compression or filters
- Browser-based screenshot extensions
- Tools that capture at non-standard resolutions
- Robot-based custom tools (typically 59-69% similarity)

## Brobot Configuration

### Default Configuration (Optimal)
The default Brobot configuration is optimized for patterns created with Windows Snipping Tool, SikuliX IDE, or Brobot FFmpeg Tool. See the [Properties Reference](../configuration/properties-reference.md) for complete configuration options.

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

For more on DPI handling, see the [DPI Resolution Guide](../capture/dpi-resolution-guide.md).

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
1. **For best results**: Use OS native screenshot tools
   - Windows: Windows Snipping Tool (Win+Shift+S)
   - macOS: Built-in screenshot (Cmd+Shift+4)
   - Linux: GNOME Screenshot or Spectacle
2. **For testing patterns**: Use [Brobot Pattern Capture Tool](./pattern-capture-tool-guide.md)
3. **If you have existing patterns**: Test their match rates and consider recapturing with native tools

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

Test your patterns to ensure they match correctly. For more details, see the [States Guide](../../01-getting-started/states.md) and [Action API](../../01-getting-started/action-hierarchy.md).

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.match.Match;

@Component
public class PatternVerificationExample {

    @Autowired
    private Action action;

    public void verifyPattern() {
        StateImage pattern = new StateImage.Builder()
            .addPattern("button-login.png")  // Use addPattern, not withImage
            .build();

        ActionResult result = action.find(pattern);

        // Get similarity score from best match
        if (result.getBestMatch().isPresent()) {
            Match bestMatch = result.getBestMatch().get();
            System.out.println("Similarity: " + bestMatch.getScore());
            // Should be > 0.94 for optimal tools
        } else {
            System.out.println("No match found");
        }
    }
}
```

## Troubleshooting

### Low Similarity Scores
If you're getting similarity scores below 90%:

1. **Check Resolution Mismatch**
   - Pattern: 1920x1080, Capture: 1536x864 → ~77% similarity
   - Solution: Use matching capture provider or enable scaling
   - See the [DPI Resolution Guide](../capture/dpi-resolution-guide.md) for troubleshooting

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

| Pattern Creation Tool | Best Brobot Provider | Runtime Match Rate | Recommendation |
|----------------------|---------------------|-------------------|----------------|
| Windows Snipping Tool | JAVACV_FFMPEG | **95-100%** | **RECOMMENDED** |
| macOS Screenshot | JAVACV_FFMPEG | **95-100%** | **RECOMMENDED** |
| Linux Screenshot | JAVACV_FFMPEG | **95-100%** | **RECOMMENDED** |
| Brobot FFmpeg Tool | JAVACV_FFMPEG | 70-80% | Testing only |
| SikuliX IDE | JAVACV_FFMPEG | 70-80% | Testing only |

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
- **Use built-in screenshot tool (Cmd+Shift+4) for best results** - achieves 95-100% match rates
- Alternative: SikuliX IDE for cross-platform consistency
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
  2. Use [mock mode](../../04-testing/mock-mode-guide.md) for testing in WSL (`brobot.mock=true`)
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

   For more on ActionConfig, see the [ActionConfig Overview](../action-config/01-overview.md).

   ```java
   import io.github.jspinak.brobot.action.Action;
   import io.github.jspinak.brobot.action.ActionResult;
   import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
   import io.github.jspinak.brobot.model.state.StateImage;
   import org.springframework.beans.factory.annotation.Autowired;

   @Component
   public class MigrationTest {

       @Autowired
       private Action action;

       public void testMigratedPattern() {
           // Run similarity test with lower threshold
           PatternFindOptions options = new PatternFindOptions.Builder()
               .setSimilarity(0.7)  // Start low for testing
               .build();

           StateImage pattern = new StateImage.Builder()
               .addPattern("legacy-button.png")
               .build();

           ActionResult result = action.perform(options, pattern.asObjectCollection());

           if (result.isSuccess()) {
               System.out.println("Found " + result.size() + " matches");
           }
       }
   }
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
1. **Use OS native screenshot tools** - Windows Snipping Tool, macOS Screenshot, or Linux Screenshot tools
2. **Keep default Brobot configuration** - JAVACV_FFMPEG for runtime capture
3. **Save patterns as PNG** - Avoid compression artifacts
4. **Organize patterns by state** - Easier maintenance

**Key insight**: Clean, artifact-free patterns from native OS tools match better during runtime than patterns captured with the same tool used for runtime capture. This counterintuitive result occurs because noise and artifacts compound when present in both pattern and runtime images.

> **Note**: Match rate claims are based on testing configurations. Results may vary based on your specific environment, display settings, and Windows version.

## Related Documentation

### Getting Started
- **[Installation Guide](../../01-getting-started/installation.md)** - Platform setup including WSL considerations
- **[States in Brobot](../../01-getting-started/states.md)** - Understanding StateImage patterns
- **[Action Hierarchy](../../01-getting-started/action-hierarchy.md)** - Using patterns with Actions

### Capture System
- **[Modular Capture System](../capture/modular-capture-system.md)** - Understanding JAVACV_FFMPEG and other providers
- **[Capture Methods Comparison](./capture-methods-comparison.md)** - Detailed comparison of all capture providers
- **[DPI Resolution Guide](../capture/dpi-resolution-guide.md)** - Handling DPI scaling and resolution mismatches
- **[Pattern Capture Tool Guide](./pattern-capture-tool-guide.md)** - Using Brobot's built-in pattern capture tool

### Configuration
- **[Properties Reference](../configuration/properties-reference.md)** - Complete list of Brobot properties including similarity, capture provider, and DPI settings
- **[ActionConfig Overview](../action-config/01-overview.md)** - Configuring pattern matching with PatternFindOptions
- **[ActionConfig Reference](../action-config/05-reference.md)** - Complete ActionConfig API including similarity configuration

### Testing & Debugging
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing patterns without display (essential for WSL/headless environments)
- **[Debugging Pattern Matching](../../04-testing/debugging-pattern-matching.md)** - Troubleshooting pattern matching issues
- **[Integration Testing](../../04-testing/integration-testing.md)** - Testing automation with patterns

### Advanced Topics
- **[Reusable Patterns](../action-config/11-reusable-patterns.md)** - Creating pattern libraries and reusable configurations
- **[Convenience Methods](../action-config/18-convenience-methods.md)** - Simplified API for pattern finding and clicking
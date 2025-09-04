# DPI Scaling Guide for Brobot

## Overview

DPI scaling issues are one of the most common causes of pattern matching failures in Brobot. This guide explains how DPI scaling works and how to configure Brobot for optimal pattern matching.

## Key Concepts

### Physical vs Logical Pixels

- **Physical Pixels**: The actual pixels on your monitor (what SikuliX IDE captures)
- **Logical Pixels**: DPI-scaled pixels that applications see (what Windows snipping tool captures)

With 125% Windows display scaling:
- A 100x100 logical pixel area appears as 125x125 physical pixels on screen
- Pattern scale factor needed: 1/1.25 = 0.8

### Pattern Sources

Different tools capture patterns differently:

1. **SikuliX IDE**: Captures in physical pixels (screen pixels)
2. **Windows Snipping Tool**: Captures in logical pixels (application pixels)
3. **Screenshot tools**: May capture in either, depending on DPI-awareness

## Configuration

### Automatic DPI Detection (Recommended)

```properties
# application.properties
brobot.dpi.resize.factor=auto
brobot.dpi.pattern.source=SIKULI_IDE  # or WINDOWS_TOOL
brobot.action.similarity=0.65
```

### Manual DPI Configuration

```properties
# For 125% Windows scaling
brobot.dpi.resize.factor=0.8

# For 150% Windows scaling
brobot.dpi.resize.factor=0.67

# For 200% Windows scaling
brobot.dpi.resize.factor=0.5
```

### Debug Mode

Enable detailed DPI diagnostics:

```properties
brobot.dpi.debug=true
```

## Diagnostic Tools

### 1. DPIScalingDiagnostic

Analyzes patterns and determines optimal DPI settings:

```java
import io.github.jspinak.brobot.tools.diagnostics.DPIScalingDiagnostic;

// Analyze a single pattern
DPIScalingDiagnostic.DiagnosticResult result = 
    DPIScalingDiagnostic.analyzePattern("images/my-pattern.png");

// Analyze multiple patterns
List<String> patterns = Arrays.asList(
    "images/pattern1.png",
    "images/pattern2.png"
);
DPIScalingDiagnostic.analyzePatterns(patterns);
```

### 2. DPIScalingStrategy

Programmatic DPI configuration:

```java
import io.github.jspinak.brobot.dpi.DPIScalingStrategy;

// Detect display scaling
double scale = DPIScalingStrategy.detectDisplayScaling();
System.out.println("Display scaling: " + (int)(scale * 100) + "%");

// Configure SikuliX for your pattern source
DPIScalingStrategy.configureSikuliX(
    DPIScalingStrategy.PatternSource.SIKULI_IDE,
    0.65f  // minimum similarity
);

// Print diagnostic information
DPIScalingStrategy.printDiagnostics();
```

## Troubleshooting

### Patterns match in SikuliX IDE but not in Brobot

**Cause**: SikuliX IDE captures in physical pixels while your display has DPI scaling.

**Solution**:
1. Enable auto DPI detection: `brobot.dpi.resize.factor=auto`
2. Run DPIScalingDiagnostic to find optimal settings
3. Ensure all patterns are captured with the same tool

### Low similarity scores (60-70%) even with DPI configured

**Possible causes**:
1. **Mixed pattern sources**: Some patterns from SikuliX IDE, others from Windows tools
2. **Browser/application zoom**: Ensure zoom is at 100% (Ctrl+0 in browsers)
3. **Compound scaling**: Both Windows DPI and application scaling active

**Solutions**:
1. Recapture all patterns with the same tool
2. Pre-scale patterns to 80% size if needed
3. Use Windows-captured patterns if application has its own scaling

### Pattern dimensions don't match expected sizes

**Example**: Pattern is 195x80 but you expect 103x60

**Explanation**: 
- 195x80 = physical pixels (125% of logical)
- 103x60 = logical pixels
- Ratio: 195/103 ≈ 1.89 (not exactly 1.25 due to compound scaling)

**Solution**: Use DPIScalingDiagnostic to analyze actual scaling

## Best Practices

### 1. Consistent Pattern Capture

Choose ONE method and use it for all patterns:
- **Option A**: SikuliX IDE for all patterns (recommended)
- **Option B**: Windows snipping tool for all patterns
- **Option C**: Application-specific screenshot tool

### 2. Verify DPI Configuration

On application startup, check the logs:

```
[Brobot] Display scaling detected: 125%
[Brobot] Calculated pattern scale factor: 0.8
[Brobot] Settings.AlwaysResize = 0.8
```

### 3. Test Pattern Matching

Create a test to verify patterns work:

```java
@Test
public void testPatternMatching() {
    // This test should extend BrobotTestBase
    Action action = new Action();
    StateImage pattern = new StateImage.Builder()
        .withImage("test-pattern.png")
        .build();
    
    ActionResult result = action.find(pattern);
    assertTrue(result.isSuccess(), 
        "Pattern should match with configured DPI settings");
    assertTrue(result.getScore() > 0.80, 
        "Similarity should be > 80%, was: " + result.getScore());
}
```

### 4. Pre-scaled Pattern Sets

For maximum compatibility, maintain multiple pattern sets:

```
images/
  patterns/
    original/      # 100% size patterns
    scaled-80/     # Pre-scaled to 80% for 125% DPI
    scaled-67/     # Pre-scaled to 67% for 150% DPI
```

## Common DPI Scaling Factors

| Windows Scaling | Pattern Scale Factor | Settings.AlwaysResize |
|----------------|---------------------|----------------------|
| 100% | 1.0 | 1.0 |
| 125% | 0.8 | 0.8 |
| 150% | 0.67 | 0.67 |
| 175% | 0.57 | 0.57 |
| 200% | 0.5 | 0.5 |

## Advanced: Handling Multiple Monitors

If you have monitors with different DPI settings:

```java
// Get all screen devices
GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
GraphicsDevice[] screens = ge.getScreenDevices();

for (GraphicsDevice screen : screens) {
    double scale = screen.getDefaultConfiguration()
        .getDefaultTransform().getScaleX();
    System.out.println("Screen: " + screen.getIDstring() + 
                      " Scale: " + (int)(scale * 100) + "%");
}
```

## Conclusion

Proper DPI configuration is essential for reliable pattern matching. Key points:

1. Use `brobot.dpi.resize.factor=auto` for automatic detection
2. Run DPIScalingDiagnostic when patterns don't match as expected
3. Capture all patterns with the same tool
4. Monitor logs to verify DPI settings are applied
5. Test pattern matching in your target environment

For additional help, use the diagnostic tools or enable debug logging with `brobot.dpi.debug=true`.
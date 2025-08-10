---
sidebar_position: 5
---

# Debugging Pattern Matching

When patterns fail to match during automation, Brobot provides several powerful debugging tools to help identify and resolve issues.

## Best Match Capture

The Best Match Capture feature helps debug pattern matching failures by capturing and saving the region that best matches your pattern, even when it doesn't meet the similarity threshold.

### How It Works

When enabled, the Best Match Capture system:
1. Detects when pattern searches fail or find low-similarity matches
2. Performs a search at very low threshold (0.1) to find any potential match
3. Captures the best matching region from the screen
4. Saves both the matched region and original pattern for comparison
5. Names files with timestamp and similarity score for easy tracking

### Configuration

Add these properties to your `application.properties`:

```properties
# Enable best match capture
brobot.debug.capture-best-match=true

# Only capture when match is below this threshold (default: 0.95)
brobot.debug.capture-threshold=0.95

# Directory to save captured images (relative to project root)
brobot.debug.capture-directory=history/best-matches

# Also save the pattern image for comparison (default: true)
brobot.debug.save-pattern-image=true
```

### Output Format

Captured images are saved with descriptive filenames:
- **Match image**: `YYYYMMDD-HHmmss_PatternName_sim###_match.png`
- **Pattern image**: `YYYYMMDD-HHmmss_PatternName_sim###_pattern.png`

Example:
```
history/best-matches/
├── 20250810-143022_claude-prompt-1_sim045_match.png
├── 20250810-143022_claude-prompt-1_sim045_pattern.png
├── 20250810-143025_submit-button_sim062_match.png
└── 20250810-143025_submit-button_sim062_pattern.png
```

### Console Output

When a best match is captured, you'll see console messages like:
```
[BEST_MATCH] Captured best match for 'claude-prompt-1' with similarity 0.453 saved to: history/best-matches/20250810-143022_claude-prompt-1_sim453_match.png
[BEST_MATCH] Pattern image saved to: history/best-matches/20250810-143022_claude-prompt-1_sim453_pattern.png
```

### Use Cases

This feature is particularly useful for:

1. **Understanding Match Failures**: Compare what the pattern looks like vs what was actually found
2. **Similarity Threshold Tuning**: See the actual similarity scores to adjust thresholds
3. **UI Changes Detection**: Identify when the UI has changed slightly (fonts, colors, scaling)
4. **Resolution Issues**: Detect when patterns fail due to different screen resolutions
5. **Alpha Channel Issues**: Identify transparency-related matching problems

### Analyzing Captured Images

When analyzing captured images:

1. **Compare visually**: Open both pattern and match images side-by-side
2. **Check for differences**:
   - Color variations (especially with dark/light themes)
   - Font rendering differences
   - Scaling or resolution mismatches
   - Anti-aliasing artifacts
   - Transparency/alpha channel issues

3. **Adjust patterns based on findings**:
   - Update pattern images if UI has changed
   - Adjust similarity thresholds if minor variations are acceptable
   - Consider using multiple pattern variations
   - Remove alpha channels if causing issues

## Progressive Similarity Testing

In addition to best match capture, Brobot automatically performs progressive similarity testing when patterns fail. This tests the pattern at decreasing thresholds to find the minimum similarity at which it would match.

### Console Output

```
[SIMILARITY DEBUG] Testing pattern 'claude-prompt-1'
[SIMILARITY DEBUG] Original MinSimilarity: 0.7
[SIMILARITY ANALYSIS]
  Threshold 0.5: FOUND with score 0.523
```

This tells you exactly what similarity threshold would be needed for the pattern to match.

## Debug Image Saving

When patterns with "prompt" in their name fail to match, Brobot automatically saves debug images to help diagnose the issue:

```
debug_images/
├── pattern_claude-prompt-1.png
├── pattern_claude-prompt-2.png
└── scene_current.png
```

## Image Analysis

Brobot provides automatic image content analysis to detect common issues:

```
[IMAGE ANALYSIS]
  Pattern: 293x83 type=ARGB bytes=97KB
  Pattern content: 5.2% black, 0.3% white, avg RGB=(45,45,48)
  Scene: 1920x1080 type=RGB bytes=8MB
  Scene content: 92.1% black, 0.1% white, avg RGB=(12,12,12)
  WARNING: Scene is mostly BLACK - possible capture failure!
```

## Verbosity Levels

Control the amount of debug information with verbosity settings:

```properties
# Set to VERBOSE for maximum debug information
brobot.logging.verbosity=VERBOSE
brobot.console.actions.level=VERBOSE

# Enable specific debug logging
logging.level.io.github.jspinak.brobot.action.internal.find=DEBUG
```

## Troubleshooting Common Issues

### Pattern Not Found at Expected Similarity

**Symptoms**: Pattern matches at 0.5 but threshold is 0.7

**Solutions**:
1. Enable best match capture to see what's being matched
2. Check for alpha channel issues (transparent backgrounds)
3. Verify color space consistency (RGB vs ARGB)
4. Consider UI theme differences (dark vs light mode)

### Black or Invalid Screen Captures

**Symptoms**: Scene images are all black or invalid

**Solutions**:
1. Check display permissions and settings
2. Verify not running in true headless mode
3. For WSL2, ensure X11 forwarding is configured
4. Check `DISPLAY` environment variable

### Patterns Match in Wrong Location

**Symptoms**: Patterns found but in unexpected screen areas

**Solutions**:
1. Define search regions to limit search area
2. Check for duplicate UI elements
3. Increase similarity threshold
4. Use fixed regions for consistent UI elements

## Best Practices

1. **Always enable during development**: Keep best match capture enabled while developing patterns
2. **Review captured images regularly**: Check the history folder to understand matching behavior
3. **Clean up periodically**: Delete old captures to save disk space
4. **Use meaningful pattern names**: Makes captured files easier to identify
5. **Adjust thresholds based on captures**: Use actual similarity scores to set appropriate thresholds
# Images Directory - Claude Automator Tutorial

Place your screenshot images here for pattern matching in the Claude Automator tutorial.

## Expected Structure

This tutorial uses a 2-state system (Prompt and Working) with the following image organization:

```
images/
├── prompt/
│   ├── claude-prompt-1.png
│   ├── claude-prompt-2.png
│   └── claude-prompt-3.png
└── working/
    ├── claude-icon-1.png
    ├── claude-icon-2.png
    ├── claude-icon-3.png
    └── claude-icon-4.png
```

## State-Image Mapping

### PromptState (images/prompt/)
- `claude-prompt-1.png` - Claude prompt input area (primary variant)
- `claude-prompt-2.png` - Claude prompt input area (alternate variant)
- `claude-prompt-3.png` - Claude prompt input area (additional variant)

**Purpose**: These images identify when Claude is ready to receive input. Multiple variants improve pattern matching reliability across different screen states.

### WorkingState (images/working/)
- `claude-icon-1.png` - Claude processing icon (variant 1)
- `claude-icon-2.png` - Claude processing icon (variant 2)
- `claude-icon-3.png` - Claude processing icon (variant 3)
- `claude-icon-4.png` - Claude processing icon (variant 4)

**Purpose**: These images identify when Claude is actively processing. The icon typically appears near the input area when Claude is working.

## Tutorial Feature: Declarative Search Regions

**This is the highlight of this tutorial!**

The Claude icon images in WorkingState use **declarative search region definition** - their search region is automatically calculated relative to the Claude prompt location in PromptState:

```java
// In WorkingState.java
claudeIcon = new StateImage.Builder()
    .addPatterns("working/claude-icon-1", ..., "working/claude-icon-4")
    .setSearchRegionOnObject(
        SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("Prompt")           // Cross-state reference!
            .setTargetObjectName("ClaudePrompt")    // Target object in PromptState
            .setAdjustments(
                MatchAdjustmentOptions.builder()
                    .setAddX(3).setAddY(10)         // Offset from prompt
                    .setAddW(30).setAddH(55)        // Search region size
                    .build())
            .build())
    .build();
```

**What this means:**
1. When Brobot finds the Claude prompt, it records its location
2. When searching for the Claude icon, it automatically searches in a region **relative to the prompt**
3. No manual region calculations needed
4. Search region updates automatically if the prompt location changes

## Mock Mode

This tutorial runs in **mock mode by default** (configured in `application.yml`):
- No actual GUI interaction occurs
- Actions are simulated with configured delays
- Perfect for testing and understanding the API
- **No image files are required for mock mode**

## To Use Real GUI Automation

### Step 1: Set Mock Mode to False

Edit `src/main/resources/application.yml`:
```yaml
brobot:
  core:
    mock: false
```

### Step 2: Capture Screenshots

You'll need to capture screenshots of Claude AI interface elements. Here's how:

#### For Prompt Images (images/prompt/):

1. Open Claude AI in your browser
2. Navigate to a conversation or start a new one
3. Ensure the prompt input area is visible and clear
4. Capture screenshots of the input area with slight variations:
   - Different text cursor positions
   - With/without placeholder text
   - Different focus states

**What to capture:**
- The input text box itself
- Include a small margin (5-10 pixels) around it
- Minimum size: 50x30 pixels
- Recommended size: 200x100 pixels

#### For Working Images (images/working/):

1. With Claude open, submit a query
2. Watch for the "thinking" or "processing" indicator
3. This is typically a small animated icon near the input area
4. Capture screenshots of this icon in different animation frames:
   - Frame 1: Initial state
   - Frame 2: Mid-animation
   - Frame 3: End of cycle
   - Frame 4: Alternate state (if applicable)

**What to capture:**
- Just the working/processing icon
- Very small region (typically 20x20 to 40x40 pixels)
- Include minimal margin (2-5 pixels)

### Step 3: Screenshot Best Practices

**Resolution**: Capture at the same screen resolution where automation will run

**Format**: PNG (lossless compression)

**Naming**: Use exact filenames listed above (case-sensitive):
- `claude-prompt-1.png` (NOT `ClaudePrompt1.png` or `prompt1.png`)

**Quality**:
- No compression artifacts
- Clear, not blurry
- Consistent lighting/color

**Consistency**:
- Capture all images from same Claude theme (light/dark mode)
- Use same browser zoom level
- Same screen scaling (100%, 125%, etc.)

### Step 4: Adjust Search Region Parameters

After capturing images, you may need to adjust the search region offsets in `WorkingState.java`:

```java
.setAdjustments(
    MatchAdjustmentOptions.builder()
        .setAddX(3)     // Horizontal offset from prompt
        .setAddY(10)    // Vertical offset from prompt
        .setAddW(30)    // Search region width
        .setAddH(55)    // Search region height
        .build())
```

**How to determine offsets:**
1. Measure distance from prompt location to icon location
2. `setAddX`: Pixels to the right (negative = left)
3. `setAddY`: Pixels down (negative = up)
4. `setAddW` and `setAddH`: Size of search region

### Step 5: Run the Application

```bash
./gradlew bootRun
```

## Configuration Options

### Similarity Threshold

Adjust pattern matching sensitivity in `application.yml`:
```yaml
brobot:
  action:
    similarity: 0.85  # Range: 0.0-1.0 (lower = more permissive)
```

**Guidelines:**
- **0.95-1.0**: Exact matches only (very strict)
- **0.85-0.95**: Recommended range (balanced)
- **0.70-0.85**: More permissive (use if patterns vary)
- **<0.70**: Very permissive (may cause false positives)

### Search Duration

Control how long Brobot searches for patterns:
```java
// In PatternFindOptions
.setSearchDuration(5.0)  // Search for up to 5 seconds
```

### Highlighting

Visual feedback for found patterns (useful for debugging):
```yaml
brobot:
  highlight:
    enabled: true
    auto-highlight-finds: true
    auto-highlight-search-regions: true
```

## Troubleshooting

### Image Not Found

**Symptoms**: Brobot reports pattern not found even though element is visible

**Solutions**:
1. **Check filename** - Must match exactly (case-sensitive)
2. **Verify file format** - Must be PNG (not JPG)
3. **Adjust similarity** - Lower the threshold (try 0.80)
4. **Recapture image** - May need fresh screenshot
5. **Check path** - Images must be in `images/prompt/` or `images/working/`
6. **Verify mock mode** - Should be `false` for real UI testing

### Pattern Matches Wrong Element

**Symptoms**: Brobot finds the wrong UI element

**Solutions**:
1. **Increase similarity** - Higher threshold (try 0.95)
2. **Capture larger region** - Include more context
3. **Use more specific pattern** - Capture unique characteristics
4. **Adjust search region** - Make it more restrictive

### Search Region Not Working

**Symptoms**: Declarative search region not finding icon

**Solutions**:
1. **Verify prompt is found first** - Icon search depends on prompt location
2. **Check offset values** - May need adjustment for your setup
3. **Increase search region size** - Make `addW` and `addH` larger
4. **Enable highlighting** - Visualize where Brobot is searching
5. **Check state names** - Must be "Prompt" and "Working" (case-sensitive)

### Performance Issues

**Symptoms**: Searches take too long

**Solutions**:
1. **Reduce search duration** - Lower timeout values
2. **Use smaller images** - Crop tightly around elements
3. **Limit search regions** - Declarative regions are faster than full screen
4. **Use faster similarity** - Values closer to 0.85 are faster than 0.95+

## Advanced: Monitoring Automation

This tutorial includes three automation patterns (see `automation/` package):

### 1. Basic Monitoring (ClaudeAutomatorRunner)
Simple state checks and transitions.

### 2. Scheduled Monitoring (ClaudeMonitoringAutomation)
Continuous polling every 2 seconds. Enable with:
```yaml
claude:
  automator:
    monitoring:
      enabled: true
      check-interval: 2
```

### 3. Reactive Monitoring (ReactiveAutomation)
Project Reactor-based streams. Requires:
```gradle
implementation 'io.projectreactor:reactor-core:3.6.0'
```

## Related Documentation

- **Tutorial Documentation**: `/docs/03-core-library/tutorials/tutorial-claude-automator/`
- **Declarative Regions Guide**: `/docs/03-core-library/guides/user-guides/declarative-region-definition.md`
- **State Management**: `/docs/02-core-concepts/states.md`
- **Pattern Matching**: `/docs/02-core-concepts/pattern-matching.md`
- **Mock Mode**: `/docs/04-testing/mock-mode-guide.md`

## Example Code Reference

The images in this directory correspond to StateImage definitions in:
- `src/main/java/com/claude/automator/states/PromptState.java`
- `src/main/java/com/claude/automator/states/WorkingState.java`

## Quick Reference

| Image Pattern | Used By | Purpose | Typical Size |
|---------------|---------|---------|--------------|
| `prompt/claude-prompt-*` | PromptState | Identify input area | 200x100 |
| `working/claude-icon-*` | WorkingState | Detect processing | 30x30 |

## Support

For issues or questions:
1. Check the comprehensive documentation in `/docs/`
2. Review the example code in `src/main/java/`
3. Try enabling `brobot.console.actions.level: VERBOSE` for detailed logging
4. File issues at https://github.com/jspinak/brobot/issues

# Image Highlighting Feature

## Overview
Brobot includes comprehensive highlighting capabilities for found images and UI elements. When patterns are found on the screen, they can be visually highlighted to provide immediate feedback during automation. The highlighting system now supports **custom colors per StateImage**, allowing different UI elements to be highlighted in different colors for better visual differentiation.

## Configuration

### Properties Files Updated

#### 1. Main Configuration (`src/main/resources/application.properties`)
Enhanced highlighting properties have been added:

```properties
# Visual feedback 
brobot.highlight.enabled=true
brobot.highlight.auto-highlight-finds=true

# Enhanced highlighting for found images
brobot.highlight.find.color=#00FF00
brobot.highlight.find.border-width=4
brobot.highlight.find.flash=true
brobot.highlight.find.flash-count=2
brobot.highlight.find.flash-interval=200
brobot.highlight.find.show-confidence=true
brobot.highlight.find.label.enabled=true
brobot.highlight.find.label.background=#00FF00
brobot.highlight.find.label.text-color=#FFFFFF

# Highlight durations
brobot.highlight.find.duration=1.5
brobot.highlight.click.duration=0.2

# Multiple match highlighting
brobot.highlight.multi-match.enabled=true
brobot.highlight.multi-match.number-matches=true
brobot.highlight.multi-match.gradient-colors=true
```

#### 2. Test Profile (`src/test/resources/application-highlight-test.properties`)
Extended test configuration with additional features:

```properties
# Find highlighting configuration
brobot.highlight.find.show-confidence=true
brobot.highlight.find.label.enabled=true
brobot.highlight.find.label.show-similarity-score=true

# Multiple match highlighting
brobot.highlight.multi-match.enabled=true
brobot.highlight.multi-match.gradient-colors=true

# Highlight animations
brobot.highlight.animation.enabled=true
brobot.highlight.animation.type=pulse
```

#### 3. Production Profile (`src/main/resources/application-highlight-production.properties`)
Optimized configuration for production use:

```properties
# Production-optimized highlighting
brobot.highlight.find.duration=1.0
brobot.highlight.find.flash-count=1
brobot.highlight.find.label.format=Match: %.2f%%

# Performance optimizations
brobot.highlight.performance.use-hardware-acceleration=true
brobot.highlight.performance.batch-renders=true
brobot.highlight.performance.max-concurrent-highlights=3
```

## Features

### 1. **Automatic Highlighting**
- When `brobot.highlight.auto-highlight-finds=true`, all successful pattern matches are automatically highlighted
- No code changes required - works with existing `action.find()` calls

### 2. **Visual Indicators**
- **Border Highlight**: Green border around found images
- **Flash Effect**: Configurable flash animation to draw attention
- **Confidence Labels**: Shows similarity score on found matches
- **Color Coding**: Different colors for multiple matches

### 3. **Multiple Match Support**
- When finding multiple instances, each match can be highlighted with different colors
- Numbered labels help identify individual matches
- Gradient color support for visual differentiation

### 4. **Click Highlighting**
- Yellow ripple effect at click locations
- Helps verify where automation is clicking

### 5. **Error Highlighting**
- Red indicators for failed pattern matches
- Cross-mark overlay for clear failure indication

### 6. **Custom Color Per StateImage** (New Feature)
- Each StateImage can have its own highlight color
- Set via `StateImage.Builder.setHighlightColor()`
- Allows visual differentiation between different UI elements
- Falls back to default color if custom color is invalid

## Usage

### Activating Highlighting

#### Method 1: Default Configuration
Highlighting is enabled by default in `application.properties`:
```bash
./gradlew bootRun
```

#### Method 2: Using Spring Profiles
Activate specific highlighting profiles:
```bash
# For testing with full highlighting features
./gradlew bootRun --args='--spring.profiles.active=highlight-test'

# For production with optimized highlighting
./gradlew bootRun --args='--spring.profiles.active=highlight-production'
```

#### Method 3: System Properties
Override specific properties at runtime:
```bash
./gradlew bootRun -Dbrobot.highlight.find.duration=2.0 -Dbrobot.highlight.find.color=#FF00FF
```

### Setting Custom Colors for StateImages

You can now specify custom highlight colors for individual StateImages using the builder pattern:

```java
// Create a StateImage with blue highlighting
StateImage iconImage = new StateImage.Builder()
    .addPatterns("icon-1.png", "icon-2.png")
    .setName("MyIcon")
    .setHighlightColor("#0000FF")  // Blue highlight
    .build();

// Create a StateImage with red highlighting
StateImage errorImage = new StateImage.Builder()
    .addPatterns("error-indicator.png")
    .setName("ErrorIndicator")
    .setHighlightColor("#FF0000")  // Red highlight
    .build();

// Create a StateImage with default highlighting (uses global config)
StateImage normalImage = new StateImage.Builder()
    .addPatterns("normal-element.png")
    .setName("NormalElement")
    // No setHighlightColor() - uses default from properties
    .build();
```

#### Real-World Example: Claude Automator

```java
public class WorkingState {
    private final StateImage claudeIcon;
    
    public WorkingState() {
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .setHighlightColor("#0000FF")  // Blue for icon
            .build();
    }
}

public class PromptState {
    private final StateImage claudePrompt;
    
    public PromptState() {
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1",
                        "prompt/claude-prompt-2",
                        "prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            // Uses default green color from properties
            .build();
    }
}
```

### Testing Highlighting

A comprehensive test class `HighlightingTest.java` has been created to verify highlighting functionality:

```java
// Test finds with automatic highlighting
action.find(stateImage);  // Highlighting applied automatically

// Test multiple matches
PatternFindOptions options = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .build();
action.perform(ActionType.FIND, collection, options);
```

Run the test:
```bash
./gradlew test --tests HighlightingTest
```

## How It Works

1. **Property Loading**: Spring Boot loads highlighting configuration from properties files
2. **Aspect Integration**: Brobot's `VisualFeedbackAspect` intercepts find operations
3. **Automatic Application**: When enabled, highlights are drawn around match regions
4. **Console Reporting**: Highlight events are logged for debugging

## Customization

### Color Schemes
Modify colors using hex values:
```properties
brobot.highlight.find.color=#00FF00      # Green for finds
brobot.highlight.click.color=#FFFF00     # Yellow for clicks
brobot.highlight.error.color=#FF0000     # Red for errors
```

### Animation Settings
Control animation behavior:
```properties
brobot.highlight.find.flash=true
brobot.highlight.find.flash-count=2
brobot.highlight.find.flash-interval=200  # milliseconds
```

### Performance Tuning
Adjust for performance vs visibility:
```properties
brobot.highlight.find.duration=0.5       # Shorter for less intrusion
brobot.highlight.performance.max-concurrent-highlights=3
```

## Integration with Claude Automator

The highlighting feature integrates seamlessly with the Claude Automator's existing pattern matching:

1. **ClaudeMonitoringAutomation**: Highlights prompt and icon detection
2. **State Transitions**: Visual feedback during state changes
3. **Debug Mode**: Enhanced visibility for troubleshooting

## Troubleshooting

### Highlights Not Appearing
1. Verify `brobot.highlight.enabled=true`
2. Check `brobot.highlight.auto-highlight-finds=true`
3. Ensure not in mock mode (`brobot.core.mock=false`)

### Performance Issues
1. Reduce `brobot.highlight.find.duration`
2. Disable `brobot.highlight.find.flash`
3. Set `brobot.highlight.search-region.enabled=false`

### Console Output
Enable detailed logging:
```properties
logging.level.io.github.jspinak.brobot.tools.logging.visual.HighlightManager=DEBUG
brobot.console.actions.report-highlight=true
```

## Best Practices

1. **Development**: Use `highlight-test` profile for maximum visibility
2. **Production**: Use `highlight-production` profile for optimized performance
3. **Debugging**: Enable console reporting to track highlight events
4. **CI/CD**: Disable highlighting in automated tests (mock mode handles this)

## Future Enhancements

Potential improvements to consider:
- Screenshot capture of highlighted regions
- Video recording of automation with highlights
- Custom highlight shapes (circles, arrows)
- Highlight persistence for analysis
- Integration with debugging tools
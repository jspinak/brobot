---
sidebar_position: 3
---

# Using Color

Color analysis in Brobot helps improve object detection accuracy by filtering matches based on color properties.

## Color-Based Finding

Brobot can use color information to:
- Filter false positives
- Identify specific UI elements by color
- Handle dynamic content with consistent colors

## Basic Color Matching with ColorFindOptions

With the new ActionConfig API, color-based finding is more intuitive:

```java
ColorFindOptions colorFind = new ColorFindOptions.Builder()
    .setTargetColor(Color.RED)
    .setColorTolerance(50)
    .setColorSpace(ColorFindOptions.ColorSpace.RGB)
    .build();

ActionResult result = new ActionResult();
result.setActionConfig(colorFind);

ObjectCollection objects = new ObjectCollection.Builder()
    .withImages(targetImage)
    .build();

ActionInterface colorAction = actionService.getAction(colorFind);
colorAction.perform(result, objects);
```

## Advanced Color Analysis

### Color Histograms

Color histograms provide statistical analysis of color distribution in images:

```java
ColorHistogramOptions histogramOptions = new ColorHistogramOptions.Builder()
    .setAnalysisType(ColorHistogramOptions.AnalysisType.DOMINANT_COLOR)
    .setColorBins(256)
    .setChannels(ColorHistogramOptions.Channels.ALL)
    .build();
```

### Color Classification

Brobot can classify regions based on dominant colors:

```java
ColorClassificationOptions classifyOptions = new ColorClassificationOptions.Builder()
    .addColorClass("Button", Color.RED, 30)  // name, color, tolerance
    .addColorClass("Background", Color.BLUE, 40)
    .addColorClass("Text", Color.WHITE, 20)
    .setClassificationMethod(ColorClassificationOptions.Method.CLUSTERING)
    .build();
```

## Examples

### Finding Red Buttons

```java
// Define a red button with color validation
StateImageObject redButton = new StateImageObject.Builder()
    .withImage("button.png")
    .build();

// Use pattern finding first, then filter by color
PatternFindOptions patternFind = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.8)
    .build();

ColorFindOptions colorFilter = new ColorFindOptions.Builder()
    .setTargetColor(Color.RED)
    .setColorTolerance(50)
    .setFilterMode(true)  // Filter previous results
    .build();

// Execute pattern finding
ActionResult patternResult = new ActionResult();
patternResult.setActionConfig(patternFind);
ActionInterface patternAction = actionService.getAction(patternFind);
patternAction.perform(patternResult, new ObjectCollection.Builder()
    .withImages(redButton)
    .build());

// Filter by color
ActionResult colorResult = new ActionResult();
colorResult.setActionConfig(colorFilter);
colorResult.setPreviousResult(patternResult);
ActionInterface colorAction = actionService.getAction(colorFilter);
colorAction.perform(colorResult, new ObjectCollection.Builder()
    .withMatches(patternResult.getMatchList())
    .build());
```

### Color-Based State Validation

```java
// Define a state with color validation
StateColorValidator blueValidator = new StateColorValidator.Builder()
    .setExpectedColor(Color.BLUE)
    .setTolerance(30)
    .setMinimumCoverage(0.7)  // 70% of region should be blue
    .build();

State colorState = new State.Builder("COLOR_STATE")
    .withColorValidator(blueValidator)
    .withImages(stateImages)
    .build();

// Validate state by color
StateValidationOptions validateOptions = new StateValidationOptions.Builder()
    .setValidationType(StateValidationOptions.Type.COLOR)
    .setColorValidator(blueValidator)
    .build();
```

### Multi-Color Pattern Matching

```java
MultiColorPatternOptions multiColor = new MultiColorPatternOptions.Builder()
    .addColorPoint(0, 0, Color.RED)      // Top-left red
    .addColorPoint(100, 0, Color.BLUE)   // Top-right blue
    .addColorPoint(50, 50, Color.GREEN)  // Center green
    .setPointTolerance(5)  // Pixel position tolerance
    .setColorTolerance(40) // Color matching tolerance
    .build();
```

## Combining Color with Other Find Methods

The new API makes it easy to chain color finding with other methods:

```java
ChainedFindOptions chainedFind = new ChainedFindOptions.Builder()
    // Step 1: Find all patterns
    .addStep(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.ALL)
        .build())
    // Step 2: Filter by color
    .addStep(new ColorFindOptions.Builder()
        .setTargetColor(Color.YELLOW)
        .setColorTolerance(30)
        .build())
    // Step 3: Filter by size
    .addStep(new SizeFindOptions.Builder()
        .setMinWidth(50)
        .setMinHeight(20)
        .build())
    .setChainMode(ChainedFindOptions.ChainMode.NESTED)
    .build();
```

## Best Practices

1. **Use color as a secondary filter** - Don't rely solely on color
2. **Account for display variations** - Colors may vary across monitors
3. **Test under different lighting** - Ambient light affects color perception
4. **Set appropriate tolerances** - Too strict = misses, too loose = false positives
5. **Consider color spaces** - RGB, HSV, or LAB depending on your needs
6. **Use color histograms** - For more robust color matching

## Migration from ActionOptions

If you're migrating from the old API:

**Old way:**
```java
ActionOptions colorFind = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setColor(Color.RED)
    .setColorTolerance(50)
    .build();
```

**New way:**
```java
ColorFindOptions colorFind = new ColorFindOptions.Builder()
    .setTargetColor(Color.RED)
    .setColorTolerance(50)
    .build();
```

The new API provides better type safety and more color-specific options.

For more information on the new ActionConfig API, see the [Migration Guide](/docs/03-core-library/guides/migration-guide).
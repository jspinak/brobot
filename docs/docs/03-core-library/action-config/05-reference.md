---
sidebar_position: 5
---

# ActionConfig API Reference

Complete reference for all ActionConfig classes and their methods.

> **New to ActionConfig?** Start with the [ActionConfig Overview](./01-overview.md) for concepts and patterns.
>
> **Looking for examples?** See [ActionConfig Examples](./03-examples.md) for practical code samples.
>
> **Migrating from ActionOptions?** Check the [Upgrading to Latest](../migration/upgrading-to-latest.md) guide.

## Quick Reference

### ActionConfig Class Hierarchy

**Find Operations** (extends BaseFindOptions):
- [PatternFindOptions](#patternfindoptions) - Template matching (most common)
- [HistogramFindOptions](#histogramfindoptions) - Color histogram comparison
- [ColorFindOptions](#colorfindoptions) - Color pattern detection
- [TextFindOptions](#textfindoptions) - OCR text recognition
- [MotionFindOptions](#motionfindoptions) - Motion detection
- [VanishOptions](#vanishoptions) - Wait for disappearance
- [FixedPixelsFindOptions](#fixedpixelsfindoptions) - Static pixel detection
- [DynamicPixelsFindOptions](#dynamicpixelsfindoptions) - Animated pixel detection
- [SimilarImagesFindOptions](#similarimagefindoptions) - Image similarity comparison

**Click & Mouse Operations**:
- [ClickOptions](#clickoptions) - Mouse clicks with verification
- [MouseMoveOptions](#mousemoveoptions) - Mouse movement
- [MouseDownOptions](#mousedownoptions) - Mouse button press
- [MouseUpOptions](#mouseupoptions) - Mouse button release
- [ScrollOptions](#scrolloptions) - Mouse wheel scrolling

**Keyboard Operations**:
- [TypeOptions](#typeoptions) - Keyboard input

**Composite Operations**:
- [DragOptions](#dragoptions) - Drag and drop
- [ClickAndTypeOptions](#clickandtypeoptions) - Combined click and type
- [PlaybackOptions](#playbackoptions) - Recorded sequence playback
- [NestedFindsOptions](#nestedfindsoptions) - Nested find operations

**Visual Operations**:
- [DefineRegionOptions](#definereginoptions) - Define screen regions
- [HighlightOptions](#highlightoptions) - Visual highlighting

**Supporting Classes** (not ActionConfig):
- [MousePressOptions](#mousepressoptions) - Mouse button configuration
- [VerificationOptions](#verificationoptions) - Post-action verification
- [RepetitionOptions](#repetitionoptions) - Action repetition
- [ActionChainOptions](#actionchainoptions) - Action chaining configuration

### Common Patterns

**Basic Find and Click**:
```java
action.perform(
    new PatternFindOptions.Builder()
        .setSimilarity(0.85)
        .then(new ClickOptions.Builder().build())
        .build(),
    buttonImage
);
```

**Click Until Verification**:
```java
new ClickOptions.Builder()
    .setVerification(VerificationOptions.builder()
        .setEvent(Event.OBJECTS_VANISH)
        .setObjectCollection(spinnerCollection)
        .build())
    .setRepetition(RepetitionOptions.builder()
        .setMaxTimesToRepeatActionSequence(10)
        .build())
    .build()
```

**Find All Matches**:
```java
new PatternFindOptions.Builder()
    .setStrategy(Strategy.ALL)
    .setMaxMatchesToActOn(-1)
    .build()
```

### Builder Conventions

- **ActionConfig classes**: `new XxxOptions.Builder()` (uppercase)
- **Supporting classes**: `XxxOptions.builder()` (lowercase - Lombok)

### Jump to Section

- [Base Classes](#base-classes) - ActionConfig and BaseFindOptions
- [Find Options](#find-options) - All find operation classes
- [Click Options](#click-options) - Click and mouse press options
- [Type Options](#type-options) - Keyboard input
- [Mouse Movement Options](#mouse-movement-options) - Mouse operations
- [Visual Options](#visual-options) - Visual operations
- [Composite Options](#composite-options) - Combined operations
- [Supporting Classes](#supporting-classes) - Helper classes
- [Enumerations](#enumerations) - All enum types
- [Builder Pattern](#builder-pattern) - Builder usage guide
- [Builder Method Reference](#builder-method-reference) - Complete method tables

## Common Imports

Most examples require these imports:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.mouse.MouseButton;
import org.sikuli.script.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
```

## Base Classes

### ActionConfig

Abstract base class for all action configurations.

```java
public abstract class ActionConfig
```

#### Common Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `pauseBeforeBegin` | `double` | 0.0 | Seconds to wait before executing |
| `pauseAfterEnd` | `double` | 0.0 | Seconds to wait after completion |
| `illustrate` | `Illustrate` | `USE_GLOBAL` | Illustration setting |
| `successCriteria` | `Predicate<ActionResult>` | null | Custom success validation |
| `subsequentActions` | `List<ActionConfig>` | empty | Chained actions |
| `logType` | `LogEventType` | `ACTION` | Log event categorization |

#### Common Methods

- `then(ActionConfig next)` - Chain another action
- `getSubsequentActions()` - Get chained actions
- `getIllustrate()` - Get illustration setting

### BaseFindOptions

Abstract base for all find operations.

```java
public abstract class BaseFindOptions extends ActionConfig
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `similarity` | `double` | 0.7 | Minimum similarity score |
| `searchRegions` | `SearchRegions` | null | Areas to search |
| `captureImage` | `boolean` | true | Capture screenshot |
| `useDefinedRegion` | `boolean` | false | Use predefined region |
| `maxMatchesToActOn` | `int` | -1 | Max matches (-1 = all) |

## Find Options

### PatternFindOptions

Standard image pattern matching using template matching algorithms.

```java
public class PatternFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `strategy` | `Strategy` | `BEST` | Find strategy (FIRST/BEST/ALL/EACH) |
| `matchFusion` | `MatchFusionOptions` | null | How to merge adjacent matches |
| `matchAdjustment` | `MatchAdjustmentOptions` | null | Adjust match position/size |

#### Strategy Options
- `FIRST` - Return first match found (fastest)
- `BEST` - Return highest-scoring match (recommended)
- `ALL` - Return all matches above threshold
- `EACH` - Process each StateImage separately

#### Examples

**Basic Pattern Finding**:
```java
PatternFindOptions find = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setSearchRegions(regions)
    .build();
```

**Find All Matches**:
```java
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.8)
    .setMaxMatchesToActOn(-1)  // Find all
    .build();
```

**With Match Fusion** (merge nearby matches):
```java
PatternFindOptions fusedFind = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .setMatchFusion(MatchFusionOptions.builder()
        .fusionMethod(FusionMethod.RELATIVE)
        .maxFusionDistanceX(10)
        .maxFusionDistanceY(10)
        .build())
    .build();
```

### HistogramFindOptions

Find by color histogram comparison.

```java
public class HistogramFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `binOptions` | `HSVBinOptions` | HSVBinOptions.builder().build() | HSV color space bin configuration |

#### HSVBinOptions Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `hueBins` | `int` | 12 | Hue histogram bins (color discrimination) |
| `saturationBins` | `int` | 2 | Saturation bins (intensity) |
| `valueBins` | `int` | 1 | Value/brightness bins |

#### Example

```java
HistogramFindOptions histogramFind = new HistogramFindOptions.Builder()
    .setSimilarity(0.8)
    .setBinOptions(HSVBinOptions.builder()
        .hueBins(90)
        .saturationBins(2)
        .valueBins(1)
        .build())
    .setMaxMatchesToActOn(5)
    .build();
```

### MotionFindOptions

Detect motion between frames.

```java
public class MotionFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `motionThreshold` | `int` | 25 | Pixel difference threshold |
| `minChangeArea` | `int` | 100 | Minimum changed area |
| `frameDelay` | `int` | 100 | Milliseconds between frames |

### VanishOptions

Wait for element to disappear.

```java
public class VanishOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `timeout` | `double` | 5.0 | Maximum wait time in seconds |

### ColorFindOptions

Find by color patterns using various color detection strategies.

```java
public class ColorFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `colorPattern` | `ColorPattern` | K_MEANS | Color detection method (K_MEANS, MEAN_COLOR, CLASSIFICATION) |
| `diameter` | `int` | 15 | Search diameter for color analysis |
| `binOptions` | `HSVBinOptions` | default | HSV color space binning |

#### Color Pattern Strategies

- `K_MEANS` - K-means clustering for dominant colors (most accurate)
- `MEAN_COLOR` - Simple color averaging (fastest)
- `CLASSIFICATION` - Color classification into predefined categories

#### Examples

**K-Means Color Detection** (find by dominant colors):
```java
ColorFindOptions kmeansFind = new ColorFindOptions.Builder()
    .setSimilarity(0.75)
    .setColorPattern(ColorPattern.K_MEANS)
    .setDiameter(20)
    .build();
```

**Mean Color Detection** (simple averaging):
```java
ColorFindOptions meanColorFind = new ColorFindOptions.Builder()
    .setSimilarity(0.8)
    .setColorPattern(ColorPattern.MEAN_COLOR)
    .setDiameter(15)
    .setBinOptions(HSVBinOptions.builder()
        .hueBins(30)
        .saturationBins(3)
        .valueBins(2)
        .build())
    .build();
```

### TextFindOptions

Find by OCR text recognition using Tesseract.

```java
public class TextFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `maxMatchRetries` | `int` | 1 | Maximum retry attempts for text matching |

#### Usage Notes

- Requires Tesseract OCR to be installed and configured
- Text to search for is provided via `ObjectCollection.withStrings()`
- Works best with clear, high-contrast text
- Consider using higher similarity thresholds for fuzzy text matching

#### Example

```java
TextFindOptions ocrFind = new TextFindOptions.Builder()
    .setSimilarity(0.85)
    .setMaxMatchRetries(3)
    .setSearchRegions(textRegion)
    .build();

// Search for "Submit" button by text
ObjectCollection submitText = ObjectCollection.withStrings("Submit");
ActionResult result = action.perform(ocrFind, submitText);
```

### FixedPixelsFindOptions

Find static/unchanging pixels by analyzing video playback for stationary regions.

```java
public class FixedPixelsFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `maxMovement` | `double` | 5.0 | Maximum pixel movement threshold |
| `startPlayback` | `boolean` | false | Start video playback for analysis |
| `playbackDuration` | `double` | 1.0 | Duration of playback analysis |

#### Use Cases

- Find static UI elements in video content
- Detect pause buttons or static overlays
- Identify non-animated regions during playback

#### Example

```java
FixedPixelsFindOptions staticFind = new FixedPixelsFindOptions.Builder()
    .setSimilarity(0.9)
    .setMaxMovement(3.0)
    .setStartPlayback(true)
    .setPlaybackDuration(2.0)
    .build();

// Find static UI controls during video playback
ActionResult result = action.perform(staticFind, videoControlsRegion);
```

### DynamicPixelsFindOptions

Find changing/animated pixels by analyzing video playback for regions with motion.

```java
public class DynamicPixelsFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `maxMovement` | `double` | 5.0 | Maximum pixel movement threshold |
| `startPlayback` | `boolean` | false | Start video playback for analysis |
| `playbackDuration` | `double` | 1.0 | Duration of playback analysis |

#### Use Cases

- Detect animated elements (spinners, progress bars)
- Find video/animation regions
- Identify moving UI elements

#### Example

```java
DynamicPixelsFindOptions animatedFind = new DynamicPixelsFindOptions.Builder()
    .setSimilarity(0.85)
    .setMaxMovement(10.0)
    .setStartPlayback(true)
    .setPlaybackDuration(1.5)
    .build();

// Find loading spinners or animated indicators
ActionResult result = action.perform(animatedFind, loadingRegion);
```

### SimilarImagesFindOptions

Compare images for similarity using various comparison strategies.

```java
public class SimilarImagesFindOptions extends BaseFindOptions
```

#### Additional Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `comparisonMethod` | `ComparisonMethod` | BEST_MATCH | Comparison strategy |
| `includeNoMatches` | `boolean` | false | Include failed comparisons in results |
| `returnAllScores` | `boolean` | false | Return all comparison scores |

#### Comparison Methods

- `BEST_MATCH` - Find best matching image (template matching)
- `STRUCTURAL_SIMILARITY` - SSIM-based comparison (perceptual quality)
- `HISTOGRAM_COMPARISON` - Color distribution matching

#### Factory Methods

Convenience factory methods for common use cases:

- `forScreenRecognition()` - Optimized for screen capture comparison
- `forDuplicateDetection()` - Detect duplicate images
- `forChangeDetection()` - Detect changes between images

#### Examples

**Screen Recognition** (optimized settings):
```java
SimilarImagesFindOptions screenMatch =
    SimilarImagesFindOptions.forScreenRecognition();

ActionResult result = action.perform(screenMatch, referenceScreenshot);
```

**Duplicate Detection** (find identical images):
```java
SimilarImagesFindOptions duplicateDetect =
    SimilarImagesFindOptions.forDuplicateDetection();

ActionResult result = action.perform(duplicateDetect, imageCollection);
```

**Change Detection** (find differences):
```java
SimilarImagesFindOptions changeDetect =
    SimilarImagesFindOptions.forChangeDetection();

ActionResult result = action.perform(changeDetect, beforeImage, afterImage);
```

**Custom Configuration**:
```java
SimilarImagesFindOptions customCompare = new SimilarImagesFindOptions.Builder()
    .setSimilarity(0.9)
    .setComparisonMethod(ComparisonMethod.STRUCTURAL_SIMILARITY)
    .setReturnAllScores(true)
    .setIncludeNoMatches(false)
    .build();
```

## Click Options

### ClickOptions

Mouse click configuration.

```java
public class ClickOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `numberOfClicks` | `int` | 1 | Number of clicks |
| `pressOptions` | `MousePressOptions` | default | Mouse button config |
| `verificationOptions` | `VerificationOptions` | null | Post-click verification |

#### Example
```java
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.LEFT)
        .setPauseBetweenClicks(0.1)
        .build())
    .build();
```

### MousePressOptions

Reusable mouse press configuration (not an ActionConfig).

```java
public class MousePressOptions
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `button` | `MouseButton` | `LEFT` | Which button to press |
| `pauseBeforeMouseDown` | `double` | 0.0 | Pause before press |
| `pauseAfterMouseDown` | `double` | 0.0 | Pause after press |
| `pauseBeforeMouseUp` | `double` | 0.0 | Pause before release |
| `pauseAfterMouseUp` | `double` | 0.0 | Pause after release |
| `pauseBetweenClicks` | `double` | 0.0 | Pause between multi-clicks |

## Type Options

### TypeOptions

Keyboard input configuration.

```java
public class TypeOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `typeDelay` | `double` | 0.03 | Delay between characters |
| `modifiers` | `String` | `""` | Modifier keys (e.g., "CTRL", "SHIFT", "CTRL+SHIFT") |

**Note**: Text to type is provided via `ObjectCollection.withStrings()`, not through TypeOptions properties. Special keys use string notation like `"{TAB}"`, `"{ENTER}"`, `"{ESC}"`, etc.

#### Example
```java
// Basic typing
TypeOptions typeOptions = new TypeOptions.Builder()
    .setTypeDelay(0.05)
    .build();

action.perform(typeOptions,
    ObjectCollection.withStrings("user@example.com"));

// With modifiers (keyboard shortcuts)
TypeOptions ctrlShiftS = new TypeOptions.Builder()
    .setModifiers("CTRL+SHIFT")
    .build();

action.perform(ctrlShiftS,
    ObjectCollection.withStrings("s"));  // Ctrl+Shift+S

// Special keys in text
action.perform(typeOptions,
    ObjectCollection.withStrings("text{TAB}more{ENTER}"));
```

## Mouse Movement Options

### MouseMoveOptions

Mouse movement configuration.

```java
public class MouseMoveOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `location` | `Location` | null | Target location |
| `moveTime` | `double` | 0.5 | Movement duration |
| `movementPattern` | `MovementPattern` | `SMOOTH` | Movement style |

### MouseDownOptions

Mouse button press (without release).

```java
public class MouseDownOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `pressOptions` | `MousePressOptions` | default | Button configuration |

### MouseUpOptions

Mouse button release.

```java
public class MouseUpOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `pressOptions` | `MousePressOptions` | default | Button configuration |

### ScrollOptions

Mouse wheel scrolling.

```java
public class ScrollOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `direction` | `ScrollDirection` | `DOWN` | Scroll direction |
| `clicks` | `int` | 3 | Number of scroll clicks |
| `pauseBetweenScrolls` | `double` | 0.1 | Pause between scrolls |

## Visual Options

### DefineRegionOptions

Define a screen region.

```java
public class DefineRegionOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `regionType` | `RegionType` | `DYNAMIC` | Type of region |
| `anchorPoint` | `AnchorPoint` | `CENTER` | Anchor position |
| `offsetX` | `int` | 0 | X offset from anchor |
| `offsetY` | `int` | 0 | Y offset from anchor |
| `width` | `int` | 0 | Region width |
| `height` | `int` | 0 | Region height |

### HighlightOptions

Visual highlighting.

```java
public class HighlightOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `duration` | `double` | 2.0 | Highlight duration |
| `color` | `Color` | `RED` | Highlight color |
| `lineWidth` | `int` | 3 | Border width |

## Composite Options

### DragOptions

Drag and drop configuration.

```java
public class DragOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `fromOptions` | `BaseFindOptions` | required | Find source |
| `toOptions` | `BaseFindOptions` | required | Find target |
| `holdTime` | `double` | 0.5 | Hold before drag |
| `dragSpeed` | `DragSpeed` | `NORMAL` | Drag speed |

#### Example
```java
DragOptions drag = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .build();
```

### ClickAndTypeOptions

Combined find, click, and type operation.

```java
public class ClickAndTypeOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `findOptions` | `PatternFindOptions` | required | Find configuration |
| `clickOptions` | `ClickOptions` | default | Click configuration |
| `typeOptions` | `TypeOptions` | required | Type configuration |

#### Example
```java
ClickAndTypeOptions fillField = new ClickAndTypeOptions.Builder()
    .setFindOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setClickOptions(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .setTypeOptions(new TypeOptions.Builder()
        .setTypeDelay(0.05)
        .build())
    .build();

// Text provided via ObjectCollection
action.perform(fillField,
    usernameField,
    ObjectCollection.withStrings("user@example.com"));
```

### PlaybackOptions

Playback recorded automation sequences.

```java
public class PlaybackOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `startPlayback` | `boolean` | false | Begin sequence playback |
| `playbackDuration` | `double` | 1.0 | Duration in seconds |

### NestedFindsOptions

Execute multiple find operations in sequence within previous results.

```java
public class NestedFindsOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `findSteps` | `List<PatternFindOptions>` | empty | Sequential find operations |

#### Example
```java
// Find button within dialog within window
PatternFindOptions findWindow = new PatternFindOptions.Builder()
    .setSimilarity(0.9).build();
PatternFindOptions findDialog = new PatternFindOptions.Builder()
    .setSimilarity(0.85).build();
PatternFindOptions findButton = new PatternFindOptions.Builder()
    .setSimilarity(0.8).build();

NestedFindsOptions nestedFind = new NestedFindsOptions.Builder()
    .setFindSteps(Arrays.asList(findWindow, findDialog, findButton))
    .build();
```

### Click-Until Behavior

**Note**: There is no `ClickUntilOptions` class. Click-until behavior is achieved using `ClickOptions` with `VerificationOptions` and `RepetitionOptions`.

#### Example: Click Until Objects Vanish
```java
StateImage loadingSpinner = new StateImage.Builder()
    .withPattern(new Pattern("loading.png"))
    .build();

ObjectCollection spinnerCollection = new ObjectCollection.Builder()
    .withStateImages(loadingSpinner)
    .build();

ClickOptions clickUntilVanish = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerification(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.OBJECTS_VANISH)
        .setObjectCollection(spinnerCollection)
        .build())
    .setRepetition(RepetitionOptions.builder()
        .setMaxTimesToRepeatActionSequence(10)
        .setPauseBetweenActionSequences(1.0)
        .build())
    .build();

StateImage refreshButton = new StateImage.Builder()
    .withPattern(new Pattern("refresh.png"))
    .build();

action.perform(clickUntilVanish, refreshButton);
```

## Supporting Classes

### VerificationOptions

Post-action verification (not an ActionConfig).

```java
public class VerificationOptions
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `event` | `Event` | `NONE` | What to verify |
| `condition` | `Condition` | `CONTINUE_UNTIL_CONDITION_MET` | How to verify |
| `objectCollection` | `ObjectCollection` | null | Objects to verify |
| `text` | `String` | null | Text to verify |

#### Event Types
- `NONE` - No verification
- `OBJECTS_APPEAR` - Wait for objects to appear
- `OBJECTS_VANISH` - Wait for objects to disappear
- `TEXT_APPEARS` - Wait for text to appear
- `TEXT_VANISHES` - Wait for text to disappear

#### Condition Types
- `CONTINUE_UNTIL_CONDITION_MET` - Keep repeating until condition is true
- `TERMINATE_ON_CONDITION` - Stop when condition becomes true

### RepetitionOptions

Action repetition configuration (not an ActionConfig).

```java
public class RepetitionOptions
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `timesToRepeatIndividualAction` | `int` | 1 | Repeat individual action (e.g., clicks per match) |
| `maxTimesToRepeatActionSequence` | `int` | 1 | Maximum times to repeat entire action sequence |
| `pauseBetweenIndividualActions` | `double` | 0.0 | Pause between individual actions |
| `pauseBetweenActionSequences` | `double` | 0.0 | Pause between action sequences |

### ActionChainOptions

Configuration for chained action execution.

```java
public class ActionChainOptions
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `initialAction` | `ActionConfig` | required | First action |
| `chainedActions` | `List<ActionConfig>` | empty | Subsequent actions |
| `strategy` | `ChainingStrategy` | `NESTED` | Execution strategy |

#### Chaining Strategies
- `NESTED` - Each action searches within previous results
- `CONFIRM` - Each action validates previous results

## Enumerations

### MouseButton

Mouse button identifiers for click and press operations.

```java
public enum MouseButton {
    LEFT,    // Primary mouse button (default)
    RIGHT,   // Context menu button
    MIDDLE   // Scroll wheel button
}
```

**Usage**:
```java
MousePressOptions.builder()
    .setButton(MouseButton.RIGHT)
    .build()
```

### ScrollDirection

Direction for mouse wheel scrolling.

```java
public enum ScrollDirection {
    UP,      // Scroll up (toward top of content)
    DOWN,    // Scroll down (toward bottom of content)
    LEFT,    // Scroll left (horizontal scrolling)
    RIGHT    // Scroll right (horizontal scrolling)
}
```

**Usage**:
```java
ScrollOptions scroll = new ScrollOptions.Builder()
    .setDirection(ScrollDirection.DOWN)
    .setClicks(5)
    .build();
```

### Illustrate

Visual illustration control for debugging and demonstration.

```java
public enum Illustrate {
    YES,         // Always illustrate this action (show highlights)
    NO,          // Never illustrate this action (no visual feedback)
    USE_GLOBAL   // Use framework-wide setting from properties
}
```

**Usage**:
```java
ClickOptions click = new ClickOptions.Builder()
    .setIllustrate(Illustrate.YES)  // Always show visual feedback
    .build();
```

**Configuration**:
```properties
# In application.properties
brobot.illustrate=true   # Global illustration setting
```

### Find Strategy

Pattern finding strategies (PatternFindOptions.Strategy).

```java
public enum Strategy {
    FIRST,   // Return first match found (fastest, non-deterministic order)
    BEST,    // Return highest-scoring match (recommended default)
    ALL,     // Return all matches above threshold
    EACH     // Process each StateImage separately
}
```

**Usage**:
```java
PatternFindOptions find = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setMaxMatchesToActOn(-1)  // Find all matches
    .build();
```

### Verification Events

Post-action verification events (VerificationOptions.Event).

```java
public enum Event {
    NONE,              // No verification
    OBJECTS_APPEAR,    // Wait for objects to become visible
    OBJECTS_VANISH,    // Wait for objects to disappear
    TEXT_APPEARS,      // Wait for text to appear (OCR)
    TEXT_VANISHES      // Wait for text to disappear (OCR)
}
```

**Usage**:
```java
VerificationOptions.builder()
    .setEvent(VerificationOptions.Event.OBJECTS_VANISH)
    .setObjectCollection(spinnerCollection)
    .build()
```

### Chaining Strategy

Action chaining execution strategies (ActionChainOptions).

```java
public enum ChainingStrategy {
    NESTED,    // Each action searches within results of previous action
    CONFIRM    // Each action validates/confirms previous action results
}
```

**Usage**:
```java
ActionChainOptions chain = new ActionChainOptions.Builder()
    .setStrategy(ChainingStrategy.NESTED)
    .build();
```

## Builder Pattern

All ActionConfig classes use the builder pattern with two builder conventions:

### Builder Conventions

**Manual Builders** (uppercase `new Builder()`):
- ActionConfig classes: `ClickOptions`, `PatternFindOptions`, `TypeOptions`, etc.
- Example: `new ClickOptions.Builder()`

**Lombok Builders** (lowercase `.builder()`):
- Supporting classes: `MousePressOptions`, `VerificationOptions`, `RepetitionOptions`
- Example: `MousePressOptions.builder()`

### Basic Pattern

```java
// General pattern
XxxOptions options = new XxxOptions.Builder()
    .setProperty1(value1)
    .setProperty2(value2)
    .build();

// With chaining
ActionConfig chain = new XxxOptions.Builder()
    .setProperty(value)
    .then(new YyyOptions.Builder()
        .setProperty(value)
        .build())
    .build();
```

## Builder Method Reference

### Common ActionConfig Builder Methods

All ActionConfig builders inherit these methods:

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setPauseBeforeBegin(double)` | `double` | Pause in seconds before action starts |
| `setPauseAfterEnd(double)` | `double` | Pause in seconds after action completes |
| `setIllustrate(Illustrate)` | `Illustrate` | Enable/disable visual illustration (YES/NO/USE_GLOBAL) |
| `setSuccessCriteria(Predicate)` | `Predicate<ActionResult>` | Custom success validation logic |
| `setLogType(LogEventType)` | `LogEventType` | Categorize log events |
| `then(ActionConfig)` | `ActionConfig` | Chain another action to execute after this one |
| `build()` | - | Build the ActionConfig instance |

### BaseFindOptions Builder Methods

All find options builders (PatternFindOptions, HistogramFindOptions, etc.) inherit these methods:

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setSimilarity(double)` | `double` | Minimum similarity threshold (0.0-1.0) |
| `setSearchRegions(SearchRegions)` | `SearchRegions` | Regions to search within |
| `setCaptureImage(boolean)` | `boolean` | Whether to capture screenshots |
| `setUseDefinedRegion(boolean)` | `boolean` | Use predefined region instead of searching |
| `setMaxMatchesToActOn(int)` | `int` | Maximum number of matches (-1 for all) |

### PatternFindOptions Builder Methods

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setStrategy(Strategy)` | `Strategy` | Find strategy (FIRST/BEST/ALL/EACH) |
| `setMatchFusion(MatchFusionOptions)` | `MatchFusionOptions` | Configure match merging |
| `setMatchAdjustment(MatchAdjustmentOptions)` | `MatchAdjustmentOptions` | Adjust match positions/sizes |

### ClickOptions Builder Methods

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setNumberOfClicks(int)` | `int` | Number of times to click |
| `setPressOptions(MousePressOptions)` | `MousePressOptions` | Mouse button configuration |
| `setVerification(VerificationOptions)` | `VerificationOptions` | Post-click verification |
| `setRepetition(RepetitionOptions)` | `RepetitionOptions` | Repetition configuration |

### TypeOptions Builder Methods

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setTypeDelay(double)` | `double` | Delay between keystrokes in seconds |
| `setModifiers(String)` | `String` | Modifier keys ("CTRL", "SHIFT", "CTRL+SHIFT", etc.) |

### DragOptions Builder Methods

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setFromOptions(BaseFindOptions)` | `BaseFindOptions` | Find configuration for drag source |
| `setToOptions(BaseFindOptions)` | `BaseFindOptions` | Find configuration for drag target |
| `setHoldTime(double)` | `double` | Time to hold before dragging |
| `setDragSpeed(DragSpeed)` | `DragSpeed` | Speed of drag operation |

### MousePressOptions Builder Methods (Lombok)

Use lowercase `.builder()` method:

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setButton(MouseButton)` | `MouseButton` | Button to press (LEFT/RIGHT/MIDDLE) |
| `setPauseBeforeMouseDown(double)` | `double` | Pause before pressing |
| `setPauseAfterMouseDown(double)` | `double` | Pause after pressing |
| `setPauseBeforeMouseUp(double)` | `double` | Pause before releasing |
| `setPauseAfterMouseUp(double)` | `double` | Pause after releasing |
| `setPauseBetweenClicks(double)` | `double` | Pause between multiple clicks |
| `build()` | - | Build the MousePressOptions |

### VerificationOptions Builder Methods (Lombok)

Use lowercase `.builder()` method:

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setEvent(Event)` | `Event` | What to verify (OBJECTS_APPEAR/VANISH, TEXT_APPEARS/VANISHES) |
| `setCondition(Condition)` | `Condition` | How to verify (CONTINUE_UNTIL_CONDITION_MET, TERMINATE_ON_CONDITION) |
| `setObjectCollection(ObjectCollection)` | `ObjectCollection` | Objects to verify |
| `setText(String)` | `String` | Text to verify |
| `build()` | - | Build the VerificationOptions |

### RepetitionOptions Builder Methods (Lombok)

Use lowercase `.builder()` method:

| Method | Parameter Type | Description |
|--------|----------------|-------------|
| `setTimesToRepeatIndividualAction(int)` | `int` | Repeat individual action N times |
| `setMaxTimesToRepeatActionSequence(int)` | `int` | Maximum action sequence repetitions |
| `setPauseBetweenIndividualActions(double)` | `double` | Pause between individual actions |
| `setPauseBetweenActionSequences(double)` | `double` | Pause between action sequences |
| `build()` | - | Build the RepetitionOptions |
## See Also

### Core Documentation
- **[ActionConfig Overview](./01-overview.md)** - Concepts and architecture
- **[Upgrading to Latest](../migration/upgrading-to-latest.md)** - Migrating from ActionOptions
- **[ActionConfig Examples](./03-examples.md)** - Practical code samples

### Advanced Topics
- **[Action Chaining](./07-action-chaining.md)** - Chaining actions with `then()`
- **[Conditional Actions](./09-conditional-actions.md)** - Repeat-until and conditional patterns
- **[ActionResult Components](./17-actionresult-components.md)** - Understanding action results
- **[Convenience Methods](./18-convenience-methods.md)** - Simpler API for common operations

### Configuration & Testing
- **[ActionConfig Factory](../configuration/action-config-factory.md)** - Factory patterns for configuration
- **[Builder Performance Guide](../advanced/builder-performance-guide.md)** - Optimizing builder usage
- **[Testing Guide](../../04-testing/testing-intro.md)** - Testing automation code

### Related Guides
- **[States in Brobot](../../01-getting-started/states.md)** - State management
- **[Finding Objects](../finding-objects/combining-finds.md)** - Finding strategies
- **[Special Keys Guide](../keyboard/special-keys-guide.md)** - Keyboard and special keys actions

---
sidebar_position: 5
---

# ActionConfig API Reference

Complete reference for all ActionConfig classes and their methods.

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

Standard image pattern matching.

```java
public class PatternFindOptions extends BaseFindOptions
```

#### Example
```java
PatternFindOptions find = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .setSearchRegions(regions)
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
| `hueBins` | `int` | 30 | Hue histogram bins |
| `saturationBins` | `int` | 32 | Saturation bins |
| `valueBins` | `int` | 32 | Value/brightness bins |
| `useGrayscale` | `boolean` | false | Use grayscale histogram |

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
    .setPressOptions(new MousePressOptions.Builder()
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
| `pauseBeforeMouseDown` | `double` | 0.3 | Pause before press |
| `pauseAfterMouseDown` | `double` | 0.3 | Pause after press |
| `pauseBeforeMouseUp` | `double` | 0.3 | Pause before release |
| `pauseAfterMouseUp` | `double` | 0.3 | Pause after release |
| `pauseBetweenClicks` | `double` | 0.3 | Pause between multi-clicks |

## Type Options

### TypeOptions

Keyboard input configuration.

```java
public class TypeOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `text` | `String` | null | Text to type |
| `keys` | `List<Key>` | empty | Special keys to press |
| `modifierKeys` | `String[]` | empty | Modifier keys (ctrl, shift, alt) |
| `pauseBeforeKeyDown` | `double` | 0.0 | Pause before key press |
| `pauseAfterKeyUp` | `double` | 0.0 | Pause after key release |
| `typeDelay` | `double` | 0.03 | Delay between characters |

#### Example
```java
TypeOptions typeWithModifiers = new TypeOptions.Builder()
    .setText("s")
    .setModifierKeys("ctrl", "shift")
    .build();
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

### ScrollMouseWheelOptions

Mouse wheel scrolling.

```java
public class ScrollMouseWheelOptions extends ActionConfig
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

### ClickUntilOptions

Click repeatedly until condition.

```java
public class ClickUntilOptions extends ActionConfig
```

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `clickOptions` | `ClickOptions` | required | Click configuration |
| `untilCondition` | `UntilCondition` | required | Stop condition |
| `conditionObjects` | `ObjectCollection` | null | Objects to monitor |
| `maxIterations` | `int` | 10 | Maximum attempts |
| `delayBetweenClicks` | `double` | 1.0 | Delay between clicks |

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
```java
public enum MouseButton {
    LEFT, RIGHT, MIDDLE
}
```

### ScrollDirection
```java
public enum ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}
```

### Illustrate
```java
public enum Illustrate {
    YES,        // Always illustrate
    NO,         // Never illustrate
    USE_GLOBAL  // Use framework setting
}
```

## Builder Pattern

All ActionConfig classes use the builder pattern:

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

## Common Methods

All builders inherit these methods from `ActionConfig.Builder`:

- `setPauseBeforeBegin(double seconds)`
- `setPauseAfterEnd(double seconds)`
- `setIllustrate(Illustrate setting)`
- `setSuccessCriteria(Predicate<ActionResult> criteria)`
- `setLogType(LogEventType logType)`
- `then(ActionConfig next)`
- `build()`
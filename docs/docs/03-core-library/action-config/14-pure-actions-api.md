# Pure Actions API Reference

## Overview

Pure actions are a new category of actions in Brobot that perform only their core function without any embedded Find operations. This separation provides better testability, clearer code, and more flexible composition.

## Available Pure Actions

### ClickV2

Performs click operations on provided locations, regions, or matches without searching.

```java
@Component("clickV2")
public class ClickV2 implements ActionInterface
```

**Key Features**:
- Clicks on any object convertible to Location
- Supports all click types (left, right, middle, double)
- No Find operations performed

**Usage**:
```java
// Click on a specific location
Location loc = new Location(100, 200);
action.perform(ActionType.CLICK, loc);

// Click on a region's center
Region region = new Region(50, 50, 100, 100);
action.perform(ActionType.CLICK, region);

// Click on matches from a previous Find
ActionResult matches = action.find(targetImage);
matches.getMatchList().forEach(match -> 
    action.perform(ActionType.CLICK, match.getRegion())
);
```

### HighlightV2

Highlights regions without searching for them first.

```java
@Component("highlightV2")
public class HighlightV2 implements ActionInterface
```

**Key Features**:
- Highlights any object with bounds (Region, Match)
- Configurable highlight color and duration
- Works with multiple regions at once

**Usage**:
```java
// Highlight a specific region
Region area = new Region(0, 0, 200, 200);
action.perform(ActionType.HIGHLIGHT, area);

// Highlight with custom options
HighlightOptions options = new HighlightOptions.Builder()
    .setHighlightColor("red")
    .setHighlightDuration(3.0)
    .build();
action.perform(options, new ObjectCollection.Builder()
    .withRegions(area)
    .build());
```

### TypeV2

Types text at the current location or after clicking on a provided location.

```java
@Component("typeV2")
public class TypeV2 implements ActionInterface
```

**Key Features**:
- Types text without searching
- Optional click before typing
- Configurable typing speed and delays
- Clear field functionality

**Usage**:
```java
// Type at current cursor position
action.perform(ActionType.TYPE, "Hello World");

// Click location then type
TypeOptions options = new TypeOptions.Builder()
    .setText("user@example.com")
    .setClickLocationFirst(true)
    .setClearField(true)
    .build();
action.perform(options, new ObjectCollection.Builder()
    .withLocations(textFieldLocation)
    .build());
```

## ActionType Enum

The ActionType enum provides a convenient, discoverable way to specify actions:

```java
public enum ActionType {
    // Mouse actions
    CLICK, DOUBLE_CLICK, RIGHT_CLICK, MIDDLE_CLICK,
    
    // Visual actions
    HIGHLIGHT,
    
    // Keyboard actions
    TYPE, KEY_DOWN, KEY_UP,
    
    // Mouse movement
    HOVER, DRAG, MOUSE_DOWN, MOUSE_UP,
    
    // Scroll actions
    SCROLL_UP, SCROLL_DOWN,
    
    // Wait actions
    WAIT, WAIT_VANISH,
    
    // Verification actions
    EXISTS, FIND
}
```

## Convenience Methods

The Action class provides overloaded perform() methods for convenience:

### perform(ActionType, Location)
```java
Location clickPoint = new Location(100, 200);
action.perform(ActionType.CLICK, clickPoint);
```

### perform(ActionType, Region)
```java
Region area = new Region(50, 50, 200, 100);
action.perform(ActionType.HIGHLIGHT, area);
```

### perform(ActionType, String)
```java
action.perform(ActionType.TYPE, "Hello World");
```

### perform(ActionType, Object...)
```java
// Accepts multiple objects of different types
action.perform(ActionType.CLICK, location1, region1, match1);
```

## ConditionalActionChain

Provides conditional execution of actions based on previous results.

```java
public class ConditionalActionChain
```

> **Note:** The original `ConditionalActionChain` class is now deprecated. Use `ConditionalActionChain` for all new development, which includes improved convenience methods and enhanced functionality.

### Available Methods

#### Static Factory Methods
- `find(PatternFindOptions)` - Start chain with a Find action
- `start(ActionConfig)` - Start chain with any action

#### Conditional Methods
- `ifFound(ActionConfig)` - Execute if previous action succeeded
- `ifFoundClick()` - Click if previous action succeeded (convenience method)
- `ifFoundType(String)` - Type text if previous action succeeded (convenience method)
- `ifNotFound(ActionConfig)` - Execute if previous action failed
- `always(ActionConfig)` - Execute regardless of previous result

#### Logging Methods
- `ifFoundLog(String)` - Log message if found
- `ifNotFoundLog(String)` - Log message if not found

#### Custom Handlers
- `ifFoundDo(Consumer<ActionResult>)` - Custom handler if found
- `ifNotFoundDo(Consumer<ActionResult>)` - Custom handler if not found

### Example Usage

```java
// Using ActionConfig objects
ConditionalActionChain.find(findOptions)
    .ifFound(clickOptions)
    .ifNotFoundLog("Button not found")
    .always(screenshotOptions)
    .perform(action, objectCollection);

// Using convenience methods (recommended for common operations)
ConditionalActionChain.find(findOptions)
    .ifFoundClick()
    .ifNotFoundLog("Button not found")
    .perform(action, objectCollection);
```

## Integration with Existing Actions

Pure actions work seamlessly with existing Brobot actions:

1. **Same ActionInterface**: All pure actions implement the standard ActionInterface
2. **Spring Integration**: Registered as Spring components for dependency injection
3. **ActionResult**: Return standard ActionResult objects
4. **ObjectCollection**: Accept standard ObjectCollection parameters

## Best Practices

### 1. Use Pure Actions for Testing
```java
@Test
public void testClickLogic() {
    // No need to mock Find operations
    ClickV2 click = new ClickV2();
    Location testLocation = new Location(100, 200);
    
    ActionResult result = click.perform(
        new ClickOptions.Builder().build(),
        new ObjectCollection.Builder()
            .withLocations(testLocation)
            .build()
    );
    
    assertTrue(result.isSuccess());
}
```

### 2. Separate Find from Actions
```java
// Good: Explicit separation
ActionResult matches = action.find(target);
if (matches.isSuccess()) {
    action.perform(ActionType.CLICK, matches.getMatchList().get(0));
}

// Better: Use ConditionalActionChain
ConditionalActionChain.find(findOptions)
    .ifFoundClick()
    .perform(action, objectCollection);
```

### 3. Reuse Find Results
```java
ActionResult matches = action.find(allButtons);
for (Match match : matches.getMatchList()) {
    // Highlight with pause after
    HighlightOptions highlight = new HighlightOptions.Builder()
        .setPauseAfterEnd(0.5)  // 500ms pause after highlighting
        .build();
    action.perform(highlight, match.getRegion());
    
    // Then click
    action.perform(ActionType.CLICK, match.getRegion());
}
```

## Migration Path

1. **Phase 1**: Pure actions available as V2 versions
2. **Phase 2**: Old actions deprecated and moved to legacy package
3. **Phase 3**: Pure actions renamed to simple names (Click instead of ClickV2)
4. **Phase 4**: Legacy actions removed

## Error Handling

Pure actions provide clear error messages:

```java
// No clickable objects provided
"No clickable objects provided"

// No text to type
"No text to type provided"

// No highlightable regions
"No highlightable regions provided"
```

## Performance Benefits

1. **No Hidden Find**: Predictable performance without unexpected searches
2. **Batch Operations**: Process multiple items from one Find
3. **Conditional Execution**: Skip unnecessary operations
4. **Clear Metrics**: Separate timing for Find vs Action operations
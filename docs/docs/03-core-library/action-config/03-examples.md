---
sidebar_position: 3
---

# ActionConfig Code Examples

This page provides practical examples for using each ActionConfig class in real-world scenarios.

## Find Actions

### Basic Pattern Finding

```java
// Simple find with default settings
PatternFindOptions find = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .build();

ActionResult result = action.perform(find, loginButton);
```

### Find All Matches

```java
// Find all instances of an icon
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setMaxMatchesToActOn(-1)  // Find all matches
    .setCaptureImage(true)     // Capture screenshot for debugging
    .build();

ActionResult result = action.perform(findAll, icon);
System.out.println("Found " + result.getMatchList().size() + " matches");
```

### Find in Specific Region

```java
Region searchArea = new Region(100, 100, 400, 300);

PatternFindOptions findInRegion = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .setSearchRegions(new SearchRegions(searchArea))
    .build();

ActionResult result = action.perform(findInRegion, targetImage);
```

### Histogram-Based Finding

```java
// Find by color distribution
HistogramFindOptions colorFind = new HistogramFindOptions.Builder()
    .setHueBins(30)
    .setSaturationBins(32)
    .setValueBins(32)
    .build();

ActionResult result = action.perform(colorFind, colorfulImage);
```

## Click Actions

### Simple Click

```java
ClickOptions click = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .build();

action.perform(click, button);
```

### Double Click with Timing

```java
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(new MousePressOptions.Builder()
        .setPauseBetweenClicks(0.1)
        .setPauseBeforeMouseDown(0.2)
        .setPauseAfterMouseUp(0.2)
        .build())
    .build();

action.perform(doubleClick, fileIcon);
```

### Right Click Menu

```java
ClickOptions rightClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();

action.perform(rightClick, targetElement);
```

### Click with Verification

```java
// Click and verify result appears
ClickOptions verifiedClick = new ClickOptions.Builder()
    .setNumberOfClicks(1)
    .setVerificationOptions(new VerificationOptions.Builder()
        .setEvent(VerificationOptions.Event.OBJECTS_APPEAR)
        .setObjectCollection(expectedPopup)
        .build())
    .build();

action.perform(verifiedClick, submitButton);
```

## Type Actions

### Simple Text Entry

```java
TypeOptions typeText = new TypeOptions.Builder()
    .setText("user@example.com")
    .build();

action.perform(typeText);
```

### Type with Delays

```java
TypeOptions slowType = new TypeOptions.Builder()
    .setText("Important message")
    .setPauseBeforeKeyDown(0.1)
    .setPauseAfterKeyUp(0.1)
    .setTypeDelay(0.05)
    .build();

action.perform(slowType);
```

### Keyboard Shortcuts

```java
// Ctrl+A (Select All)
TypeOptions selectAll = new TypeOptions.Builder()
    .setText("a")
    .setModifierKeys("ctrl")
    .build();

action.perform(selectAll);

// Ctrl+Shift+S (Save As)
TypeOptions saveAs = new TypeOptions.Builder()
    .setText("s")
    .setModifierKeys("ctrl", "shift")
    .build();

action.perform(saveAs);
```

### Special Keys

```java
// Navigate with Tab and Enter
TypeOptions navigate = new TypeOptions.Builder()
    .setKeys(Key.TAB, Key.TAB, Key.ENTER)
    .setPauseBetweenKeys(0.3)
    .build();

action.perform(navigate);
```

## Mouse Actions

### Move to Location

```java
Location targetPos = new Location(500, 300);

MouseMoveOptions moveTo = new MouseMoveOptions.Builder()
    .setLocation(targetPos)
    .setMoveTime(1.0)  // 1 second movement
    .build();

action.perform(moveTo);
```

### Custom Drag Operation

```java
// Manual drag using mouse down/move/up
MouseDownOptions startDrag = new MouseDownOptions.Builder()
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.LEFT)
        .setPauseAfterMouseDown(0.5)
        .build())
    .build();

MouseMoveOptions dragTo = new MouseMoveOptions.Builder()
    .setLocation(dropLocation)
    .setMoveTime(2.0)  // Slow drag
    .build();

MouseUpOptions endDrag = new MouseUpOptions.Builder()
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.LEFT)
        .build())
    .build();

// Execute drag sequence
action.perform(startDrag, dragSource);
action.perform(dragTo);
action.perform(endDrag);
```

### Scroll

```java
ScrollMouseWheelOptions scrollDown = new ScrollMouseWheelOptions.Builder()
    .setDirection(ScrollDirection.DOWN)
    .setClicks(5)
    .setPauseBetweenScrolls(0.1)
    .build();

action.perform(scrollDown, scrollableArea);
```

## Visual Actions

### Define Region

```java
// Define a region relative to an anchor
DefineRegionOptions defineArea = new DefineRegionOptions.Builder()
    .setAnchorPoint(AnchorPoint.TOP_LEFT)
    .setOffsetX(50)
    .setOffsetY(100)
    .setWidth(300)
    .setHeight(200)
    .build();

ActionResult result = action.perform(defineArea, anchorImage);
Region definedRegion = result.getDefinedRegion();
```

### Highlight

```java
// Highlight important area
HighlightOptions highlight = new HighlightOptions.Builder()
    .setDuration(3.0)
    .setColor(Color.RED)
    .setLineWidth(3)
    .build();

action.perform(highlight, importantArea);
```

## Composite Actions

### Drag and Drop

```java
DragOptions dragDrop = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setHoldTime(0.5)
    .build();

ActionResult result = action.perform(
    dragDrop, 
    sourceCollection, 
    targetCollection
);

// Get the movement performed
Movement movement = result.getMovement().orElse(null);
```

### Click Until Condition

```java
// Keep clicking refresh until loading disappears
ClickUntilOptions refreshUntilLoaded = new ClickUntilOptions.Builder()
    .setClickOptions(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build())
    .setUntilCondition(ClickUntilOptions.UntilCondition.OBJECTS_VANISH)
    .setConditionObjects(loadingSpinner)
    .setMaxIterations(10)
    .setDelayBetweenClicks(2.0)
    .build();

action.perform(refreshUntilLoaded, refreshButton);
```

## Action Chaining

### Login Sequence

```java
// Complete login flow
ActionConfig loginFlow = new PatternFindOptions.Builder()
    .setSimilarity(0.9)
    .then(new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .then(new TypeOptions.Builder()
            .setText("username@example.com")
            .then(new TypeOptions.Builder()
                .setKeys(Key.TAB)
                .then(new TypeOptions.Builder()
                    .setText("password123")
                    .then(new PatternFindOptions.Builder()
                        .setSimilarity(0.9)
                        .then(new ClickOptions.Builder()
                            .setNumberOfClicks(1)
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

ActionResult result = action.perform(
    loginFlow, 
    usernameField, 
    passwordField, 
    loginButton
);
```

### Form Filling

```java
// Fill multiple form fields
ActionConfig fillForm = new PatternFindOptions.Builder()
    .setSimilarity(0.85)
    .then(new ClickOptions.Builder()
        .then(new TypeOptions.Builder()
            .setText("John Doe")
            .then(new TypeOptions.Builder()
                .setKeys(Key.TAB)
                .then(new TypeOptions.Builder()
                    .setText("john@example.com")
                    .then(new TypeOptions.Builder()
                        .setKeys(Key.TAB)
                        .then(new TypeOptions.Builder()
                            .setText("555-1234")
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

action.perform(fillForm, nameField);
```

## Error Handling

### Check Action Success

```java
ActionResult result = action.perform(clickOptions, button);

if (!result.isSuccess()) {
    logger.error("Action failed: " + result.getFailureReason());
    // Handle failure
} else {
    logger.info("Successfully clicked at: " + 
        result.getFirstMatch().getLocation());
}
```

### Custom Success Criteria

```java
PatternFindOptions findMultiple = new PatternFindOptions.Builder()
    .setSimilarity(0.8)
    .setSuccessCriteria(result -> 
        result.getMatchList().size() >= 3)  // Need at least 3 matches
    .build();

ActionResult result = action.perform(findMultiple, icon);
```

### Handle Execution History

```java
// Execute complex chain
ActionResult result = action.perform(complexChain, objects);

// Check each step
for (ActionRecord record : result.getExecutionHistory()) {
    if (!record.isActionSuccess()) {
        logger.error("Failed at step: " + record.getActionType());
        logger.error("Duration: " + record.getDuration() + "s");
        break;
    }
}
```

## Best Practices

### 1. Use Specific Options Classes

```java
// Good: Type-safe and clear
ClickOptions click = new ClickOptions.Builder().build();

// Bad: Using generic ActionConfig
ActionConfig click = new ClickOptions.Builder().build();
```

### 2. Configure Timing Appropriately

```java
// Good: Explicit timing for reliability
TypeOptions reliableType = new TypeOptions.Builder()
    .setText("important data")
    .setPauseBeforeKeyDown(0.1)
    .setPauseAfterKeyUp(0.1)
    .setTypeDelay(0.05)
    .build();
```

### 3. Use Builders Fluently

```java
// Good: Readable and maintainable
ActionConfig workflow = find(0.9)
    .then(click())
    .then(type("Hello"))
    .then(find(0.9))
    .then(click());
```

### 4. Handle All Results

```java
// Good: Check results and handle failures
ActionResult result = action.perform(options, target);
if (result.isSuccess()) {
    processSuccess(result);
} else {
    handleFailure(result);
}
```
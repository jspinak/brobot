---
sidebar_position: 1
title: 'Basic Actions API Reference'
description: 'Complete reference for Brobot core action methods'
keywords: [action, api, click, type, find, move, drag, scroll, reference]
---

# Basic Actions API Reference

This is the comprehensive reference for Brobot's core action methods. All methods are accessed through the `Action` class, which is autowired into your Spring components.

## Getting Started

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ActionResult;

@Component
public class MyAutomation {
    @Autowired
    private Action action;

    public void performAutomation() {
        // Use action methods here
    }
}
```

> **💡 Important**: Always use `Action` class methods. Never call SikuliX methods directly. See [CLAUDE.md](/CLAUDE.md) for API usage guidelines.

---

## Click Actions

### click()

Performs a single left-click on a target.

**Method Signatures:**

```java
// Click on StateImage
ActionResult click(StateImage... stateImages)

// Click on StateImage with options
ActionResult click(ClickOptions options, StateImage... stateImages)

// Click at specific location
ActionResult click(Location location)

// Click at position name
ActionResult click(Positions.Name position)
```

**Parameters:**
- `stateImages` - One or more StateImage objects to find and click
- `options` - ClickOptions to configure click behavior
- `location` - Exact coordinates to click
- `position` - Predefined screen position (TOPLEFT, MIDDLEMIDDLE, etc.)

**Returns:** `ActionResult` containing success status, match locations, and timing information

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.click.ClickOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;

// Simple click on image
ActionResult result = action.click(submitButton);

// Click with custom options
ClickOptions options = new ClickOptions.Builder()
    .withClickUntil(ClickUntil.OBJECTS_VANISH)
    .withMaxClicks(3)
    .withPauseBetweenClicks(0.5)
    .build();
ActionResult result = action.click(options, dialogCloseButton);

// Click at screen center
ActionResult result = action.click(new Location(Positions.Name.MIDDLEMIDDLE));
```

**See Also:**
- [ClickOptions Reference](../action-config/05-reference.md#clickoptions)
- [ActionResult Architecture](../architecture/actionresult-architecture.md)

---

### doubleClick()

Performs a double-click (two rapid clicks) on a target.

**Method Signatures:**

```java
ActionResult doubleClick(StateImage... stateImages)
ActionResult doubleClick(ClickOptions options, StateImage... stateImages)
ActionResult doubleClick(Location location)
```

**Parameters:**
- Same as `click()` method

**Returns:** `ActionResult`

**Example:**

```java
// Double-click to open item
ActionResult result = action.doubleClick(folderIcon);

// Double-click with options
ClickOptions options = new ClickOptions.Builder()
    .withBeforeActionLog("Opening folder...")
    .withSuccessLog("Folder opened")
    .build();
ActionResult result = action.doubleClick(options, documentIcon);
```

---

### rightClick()

Performs a right-click to open context menu.

**Method Signatures:**

```java
ActionResult rightClick(StateImage... stateImages)
ActionResult rightClick(ClickOptions options, StateImage... stateImages)
ActionResult rightClick(Location location)
```

**Parameters:**
- Same as `click()` method

**Returns:** `ActionResult`

**Example:**

```java
// Right-click for context menu
ActionResult result = action.rightClick(fileItem);

// Right-click and verify menu appears
ClickOptions options = new ClickOptions.Builder()
    .withClickUntil(ClickUntil.OBJECTS_APPEAR)
    .build();
ActionResult result = action.rightClick(options, tableRow);
```

---

## Find Actions

### find()

Searches for StateImage objects on screen without performing any action.

**Method Signatures:**

```java
ActionResult find(StateImage... stateImages)
ActionResult find(PatternFindOptions options, StateImage... stateImages)
ActionResult find(int maxMatches, StateImage... stateImages)
```

**Parameters:**
- `stateImages` - One or more StateImage objects to search for
- `options` - PatternFindOptions to configure search behavior
- `maxMatches` - Maximum number of matches to find (default: 1)

**Returns:** `ActionResult` with match locations and similarity scores

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.find.PatternFindOptions;

// Simple find
ActionResult result = action.find(loginButton);
if (result.isSuccess()) {
    System.out.println("Found at: " + result.getMatchLocations().get(0));
}

// Find with options
PatternFindOptions options = new PatternFindOptions.Builder()
    .withMinSimilarity(0.85)
    .withMaxWait(5.0)
    .build();
ActionResult result = action.find(options, targetImage);

// Find multiple matches
ActionResult result = action.find(5, iconPattern);
System.out.println("Found " + result.getMatchLocations().size() + " matches");
```

**See Also:**
- [PatternFindOptions Reference](../action-config/05-reference.md#patternfindoptions)
- [Finding Objects Guide](../finding-objects/combining-finds.md)

---

## Type Actions

### type()

Types text at the current cursor location or at a found StateImage.

**Method Signatures:**

```java
ActionResult type(String text)
ActionResult type(TypeOptions options, String text)
ActionResult type(StateImage stateImage, String text)
ActionResult type(TypeOptions options, StateImage stateImage, String text)
```

**Parameters:**
- `text` - The string to type
- `options` - TypeOptions to configure typing behavior
- `stateImage` - Optional StateImage to click before typing

**Returns:** `ActionResult`

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.type.TypeOptions;

// Type at current cursor position
action.type("username@example.com");

// Click field then type
action.type(usernameField, "john.doe");

// Type with options
TypeOptions options = new TypeOptions.Builder()
    .withClearFieldFirst(true)
    .withTypeDelay(0.1)
    .build();
action.type(options, passwordField, "myPassword123");
```

**Security Note:** For sensitive data like passwords, use environment variables:
```java
String password = System.getenv("APP_PASSWORD");
action.type(passwordField, password);
```

**See Also:**
- [TypeOptions Reference](../action-config/05-reference.md#typeoptions)
- [Security Best Practices](../user-guides/annotations.md)

---

## Move Actions

### move()

Moves the mouse cursor to a location without clicking.

**Method Signatures:**

```java
ActionResult move(StateImage... stateImages)
ActionResult move(MoveOptions options, StateImage... stateImages)
ActionResult move(Location location)
ActionResult move(Positions.Name position)
```

**Parameters:**
- `stateImages` - StateImage to move to
- `options` - MoveOptions to configure movement
- `location` - Exact coordinates
- `position` - Predefined screen position

**Returns:** `ActionResult`

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.move.MoveOptions;

// Move to image
action.move(hoverTarget);

// Move to specific location
action.move(new Location(100, 200));

// Move to screen position
action.move(Positions.Name.TOPRIGHT);

// Move with options
MoveOptions options = new MoveOptions.Builder()
    .withBeforeActionLog("Hovering over menu...")
    .build();
action.move(options, menuItem);
```

---

## Drag and Drop Actions

### drag()

Initiates a drag operation from a source location.

**Method Signatures:**

```java
ActionResult drag(StateImage sourceImage)
ActionResult drag(DragOptions options, StateImage sourceImage)
ActionResult drag(Location sourceLocation)
```

**Parameters:**
- `sourceImage` - StateImage to drag from
- `options` - DragOptions to configure drag behavior
- `sourceLocation` - Exact coordinates to drag from

**Returns:** `ActionResult`

### drop()

Completes a drag operation by dropping at target location.

**Method Signatures:**

```java
ActionResult drop(StateImage targetImage)
ActionResult drop(DragOptions options, StateImage targetImage)
ActionResult drop(Location targetLocation)
```

**Parameters:**
- `targetImage` - StateImage to drop on
- `options` - DragOptions to configure drop behavior
- `targetLocation` - Exact coordinates to drop at

**Returns:** `ActionResult`

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.drag.DragOptions;

// Simple drag and drop
action.drag(fileIcon);
action.drop(folderIcon);

// Drag and drop with options
DragOptions options = new DragOptions.Builder()
    .withBeforeActionLog("Moving file to folder...")
    .withSuccessLog("File moved successfully")
    .withPauseBetweenActions(0.5)
    .build();

action.drag(options, sourceElement);
action.drop(options, destinationElement);

// Drag to specific coordinates
action.drag(dragHandle);
action.drop(new Location(500, 300));
```

**See Also:**
- [DragOptions Reference](../action-config/05-reference.md#dragoptions)

---

## Scroll Actions

### scroll()

Scrolls the screen or a specific region.

**Method Signatures:**

```java
ActionResult scroll(int clicks)
ActionResult scroll(ScrollOptions options, int clicks)
ActionResult scroll(StateImage targetRegion, int clicks)
ActionResult scroll(ScrollOptions options, StateImage targetRegion, int clicks)
```

**Parameters:**
- `clicks` - Number of scroll clicks (positive = down/right, negative = up/left)
- `options` - ScrollOptions to configure scroll behavior
- `targetRegion` - Optional StateImage to scroll within

**Returns:** `ActionResult`

**Example:**

```java
import io.github.jspinak.brobot.actions.composites.methods.scroll.ScrollOptions;

// Scroll down 5 clicks
action.scroll(5);

// Scroll up 3 clicks
action.scroll(-3);

// Scroll within specific region
action.scroll(scrollableList, 10);

// Scroll with options
ScrollOptions options = new ScrollOptions.Builder()
    .withDirection(ScrollOptions.Direction.VERTICAL)
    .withPauseBetweenScrolls(0.2)
    .build();
action.scroll(options, 5);
```

---

## Wait Actions

### wait()

Waits for a condition to be met or for a specific duration.

**Method Signatures:**

```java
void wait(double seconds)
ActionResult waitUntilFound(StateImage... stateImages)
ActionResult waitUntilFound(double maxWait, StateImage... stateImages)
ActionResult waitUntilVanish(StateImage... stateImages)
ActionResult waitUntilVanish(double maxWait, StateImage... stateImages)
```

**Parameters:**
- `seconds` - Duration to wait in seconds
- `maxWait` - Maximum time to wait for condition (default: 10.0)
- `stateImages` - StateImage objects to wait for

**Returns:** `ActionResult` (for conditional waits) or void (for duration waits)

**Example:**

```java
// Wait for fixed duration
action.wait(2.5);  // Wait 2.5 seconds

// Wait until element appears
ActionResult result = action.waitUntilFound(loadingSpinner);
if (result.isSuccess()) {
    System.out.println("Loading started");
}

// Wait until element disappears
ActionResult result = action.waitUntilVanish(30.0, loadingSpinner);
if (result.isSuccess()) {
    System.out.println("Loading complete");
}

// Wait with timeout
ActionResult result = action.waitUntilFound(5.0, errorMessage);
if (!result.isSuccess()) {
    System.out.println("Element did not appear within 5 seconds");
}
```

---

## ActionResult

All action methods return an `ActionResult` object containing:

**Key Properties:**
- `isSuccess()` - Whether the action succeeded
- `getMatchLocations()` - List of Match objects with locations
- `getDuration()` - Time taken to execute action
- `getText()` - Any text associated with the action
- `getActionConfig()` - The ActionConfig used

**Example Usage:**

```java
ActionResult result = action.click(submitButton);

if (result.isSuccess()) {
    System.out.println("Click successful");
    System.out.println("Location: " + result.getMatchLocations().get(0));
    System.out.println("Duration: " + result.getDuration() + "s");
} else {
    System.out.println("Click failed - element not found");
}

// Check match similarity
if (!result.getMatchLocations().isEmpty()) {
    double similarity = result.getMatchLocations().get(0).getSimilarity();
    System.out.println("Match similarity: " + similarity);
}
```

**See Also:**
- [ActionResult Architecture](../architecture/actionresult-architecture.md)

---

## StateImage

StateImage objects represent visual patterns to find on screen.

**Basic Usage:**

```java
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

// In a @State class
@State
public class LoginState {

    @StateImage
    private StateImage loginButton;  // Auto-loaded from images/LoginState/loginButton.png

    @StateImage
    private StateImage usernameField;

    // StateImages are automatically populated by Brobot
}
```

**Builder Pattern:**

```java
StateImage customImage = new StateImage.Builder()
    .withName("customButton")
    .withFilename("images/buttons/submit.png")
    .withSimilarity(0.90)
    .build();
```

**See Also:**
- [States Guide](../../01-getting-started/states.md)
- [StateImage Patterns](../user-guides/search-regions-and-fixed-locations.md)

---

## ActionConfig Options

All action methods can be configured using Options builders:

**Common Options Classes:**
- `PatternFindOptions` - Configure find behavior
- `ClickOptions` - Configure click behavior
- `TypeOptions` - Configure typing behavior
- `DragOptions` - Configure drag/drop behavior
- `ScrollOptions` - Configure scroll behavior
- `MoveOptions` - Configure mouse movement

**See:** [Complete ActionConfig Reference](../action-config/05-reference.md)

---

## Quick Reference Table

| Action | Method | Common Use Case |
|--------|--------|----------------|
| **Find** | `action.find(image)` | Locate elements without interaction |
| **Click** | `action.click(image)` | Click buttons, links, controls |
| **Double Click** | `action.doubleClick(image)` | Open files, folders |
| **Right Click** | `action.rightClick(image)` | Open context menus |
| **Type** | `action.type(field, text)` | Enter text in forms |
| **Move** | `action.move(image)` | Hover for tooltips, menus |
| **Drag** | `action.drag(source)` | Start drag operation |
| **Drop** | `action.drop(target)` | Complete drag operation |
| **Scroll** | `action.scroll(clicks)` | Navigate long lists, pages |
| **Wait** | `action.wait(seconds)` | Pause execution |
| **Wait Until Found** | `action.waitUntilFound(image)` | Wait for element to appear |
| **Wait Until Vanish** | `action.waitUntilVanish(image)` | Wait for element to disappear |

---

## Best Practices

### 1. Always Check ActionResult

```java
ActionResult result = action.click(submitButton);
if (!result.isSuccess()) {
    // Handle failure - element not found or action failed
    throw new RuntimeException("Failed to click submit button");
}
```

### 2. Use ActionConfig for Complex Scenarios

```java
ClickOptions options = new ClickOptions.Builder()
    .withBeforeActionLog("Clicking submit...")
    .withSuccessLog("Form submitted")
    .withFailureLog("Submit failed - button not found")
    .withMaxWait(10.0)
    .build();

ActionResult result = action.click(options, submitButton);
```

### 3. Handle Timing Issues

```java
// Wait for element before interacting
action.waitUntilFound(5.0, loginButton);
action.click(loginButton);

// Or use maxWait in options
PatternFindOptions options = new PatternFindOptions.Builder()
    .withMaxWait(5.0)
    .build();
action.click(options, loginButton);
```

### 4. Use Appropriate Similarity Thresholds

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withMinSimilarity(0.85)  // Lower for variable elements
    .build();
action.find(options, dynamicContent);
```

---

## Related Documentation

### Core Guides
- [Getting Started - Quick Start](../../01-getting-started/quick-start.md)
- [States and Transitions](../../01-getting-started/states.md)
- [ActionConfig Overview](../action-config/01-overview.md)

### Advanced Topics
- [Complete ActionConfig Reference](../action-config/05-reference.md)
- [Finding Objects Guide](../finding-objects/combining-finds.md)
- [Error Handling](../user-guides/annotations.md)

### Testing
- [Mock Mode Testing](../../04-testing/mock-mode-guide.md)
- [Unit Testing](../../04-testing/unit-testing.md)

### API Documentation
- [ActionResult Architecture](../architecture/actionresult-architecture.md)
- [BrobotProperties Reference](../configuration/properties-reference.md)

---

## Examples

For complete working examples, see:
- [Quick Start Tutorial](../../01-getting-started/quick-start.md)
- [Tutorial Basics](../../02-tutorials/tutorial-basics/index.md)
- [ActionConfig Examples](../action-config/03-examples.md)

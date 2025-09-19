---
sidebar_position: 18
---

# Convenience Methods

Brobot 2.1 introduces convenience methods that dramatically simplify common automation tasks. These methods provide direct access to frequently-used operations without the verbosity of creating ObjectCollections.

## Overview

The convenience methods follow a hybrid approach:
- **Convenience**: Direct methods for single-object operations
- **Consistency**: All methods internally delegate to the canonical ObjectCollection-based implementations
- **Compatibility**: Existing code continues to work unchanged

## Before and After

### The Traditional Approach

Previously, every action required wrapping objects in an ObjectCollection:

```java
// Clicking a region - verbose
ObjectCollection oc = new ObjectCollection.Builder()
    .withRegions(region)
    .build();
ActionResult result = action.click(oc);

// Typing text - verbose
ObjectCollection textOc = new ObjectCollection.Builder()
    .withStrings("Hello World")
    .build();
action.type(textOc);

// Moving to a location - verbose
MouseMoveOptions moveOptions = new MouseMoveOptions.Builder().build();
ObjectCollection locOc = new ObjectCollection.Builder()
    .withLocations(location)
    .build();
action.perform(moveOptions, locOc);
```

### The New Convenience Methods

With convenience methods, common operations become one-liners:

```java
// Clicking a region - simple
ActionResult result = action.click(region);

// Typing text - simple
action.type("Hello World");

// Moving to a location - simple
action.move(location);
```

## Available Convenience Methods

### Click Operations

```java
// Click on a specific region
action.click(region);

// Click at a specific location
action.click(location);

// Click on a previously found match
Match match = action.find(pattern).getBestMatch().orElse(null);
if (match != null) {
    action.click(match);
}
```

### Type Operations

```java
// Type plain text
action.type("user@example.com");

// Type in a specific field (find and click, then type)
action.click(usernameField);
action.type("username123");
```

### Find Operations

```java
// Find a pattern directly
Pattern pattern = new Pattern("button.png");
ActionResult result = action.find(pattern);

// Traditional approach still works
StateImage image = new StateImage.Builder()
    .addPattern(pattern)
    .build();
ActionResult result = action.find(image);
```

### Mouse Movement

```java
// Move to a specific location
Location loc = new Location(500, 300);
action.move(loc);

// Move to center of a region
Region region = new Region(100, 100, 200, 200);
action.move(region);

// Move to a match
Match match = findResult.getBestMatch().orElse(null);
if (match != null) {
    action.move(match);
}
```

### Highlighting

```java
// Highlight a region for debugging
Region searchArea = new Region(0, 0, 800, 600);
action.highlight(searchArea);

// Highlight a found match
Match match = action.find(pattern).getBestMatch().orElse(null);
if (match != null) {
    action.highlight(match);
}
```

### Drag Operations

```java
// Drag from one location to another
Location from = new Location(100, 100);
Location to = new Location(500, 500);
action.drag(from, to);

// Drag between region centers
Region sourceRegion = new Region(50, 50, 100, 100);
Region targetRegion = new Region(400, 400, 100, 100);
action.drag(sourceRegion, targetRegion);
```

### Scrolling

```java
// Scroll down 3 steps
action.scroll(ScrollOptions.Direction.DOWN, 3);

// Scroll up 5 steps
action.scroll(ScrollOptions.Direction.UP, 5);
```

### Waiting for Elements to Vanish

```java
// Wait for a pattern to disappear
Pattern loadingSpinner = new Pattern("loading.png");
action.vanish(loadingSpinner);

// Wait for a StateImage to vanish
action.vanish(loadingImage);
```

## Common Workflows

### Example 1: Login Form Automation

```java
@Autowired
private Action action;

public void login(String username, String password) {
    // Click username field and type
    action.click(usernameField);
    action.type(username);

    // Click password field and type
    action.click(passwordField);
    action.type(password);

    // Click submit button
    action.click(submitButton);

    // Wait for loading to complete
    action.vanish(loadingSpinner);
}
```

### Example 2: Drag and Drop File Upload

```java
public void uploadFile() {
    // Find the file to upload
    ActionResult fileResult = action.find(fileIcon);

    // Find the upload area
    ActionResult uploadResult = action.find(uploadArea);

    // Drag the file to upload area
    if (fileResult.isSuccess() && uploadResult.isSuccess()) {
        Match file = fileResult.getBestMatch().orElse(null);
        Match upload = uploadResult.getBestMatch().orElse(null);

        if (file != null && upload != null) {
            action.drag(file.getRegion(), upload.getRegion());
        }
    }
}
```

### Example 3: Search and Navigation

```java
public void searchAndNavigate(String searchTerm) {
    // Click on search box
    action.click(searchBox);

    // Type search term
    action.type(searchTerm);

    // Press Enter (using traditional approach for special keys)
    action.type(new ObjectCollection.Builder()
        .withStrings("\n")
        .build());

    // Wait for results to load
    action.vanish(loadingIndicator);

    // Find and click first result
    ActionResult results = action.find(searchResult);
    results.getBestMatch().ifPresent(match -> {
        action.click(match);
    });
}
```

### Example 4: Form Validation with Highlighting

```java
public boolean validateForm() {
    // Find all required fields
    ActionResult nameResult = action.find(nameField);
    ActionResult emailResult = action.find(emailField);
    ActionResult phoneResult = action.find(phoneField);

    // Highlight missing fields
    if (!nameResult.isSuccess()) {
        action.highlight(nameFieldRegion);
        return false;
    }

    if (!emailResult.isSuccess()) {
        action.highlight(emailFieldRegion);
        return false;
    }

    if (!phoneResult.isSuccess()) {
        action.highlight(phoneFieldRegion);
        return false;
    }

    return true;
}
```

## Best Practices

### When to Use Convenience Methods

✅ **Use convenience methods for:**
- Single-object operations
- Simple, straightforward actions
- Rapid prototyping
- Clear, readable automation scripts
- Working with results from previous finds

### When to Use ObjectCollections

✅ **Use ObjectCollections for:**
- Multiple objects in a single operation
- Complex search criteria
- Operations requiring specific configurations
- When you need fine-grained control

### Combining Approaches

You can freely mix convenience methods with traditional approaches:

```java
// Use convenience method for simple find
ActionResult result = action.find(pattern);

// Use ObjectCollection for complex multi-object operation
if (result.isSuccess()) {
    ObjectCollection complexCollection = new ObjectCollection.Builder()
        .withRegions(result.getBestMatch().get().getRegion())
        .withDefinedSearchRegion(customSearchArea)
        .withStrings("Complex Text")
        .build();

    // Perform complex action with full configuration
    TypeOptions typeOptions = new TypeOptions.Builder()
        .setModifiers("CTRL")
        .setPauseAfterKeyDown(0.5)
        .build();

    action.perform(typeOptions, complexCollection);
}
```

## Method Reference

### Click Methods

| Method | Description | Since |
|--------|-------------|--------|
| `click(Region region)` | Click on a region | 2.1 |
| `click(Location location)` | Click at a specific location | 2.1 |
| `click(Match match)` | Click on a match's region | 2.1 |

### Type Methods

| Method | Description | Since |
|--------|-------------|--------|
| `type(String text)` | Type plain text | 2.1 |

### Find Methods

| Method | Description | Since |
|--------|-------------|--------|
| `find(Pattern pattern)` | Find a pattern on screen | 2.1 |

### Move Methods

| Method | Description | Since |
|--------|-------------|--------|
| `move(Location location)` | Move mouse to location | 2.1 |
| `move(Region region)` | Move to region center | 2.1 |
| `move(Match match)` | Move to match center | 2.1 |

### Highlight Methods

| Method | Description | Since |
|--------|-------------|--------|
| `highlight(Region region)` | Highlight a region | 2.1 |
| `highlight(Match match)` | Highlight a match | 2.1 |

### Drag Methods

| Method | Description | Since |
|--------|-------------|--------|
| `drag(Location from, Location to)` | Drag between locations | 2.1 |
| `drag(Region from, Region to)` | Drag between region centers | 2.1 |

### Scroll Methods

| Method | Description | Since |
|--------|-------------|--------|
| `scroll(Direction dir, int steps)` | Scroll mouse wheel | 2.1 |

### Vanish Methods

| Method | Description | Since |
|--------|-------------|--------|
| `vanish(StateImage image)` | Wait for image to disappear | 2.1 |
| `vanish(Pattern pattern)` | Wait for pattern to disappear | 2.1 |

## Implementation Details

### Architecture

All convenience methods follow the same pattern:

1. **Thin Wrapper**: Each convenience method is a thin wrapper
2. **Delegation**: Internally creates an ObjectCollection and delegates to the canonical method
3. **Consistency**: Ensures all code paths go through the same validation, logging, and processing

Example implementation:

```java
public ActionResult click(Region region) {
    // Create ObjectCollection with the region
    ObjectCollection collection = new ObjectCollection.Builder()
        .withRegions(region)
        .build();

    // Delegate to canonical click method
    return click(collection);
}
```

### Benefits

- **No Code Duplication**: Business logic remains in one place
- **Consistent Behavior**: All clicks work the same way regardless of how they're called
- **Easy Maintenance**: Changes to click behavior only need to be made once
- **Full Compatibility**: Existing code continues to work without modifications

## Migration Guide

### Migrating Existing Code

You can migrate existing code gradually:

```java
// Old code - still works
ObjectCollection oc = new ObjectCollection.Builder()
    .withRegions(region)
    .build();
action.click(oc);

// New code - cleaner
action.click(region);
```

### No Breaking Changes

The convenience methods are purely additive - no existing functionality has been removed or changed. You can:
- Continue using ObjectCollections everywhere
- Gradually adopt convenience methods where they make sense
- Mix both approaches in the same codebase

## Summary

The convenience methods in Brobot 2.1 significantly improve developer experience by:

- **Reducing Boilerplate**: Common operations require 75% less code
- **Improving Readability**: Code reads more like natural language
- **Maintaining Flexibility**: Complex operations still have full ObjectCollection support
- **Preserving Architecture**: All benefits of the model-based approach are maintained

These methods make Brobot more accessible to new users while maintaining the power and flexibility that advanced users depend on.
# Action Refactoring Migration Guide

## Overview

Brobot is undergoing a significant architectural improvement to separate Find operations from other actions. This refactoring enables better testing, cleaner code, and more flexible action composition. This guide will help you migrate your code to use the new pure actions and conditional action chains.

## Key Changes

### 1. Separation of Find from Actions

**Before**: Actions like Click, Highlight, and Type had Find operations embedded within them.

```java
// Old approach - Find is hidden inside Click
action.click(stateImage);
```

**After**: Find is now a separate operation that can be chained with pure actions.

```java
// New approach - Explicit Find then Click
ConditionalActionChain.find(findOptions)
    .ifFound(clickOptions)
    .perform(action, objectCollection);
```

### 2. Pure Actions

We've introduced "pure" actions that only perform their core function without any Find operations:

- `ClickV2` - Pure click operations
- `HighlightV2` - Pure highlight operations  
- `TypeV2` - Pure typing operations
- More pure actions coming soon

### 3. Enhanced Conditional Action Chains

The `ConditionalActionChain` class has been replaced with `ConditionalActionChain`, which provides:

- **Convenience Methods**: `ifFoundClick()`, `ifFoundType()`, `alwaysClick()` etc.
- **Better Performance**: Optimized execution paths
- **Enhanced Debugging**: Improved logging and error reporting

> **Migration Note:** `ConditionalActionChain` is now deprecated. Update your imports to use `ConditionalActionChain` for all new development.

### 4. Convenience Methods

The Action class now provides convenience methods for common operations:

```java
// Simple one-line operations
action.perform(ActionType.CLICK, location);
action.perform(ActionType.HIGHLIGHT, region);
action.perform(ActionType.TYPE, "Hello World");
```

## Migration Examples

### Example 1: Click on an Image

**Old Code**:
```java
ClickOptions options = new ClickOptions.Builder()
    .setClickType(ClickOptions.Type.LEFT)
    .build();
action.perform(options, stateImage);
```

**New Code** (Enhanced):
```java
ConditionalActionChain.find(new PatternFindOptions.Builder().build())
    .ifFoundClick()
    .perform(action, new ObjectCollection.Builder()
        .withImages(stateImage)
        .build());
```

**New Code** (Convenience):
```java
// First find the image
ActionResult findResult = action.find(stateImage);
// Then click if found
if (findResult.isSuccess()) {
    action.perform(ActionType.CLICK, findResult.getMatchList().get(0).getRegion());
}
```

### Example 2: Type in a Field

**Old Code**:
```java
TypeOptions options = new TypeOptions.Builder()
    .setText("Hello World")
    .build();
action.perform(options, textFieldImage);
```

**New Code**:
```java
ConditionalActionChain.find(new PatternFindOptions.Builder().build())
    .ifFoundType("Hello World")
    .perform(action, new ObjectCollection.Builder()
        .withImages(textFieldImage)
        .build());
```

### Example 3: Highlight Multiple Regions

**Old Code**:
```java
HighlightOptions options = new HighlightOptions.Builder()
    .setHighlightDuration(2.0)
    .build();
action.perform(options, objectCollection);
```

**New Code** (Direct highlight without Find):
```java
// If you already have regions to highlight
action.perform(ActionType.HIGHLIGHT, region1, region2, region3);
```

**New Code** (Find then highlight):
```java
ConditionalActionChain.find(findOptions)
    .ifFound(new HighlightOptions.Builder()
        .setHighlightDuration(2.0)
        .build())
    .perform(action, objectCollection);
```

### Example 4: Complex Action Chains

**Old Code**:
```java
// Had to use multiple action calls
ActionResult result1 = action.find(loginButton);
if (result1.isSuccess()) {
    action.click(loginButton);
    action.type(usernameField, username);
    action.type(passwordField, password);
    action.click(submitButton);
}
```

**New Code**:
```java
ConditionalActionChain.find(loginButton)
    .ifFoundClick()
    .then(find(usernameField))
    .ifFoundType(username)
    .then(find(passwordField))
    .ifFoundType(password)
    .then(find(submitButton))
    .ifFoundClick()
    .perform(action, objectCollection);
```

## Using Pure Actions Directly

Pure actions can be used directly when you already have locations or regions:

```java
// Direct click on a known location
Location buttonLocation = new Location(100, 200);
action.perform(ActionType.CLICK, buttonLocation);

// Direct highlight on a region
Region area = new Region(50, 50, 200, 100);
action.perform(ActionType.HIGHLIGHT, area);

// Type at current cursor position
action.perform(ActionType.TYPE, "Hello World");
```

## Backward Compatibility

### Deprecation Timeline

1. **Version 2.0**: Introduction of pure actions and ConditionalActionChain
   - Old actions still work but show deprecation warnings
   - New actions available as V2 versions (ClickV2, TypeV2, etc.)

2. **Version 2.5**: Old actions moved to legacy package
   - Import statements will need updating
   - Functionality remains the same

3. **Version 3.0**: Legacy actions removed
   - Full migration required

### Using Composite Actions

For users who prefer the old single-method approach, we provide composite actions:

```java
// Composite action that combines Find and Click
FindAndClick findAndClick = new FindAndClick(findOptions, clickOptions);
action.perform(findAndClick, stateImage);
```

## Benefits of Migration

1. **Better Testing**: Test click logic without mocking Find operations
2. **Performance**: Find once, act multiple times on the same matches
3. **Clarity**: Explicit separation between finding and acting
4. **Flexibility**: Build complex conditional chains easily
5. **Debugging**: Clear distinction between find failures and action failures

## Common Patterns

### Pattern 1: Find Once, Act Multiple Times

```java
ActionResult matches = action.find(targetImage);
if (matches.isSuccess()) {
    for (Match match : matches.getMatchList()) {
        // Highlight with pause after
        HighlightOptions highlight = new HighlightOptions.Builder()
            .setPauseAfterEnd(0.5)  // 500ms pause after highlighting
            .build();
        action.perform(highlight, match.getRegion());
        
        // Then click
        action.perform(ActionType.CLICK, match.getRegion());
    }
}
```

### Pattern 2: Conditional Actions

```java
ConditionalActionChain.find(saveButton)
    .ifFoundClick()
    .ifNotFoundLog("Save button not found, trying alt method")
    .ifNotFound(find(altSaveButton))
    .ifFoundClick()
    .always(takeScreenshot())
    .perform(action, objectCollection);
```

### Pattern 3: Validation Chains

```java
ConditionalActionChain.find(dialogBox)
    .ifFoundHighlight()
    .then(find(confirmButton))
    .ifFoundClick()
    .then(waitVanish(dialogBox))
    .ifNotFoundDo(result -> { throw new RuntimeException("Dialog didn't close"); })
    .perform(action, objectCollection);
```

## Migration Checklist

- [ ] Identify all uses of embedded Find in actions
- [ ] Replace with ConditionalActionChain or pure actions
- [ ] Update imports: `ConditionalActionChain` → `ConditionalActionChain`
- [ ] Update method calls to use convenience methods (e.g., `ifFoundClick()`)
- [ ] Test thoroughly - behavior should be identical
- [ ] Consider using convenience methods for simple cases
- [ ] Update any custom actions to follow the new pattern

## Getting Help

- See the [API Reference](../action-config/14-pure-actions-api.md) for detailed documentation
- Check [Examples](../action-config/15-conditional-chains-examples.md) for more usage patterns
- File issues at the [Brobot GitHub repository](https://github.com/jspinak/brobot)

## Next Steps

1. Start with high-impact areas of your code
2. Migrate incrementally - both old and new APIs work together
3. Use the deprecation warnings as a guide
4. Take advantage of the new flexibility to simplify complex workflows
# Pure Actions Quick Start Example

This example demonstrates Pure Actions - the new approach in Brobot that separates finding elements from performing actions on them.

## Documentation Reference

This example corresponds to: `/docs/01-getting-started/pure-actions-quickstart.md`

## What This Example Shows

1. **Old Way vs New Way** - Evolution from embedded find to pure actions
2. **Conditional Chains** - Elegant chaining with automatic conditional execution
3. **Common Use Cases**:
   - Click a button
   - Type in a field
   - Highlight found elements
   - Right-click menu interaction
4. **Convenience Methods** - One-line methods for common operations
5. **Working with Results** - How to use find results effectively
6. **Error Handling** - Explicit and clear error handling patterns
7. **Best Practices** - Reusing results, handling success/failure

## Key Concepts

### Pure Actions Separation
```java
// Find first
ActionResult found = action.find(buttonImage);

// Then act
if (found.isSuccess()) {
    action.perform(ActionType.CLICK, found.getFirstMatch());
}
```

### Conditional Chains
```java
ConditionalActionChain.find(findOptions)
    .ifFound(click())
    .ifNotFound(log("Button not found"))
    .perform(action, objectCollection);
```

### Convenience Methods
```java
action.perform(ActionType.CLICK, location);
action.perform(ActionType.TYPE, "Hello World");
action.perform(ActionType.HIGHLIGHT, region);
```

## Running the Example

```bash
./gradlew bootRun
```

The example runs in mock mode by default, demonstrating all concepts without requiring actual GUI interaction.

## Benefits of Pure Actions

1. **Clearer Code** - Separation of concerns makes intent obvious
2. **Better Testing** - Can test finding and acting separately
3. **More Control** - Decide what to do based on find results
4. **Explicit Error Handling** - Know exactly when and why actions fail
5. **Reusability** - Find once, use results multiple times

## Creating Test Images

To run with real GUI:
1. Create an `images/` directory
2. Add screenshots:
   - `submit-button.png`
   - `text-field.png`
   - `target-element.png`
   - `delete-option.png`
   - `multi-element.png`
   - `critical-button.png`
   - `save-button.png`
   - `button-pattern.png`
3. Set `brobot.core.mock: false` in application.yml

## Next Steps

After understanding pure actions:
1. Explore conditional chains in more detail
2. Look at the action-config examples for advanced patterns
3. Check out the testing examples to see how pure actions improve testability
---
sidebar_position: 7
title: Action Chaining
description: Learn how to chain multiple actions together using ActionChainOptions for complex automation workflows
---

# Action Chaining with ActionChainOptions

Action chaining is a powerful pattern in Brobot that allows you to compose multiple actions into complex workflows. The `ActionChainOptions` class provides a unified, type-safe way to chain actions together with different execution strategies.

## Why Action Chaining?

Traditional automation often requires executing multiple actions in sequence:
- Click a button, then verify a menu appears
- Fill out a form with multiple fields
- Navigate through a multi-step wizard
- Perform complex drag-and-drop operations

ActionChainOptions provides a clean, fluent API for these scenarios while offering advanced features like nested searches and confirmation patterns.

## Basic Concepts

### Chaining Strategies

ActionChainOptions supports two chaining strategies:

1. **NESTED**: Each action searches within the results of the previous action
   - Useful for hierarchical UI navigation
   - Example: Find a dialog, then find a button within that dialog

2. **CONFIRM**: Each action validates the results of the previous action
   - Useful for reducing false positives
   - Example: Find potential matches, then confirm with additional criteria

### Action Flow

When you chain actions:
1. The first action executes with the initial ObjectCollections
2. Subsequent actions can use results from previous actions (NESTED) or validate them (CONFIRM)
3. The chain continues until all actions complete or one fails
4. The final result contains the complete execution history

## Simple Examples

### Sequential Actions

The most basic use case is executing actions in sequence:

```java
@Autowired
private ActionChainExecutor chainExecutor;

// Click a button, then type text
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder()
        .setTypeDelay(0.1)
        .build())
    .build();

// Execute with appropriate ObjectCollections
ActionResult result = chainExecutor.executeChain(chain, new ActionResult(),
    buttonImage.asObjectCollection(),
    new ObjectCollection.Builder().withStrings("Hello World").build()
);
```

### Click and Verify

A common pattern is clicking something and verifying the result:

```java
ActionChainOptions clickVerify = new ActionChainOptions.Builder(
    new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setPauseBeforeBegin(2.0) // Wait for result
        .build())
    .build();
```

## Advanced Patterns

### Nested Search

Find elements within other elements using the NESTED strategy:

```java
// Find a dialog, then find a button within it
ActionChainOptions nestedSearch = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .build();
```

### Confirmation Pattern

Use CONFIRM strategy to validate matches:

```java
// Find with low threshold, confirm with high threshold
ActionChainOptions confirmChain = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setSimilarity(0.7) // Lower threshold
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
    .then(new PatternFindOptions.Builder()
        .setSimilarity(0.9) // Higher threshold
        .build())
    .build();
```

## Replacing Deprecated Patterns

ActionChainOptions replaces several deprecated composite action patterns:

### Instead of MultipleActionsObject

```java
// Old way
MultipleActionsObject mao = new MultipleActionsObject();
mao.add(new ActionParameters(clickOptions, buttonCollection));
mao.add(new ActionParameters(typeOptions, textCollection));

// New way
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();
```

### Instead of ActionResultCombo

```java
// Old way
ActionResultCombo combo = new ActionResultCombo();
combo.setActionOptions(clickOptions);
combo.setResultOptions(findOptions);

// New way
ActionChainOptions chain = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new PatternFindOptions.Builder().build())
    .build();
```

## Critical Insight: Object Type Preservation in Chains

:::warning Important
When chaining actions that use different object types (e.g., StateImage for find/click vs StateString for type), be aware that the **NESTED strategy does NOT preserve all object types** through the chain.
:::

### The Object Type Problem

The NESTED strategy creates a new ObjectCollection containing only regions from previous matches, **discarding other object types like StateStrings**. This is a common pitfall when trying to chain find->click->type operations.

```java
// ❌ WRONG: This will fail - strings are lost in NESTED chain
PatternFindOptions findClickType = new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())  // Uses StateImage
    .then(new TypeOptions.Builder().build())   // Needs StateString - but it's lost!
    .build();

ObjectCollection mixed = new ObjectCollection.Builder()
    .withImages(buttonImage)      // Used by find & click
    .withStrings("Hello World")   // Lost during NESTED chaining!
    .build();

// The type action will receive NO strings and fail silently
```

### Solutions for Mixed Object Types

#### Solution 1: Split Chains by Object Type (RECOMMENDED)

Chain actions that use the same object type, then execute different types separately:

```java
// ✅ CORRECT: Chain find->click (both use StateImage)
PatternFindOptions findClick = new PatternFindOptions.Builder()
    .setPauseAfterEnd(0.5)
    .then(new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build())
    .build();

// Execute find->click with images
ObjectCollection imageCollection = new ObjectCollection.Builder()
    .withImages(buttonImage)
    .build();
ActionResult clickResult = action.perform(findClick, imageCollection);

// Then execute type separately with strings
TypeOptions typeOptions = new TypeOptions.Builder()
    .setPauseBeforeBegin(0.5)
    .build();
ObjectCollection stringCollection = new ObjectCollection.Builder()
    .withStrings("Hello World")
    .build();
ActionResult typeResult = action.perform(typeOptions, stringCollection);
```

#### Solution 2: Use CONFIRM Strategy

The CONFIRM strategy preserves the original ObjectCollection, but may not be semantically appropriate for all workflows:

```java
// ✅ Alternative: CONFIRM strategy preserves all objects
ActionChainOptions chain = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder().build())
    .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)  // Preserves ObjectCollection
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();

// Now mixed collections work
ObjectCollection mixed = new ObjectCollection.Builder()
    .withImages(buttonImage)
    .withStrings("Hello World")
    .build();
```

#### Solution 3: Custom Coordination

For complex workflows, manually coordinate between actions:

```java
// ✅ Full control: Coordinate actions manually
ActionResult findResult = action.find(targetImage);
if (findResult.isSuccess()) {
    // Click at the found location
    ObjectCollection clickTarget = new ObjectCollection.Builder()
        .withRegions(findResult.getMatchList().get(0).getRegion())
        .build();
    action.perform(new ClickOptions.Builder().build(), clickTarget);
    
    // Then type
    ObjectCollection typeTarget = new ObjectCollection.Builder()
        .withStrings("Hello World")
        .build();
    action.perform(new TypeOptions.Builder().build(), typeTarget);
}
```

## Best Practices

1. **Understand object type flow**: Know which actions use which object types
   - Find/Click/Drag: Use StateImage, StateRegion, StateLocation
   - Type: Uses StateString
   - Highlight: Uses any visual object type
2. **Chain same-type actions**: Group actions that use the same object type
3. **Use appropriate strategies**: 
   - NESTED: For hierarchical searches within same object type
   - CONFIRM: When you need to preserve all object types
4. **Set proper delays**: Use `setPauseAfterEnd()` to allow UI updates between actions
5. **Handle failures gracefully**: Check intermediate results when needed
6. **Create reusable patterns**: Build utility methods for common chains
7. **Keep chains focused**: Break complex workflows into smaller, manageable chains

## Common Pattern: Find-Click-Type Workflow

This is one of the most common automation patterns - finding an input field, clicking it, and typing text. Here's the definitive guide:

### ❌ Common Mistake

```java
// This looks logical but FAILS due to object type loss
PatternFindOptions chainedActions = new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .build();

ObjectCollection everything = new ObjectCollection.Builder()
    .withImages(inputFieldImage)
    .withStrings("user@example.com")
    .build();

// The type action never receives the string!
action.perform(chainedActions, everything);
```

### ✅ Correct Implementation

```java
@Component
public class LoginAutomation {
    @Autowired
    private Action action;
    
    public boolean fillLoginForm(StateImage emailField, StateImage passwordField,
                                 String email, String password) {
        // Step 1: Find and click email field
        PatternFindOptions findClickEmail = new PatternFindOptions.Builder()
            .setSimilarity(0.85)
            .setPauseAfterEnd(0.3)
            .then(new ClickOptions.Builder()
                .setPauseAfterEnd(0.5)  // Wait for field to be active
                .build())
            .build();
        
        ObjectCollection emailFieldTarget = new ObjectCollection.Builder()
            .withImages(emailField)
            .build();
        
        ActionResult emailClick = action.perform(findClickEmail, emailFieldTarget);
        if (!emailClick.isSuccess()) {
            log.error("Failed to find/click email field");
            return false;
        }
        
        // Step 2: Type email
        TypeOptions typeEmail = new TypeOptions.Builder()
            .setPauseBeforeBegin(0.2)
            .setTypeDelay(0.05)  // Human-like typing speed
            .build();
        
        ObjectCollection emailText = new ObjectCollection.Builder()
            .withStrings(email)
            .build();
        
        action.perform(typeEmail, emailText);
        
        // Step 3: Tab to password field (or find-click it)
        action.perform(new TypeOptions.Builder().build(), 
            new ObjectCollection.Builder().withStrings("\t").build());
        
        // Step 4: Type password
        ObjectCollection passwordText = new ObjectCollection.Builder()
            .withStrings(password)
            .build();
        
        action.perform(typeEmail, passwordText);
        
        return true;
    }
}
```

### Alternative: Using State Transitions

For complex forms, consider using state transitions instead of chained actions:

```java
@Transition(from = LoginFormState.class, to = FilledFormState.class)
public class FillLoginFormTransition {
    
    public boolean execute() {
        // Each action is clear and separated
        // State management handles the flow
        return fillEmailField() && fillPasswordField() && clickSubmit();
    }
}
```

## Enhanced Conditional Action Chaining

For more advanced conditional workflows with proper sequential composition, see the [Enhanced Conditional Action Chains](./conditional-chains-examples) documentation. The `ConditionalActionChain` class provides:

- The crucial `then()` method for sequential action composition
- Convenience methods like `click()`, `type()`, `scrollDown()`
- Built-in keyboard shortcuts
- Proper conditional execution logic
- No explicit `wait()` methods (following model-based principles)

## Next Steps

- Explore [Enhanced Conditional Action Chains](./conditional-chains-examples) for advanced patterns
- Learn about [Complex Workflows](./complex-workflows) for multi-step automation
- Learn about [Conditional Actions](./conditional-actions) using RepeatUntilConfig
- See [Form Automation](./form-automation) for practical examples
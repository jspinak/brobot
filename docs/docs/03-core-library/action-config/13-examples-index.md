---
sidebar_position: 13
title: Examples Index
description: Complete index of ActionConfig examples and patterns
---

# ActionConfig Examples Index

This page provides a comprehensive index of all ActionConfig examples, organized by use case and complexity level.

> **New to ActionConfig?** Start with the [ActionConfig Overview](./01-overview.md) for conceptual foundation.

## Prerequisites

All examples in this index assume you have:
- A Spring `@Component` class with `@Autowired Action action;`
- StateImage objects initialized for your UI elements
- Required imports (see [Upgrading to Latest](../migration/upgrading-to-latest.md) for complete example)

For complete setup instructions, see:
- **[Upgrading to Latest](../migration/upgrading-to-latest.md)** - Complete class structure and imports
- **[Convenience Methods](./18-convenience-methods.md)** - Direct action methods

## Quick Start Examples

### Convenience Methods (New in 1.1+)

The simplest way to perform common actions:

```java
// Direct clicking
action.click(region);           // Click a region
action.click(location);          // Click a location
action.click(match);             // Click a match

// Direct typing
action.type("Hello World");     // Type text
action.type(stateString);       // Type with state context

// Direct finding
action.find(pattern);            // Find a pattern
action.find(stateImage);         // Find a StateImage

// Direct mouse operations
action.move(location);           // Move to location
action.move(region);             // Move to region center
action.drag(from, to);           // Drag between points

// Other conveniences
action.highlight(region);        // Highlight for debugging
action.scroll(Direction.DOWN, 3); // Scroll down
action.vanish(pattern);          // Wait for disappearance
```

See [Convenience Methods Documentation](./18-convenience-methods.md) for complete details.

### Basic Actions

#### Click Examples
```java
// Simple click
ClickOptions click = new ClickOptions.Builder().build();

// Right-click with pause
ClickOptions rightClick = new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .setPauseAfterEnd(0.5)
    .build();

// Double-click
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .build();
```

#### Type Examples
```java
// Basic typing
TypeOptions type = new TypeOptions.Builder()
    .setTypeDelay(0.05)
    .build();
ObjectCollection text = new ObjectCollection.Builder()
    .withStrings("Hello World")
    .build();

// Typing with delay
TypeOptions slowType = new TypeOptions.Builder()
    .setTypeDelay(0.1)
    .build();
ObjectCollection importantText = new ObjectCollection.Builder()
    .withStrings("Important text")
    .build();

// Clear field then type (using keyboard shortcuts)
ActionChainOptions clearAndType = new ActionChainOptions.Builder(
    new KeyDownOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().build())  // Types "a"
    .then(new KeyUpOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().build())  // Types new text
    .build();
// Execute with: ObjectCollection("a"), then ObjectCollection("New text")
```

#### Find Examples
```java
// Find first match
PatternFindOptions findFirst = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .build();

// Find best match with similarity
PatternFindOptions findBest = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.9)
    .build();

// Find all matches
PatternFindOptions findAll = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setPauseBeforeBegin(1.0)
    .build();
```

## Modern Conditional Workflows (Recommended)

### ConditionalActionChain - The Modern Approach
[View comprehensive examples](./15-conditional-chains-examples.md)

For conditional workflows, use ConditionalActionChain instead of manual retry loops or complex ActionChainOptions:

```java
// Simple find and click with error handling
ConditionalActionChain
    .find(buttonImage)
    .ifFoundClick()
    .ifNotFoundLog("Button not found")
    .perform(action, objectCollection);

// Multi-step form filling with sequential composition
ConditionalActionChain
    .find(usernameField)
    .ifFoundClick()
    .ifFoundType("user@example.com")
    .then(passwordField)  // Sequential: move to next element
    .ifFoundClick()
    .ifFoundType("password")
    .then(submitButton)
    .ifFoundClick()
    .perform(action, objectCollection);

// Retry pattern (much simpler than RepeatUntilConfig)
ConditionalActionChain
    .find(buttonImage)
    .ifFoundClick()
    .then(targetImage)
    .ifNotFoundClick(buttonImage)  // Retry 1
    .ifNotFoundClick(buttonImage)  // Retry 2
    .ifNotFoundClick(buttonImage)  // Retry 3
    .then(targetImage)
    .ifFoundLog("Success!")
    .perform(action, objectCollection);

// Built-in keyboard shortcuts
ConditionalActionChain
    .find(editorField)
    .ifFoundClick()
    .pressCtrlA()      // Select all
    .pressDelete()     // Clear
    .type("New text")  // Type
    .pressCtrlS()      // Save
    .perform(action, objectCollection);

// Scroll integration
ConditionalActionChain
    .find(targetElement)
    .ifNotFoundScrollDown()
    .ifNotFoundScrollDown()
    .ifFoundClick()
    .perform(action, objectCollection);
```

**Key Features**:
- `then()` method for sequential action composition
- Built-in keyboard shortcuts (`pressCtrlA()`, `pressCtrlS()`, `pressEnter()`, etc.)
- Conditional execution (`ifFound*`, `ifNotFound*`)
- Cleaner and more readable than ActionChainOptions for conditional logic
- Integrated scrolling support

## Action Chaining Examples

### Sequential Actions
[View detailed examples](./07-action-chaining.md)

```java
// Click and type
ActionChainOptions clickAndType = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder()
        .setText("Hello")
        .build())
    .build();

// Multi-step form filling
ActionChainOptions formFill = new ActionChainOptions.Builder(
    new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().setText("John").build())
    .then(new TypeOptions.Builder().setText("\t").build())
    .then(new TypeOptions.Builder().setText("Doe").build())
    .build();
```

### Nested Actions
```java
// Find dialog, then find button within
ActionChainOptions nestedFind = new ActionChainOptions.Builder(
    new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
    .then(new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .build())
    .build();
```

## Conditional Actions

### ConditionalActionWrapper (Spring Applications)
[View detailed documentation](./16-conditional-action-wrapper.md)

```java
// Spring-integrated conditional actions
@Autowired
private ConditionalActionWrapper actions;

// Simple find and click with error handling
actions.findAndClick(submitButton);

// Complex conditional chain
actions.createChain()
    .find(loginButton)
    .ifFound(ConditionalActionWrapper.click())
    .ifNotFoundLog("Login button not found")
    .execute();
```

### Click Until Examples
[View detailed examples](./09-conditional-actions.md)

> **Modern Approach**: For new code, consider using `ConditionalActionChain` (see above) instead of RepeatUntilConfig.
> See [Conditional Chains Examples](./15-conditional-chains-examples.md#retry-pattern) for simpler alternatives.

```java
// Option 1: Using ConditionalActionChain (Recommended for 1-5 retries)
ConditionalActionChain
    .find(button)
    .ifFoundClick()
    .then(target)
    .ifNotFoundClick(button)  // Retry 1
    .ifNotFoundClick(button)  // Retry 2
    .ifNotFoundClick(button)  // Retry 3
    .then(target)
    .ifFoundLog("Target appeared!")
    .perform(action, objectCollection);

// Option 2: Using RepeatUntilConfig (For 10+ retries)
public boolean clickUntilImageAppears(StateImage button, StateImage target) {
    // RepeatUntilConfig for complex retry logic
    RepeatUntilConfig config = new RepeatUntilConfig.Builder()
        .setDoAction(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .setActionObjectCollection(button.asObjectCollection())
        .setUntilAction(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .setPauseBeforeBegin(2.0)
            .build())
        .setConditionObjectCollection(target.asObjectCollection())
        .setMaxActions(10)
        .build();

    // Execute via Action class
    return action.perform(config, new ObjectCollection.Builder().build()).isSuccess();
}
```

### Wait Patterns
```java
// Wait for element to vanish
VanishOptions vanish = new VanishOptions.Builder()
    .setPauseBeforeBegin(1.0)
    .setTimeout(10.0)
    .build();
action.perform(vanish, elementToVanish.asObjectCollection());

// Wait with custom intervals (using ConditionalActionChain)
public boolean waitForCondition(StateImage element, int maxAttempts) {
    // Use built-in retry mechanism
    ConditionalActionChain chain = ConditionalActionChain.find(element);
    for (int i = 0; i < maxAttempts - 1; i++) {
        chain.ifNotFoundWait(1.0);  // Wait 1 second between attempts
    }
    ActionResult result = chain.perform(action, new ObjectCollection.Builder().build());
    return result.isSuccess();
}
```

## Complex Workflows

### Form Automation
[View detailed examples](./10-form-automation.md)

```java
// Option 1: Using ConditionalActionChain (Recommended)
public boolean fillRegistrationForm(String firstName, String lastName, String email) {
    return ConditionalActionChain
        .find(firstNameField)
        .ifFoundClick()
        .ifFoundClearAndType(firstName)  // Built-in clear and type
        .then(lastNameField)
        .ifFoundClick()
        .ifFoundClearAndType(lastName)
        .then(emailField)
        .ifFoundClick()
        .ifFoundClearAndType(email)
        .then(submitButton)
        .ifFoundClick()
        .perform(action, new ObjectCollection.Builder().build())
        .isSuccess();
}

// Option 2: Using ActionChainOptions (Traditional)
public boolean fillRegistrationFormTraditional(String firstName, String lastName, String email) {
    // Note: UserData is a placeholder class - create your own
    ActionChainOptions chain = new ActionChainOptions.Builder(
        new ClickOptions.Builder().build())  // Click first name field
        .then(new TypeOptions.Builder().build())  // Type first name
        .then(new ClickOptions.Builder().build())  // Click last name field
        .then(new TypeOptions.Builder().build())  // Type last name
        .then(new ClickOptions.Builder().build())  // Click email field
        .then(new TypeOptions.Builder().build())  // Type email
        .then(new ClickOptions.Builder().setPauseAfterEnd(1.0).build())  // Click submit
        .build();

    // Execute with corresponding ObjectCollections
    return chainExecutor.executeChain(chain, new ActionResult(),
        firstNameField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(firstName).build(),
        lastNameField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(lastName).build(),
        emailField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(email).build(),
        submitButton.asObjectCollection()).isSuccess();
}
```

### Navigation Patterns
[View detailed examples](./08-complex-workflows.md)

```java
// Navigate through menu hierarchy - Modern approach
public boolean navigateToSettings() {
    return ConditionalActionChain
        .find(menuButton)
        .ifFoundClick()
        .then(settingsOption)
        .ifFoundClick()
        .perform(action, new ObjectCollection.Builder().build())
        .isSuccess();
}

// Traditional ActionChainOptions approach
public boolean navigateToSettingsTraditional() {
    ActionChainOptions chain = new ActionChainOptions.Builder(
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build())
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .build();

    return chainExecutor.executeChain(chain, new ActionResult(),
        menuButton.asObjectCollection(),
        settingsOption.asObjectCollection(),
        settingsOption.asObjectCollection()).isSuccess();
}
```

## Reusable Patterns

### Pattern Library
[View detailed examples](./11-reusable-patterns.md)

> **Note**: `AutomationPattern` and `PatternContext` are example interfaces, not part of Brobot core.
> Create your own pattern abstraction based on your project needs.

```java
// Example: Login pattern as reusable component
@Component
public class LoginPattern {

    @Autowired
    private Action action;

    // Modern approach with ConditionalActionChain
    public boolean login(String username, String password) {
        return ConditionalActionChain
            .find(usernameField)
            .ifFoundClick()
            .ifFoundType(username)
            .then(passwordField)
            .ifFoundClick()
            .ifFoundType(password)
            .then(submitButton)
            .ifFoundClick()
            .perform(action, new ObjectCollection.Builder().build())
            .isSuccess();
    }

    // Traditional approach with ActionChainOptions
    public boolean loginTraditional(String username, String password) {
        ActionChainOptions chain = new ActionChainOptions.Builder(
            new ClickOptions.Builder().build())  // Click username
            .then(new TypeOptions.Builder().build())  // Type username
            .then(new ClickOptions.Builder().build())  // Click password
            .then(new TypeOptions.Builder().build())  // Type password
            .then(new ClickOptions.Builder().build())  // Click submit
            .build();

        // Execute with corresponding data
        return chainExecutor.executeChain(chain, new ActionResult(),
            usernameField.asObjectCollection(),
            new ObjectCollection.Builder().withStrings(username).build(),
            passwordField.asObjectCollection(),
            new ObjectCollection.Builder().withStrings(password).build(),
            submitButton.asObjectCollection()).isSuccess();
    }
}
```

## Mouse Actions

### Drag and Drop
```java
// Using DragOptions
DragOptions drag = new DragOptions.Builder()
    .setDelayBetweenMouseDownAndMove(0.5)
    .setDelayAfterDrag(0.5)
    .build();
action.perform(drag, sourceElement.asObjectCollection(), targetElement.asObjectCollection());

// Custom drag with chain (for specialized control)
ActionChainOptions customDrag = new ActionChainOptions.Builder(
    new MouseMoveOptions.Builder().build())  // Move to source
    .then(new MouseDownOptions.Builder().build())  // Press mouse
    .then(new MouseMoveOptions.Builder().build())  // Move to target
    .then(new MouseUpOptions.Builder().build())  // Release mouse
    .build();
chainExecutor.executeChain(customDrag, new ActionResult(),
    sourceElement.asObjectCollection(),
    sourceElement.asObjectCollection(),
    targetElement.asObjectCollection(),
    targetElement.asObjectCollection());
```

### Scrolling
```java
// Scroll down
ScrollOptions scrollDown = new ScrollOptions.Builder()
    .setDirection(ScrollOptions.Direction.DOWN)
    .setScrollSteps(5)
    .build();
action.perform(scrollDown, new ObjectCollection.Builder().build());

// Scroll until element visible - Modern approach
public boolean scrollToElement(StateImage element) {
    return ConditionalActionChain
        .find(element)
        .ifNotFoundScrollDown()
        .ifNotFoundScrollDown()
        .ifNotFoundScrollDown()
        .ifNotFoundScrollDown()
        .ifNotFoundScrollDown()
        .ifFoundClick()
        .perform(action, new ObjectCollection.Builder().build())
        .isSuccess();
}

// Traditional approach with manual loop
public boolean scrollToElementTraditional(StateImage element) {
    ScrollOptions scroll = new ScrollOptions.Builder()
        .setDirection(ScrollOptions.Direction.DOWN)
        .setScrollSteps(3)
        .build();

    for (int i = 0; i < 10; i++) {
        ActionResult findResult = action.find(element);
        if (findResult.isSuccess()) {
            return true;
        }
        action.perform(scroll, new ObjectCollection.Builder().build());
        try {
            Thread.sleep(500);  // Wait between scrolls
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    return false;
}
```

## Keyboard Actions

### Shortcuts

```java
// Modern approach: Built-in keyboard shortcuts in ConditionalActionChain
ConditionalActionChain
    .find(editorField)
    .ifFoundClick()
    .pressCtrlA()      // Select all (built-in)
    .pressCtrlC()      // Copy (built-in)
    .pressCtrlV()      // Paste (built-in)
    .pressCtrlS()      // Save (built-in)
    .perform(action, new ObjectCollection.Builder().build());

// Traditional approach: Manual key combinations
ActionChainOptions copy = new ActionChainOptions.Builder(
    new KeyDownOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().build())  // Type "c"
    .then(new KeyUpOptions.Builder().setKey("ctrl").build())
    .build();
chainExecutor.executeChain(copy, new ActionResult(),
    new ObjectCollection.Builder().withStrings("c").build());

// Select all and delete
ActionChainOptions selectAllDelete = new ActionChainOptions.Builder(
    new KeyDownOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().build())  // Type "a"
    .then(new KeyUpOptions.Builder().setKey("ctrl").build())
    .then(new TypeOptions.Builder().build())  // Type backspace
    .build();
chainExecutor.executeChain(selectAllDelete, new ActionResult(),
    new ObjectCollection.Builder().withStrings("a").build(),
    new ObjectCollection.Builder().withStrings("\b").build());
```

## Find Strategies

### Text Finding
```java
TextFindOptions findText = new TextFindOptions.Builder()
    .setMaxMatchRetries(3)
    .build();
```

### Color Finding
```java
ColorFindOptions findColor = new ColorFindOptions.Builder()
    .setColorStrategy(ColorFindOptions.Color.KMEANS)
    .setKmeans(3)  // Find 3 dominant colors
    .build();
```

### Motion Detection
```java
MotionFindOptions findMotion = new MotionFindOptions.Builder()
    .setMotionThreshold(0.1)
    .build();
```

## Error Handling

For comprehensive troubleshooting guidance, see:
- **[Troubleshooting Action Chains](./troubleshooting-chains.md)** - Common issues and solutions
- **[ActionResult Components](./17-actionresult-components.md)** - Understanding action results

### Retry Patterns

```java
// Modern approach: Built-in retry with ConditionalActionChain
public boolean performWithRetry(StateImage target, int maxRetries) {
    ConditionalActionChain chain = ConditionalActionChain.find(target).ifFoundClick();

    // Add retries
    for (int i = 0; i < maxRetries - 1; i++) {
        chain.ifNotFoundClick(target);
    }

    return chain.perform(action, new ObjectCollection.Builder().build()).isSuccess();
}

// Traditional approach: Manual retry loop
public boolean performWithRetryTraditional(ActionConfig actionConfig,
                                          ObjectCollection target,
                                          int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        ActionResult result = action.perform(actionConfig, target);
        if (result.isSuccess()) {
            return true;
        }

        System.out.println("Attempt " + (i + 1) + " failed, retrying...");
        try {
            Thread.sleep(1000);  // Wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    return false;
}
```

### Fallback Strategies

```java
// Modern approach: ConditionalActionChain with fallback
public boolean clickWithFallback(StateImage primary, StateImage fallback) {
    return ConditionalActionChain
        .find(primary)
        .ifFoundClick()
        .ifNotFoundLog("Primary not found, trying fallback")
        .then(fallback)
        .ifFoundClick()
        .perform(action, new ObjectCollection.Builder().build())
        .isSuccess();
}

// Traditional approach: Manual fallback logic
public boolean clickWithFallbackTraditional(StateImage primary, StateImage fallback) {
    ActionResult primaryResult = action.click(primary.asObjectCollection());
    if (primaryResult.isSuccess()) {
        return true;
    }

    System.out.println("Primary target failed, trying fallback");
    ActionResult fallbackResult = action.click(fallback.asObjectCollection());
    return fallbackResult.isSuccess();
}
```

## Performance Optimization

### Batch Operations
```java
// Process multiple items efficiently
public void processItems(List<StateImage> items) {
    if (items.isEmpty()) {
        return;
    }

    // Build chain dynamically
    ActionChainOptions.Builder chain = new ActionChainOptions.Builder(
        new ClickOptions.Builder().build());

    for (int i = 1; i < items.size(); i++) {
        chain.then(new ClickOptions.Builder().build());
    }

    // Execute with all items
    ObjectCollection[] collections = items.stream()
        .map(StateImage::asObjectCollection)
        .toArray(ObjectCollection[]::new);

    chainExecutor.executeChain(chain.build(), new ActionResult(), collections);
}
```

## Migration Examples

### Before and After
[View complete migration guide](../migration/upgrading-to-latest.md)

```java
// Before (ActionOptions) - DEPRECATED
ActionOptions old = new ActionOptions.Builder()
    .setAction(ActionType.CLICK)
    .setPauseAfterEnd(0.5)
    .build();

// After (ActionConfig)
ClickOptions newClick = new ClickOptions.Builder()
    .setPauseAfterEnd(0.5)
    .build();
```

## Resources

### Core Documentation
- **[ActionConfig Overview](./01-overview.md)** - Concepts and architecture introduction
- **[Code Examples](./03-examples.md)** - Detailed examples for each ActionConfig class
- **[ActionConfig API Reference](./05-reference.md)** - Complete API documentation

### Migration & Workflows
- **[Upgrading to Latest](../migration/upgrading-to-latest.md)** - Complete migration guide
- **[Action Chaining](./07-action-chaining.md)** - Complex multi-step workflows
- **[Complex Workflows](./08-complex-workflows.md)** - Navigation and workflow patterns

### Conditional Actions & Modern Patterns
- **[Conditional Actions](./09-conditional-actions.md)** - Click-until and repeat patterns
- **[Conditional Action Chains Examples](./15-conditional-chains-examples.md)** - Sequential composition with then()
- **[ConditionalActionWrapper](./16-conditional-action-wrapper.md)** - Spring-integrated conditional actions
- **[Reusable Patterns](./11-reusable-patterns.md)** - Pattern library approaches

### Alternatives & Helpers
- **[Convenience Methods](./18-convenience-methods.md)** - Simpler API for common operations
- **[Form Automation](./10-form-automation.md)** - Form filling patterns

### Troubleshooting & Results
- **[Troubleshooting Action Chains](./troubleshooting-chains.md)** - Common issues and solutions
- **[ActionResult Components](./17-actionresult-components.md)** - Understanding action results

## Contributing Examples

To contribute your own examples:
1. Fork the repository
2. Add your example to the appropriate section
3. Include comments explaining the use case
4. Submit a pull request

Happy automating with Brobot!
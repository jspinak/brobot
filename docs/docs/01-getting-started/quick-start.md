---
sidebar_position: 7
title: 'Quick Start Guide'
---

# Quick Start Guide

This guide will help you get started with Brobot 1.1.0 using the new ActionConfig API.

## Installation

Add Brobot to your project:

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.1.0</version>
</dependency>
```

```gradle
// Gradle
implementation 'io.github.jspinak:brobot:1.1.0'
```

## Your First Brobot Application

Here's a simple example that demonstrates the core concepts:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j  // Add logging support
public class SimpleAutomation {
    
    @Autowired
    private Action action;
    
    public void clickButton() {
        // 1. Define what to look for
        StateImage buttonImage = new StateImage.Builder()
                .setName("submit-button")
                .addPatterns("submit-button")
                .build();
        
        // 2. Configure how to find it
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        
        // 3. Add the button to the objects to find
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build();
        
        // 4. Find the button
        ActionResult findResult = action.perform(findOptions, objects);
        
        // 4. Click the found button
        if (findResult.isSuccess()) {
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setClickType(ClickOptions.Type.LEFT)
                    .build();
            
            // Click on the same objects we found
            ActionResult clickResult = action.perform(clickOptions, objects);
        }
    }
}
```

### Simplified Version Using Convenience Methods

The above example can be greatly simplified using Brobot's convenience methods and default settings:

```java
@Component
@Slf4j
public class SimpleAutomation {
    
    @Autowired
    private Action action;
    
    public void clickButtonSimplified() {
        // 1. Define the button image
        StateImage buttonImage = new StateImage.Builder()
                .setName("submit-button")
                .addPatterns("submit-button")
                .build();
        
        // 2. Find and click in one line
        action.click(buttonImage);
        
        // That's it! 🎉
    }
}
```

**What happens behind the scenes:**
- Uses default similarity of 0.7 (70% match)
- Automatically finds the image first, then clicks if found
- Uses `PatternFindOptions.Strategy.FIRST` (clicks first match)
- Uses standard left click with no delays
- No need to create ObjectCollections manually

### Even More Concise

For quick prototyping or simple cases:

```java
// Find an image on screen
ActionResult found = action.find(submitButton);

// Click an image (finds it first automatically)
action.click(submitButton);

// Type text (with automatic focus)
action.type(new ObjectCollection.Builder().withStrings("Hello World").build());

// Chain find and click with fluent API
new PatternFindOptions.Builder()
    .then(new ClickOptions.Builder().build())
    .build();
```

**Default values used:**
- **Similarity**: 0.7 (defined in Sikuli's `Settings.MinSimilarity`)
- **Search Strategy**: FIRST (find first match)
- **Search Duration**: 3 seconds timeout
- **Click Type**: Single left click
- **Search Region**: Entire screen
- **Image Saving**: DISABLED (no debug images saved by default)

These convenience methods are perfect when:
- You're prototyping or testing
- Default settings work for your use case
- You want minimal, readable code
- You don't need fine-grained control

:::tip Important: Debug Images Not Saved by Default
Brobot does **NOT** save debug images or action history by default to prevent filling up disk space. To enable image saving for debugging:

```properties
# In application.properties - only enable when debugging
brobot.screenshot.save-history=true
brobot.debug.image.enabled=true
```

Remember to disable these after debugging!
:::

For production code requiring specific settings (higher similarity, custom timeouts, logging), use the full builder pattern shown in the first example.

### Minimal Complete Example

Here's the absolute minimum code needed for a working Brobot application:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MinimalBrobot {
    
    public static void main(String[] args) {
        // Start Spring context
        ApplicationContext context = SpringApplication.run(MinimalBrobot.class, args);
        
        // Get the Action bean
        Action action = context.getBean(Action.class);
        
        // Create an image to find
        StateImage button = new StateImage.Builder()
                .addPatterns("button")  // No .png extension needed
                .build();
        
        // Find and click it
        action.click(button);
        
        // Done!
    }
}
```

**That's all you need!** Brobot handles:
- Image loading from resources
- Screen capture
- Pattern matching with 70% similarity
- Mouse movement and clicking
- Error handling if image not found

## Understanding the New API

### 1. Type-Safe Configuration

Instead of the generic `ActionOptions`, Brobot 1.1.0 uses specific configuration classes:

```java
// Find operations
PatternFindOptions patternFind = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.ALL)
    .setSimilarity(0.85)
    .build();

// Click operations
ClickOptions click = new ClickOptions.Builder()
    .setClickType(ClickOptions.Type.DOUBLE_LEFT)
    .build();

// Text/OCR operations
TextFindOptions textFind = new TextFindOptions.Builder()
    .setLanguage("eng")
    .setMinScore(0.8)
    .build();
```

### 2. Action Execution Pattern

The new API follows a consistent pattern:

```java
// 1. Create configuration
ActionConfig config = new SomeOptions.Builder().build();

// 2. Create object collection
ObjectCollection objects = new ObjectCollection.Builder()
    .withImages(images)
    .build();

// 3. Execute action
ActionResult result = action.perform(config, objects);

// 4. Check results
if (result.isSuccess()) {
    // Handle success
}
```

### 3. Common Actions

#### Finding Images
```java
PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch();
// or
PatternFindOptions findOptions = PatternFindOptions.forQuickSearch();
```

#### Clicking
```java
ClickOptions clickOptions = new ClickOptions.Builder()
    .setClickType(ClickOptions.Type.RIGHT)
    .setPauseAfterEnd(0.5)
    .build();
```

#### Typing Text
```java
TypeOptions typeOptions = new TypeOptions.Builder()
    .setModifierDelay(0.05)
    .setClearFieldFirst(true)
    .build();
```

#### Dragging
```java
DragOptions dragOptions = new DragOptions.Builder()
    .setFromIndex(0)  // First match
    .setToIndex(1)    // Second match
    .setDragDuration(1.0)
    .build();
```

## Working with States

Define states to model your application:

```java
@State(initial = true)  // Mark as initial state, includes @Component
@Getter  // Generate getters
@Slf4j   // Add logging
public class LoginState {
    
    private final State state;
    
    public LoginState(StateService stateService) {
        StateImage usernameField = new StateImage.Builder()
                .setName("username-field.png")
                .build();
        
        StateImage passwordField = new StateImage.Builder()
                .setName("password-field.png")
                .build();
        
        StateImage loginButton = new StateImage.Builder()
                .setName("login-button.png")
                .build();
        
        state = new State.Builder("LOGIN")
                .withImages(usernameField, passwordField, loginButton)
                .build();
        
        stateService.save(state);
    }
    
    // Getters...
}
```

## State Transitions

Define transitions between states:

```java
@Component
public class LoginTransitions {
    
    @Autowired
    private Action action;
    
    @Autowired
    private LoginState loginState;
    
    public LoginTransitions(StateTransitionsRepository repo) {
        StateTransitions transitions = new StateTransitions.Builder("LOGIN")
                .addTransition(this::performLogin, "DASHBOARD")
                .build();
        repo.add(transitions);
    }
    
    private boolean performLogin() {
        // Click username field
        if (!clickElement(loginState.getUsernameField())) return false;
        
        // Type username
        if (!typeText("user@example.com")) return false;
        
        // Click password field
        if (!clickElement(loginState.getPasswordField())) return false;
        
        // Type password
        if (!typeText("password123")) return false;
        
        // Click login button
        return clickElement(loginState.getLoginButton());
    }
    
    // Helper methods...
}
```

## Next Steps

1. **Explore the Examples**: Check out the [LoginAutomationExample](https://github.com/jspinak/brobot/tree/main/examples/LoginAutomationExample.java) for a complete working example
2. **Read the Migration Guide**: If upgrading from Brobot 1.x, see the [Migration Guide](/docs/core-library/guides/migration-guide)
3. **Learn State Management**: Deep dive into [States](states.md) and [Transitions](transitions.md)
4. **Advanced Features**: Explore [color-based finding](/docs/core-library/guides/finding-objects/using-color), [motion detection](/docs/core-library/guides/finding-objects/movement), and [screen-adaptive regions](/docs/core-library/guides/screen-adaptive-regions) for resolution-independent automation

## Getting Help

- **Documentation**: Browse the full documentation at [jspinak.github.io/brobot](https://jspinak.github.io/brobot/)
- **Examples**: Find more examples in the `/examples` directory
- **Community**: Join discussions and ask questions on GitHub

## Tips for Success

1. **Start Simple**: Begin with basic find and click operations
2. **Use Type-Safe Builders**: Let your IDE guide you with auto-completion
3. **Model Your Application**: Think in terms of states and transitions
4. **Handle Failures**: Always check `ActionResult.isSuccess()`
5. **Enable History**: Use `BrobotSettings.saveHistory = true` for debugging
6. **Avoid Thread.sleep()**: Use `setPauseBeforeBegin()` or `setPauseAfterEnd()` in action options instead

### Important: Pausing in Brobot

Never use `Thread.sleep()` in Brobot code. Instead, use the built-in pause options:

```java
// ❌ Don't do this:
action.click(button);
Thread.sleep(1000);  // Bad practice in Brobot

// ✅ Do this instead:
ClickOptions clickWithPause = new ClickOptions.Builder()
    .setPauseAfterEnd(1.0)  // 1 second pause after clicking
    .build();
action.perform(clickWithPause, button);

// Or for pauses before actions:
PatternFindOptions findWithPause = new PatternFindOptions.Builder()
    .setPauseBeforeBegin(0.5)  // 500ms pause before searching
    .build();
```

This approach keeps pauses as part of the action configuration, making them mockable, testable, and part of the action history.

Happy automating with Brobot 1.1.0!
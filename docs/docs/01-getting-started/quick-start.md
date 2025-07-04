---
sidebar_position: 7
title: 'Quick Start Guide'
---

# Quick Start Guide

This guide will help you get started with Brobot 1.1.0 using the new ActionConfig API.

## Your First Brobot Application

Here's a simple example that demonstrates the core concepts:

```java
import io.github.jspinak.brobot.action.ActionService;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleAutomation {
    
    @Autowired
    private ActionService actionService;
    
    public void clickButton() {
        // 1. Define what to look for
        StateImage buttonImage = new StateImage.Builder()
                .setName("submit-button.png")
                .build();
        
        // 2. Configure how to find it
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        
        // 3. Find the button
        ActionResult findResult = new ActionResult();
        findResult.setActionConfig(findOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(buttonImage)
                .build();
        
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(findResult, objects);
        
        // 4. Click the found button
        if (findResult.isSuccess()) {
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setClickType(ClickOptions.Type.LEFT)
                    .build();
            
            ActionResult clickResult = new ActionResult();
            clickResult.setActionConfig(clickOptions);
            
            ObjectCollection clickObjects = new ObjectCollection.Builder()
                    .withMatches(findResult.getMatchList())
                    .build();
            
            ActionInterface clickAction = actionService.getAction(clickOptions);
            clickAction.perform(clickResult, clickObjects);
        }
    }
}
```

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
    .setClickType(ClickOptions.Type.DOUBLE)
    .setNumberOfClicks(2)
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

// 2. Create result container
ActionResult result = new ActionResult();
result.setActionConfig(config);

// 3. Create object collection
ObjectCollection objects = new ObjectCollection.Builder()
    .withImages(images)
    .build();

// 4. Get and execute action
ActionInterface action = actionService.getAction(config);
action.perform(result, objects);

// 5. Check results
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
@Component
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
    private ActionService actionService;
    
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
2. **Read the Migration Guide**: If upgrading from Brobot 1.x, see the [Migration Guide](/docs/03-core-library/guides/migration-guide)
3. **Learn State Management**: Deep dive into [States](states.md) and [Transitions](transitions.md)
4. **Advanced Features**: Explore [color-based finding](/docs/03-core-library/guides/finding-objects/using-color-v2), [motion detection](/docs/03-core-library/guides/finding-objects/movement-v2), and more

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

Happy automating with Brobot 1.1.0!
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

Brobot uses a **model-based approach** with States and Transitions. Here's how to build a simple login automation:

### Step 1: Define Your State

States organize UI elements that appear together:

```java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State  // Includes @Component, registers as Brobot state
@Getter
public class LoginState {

    private final StateImage loginButton = new StateImage.Builder().addPatterns("login-button").build();
    private final StateImage usernameField = new StateImage.Builder().addPatterns("username-field").build();
    private final StateImage passwordField = new StateImage.Builder().addPatterns("password-field").build();
}
```

### Step 2: Define Transitions

Transitions define how to navigate between states:

```java
import io.github.jspinak.brobot.annotations.*;
import io.github.jspinak.brobot.action.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@TransitionSet(state = LoginState.class)
@RequiredArgsConstructor
public class LoginTransitions {

    private final Action action;
    private final LoginState loginState;

    @IncomingTransition(description = "Verify login screen is visible")
    public boolean verifyLoginScreen() {
        return action.find(loginState.getLoginButton()).isSuccess();
    }

    @OutgoingTransition(activate = {DashboardState.class})
    public boolean login() {
        return action.click(loginState.getUsernameField()).isSuccess() &&
               action.type("user@example.com").isSuccess() &&
               action.click(loginState.getPasswordField()).isSuccess() &&
               action.type("password123").isSuccess() &&
               action.click(loginState.getLoginButton()).isSuccess();
    }
}
```

### Step 3: Use Navigation

Now you can navigate automatically:

```java
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginAutomation {

    private final StateNavigator navigator;

    public boolean navigateToDashboard() {
        // Brobot automatically figures out the path and executes transitions!
        return navigator.openState("Dashboard");
    }
}
```

**That's it!** Brobot handles:
- Finding the current state
- Determining the path to the target state
- Executing all necessary transitions
- Verifying arrival at each state

### Why This Approach?

**Model-Based Benefits:**
- ✅ **Separation of Concerns**: UI elements (State) separate from logic (Transitions)
- ✅ **Automatic Pathfinding**: Navigator finds the shortest path between states
- ✅ **Reusable**: Transitions can be reused in different navigation scenarios
- ✅ **Testable**: Mock transitions for unit testing
- ✅ **Maintainable**: UI changes only affect State definitions

### Quick Actions (Without States)

For simple scripts or prototyping, you can use Brobot's Action API directly:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuickScript {

    private final Action action;

    public void simpleClick() {
        StateImage button = new StateImage.Builder().addPatterns("submit-button").build();
        action.click(button);
    }
}
```

**Default behavior:**
- **Similarity**: 0.7 (70% match required) - from `Settings.MinSimilarity`
- **Search Strategy**: FIRST (stops at first Pattern's best match ≥ 0.7 similarity)
- **Search Duration**: 3.0 seconds
- **Click Type**: Single left click
- **Search Region**: Entire screen

:::tip Search Strategy Details
- **Each Pattern returns its BEST match** - SikuliX finds the highest correlation for each pattern image
- **FIRST strategy** - Stops after finding the first Pattern that has a match ≥ 0.7 (fast, good when Patterns are ordered by priority)
- **BEST strategy** - Searches ALL Patterns and returns the one with highest similarity across all (slower, most accurate)

If your StateImage has multiple Patterns, FIRST returns the best match of the first Pattern found. BEST returns the best match across all Patterns.
:::

:::tip When to Use Each Approach

**Use States & Transitions (Recommended):**
- Production applications
- Complex multi-screen workflows
- When you need navigation between different UI states
- Team projects requiring maintainable code

**Use Direct Actions:**
- Quick prototypes or scripts
- Single-screen automation
- One-off tasks
- Learning Brobot basics
:::

:::info Debug Images Not Saved by Default
Brobot does **NOT** save debug images or action history by default. Enable only when debugging:

```properties
# In application.properties
brobot.screenshot.save-history=true
```

Remember to disable after debugging!
:::

### Minimal Complete Example

Here's the absolute minimum code needed for a working Brobot application:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinimalBrobot {

    public static void main(String[] args) {
        var context = SpringApplication.run(MinimalBrobot.class, args);
        var action = context.getBean(Action.class);

        var button = new StateImage.Builder().addPatterns("button").build();
        action.click(button);
    }
}
```

**That's all you need!** Brobot handles:
- Image loading from resources
- Screen capture
- Pattern matching with 70% similarity
- Mouse movement and clicking
- Error handling if image not found

## Using Actions Directly (Without States)

For simple scripts that don't need the full States + Transitions architecture, you can use Brobot's Action API directly. See the **[Pure Actions Quick Start Guide](pure-actions-quickstart.md)** for detailed examples and patterns.

Quick example:
```java
@Component
@RequiredArgsConstructor
public class QuickScript {
    private final Action action;

    public void simpleTask() {
        StateImage button = new StateImage.Builder().addPatterns("submit-button").build();
        action.click(button);  // Finds and clicks
        action.type("Hello World");
    }
}
```

## Next Steps

1. **Explore the Examples**: Check out the [LoginAutomationExample](https://github.com/jspinak/brobot/tree/main/examples/LoginAutomationExample.java) for a complete working example
2. **Read the Migration Guide**: If upgrading from Brobot 1.x, see the [Migration Guide](/docs/core-library/action-config/migration-guide)
3. **Learn State Management**: Deep dive into [States](states.md) and [Transitions](transitions.md)
4. **Advanced Features**: Explore [color-based finding](/docs/core-library/guides/finding-objects/using-color), [motion detection](/docs/core-library/guides/finding-objects/movement), and [screen-adaptive regions](/docs/core-library/guides/user-guides/screen-adaptive-regions) for resolution-independent automation

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

### Configuring Click Actions

:::note API Change in Brobot 1.1.0
The `ClickOptions.Type` enum has been replaced with more flexible configuration:
- Use `MousePressOptions.builder().setButton(MouseButton.RIGHT)` for button selection
- Use `setNumberOfClicks(2)` for double-clicks
- LEFT button is the default, so basic `new ClickOptions.Builder().build()` is sufficient for left-clicks
:::

```java
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;

// Single left-click (default)
ClickOptions leftClick = new ClickOptions.Builder().build();
action.perform(leftClick, button);

// Double left-click
ClickOptions doubleClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .build();
action.perform(doubleClick, button);

// Right-click
ClickOptions rightClick = new ClickOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .build();
action.perform(rightClick, button);

// Double right-click with pause
ClickOptions advancedClick = new ClickOptions.Builder()
    .setNumberOfClicks(2)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .build())
    .setPauseAfterEnd(0.5)  // 500ms pause after clicking
    .build();
action.perform(advancedClick, button);
```

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
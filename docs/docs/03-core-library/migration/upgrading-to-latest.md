---
sidebar_position: 1
title: 'Upgrading to Latest Brobot'
description: 'Guide for upgrading to the latest Brobot version'
keywords: [migration, upgrade, latest version]
---

# Upgrading to Latest Brobot

> **Philosophy**: Brobot is in active development and prioritizes clean, maintainable code over backward compatibility. See [CLAUDE.md](/CLAUDE.md) for development philosophy.

## Current Recommended Approach

**For new projects**: Follow the [Quick Start Guide](../../01-getting-started/quick-start.md)

**For existing projects**: We recommend using the current patterns rather than maintaining old code:

### Key Current Patterns

#### 1. Use ActionConfig (not ActionOptions)

**Modern approach:**
```java
import io.github.jspinak.brobot.actions.composites.methods.find.PatternFindOptions;
import io.github.jspinak.brobot.actions.composites.methods.click.ClickOptions;

// Specialized config classes for each action type
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .withMinSimilarity(0.85)
    .withMaxWait(5.0)
    .build();

ClickOptions clickOptions = new ClickOptions.Builder()
    .withMaxClicks(3)
    .withClickUntil(ClickUntil.OBJECTS_VANISH)
    .build();
```

**See:** [ActionConfig Overview](../action-config/01-overview.md)

#### 2. Use @State and @Transition Annotations

**Modern approach:**
```java
@State
public class LoginState {
    @StateImage
    private StateImage loginButton;

    @StateImage
    private StateImage usernameField;
}

@Transition
public class LoginTransitions {
    @OutgoingTransition
    public void clickLogin(Action action) {
        action.click(loginState.getLoginButton());
    }
}
```

**See:** [States Guide](../../01-getting-started/states.md)

#### 3. Use Action Class Methods

**Modern approach:**
```java
@Autowired
private Action action;

public void performLogin() {
    action.click(loginButton);
    action.type(usernameField, "username");
    action.type(passwordField, "password");
    action.click(submitButton);
}
```

**See:** [Basic Actions API Reference](../api-reference/basic-actions.md)

## If You Have Old Code

### Option 1: Fresh Start (Recommended)
- Start with current patterns from documentation
- Copy business logic, not old API calls
- Use modern ActionConfig system
- Much faster than migrating line-by-line

### Option 2: Incremental Update
1. Update dependencies to latest Brobot version
2. Fix compilation errors by referencing current documentation
3. Replace old patterns with current equivalents
4. Test thoroughly

## Common Pattern Updates

### Finding Objects
```java
// Use specialized PatternFindOptions
PatternFindOptions options = new PatternFindOptions.Builder()
    .withMinSimilarity(0.85)
    .build();
ActionResult result = action.find(options, targetImage);
```

### Clicking
```java
// Use specialized ClickOptions
ClickOptions options = new ClickOptions.Builder()
    .withClickUntil(ClickUntil.OBJECTS_VANISH)
    .build();
ActionResult result = action.click(options, button);
```

### Typing
```java
// Use specialized TypeOptions
TypeOptions options = new TypeOptions.Builder()
    .withClearFieldFirst(true)
    .build();
action.type(options, inputField, "text");
```

## Getting Help

- **Documentation**: Start with [Getting Started](../../01-getting-started/quick-start.md)
- **API Reference**: See [Basic Actions API](../api-reference/basic-actions.md)
- **Examples**: Check [Tutorials](../../02-tutorials/tutorial-basics/index.md)
- **Testing**: See [Testing Guide](../../04-testing/testing-intro.md)

## Historical Migration Guides

If you specifically need old version migration information, see `/archive/migration-guides-historical/` in the repository. However, we recommend using current patterns instead of maintaining old code.

## Version Information

**Current Version**: See repository version
**Upgrade Frequency**: Brobot is actively developed with frequent updates
**Breaking Changes**: Expected - project prioritizes clean code over backward compatibility

## Philosophy

From CLAUDE.md:

> **This library is in active development. We prioritize clean, maintainable code over backward compatibility.**
>
> **Key Principles:**
> 1. No Backward Compatibility Burden - remove old code paths entirely when refactoring
> 2. Single Source of Truth - one property, one configuration, one way to do things
> 3. Clean Refactoring - update all references immediately when changing APIs
> 4. No Deprecation Warnings - just make the change and update all affected code
> 5. Simplicity First - remove complexity, don't add compatibility layers

This approach keeps the codebase clean, reduces technical debt, and makes the library easier to understand and maintain.

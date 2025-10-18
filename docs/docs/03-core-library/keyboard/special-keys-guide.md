# Special Keys Guide for Brobot

## Overview

Brobot uses SikuliX's key representation system for special keyboard keys. When automating keyboard input, special keys like ENTER, ESC, TAB, and function keys must be represented using their proper Unicode escape sequences or string representations.

## Key Representations

### Common Special Keys

| Key Name | SikuliX Constant | Unicode/String Value | Usage Example |
|----------|------------------|---------------------|---------------|
| Enter | `Key.ENTER` | `"\n"` | Confirm dialogs, submit forms |
| Escape | `Key.ESC` | `"\u001b"` | Close windows, cancel operations |
| Tab | `Key.TAB` | `"\t"` | Navigate between fields |
| Space | `Key.SPACE` | `" "` | Space character |
| Backspace | `Key.BACKSPACE` | `"\b"` | Delete previous character |
| Delete | `Key.DELETE` | `"\u007f"` or `"\ue006"` | Delete next character |

### Navigation Keys

| Key Name | SikuliX Constant | Unicode Value | Usage Example |
|----------|------------------|---------------|---------------|
| Up Arrow | `Key.UP` | `"\ue000"` | Navigate up in lists |
| Right Arrow | `Key.RIGHT` | `"\ue001"` | Move cursor right |
| Down Arrow | `Key.DOWN` | `"\ue002"` | Navigate down in lists |
| Left Arrow | `Key.LEFT` | `"\ue003"` | Move cursor left |
| Page Up | `Key.PAGE_UP` | `"\ue004"` | Scroll up one page |
| Page Down | `Key.PAGE_DOWN` | `"\ue005"` | Scroll down one page |
| Home | `Key.HOME` | `"\ue008"` | Go to beginning |
| End | `Key.END` | `"\ue007"` | Go to end |
| Insert | `Key.INSERT` | `"\ue009"` | Toggle insert mode |

### Function Keys

| Key Name | SikuliX Constant | Unicode Value |
|----------|------------------|---------------|
| F1 | `Key.F1` | `"\ue011"` |
| F2 | `Key.F2` | `"\ue012"` |
| F3 | `Key.F3` | `"\ue013"` |
| F4 | `Key.F4` | `"\ue014"` |
| F5 | `Key.F5` | `"\ue015"` |
| F6 | `Key.F6` | `"\ue016"` |
| F7 | `Key.F7` | `"\ue017"` |
| F8 | `Key.F8` | `"\ue018"` |
| F9 | `Key.F9` | `"\ue019"` |
| F10 | `Key.F10` | `"\ue01a"` |
| F11 | `Key.F11` | `"\ue01b"` |
| F12 | `Key.F12` | `"\ue01c"` |

### Modifier Keys

| Key Name | SikuliX Constant | Unicode Value | Platform Notes |
|----------|------------------|---------------|----------------|
| Shift | `Key.SHIFT` | `"\ue020"` | All platforms |
| Control | `Key.CTRL` | `"\ue021"` | Windows/Linux |
| Alt | `Key.ALT` | `"\ue022"` | All platforms |
| Command/Meta | `Key.CMD` or `Key.META` | `"\ue023"` | macOS Command key |
| Windows | `Key.WIN` | `"\ue042"` | Windows key |

## Prerequisites and Setup

Before using special keys in your automation, ensure you have the basic Brobot setup:

### Required Imports

```java
// Core Brobot imports
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.buildWith.ObjectCollection;

// SikuliX imports
import org.sikuli.script.Key;

// Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
```

### Action Injection Pattern

All Brobot automation requires the Action class to be injected:

```java
@Component
public class MyAutomation {

    @Autowired
    private Action action;

    // Your automation methods here
}
```

This pattern is **required** for all examples in this guide.

## Usage in Brobot

### Method 1: SikuliX Key Constants (Recommended)

Use the SikuliX Key class constants which are more readable and maintainable:

```java
import io.github.jspinak.brobot.action.Action;
import org.sikuli.script.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyboardExample {

    @Autowired
    private Action action;

    public void pressSpecialKeys() {
        // Simple form - recommended for single keys
        action.type(Key.ENTER);   // Press ENTER
        action.type(Key.ESC);     // Press ESC
        action.type(Key.TAB);     // Press TAB
        action.type(Key.SPACE);   // Press SPACE
    }
}

// In StateString definitions
StateString enterKey = new StateString.Builder()
    .setString(Key.ENTER)  // Uses Key.ENTER constant
    .setName("Enter Key")
    .build();

StateString escapeKey = new StateString.Builder()
    .setString(Key.ESC)    // Uses Key.ESC constant
    .setName("Escape Key")
    .build();
```

### Method 2: Direct Unicode String (Alternative)

You can also use the Unicode escape sequence directly if you prefer not to import the SikuliX Key class:

```java
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.annotations.State;

// In a State class - define special keys as StateStrings
@State
public class AmountState {
    private final StateString enter;
    private final StateString escape;

    public AmountState() {
        // Enter key for confirming
        enter = new StateString.Builder()
            .setString("\n")  // Unicode for ENTER
            .setName("Enter Key")
            .build();

        // Escape key for closing
        escape = new StateString.Builder()
            .setString("\u001b")  // Unicode for ESC
            .setName("Escape Key")
            .build();
    }
}
```

### Method 3: In Transition Classes

Use special keys in your transition methods:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.model.buildWith.ObjectCollection;
import org.sikuli.script.Key;
import org.springframework.beans.factory.annotation.Autowired;

@TransitionSet(state = DialogState.class)
public class DialogTransitions {

    @Autowired
    private Action action;

    @OutgoingTransition(activate = {MainScreenState.class})
    public boolean closeDialog() {
        TypeOptions options = new TypeOptions.Builder()
            .withBeforeActionLog("Closing dialog with ESC key...")
            .withSuccessLog("Dialog closed")
            .withFailureLog("Failed to close dialog")
            .build();

        // Type the ESC key using perform() with options
        return action.perform(options,
            new ObjectCollection.Builder().withStrings(Key.ESC).build()
        ).isSuccess();
    }

    public boolean submitForm() {
        // Simple form without options
        return action.type(Key.ENTER).isSuccess();
    }
}
```

### Method 4: Combining with Regular Text

You can combine special keys with regular text:

```java
import io.github.jspinak.brobot.action.Action;
import org.sikuli.script.Key;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class FormFiller {

    @Autowired
    private Action action;

    public void fillLoginForm() {
        // Type text and press Tab to move to next field
        action.type("username" + Key.TAB + "password");

        // Type text and press Enter to submit
        action.type("search term" + Key.ENTER);
    }
}
```

### Method 5: Using StateString for Reusable Keys

Create reusable special key objects:

```java
import io.github.jspinak.brobot.model.state.StateString;
import org.sikuli.script.Key;

public class CommonKeys {
    public static final StateString ENTER = new StateString.Builder()
        .setString(Key.ENTER)
        .setName("Enter Key")
        .build();

    public static final StateString ESCAPE = new StateString.Builder()
        .setString(Key.ESC)
        .setName("Escape Key")
        .build();

    public static final StateString TAB = new StateString.Builder()
        .setString(Key.TAB)
        .setName("Tab Key")
        .build();
}

// Usage
action.type(CommonKeys.ENTER);
```

### Complete Working Example

Here's a full, runnable example showing special keys in a complete Brobot component:

```java
package com.example.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.buildWith.ObjectCollection;
import org.sikuli.script.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DialogAutomation {

    @Autowired
    private Action action;

    // Define reusable key constants
    private final StateString ENTER_KEY = new StateString.Builder()
        .setString(Key.ENTER)
        .setName("Enter Key")
        .build();

    private final StateString ESC_KEY = new StateString.Builder()
        .setString(Key.ESC)
        .setName("Escape Key")
        .build();

    /**
     * Submit form by pressing Enter
     */
    public boolean submitForm() {
        return action.type(ENTER_KEY).isSuccess();
    }

    /**
     * Close dialog by pressing Escape
     */
    public boolean closeDialog() {
        return action.type(ESC_KEY).isSuccess();
    }

    /**
     * Navigate form fields with Tab
     */
    public void fillForm(String username, String password) {
        action.type(username);
        action.type(Key.TAB);  // Move to next field
        action.type(password);
        action.type(Key.ENTER); // Submit
    }

    /**
     * Type with timing control and logging
     */
    public void typeWithOptions(String text) {
        TypeOptions options = new TypeOptions.Builder()
            .withBeforeActionLog("Typing: " + text)
            .setPauseBeforeBegin(0.5)
            .setPauseAfterEnd(1.0)
            .setTypeDelay(0.1)
            .build();

        // Using perform() with ObjectCollection for options
        action.perform(options,
            new ObjectCollection.Builder().withStrings(text).build());
    }
}
```

This example shows:
- ✅ Proper Action injection with @Autowired
- ✅ Correct StateString builder using setString()
- ✅ Reusable key constants
- ✅ Multiple usage patterns
- ✅ Complete, compilable class
- ✅ TypeOptions with logging and timing

## Keyboard Shortcuts and Combinations

### Using Modifier Keys

For keyboard shortcuts with modifiers (Ctrl, Alt, Shift), Brobot provides several approaches:

> **Note**: The Action class doesn't expose low-level keyDown/keyUp methods directly. For complex key combinations, use one of these patterns:

**Method 1: SikuliX Key Modifiers (Simplest)**

```java
import org.sikuli.script.Key;
import org.sikuli.script.Region;

// Note: This requires direct SikuliX Region access
// Check current Brobot API for keyboard combination support
Region region = new Region(0, 0, 100, 100);
region.type("c", Key.CTRL);  // Ctrl+C
region.type("v", Key.CTRL);  // Ctrl+V
```

**Method 2: Action Chains (For Advanced Scenarios)**

For complex keyboard operations, consider using ConditionalActionChain or contact Brobot maintainers for the current keyboard shortcut API.

> **For current keyboard shortcut implementation**, see:
> - [ActionConfig Reference](../action-config/05-reference.md) - KeyDownOptions and KeyUpOptions
> - [Form Automation](../action-config/10-form-automation.md) - Practical keyboard combination examples
> - [AI Brobot Project Creation Guide](../../01-getting-started/ai-brobot-project-creation.md) - Complete automation examples

## Common Pitfalls and Solutions

### Pitfall 1: Using String Literals Instead of Unicode

❌ **Wrong:**
```java
action.type("ESC", options);  // This types the letters E, S, C
action.type("ENTER", options); // This types the letters E, N, T, E, R
```

✅ **Correct:**
```java
action.type("\u001b", options); // ESC key
action.type("\n", options);     // ENTER key
```

### Pitfall 2: Platform Differences

Some keys behave differently across platforms:
- Use `Key.CMD` on macOS instead of `Key.CTRL` for command shortcuts
- `Key.WIN` is Windows-specific
- Line endings: Windows uses `\r\n`, Unix/Mac use `\n`

### Pitfall 3: Timing Issues

Special keys may need pauses for the application to process them:

```java
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.buildWith.ObjectCollection;
import org.sikuli.script.Key;

TypeOptions options = new TypeOptions.Builder()
    .setPauseBeforeBegin(0.5)  // Wait before typing
    .setPauseAfterEnd(1.0)      // Wait after typing
    .setTypeDelay(0.1)          // Delay between keystrokes
    .build();

// Apply timing to action
action.perform(options,
    new ObjectCollection.Builder().withStrings(Key.ENTER).build());
```

## Testing Special Keys

When testing automation that uses special keys:

1. **Mock Mode**: Special keys work in mock mode for testing
2. **Logging**: Use verbose logging to verify correct key sequences
3. **Visual Feedback**: Enable visual feedback to see what keys are being "pressed"

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.buildWith.ObjectCollection;
import org.sikuli.script.Key;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Test
public void testSpecialKey() {
    TypeOptions debugOptions = new TypeOptions.Builder()
        .withBeforeActionLog("Pressing special key: ESC")
        .withSuccessLog("Special key pressed successfully")
        .withFailureLog("Failed to press special key")
        .build();

    ActionResult result = action.perform(debugOptions,
        new ObjectCollection.Builder().withStrings(Key.ESC).build());

    assertTrue(result.isSuccess());
}
```

> **For comprehensive testing strategies**, see the [Mock Mode Guide](../../04-testing/mock-mode-guide.md).

## Best Practices

1. **Use Constants**: Define special keys as constants or StateStrings for reusability
2. **Document Intent**: Always comment what special key you're using and why
3. **Test Cross-Platform**: If your automation runs on multiple OS, test special keys on each
4. **Handle Failures**: Special keys may fail if the window loses focus
5. **Use Appropriate Delays**: Some applications need time to process special keys

## Reference Table - All Special Keys

| Category | Key | Unicode | Common Use |
|----------|-----|---------|------------|
| **Control** | Enter | `\n` | Submit, confirm |
| | Escape | `\u001b` | Cancel, close |
| | Tab | `\t` | Next field |
| | Space | ` ` | Select, space |
| | Backspace | `\b` | Delete back |
| | Delete | `\u007f` | Delete forward |
| **Navigation** | Up | `\ue000` | Move up |
| | Down | `\ue002` | Move down |
| | Left | `\ue003` | Move left |
| | Right | `\ue001` | Move right |
| | Home | `\ue008` | Start of line |
| | End | `\ue007` | End of line |
| | Page Up | `\ue004` | Previous page |
| | Page Down | `\ue005` | Next page |
| **Function** | F1-F12 | `\ue011`-`\ue01c` | Various |
| **Modifier** | Shift | `\ue020` | Uppercase, select |
| | Ctrl | `\ue021` | Shortcuts |
| | Alt | `\ue022` | Menu access |
| | Cmd/Meta | `\ue023` | macOS commands |

## Related Documentation

### Getting Started
- **[Quick Start Guide](../../01-getting-started/quick-start.md)** - Introduction to Brobot actions including typing
- **[Installation](../../01-getting-started/installation.md)** - Setting up Brobot with SikuliX dependencies
- **[Introduction](../../01-getting-started/introduction.md)** - GUI automation fundamentals

### Core Concepts
- **[States Guide](../../01-getting-started/states.md)** - Using StateString to organize keyboard inputs
- **[Transitions Guide](../../01-getting-started/transitions.md)** - Using special keys in state transitions
- **[AI Brobot Project Creation](../../01-getting-started/ai-brobot-project-creation.md)** - Complete special keys examples (lines 1162-1196)

### Action Configuration
- **[ActionConfig Overview](../action-config/01-overview.md)** - Understanding TypeOptions and the ActionConfig system
- **[ActionConfig Reference](../action-config/05-reference.md)** - Complete TypeOptions API documentation
- **[Convenience Methods](../action-config/18-convenience-methods.md)** - Simple action.type() method usage
- **[Form Automation](../action-config/10-form-automation.md)** - Using Tab/Enter/shortcuts in forms

### Testing
- **[Mock Mode Guide](../../04-testing/mock-mode-guide.md)** - Testing keyboard automation without real GUI
- **[Testing Introduction](../../04-testing/testing-intro.md)** - Test strategies overview

### External Resources
- **[SikuliX Key Documentation](https://sikulix-2014.readthedocs.io/en/latest/keys.html)** - Official SikuliX key reference
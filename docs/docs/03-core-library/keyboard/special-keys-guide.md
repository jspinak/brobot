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

## Usage in Brobot

### Method 1: SikuliX Key Constants (Recommended)

Use the SikuliX Key class constants which are more readable and maintainable:

```java
import org.sikuli.script.Key;

// Using SikuliX Key constants
action.type(Key.ENTER, typeOptions);  // Press ENTER
action.type(Key.ESC, typeOptions);    // Press ESC
action.type(Key.TAB, typeOptions);    // Press TAB
action.type(Key.SPACE, typeOptions);  // Press SPACE

// In StateString definitions
StateString enterKey = new StateString.Builder()
    .withString(Key.ENTER)  // Uses Key.ENTER constant
    .setName("Enter Key")
    .build();

StateString escapeKey = new StateString.Builder()
    .withString(Key.ESC)    // Uses Key.ESC constant
    .setName("Escape Key")
    .build();
```

### Method 2: Direct Unicode String (Alternative)

You can also use the Unicode escape sequence directly if you prefer not to import the SikuliX Key class:

```java
// In a State class - define special keys as StateStrings
public class AmountState {
    private final StateString enter;
    private final StateString escape;

    public AmountState() {
        // Enter key for confirming
        enter = new StateString.Builder()
            .withString("\n")  // Unicode for ENTER
            .setName("Enter Key")
            .build();

        // Escape key for closing
        escape = new StateString.Builder()
            .withString("\u001b")  // Unicode for ESC
            .setName("Escape Key")
            .build();
    }
}
```

### Method 3: In Transition Classes

Use special keys in your transition methods:

```java
import org.sikuli.script.Key;

@TransitionSet(state = DialogState.class)
public class DialogTransitions {

    @OutgoingTransition(to = MainScreenState.class)
    public boolean closeDialog() {
        TypeOptions options = new TypeOptions.Builder()
            .withBeforeActionLog("Closing dialog with ESC key...")
            .withSuccessLog("Dialog closed")
            .withFailureLog("Failed to close dialog")
            .build();

        // Type the ESC key using SikuliX constant
        return action.type(Key.ESC, options).isSuccess();
    }

    public boolean submitForm() {
        TypeOptions options = new TypeOptions.Builder()
            .withBeforeActionLog("Submitting form with Enter key...")
            .build();

        // Type the Enter key using SikuliX constant
        return action.type(Key.ENTER, options).isSuccess();
    }
}
```

### Method 4: Combining with Regular Text

You can combine special keys with regular text:

```java
import org.sikuli.script.Key;

// Type text and press Tab to move to next field
action.type("username" + Key.TAB + "password", typeOptions);

// Type text and press Enter to submit
action.type("search term" + Key.ENTER, typeOptions);
```

### Method 5: Using StateString for Reusable Keys

Create reusable special key objects:

```java
import org.sikuli.script.Key;

public class CommonKeys {
    public static final StateString ENTER = new StateString.Builder()
        .withString(Key.ENTER)
        .setName("Enter Key")
        .build();

    public static final StateString ESCAPE = new StateString.Builder()
        .withString(Key.ESC)
        .setName("Escape Key")
        .build();

    public static final StateString TAB = new StateString.Builder()
        .withString(Key.TAB)
        .setName("Tab Key")
        .build();
}

// Usage
action.type(CommonKeys.ENTER, typeOptions);
```

## Keyboard Shortcuts and Combinations

### Using Modifier Keys

For keyboard shortcuts with modifiers (Ctrl, Alt, Shift), you typically need to use `KeyDown` and `KeyUp` actions:

```java
// Ctrl+C (Copy)
action.keyDown(KeyboardController.SpecialKey.CTRL);
action.type("c");
action.keyUp(KeyboardController.SpecialKey.CTRL);

// Alt+Tab (Switch windows)
action.keyDown(KeyboardController.SpecialKey.ALT);
action.type("\t");  // Tab key
action.keyUp(KeyboardController.SpecialKey.ALT);
```

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
TypeOptions options = new TypeOptions.Builder()
    .setPauseBeforeBegin(0.5)  // Wait before typing
    .setPauseAfterEnd(1.0)      // Wait after typing
    .setTypeDelay(0.1)          // Delay between keystrokes
    .build();
```

## Testing Special Keys

When testing automation that uses special keys:

1. **Mock Mode**: Special keys work in mock mode for testing
2. **Logging**: Use verbose logging to verify correct key sequences
3. **Visual Feedback**: Enable visual feedback to see what keys are being "pressed"

```java
TypeOptions debugOptions = new TypeOptions.Builder()
    .withBeforeActionLog("Pressing special key: ESC")
    .withSuccessLog("Special key pressed successfully")
    .withFailureLog("Failed to press special key")
    .build();
```

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

## Further Reading

- [SikuliX Key Documentation](https://sikulix-2014.readthedocs.io/en/latest/keys.html)
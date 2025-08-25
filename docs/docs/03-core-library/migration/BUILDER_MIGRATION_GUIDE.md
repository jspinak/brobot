# Builder Pattern Migration Guide

## Overview
This guide helps migrate existing code to use the new standardized builder pattern with `setXxx()` naming convention.

## Quick Reference

### Before (Old Pattern)
```java
ClickOptions options = ClickOptions.builder()
    .numberOfClicks(2)
    .pressOptions(MousePressOptions.builder()
        .button(MouseButton.RIGHT)
        .pauseAfterMouseUp(0.5)
        .build())
    .verification(VerificationOptions.builder()
        .event(VerificationOptions.Event.TEXT_APPEARS)
        .text("Success")
        .build())
    .build();
```

### After (New Pattern)
```java
ClickOptions options = ClickOptions.builder()
    .setNumberOfClicks(2)
    .setPressOptions(MousePressOptions.builder()
        .setButton(MouseButton.RIGHT)
        .setPauseAfterMouseUp(0.5)
        .build())
    .setVerification(VerificationOptions.builder()
        .setEvent(VerificationOptions.Event.TEXT_APPEARS)
        .setText("Success")
        .build())
    .build();
```

## Migration Mappings

### MousePressOptions
| Old Method | New Method |
|------------|------------|
| `.button(value)` | `.setButton(value)` |
| `.pauseBeforeMouseDown(value)` | `.setPauseBeforeMouseDown(value)` |
| `.pauseAfterMouseDown(value)` | `.setPauseAfterMouseDown(value)` |
| `.pauseBeforeMouseUp(value)` | `.setPauseBeforeMouseUp(value)` |
| `.pauseAfterMouseUp(value)` | `.setPauseAfterMouseUp(value)` |

### VerificationOptions
| Old Method | New Method |
|------------|------------|
| `.condition(value)` | `.setCondition(value)` |
| `.event(value)` | `.setEvent(value)` |
| `.text(value)` | `.setText(value)` |
| `.objectCollection(value)` | `.setObjectCollection(value)` |

### RepetitionOptions
| Old Method | New Method |
|------------|------------|
| `.timesToRepeatIndividualAction(value)` | `.setTimesToRepeatIndividualAction(value)` |
| `.pauseBetweenIndividualActions(value)` | `.setPauseBetweenIndividualActions(value)` |
| `.timesToRepeatActionSequence(value)` | `.setTimesToRepeatActionSequence(value)` |
| `.pauseBetweenActionSequences(value)` | `.setPauseBetweenActionSequences(value)` |

### MatchAdjustmentOptions
| Old Method | New Method |
|------------|------------|
| `.targetPosition(value)` | `.setTargetPosition(value)` |
| `.targetOffset(value)` | `.setTargetOffset(value)` |
| `.addW(value)` | `.setAddW(value)` |
| `.addH(value)` | `.setAddH(value)` |
| `.addX(value)` | `.setAddX(value)` |
| `.addY(value)` | `.setAddY(value)` |
| `.absoluteW(value)` | `.setAbsoluteW(value)` |
| `.absoluteH(value)` | `.setAbsoluteH(value)` |

### MatchFusionOptions
| Old Method | New Method |
|------------|------------|
| `.fusionMethod(value)` | `.setFusionMethod(value)` |
| `.maxFusionDistanceX(value)` | `.setMaxFusionDistanceX(value)` |
| `.maxFusionDistanceY(value)` | `.setMaxFusionDistanceY(value)` |
| `.sceneToUseForCaptureAfterFusingMatches(value)` | `.setSceneToUseForCaptureAfterFusingMatches(value)` |

## Automated Migration Script

### Using sed (Linux/Mac)
```bash
#!/bin/bash
# migrate-builders.sh

# Backup files first
find . -name "*.java" -exec cp {} {}.backup \;

# MousePressOptions migrations
find . -name "*.java" -exec sed -i 's/\.button(/\.setButton(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.pauseBeforeMouseDown(/\.setPauseBeforeMouseDown(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.pauseAfterMouseDown(/\.setPauseAfterMouseDown(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.pauseBeforeMouseUp(/\.setPauseBeforeMouseUp(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.pauseAfterMouseUp(/\.setPauseAfterMouseUp(/g' {} \;

# VerificationOptions migrations
find . -name "*.java" -exec sed -i 's/\.event(/\.setEvent(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.text(/\.setText(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.condition(/\.setCondition(/g' {} \;

# RepetitionOptions migrations
find . -name "*.java" -exec sed -i 's/\.timesToRepeatIndividualAction(/\.setTimesToRepeatIndividualAction(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.pauseBetweenIndividualActions(/\.setPauseBetweenIndividualActions(/g' {} \;

# MatchAdjustmentOptions migrations
find . -name "*.java" -exec sed -i 's/\.addW(/\.setAddW(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.addH(/\.setAddH(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.addX(/\.setAddX(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.addY(/\.setAddY(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.targetOffset(/\.setTargetOffset(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.targetPosition(/\.setTargetPosition(/g' {} \;

# MatchFusionOptions migrations
find . -name "*.java" -exec sed -i 's/\.fusionMethod(/\.setFusionMethod(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.maxFusionDistanceX(/\.setMaxFusionDistanceX(/g' {} \;
find . -name "*.java" -exec sed -i 's/\.maxFusionDistanceY(/\.setMaxFusionDistanceY(/g' {} \;

echo "Migration complete. Backup files created with .backup extension"
```

### Using IntelliJ IDEA

1. **Structural Search and Replace** (Ctrl+Shift+A → "Structural Search")
   
   Search Template:
   ```
   $instance$.button($param$)
   ```
   
   Replace Template:
   ```
   $instance$.setButton($param$)
   ```

2. **Repeat for each method mapping**

### Using Regular Expressions in IDE

Find: `(\w+)\.button\(`
Replace: `$1.setButton(`

Find: `(\w+)\.pauseAfterMouseUp\(`
Replace: `$1.setPauseAfterMouseUp(`

## Common Migration Issues

### Issue 1: Chained Builder Calls
**Problem**: Long builder chains may be incorrectly migrated
```java
// May break if not careful
builder.button(LEFT).pauseAfterMouseUp(0.5).build()
```

**Solution**: Migrate carefully, test after each change
```java
builder.setButton(LEFT).setPauseAfterMouseUp(0.5).build()
```

### Issue 2: Static Imports
**Problem**: Static imports may hide the actual class
```java
import static io.github.jspinak.brobot.model.action.MouseButton.*;
// Later in code:
.button(RIGHT) // Which builder is this?
```

**Solution**: Use IDE's "Find Usages" to identify the correct class

### Issue 3: Custom Extensions
**Problem**: Custom classes extending Brobot classes may have different patterns
```java
public class MyCustomOptions extends ClickOptions {
    // May have its own builder pattern
}
```

**Solution**: Update custom classes to follow the same convention

## Validation Checklist

After migration, verify:

- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] IDE autocomplete shows `setXxx` methods
- [ ] No warnings about deprecated methods
- [ ] Documentation/comments updated if needed
- [ ] Custom builders follow same pattern

## Gradual Migration Strategy

### Phase 1: Core Library (Completed)
- ✅ Update Lombok annotations
- ✅ Fix compilation errors in main code
- ✅ Update core test files

### Phase 2: Applications
1. Update direct builder usage
2. Run tests after each module
3. Update documentation

### Phase 3: External Integrations
1. Update published APIs
2. Provide compatibility layer if needed
3. Update client code

## Compatibility Layer (Optional)

For gradual migration, you can create compatibility methods:

```java
@Deprecated
public Builder button(MouseButton button) {
    return setButton(button);
}

@Deprecated  
public Builder pauseAfterMouseUp(double pause) {
    return setPauseAfterMouseUp(pause);
}
```

**Note**: Remove these after full migration.

## IDE Support

### IntelliJ IDEA
- Use "Refactor → Rename" for systematic changes
- Enable "Deprecated API usage" inspection
- Use "Code → Inspect Code" to find issues

### Eclipse
- Use "Search → File Search" with regex
- Enable "Deprecated API" warnings
- Use "Source → Clean Up" with custom profile

### VS Code
- Use Find and Replace with regex
- Install Java extension pack
- Use "Problems" panel to track issues

## Testing Migration

### Unit Test for Builder Migration
```java
@Test
public void testBuilderMigration() {
    // Old pattern (should not compile)
    // ClickOptions old = ClickOptions.builder()
    //     .numberOfClicks(2)  // This should fail
    //     .build();
    
    // New pattern (should work)
    ClickOptions newPattern = ClickOptions.builder()
        .setNumberOfClicks(2)  // This should work
        .build();
    
    assertEquals(2, newPattern.getNumberOfClicks());
}
```

## Support

If you encounter issues during migration:

1. Check this guide for the correct mapping
2. Refer to JavaDoc for the latest API
3. Run tests to validate changes
4. Report issues at: https://github.com/jspinak/brobot/issues
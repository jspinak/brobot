# Quick Start Example

This example demonstrates the basics of getting started with Brobot 1.1.0 using the new ActionConfig API.

## Documentation Reference

This example corresponds to: `/docs/01-getting-started/quick-start.md`

## What This Example Shows

1. **Minimal Complete Example** - The absolute minimum code needed (`MinimalBrobot.java`)
2. **Full vs Simplified Approaches** - Explicit steps vs convenience methods
3. **Convenience Methods** - `action.find()`, `action.click()`, `action.type()`
4. **Production Example** - Error handling and logging best practices

## Key Files

- `MinimalBrobot.java` - The smallest possible Brobot application
- `SimpleAutomation.java` - Shows different approaches:
  - Full explicit version
  - Simplified convenience version
  - Various convenience methods
  - Production-ready example
- `QuickStartRunner.java` - Runs all examples on startup

## Running the Example

```bash
./gradlew bootRun
```

The example runs in mock mode by default, so no actual GUI interaction occurs.

## Key Concepts Demonstrated

### Minimal Code
```java
// This is all you need!
StateImage button = new StateImage.Builder()
    .addPatterns("button")
    .build();
action.click(button);
```

### Default Values
- **Similarity**: 0.7 (70% match)
- **Strategy**: FIRST (find first match)
- **Click Type**: Single left click
- **Search Duration**: 3 seconds timeout
- **Search Region**: Entire screen

### Convenience vs Control

**Use convenience methods when:**
- Prototyping or testing
- Default settings work for you
- You want minimal, readable code

**Use full builders when:**
- You need specific similarity thresholds
- Custom timeouts are required
- Detailed logging is important
- Production code requiring fine control

## To Run with Real GUI

1. Create an `images/` directory
2. Add screenshots:
   - `button.png`
   - `submit-button.png`
   - `form/username-field.png`
   - `form/password-field.png`
   - `form/submit-button.png`
3. Set `brobot.core.mock: false` in application.yml

## Next Steps

After understanding these basics:
1. Look at the action-hierarchy example for complex actions
2. Check out pure-actions-quickstart for the modern API
3. Explore state management in the tutorial-basics example
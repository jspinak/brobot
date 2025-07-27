# Tutorials Overview

## Version Information

:::tip Latest Patterns (v1.1.0)
For the most up-to-date Brobot patterns and best practices, see the **[Claude Automator Example](./tutorial-claude-automator/intro.md)** tutorial. This demonstrates:
- Modern ActionConfig classes (replacing deprecated ActionOptions)
- Fluent API with action chaining
- Enhanced developer experience features
- Direct state component access
- JavaStateTransition usage
:::

:::caution Older Tutorials
The following tutorials were written for earlier versions of Brobot and may use deprecated patterns:
- **Tutorial Basics** - Core concepts (may use older ActionOptions)
- **MrDoob Tutorial** - Web automation example (pre-v1.0 patterns)

While these tutorials still contain valuable concepts, refer to the Claude Automator example for current best practices.
:::

## Choosing a Tutorial

### New to Brobot?
Start with the **[Claude Automator Example](./tutorial-claude-automator/intro.md)** - it's the most current and demonstrates all modern patterns.

### Migrating from Older Versions?
1. Review the Claude Automator example for new patterns
2. Check the [Migration Guide](/docs/core-library/guides/migration-guide) 
3. Reference older tutorials for conceptual understanding

### Learning Specific Concepts?
- **State Management**: All tutorials cover this
- **Modern Patterns**: Claude Automator only
- **Web Automation**: MrDoob tutorial (concepts still valid)

## Quick Pattern Comparison

### Old Pattern (Pre-v1.0)
```java
ActionOptions options = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.CLICK)
    .build();
```

### New Pattern (v1.1.0+)
```java
ClickOptions options = new ClickOptions.Builder()
    .build();
```

For a complete migration reference, see the [Action Config Migration Guide](/docs/core-library/action-config/migration-guide).
---
sidebar_position: 8
title: Complex Workflows
description: Build sophisticated multi-step automation workflows using ActionChainOptions
---

# Complex Workflows with Action Chains

This guide demonstrates how to build sophisticated automation workflows by combining multiple actions into complex sequences. These patterns are essential for real-world automation scenarios.

> **Foundation**: This guide assumes familiarity with [Action Chaining](./07-action-chaining.md) basics. For NESTED vs CONFIRM strategies and object type preservation, see that guide.

## Required Imports

All examples in this guide require these imports:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;
```

## Setup: Dependency Injection

All workflow examples require `ActionChainExecutor` and optionally `Action`:

```java
@Component
public class MyWorkflows {
    private final ActionChainExecutor chainExecutor;
    private final Action action;

    public MyWorkflows(ActionChainExecutor chainExecutor, Action action) {
        this.chainExecutor = chainExecutor;
        this.action = action;
    }

    // Workflow methods go here...
}
```

## StateImage Initialization

Before using workflows, create your StateImages:

```java
StateImage usernameField = new StateImage.Builder()
    .setName("username-field")
    .addPattern("images/forms/username.png")
    .build();

StateImage passwordField = new StateImage.Builder()
    .setName("password-field")
    .addPattern("images/forms/password.png")
    .build();

StateImage loginButton = new StateImage.Builder()
    .setName("login-button")
    .addPattern("images/forms/login-btn.png")
    .build();
```

## Form Automation Example

One of the most common automation tasks is filling out forms. Here's a comprehensive example:

```java
public ActionResult fillLoginForm(StateImage usernameField, String username,
                                  StateImage passwordField, String password,
                                  StateImage loginButton) {
    
    ActionChainOptions loginChain = new ActionChainOptions.Builder(
        // Step 1: Click username field
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Step 2: Type username
        .then(new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build())
        // Step 3: Click password field
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Step 4: Type password
        .then(new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build())
        // Step 5: Click login button
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(1.0) // Wait for login to process
            .build())
        .build();
    
    // Execute with the appropriate ObjectCollections for each step
    return chainExecutor.executeChain(loginChain, new ActionResult(),
        usernameField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(username).build(),
        passwordField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(password).build(),
        loginButton.asObjectCollection()
    );
}
```

### Key Points:
- Each action in the chain corresponds to a specific UI interaction
- Delays are added to allow the UI to respond
- Object collections are passed in the same order as the actions
- **Important**: The number of ObjectCollections must match the number of actions in the chain

### Object Collection Ordering

The `executeChain()` method accepts variable arguments of ObjectCollections. The ordering is critical:
- First ObjectCollection → First action
- Second ObjectCollection → Second action
- And so on...

In the example above:
1. `usernameField.asObjectCollection()` → ClickOptions (click username field)
2. `new ObjectCollection.Builder().withStrings(username).build()` → TypeOptions (type username)
3. `passwordField.asObjectCollection()` → ClickOptions (click password field)
4. `new ObjectCollection.Builder().withStrings(password).build()` → TypeOptions (type password)
5. `loginButton.asObjectCollection()` → ClickOptions (click login button)

**Mismatch Warning**: If the count doesn't match, actions will receive wrong data or fail.

## Recommended: ConditionalActionChain for Forms

For most form workflows, `ConditionalActionChain` provides a more intuitive API:

```java
import io.github.jspinak.brobot.action.ConditionalActionChain;

public ActionResult fillLoginFormSimple(StateImage usernameField, String username,
                                        StateImage passwordField, String password,
                                        StateImage loginButton) {
    return ConditionalActionChain
        .find(usernameField)
        .ifFoundClick()
        .ifFoundType(username)
        .then(passwordField)
        .ifFoundClick()
        .ifFoundType(password)
        .then(loginButton)
        .ifFoundClick()
        .perform(action);
}
```

**Advantages**:
- Clearer intent with `ifFoundClick()`, `ifFoundType()` methods
- Automatic error handling with conditional logic
- No need to manage ObjectCollection ordering manually
- Built-in logging and debugging

See [Conditional Action Chains](./15-conditional-chains-examples.md) for comprehensive examples.

## Multi-Step Navigation

Navigate through complex UI hierarchies using implicit chaining (simpler and clearer):

```java
public ActionResult navigateToSettings(StateImage menuButton,
                                       StateImage settingsMenu,
                                       StateImage securityTab) {

    // Create reusable find-and-click pattern
    PatternFindOptions findAndClick = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .build();

    // Step 1: Click menu button
    action.perform(new ClickOptions.Builder()
        .setPauseAfterEnd(0.5)
        .build(), menuButton.asObjectCollection());

    // Step 2: Find and click settings in menu
    action.perform(findAndClick, settingsMenu.asObjectCollection());

    // Step 3: Find and click security tab
    ActionResult result = action.perform(findAndClick, securityTab.asObjectCollection());

    return result;
}
```

### Alternative: Using ConditionalActionChain

```java
public ActionResult navigateToSettingsSimple(StateImage menuButton,
                                             StateImage settingsMenu,
                                             StateImage securityTab) {
    return ConditionalActionChain
        .find(menuButton)
        .ifFoundClick()
        .then(settingsMenu)
        .ifFoundClick()
        .then(securityTab)
        .ifFoundClick()
        .perform(action);
}
```

## Complex Interaction Workflow

This example shows a workflow that combines finding, hovering, clicking, and verification:

```java
public ActionResult complexWorkflow(StateImage target, StateImage expectedResult) {
    
    ActionChainOptions workflow = new ActionChainOptions.Builder(
        // Step 1: Find the target element
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.8)
            .build())
        // Step 2: Move mouse to target (hover effect)
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.5) // Hold hover
            .build())
        // Step 3: Click the target
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.3)
            .build())
        // Step 4: Verify the expected result appears
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .setPauseBeforeBegin(2.0) // Wait for UI update
            .build())
        .build();
    
    // Note: Same target is used for find, move, and click
    return chainExecutor.executeChain(workflow, new ActionResult(),
        target.asObjectCollection(),
        target.asObjectCollection(), // For mouse move
        target.asObjectCollection(), // For click
        expectedResult.asObjectCollection()
    );
}
```

## Drag and Drop Operations

Brobot provides a dedicated `DragOptions` API for drag-and-drop operations:

```java
public ActionResult dragAndDrop(StateImage source, StateImage target) {

    DragOptions dragOptions = new DragOptions.Builder()
        .setDelayBetweenMouseDownAndMove(0.5)
        .setDelayAfterDrag(0.5)
        .build();

    ObjectCollection sourceCollection = source.asObjectCollection();
    ObjectCollection targetCollection = target.asObjectCollection();

    return action.perform(dragOptions, sourceCollection, targetCollection);
}
```

**Key Points**:
- DragOptions handles finding, mouse down, move, and mouse up automatically
- Pass source and target as separate ObjectCollections
- Configure timing delays to ensure smooth drag operations
- Result contains a `Movement` object with start/end locations

### Advanced: Custom Drag Implementation

For specialized drag operations requiring fine control, you can build custom drag workflows:

```java
public ActionResult customDragDrop(StateImage source, StateImage target) {

    ActionChainOptions dragChain = new ActionChainOptions.Builder(
        // Find source element
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build())
        // Move to source
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build())
        // Mouse down
        .then(new MouseDownOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Find target location
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build())
        // Move to target (while holding)
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build())
        // Release mouse
        .then(new MouseUpOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .build();

    return chainExecutor.executeChain(dragChain, new ActionResult(),
        source.asObjectCollection(),
        source.asObjectCollection(), // For mouse move
        source.asObjectCollection(), // For mouse down
        target.asObjectCollection(), // Find target
        target.asObjectCollection(), // Move to target
        target.asObjectCollection()  // Mouse up at target
    );
}
```

**When to use custom implementation**:
- Need precise control over each drag phase
- Require custom validation between drag steps
- Implementing complex drag patterns like multi-stop dragging

## Dynamic Workflow Building

Build workflows dynamically based on runtime conditions:

```java
public class DynamicWorkflowBuilder {
    
    public ActionChainOptions buildWorkflow(List<WorkflowStep> steps) {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must have at least one step");
        }
        
        // Start with the first step
        ActionChainOptions.Builder builder = new ActionChainOptions.Builder(
            steps.get(0).toActionConfig()
        );
        
        // Add remaining steps
        for (int i = 1; i < steps.size(); i++) {
            builder.then(steps.get(i).toActionConfig());
        }
        
        return builder.build();
    }
    
    // Example workflow step
    public static class WorkflowStep {
        private final String actionType;
        private final Map<String, Object> parameters;

        // Returns an ActionConfig (abstract base class for all action configuration)
        public ActionConfig toActionConfig() {
            switch (actionType) {
                case "click":
                    return new ClickOptions.Builder()
                        .setPauseAfterEnd((Double) parameters.getOrDefault("pause", 0.3))
                        .build();
                case "type":
                    return new TypeOptions.Builder()
                        .setTypeDelay((Double) parameters.getOrDefault("delay", 0.05))
                        .build();
                case "find":
                    return new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.valueOf(
                            (String) parameters.getOrDefault("strategy", "FIRST")))
                        .build();
                default:
                    throw new IllegalArgumentException("Unknown action type: " + actionType);
            }
        }
    }
}
```

## Error Handling in Workflows

Handle errors gracefully in complex workflows:

```java
public ActionResult robustWorkflow(StateImage element1, StateImage element2) {
    
    // Build the main workflow
    ActionChainOptions mainWorkflow = new ActionChainOptions.Builder(
        new PatternFindOptions.Builder().build())
        .then(new ClickOptions.Builder().build())
        .then(new PatternFindOptions.Builder().build())
        .build();
    
    // Execute with error handling
    ActionResult result = chainExecutor.executeChain(
        mainWorkflow, new ActionResult(),
        element1.asObjectCollection(),
        element1.asObjectCollection(),
        element2.asObjectCollection()
    );
    
    // Check intermediate results if needed
    if (!result.isSuccess()) {
        // Try alternative workflow
        ActionChainOptions fallbackWorkflow = new ActionChainOptions.Builder(
            new ClickOptions.Builder()
                .setPauseAfterEnd(1.0) // Longer pause
                .build())
            .then(new PatternFindOptions.Builder()
                .setSimilarity(0.7) // Lower threshold
                .build())
            .build();
        
        result = chainExecutor.executeChain(
            fallbackWorkflow, new ActionResult(),
            element1.asObjectCollection(),
            element2.asObjectCollection()
        );
    }
    
    return result;
}
```

## Best Practices for Complex Workflows

1. **Break down complexity**: Divide large workflows into smaller, reusable chains
2. **Add appropriate delays**: UI elements need time to respond
3. **Use meaningful variable names**: Make your workflow self-documenting
4. **Consider failure points**: Build in fallback strategies
5. **Log intermediate results**: Helps with debugging complex chains
6. **Test incrementally**: Build and test workflows step by step
7. **Understand NESTED strategy limitations**: The NESTED strategy creates new ObjectCollections containing only regions from previous matches, **discarding other object types like StateStrings**. This can cause silent failures when chaining actions that use different object types (e.g., find→click→type). For details and solutions, see [Action Chaining](./07-action-chaining.md#critical-insight-object-type-preservation-in-chains)

## Performance Considerations

- **Minimize waits**: Use smart waiting strategies instead of fixed delays
- **Reuse search results**: Use NESTED strategy when searching within found elements
- **Batch similar actions**: Group similar operations when possible
- **Profile your workflows**: Identify and optimize bottlenecks

## Next Steps

### Related Action Guides
- **[Action Chaining](./07-action-chaining.md)** - Essential foundation for understanding NESTED vs CONFIRM strategies and object type preservation
- **[Conditional Action Chains](./15-conditional-chains-examples.md)** - Modern fluent API for building workflows with conditional logic
- **[Conditional Actions](./09-conditional-actions.md)** - Using RepeatUntilConfig for dynamic conditional workflows
- **[Form Automation](./10-form-automation.md)** - Specialized patterns for automating form interactions
- **[Reusable Patterns](./11-reusable-patterns.md)** - Building a library of common workflow patterns

### Reference Documentation
- **[ActionConfig Overview](./01-overview.md)** - Conceptual foundation and architecture
- **[API Reference](./05-reference.md)** - Complete ActionConfig class documentation
- **[Troubleshooting Action Chains](./troubleshooting-chains.md)** - Common errors and solutions

### Migration Resources
- **[Quick Migration Reference](./02-migration-quick-reference.md)** - Fast lookup for migrating from ActionOptions
- **[Migration Guide](./12-migration-guide.md)** - Complete migration strategy